package com.example.data

import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.ConciergeOffer
import com.example.model.ConciergeRequest
import com.example.model.DiscountCoupon
import com.example.model.ListingCategory
import com.example.model.ListingReview
import com.example.model.SubscriptionPlan
import com.example.model.VipPaymentRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Modular Firestore repository for Concierge Requests & Business VIP Offers,
 * 5-Star Listing Reviews, QR Discount Coupons, and Developer Admin operations.
 */
class KemetCommunityRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول بحساب Google أولاً.")
    }

    private fun <T> unwrapSnapshotErrors(
        flow: Flow<T>,
        uid: String,
        operationType: OperationType,
        path: String
    ): Flow<T> = flow.catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    fun observeConciergeRequests(): Flow<List<ConciergeRequest>> = flow {
        val uid = requireUserId()
        emitAll(
            unwrapSnapshotErrors(
                db.collection("concierge_requests")
                    .whereEqualTo("status", "OPEN")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            ConciergeRequest::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                    },
                uid,
                OperationType.LIST,
                "concierge_requests"
            )
        )
    }

    suspend fun createConciergeRequest(
        requesterName: String,
        category: ListingCategory,
        title: String,
        details: String,
        budgetEgp: Long,
        city: String
    ): Result<Unit> {
        var path = "concierge_requests"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim()
            val cleanDetails = details.trim()
            val cleanCity = city.trim()
            require(cleanTitle.length in 2..100) { "عنوان الطلب يجب أن يكون بين 2 و 100 حرف." }
            require(cleanDetails.length in 2..500) { "تفاصيل الطلب مطلوبة (2-500 حرف)." }
            require(cleanCity.length in 1..60) { "المدينة مطلوبة." }
            require(budgetEgp in 0L..10_000_000L) { "الميزانية غير صالحة." }

            val docRef = db.collection("concierge_requests").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "requesterName" to requesterName.trim().ifBlank { "عميل KemetLoop" }.take(60),
                    "category" to category.name,
                    "title" to cleanTitle,
                    "details" to cleanDetails,
                    "budgetEgp" to budgetEgp,
                    "city" to cleanCity,
                    "status" to "OPEN",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeOffersForRequest(requestId: String, requesterUid: String): Flow<List<ConciergeOffer>> = flow {
        val uid = requireUserId()
        val path = "concierge_requests/$requestId/offers"
        val query = if (requesterUid == uid) {
            db.collection("concierge_requests").document(requestId)
                .collection("offers").whereEqualTo("requesterUid", uid)
        } else {
            db.collection("concierge_requests").document(requestId)
                .collection("offers").whereEqualTo("merchantUserId", uid)
        }
        emitAll(
            unwrapSnapshotErrors(
                query.snapshots().map { snapshot ->
                    snapshot.toObjects(
                        ConciergeOffer::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                },
                uid,
                OperationType.LIST,
                path
            )
        )
    }

    suspend fun submitConciergeOffer(
        requestId: String,
        requesterUid: String,
        merchantName: String,
        priceEgp: Long,
        proposal: String,
        whatsappPhone: String
    ): Result<Unit> {
        var path = "concierge_requests/$requestId/offers"
        return try {
            val uid = requireUserId()
            val cleanProposal = proposal.trim()
            require(cleanProposal.length in 2..400) { "تفاصيل العرض مطلوبة (2-400 حرف)." }
            require(priceEgp in 0L..10_000_000L) { "السعر غير صالح." }

            val docRef = db.collection("concierge_requests").document(requestId)
                .collection("offers").document()
            path = docRef.path
            val payload = mutableMapOf<String, Any?>(
                "requestId" to requestId,
                "requesterUid" to requesterUid,
                "merchantUserId" to uid,
                "merchantName" to merchantName.trim().ifBlank { "تاجر Business VIP" }.take(60),
                "priceEgp" to priceEgp,
                "proposal" to cleanProposal,
                "whatsappPhone" to whatsappPhone.trim().take(25).takeIf { it.isNotEmpty() },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeListingReviews(listingId: String): Flow<List<ListingReview>> = flow {
        val uid = requireUserId()
        val path = "listings/$listingId/reviews"
        emitAll(
            unwrapSnapshotErrors(
                db.collection("listings").document(listingId).collection("reviews")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            ListingReview::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                    },
                uid,
                OperationType.LIST,
                path
            )
        )
    }

    suspend fun submitListingReview(
        listingId: String,
        reviewerName: String,
        rating: Int,
        comment: String
    ): Result<Unit> {
        var path = "listings/$listingId/reviews"
        return try {
            val uid = requireUserId()
            val cleanComment = comment.trim()
            require(rating in 1..5) { "التقييم يجب أن يكون من 1 إلى 5 نجوم." }
            require(cleanComment.length in 1..300) { "يرجى كتابة تعليق التقييم." }

            val docRef = db.collection("listings").document(listingId)
                .collection("reviews").document(uid)
            path = docRef.path
            val existing = docRef.get().await()
            val safeName = reviewerName.trim().ifBlank { "مستخدم KemetLoop" }.take(60)
            if (!existing.exists()) {
                docRef.set(
                    mapOf(
                        "listingId" to listingId,
                        "userId" to uid,
                        "reviewerName" to safeName,
                        "rating" to rating.toLong(),
                        "comment" to cleanComment,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "reviewerName" to safeName,
                        "rating" to rating.toLong(),
                        "comment" to cleanComment,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeCoupons(): Flow<List<DiscountCoupon>> = flow {
        val uid = requireUserId()
        emitAll(
            unwrapSnapshotErrors(
                db.collection("coupons")
                    .whereEqualTo("status", "ACTIVE")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            DiscountCoupon::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                    },
                uid,
                OperationType.LIST,
                "coupons"
            )
        )
    }

    suspend fun createCoupon(
        merchantName: String,
        code: String,
        title: String,
        discountPercent: Long,
        city: String
    ): Result<Unit> {
        var path = "coupons"
        return try {
            val uid = requireUserId()
            val cleanCode = code.trim().uppercase().take(32)
            val cleanTitle = title.trim()
            val cleanCity = city.trim().ifBlank { "القاهرة" }.take(60)
            require(cleanCode.length in 3..32) { "كود الكوبون يجب أن يكون 3 أحرف على الأقل." }
            require(cleanTitle.length in 2..100) { "عنوان الكوبون مطلوب." }
            require(discountPercent in 1L..90L) { "نسبة الخصم يجب أن تكون بين 1% و 90%." }

            val docRef = db.collection("coupons").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "merchantName" to merchantName.trim().ifBlank { "تاجر KemetLoop" }.take(60),
                    "code" to cleanCode,
                    "title" to cleanTitle,
                    "discountPercent" to discountPercent,
                    "city" to cleanCity,
                    "status" to "ACTIVE",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeVipRequests(isAdminEmail: Boolean): Flow<List<VipPaymentRequest>> = flow {
        val uid = requireUserId()
        val query = if (isAdminEmail) {
            db.collection("vip_requests")
        } else {
            db.collection("vip_requests").whereEqualTo("userId", uid)
        }
        emitAll(
            unwrapSnapshotErrors(
                query.snapshots().map { snapshot ->
                    snapshot.toObjects(
                        VipPaymentRequest::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                },
                uid,
                OperationType.LIST,
                "vip_requests"
            )
        )
    }

    suspend fun approveVipRequestAsAdmin(request: VipPaymentRequest): Result<Unit> {
        val path = "vip_requests/${request.id}"
        return try {
            requireUserId()
            val plan = SubscriptionPlan.fromRaw(request.targetPlan)
            val reqRef = db.collection("vip_requests").document(request.id)
            val subRef = db.collection("users").document(request.userId)
                .collection("payment_submissions").document(request.id)
            val userRef = db.collection("users").document(request.userId)

            val subSnap = subRef.get().await()
            db.runBatch { batch ->
                batch.update(
                    reqRef,
                    mapOf(
                        "status" to "APPROVED",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                if (subSnap.exists()) {
                    batch.update(
                        subRef,
                        mapOf(
                            "status" to "APPROVED",
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                batch.update(
                    userRef,
                    mapOf(
                        "plan" to plan.name,
                        "billingCycle" to request.billingCycle,
                        "verifiedBusiness" to (plan == SubscriptionPlan.BUSINESS_VIP),
                        "coins" to FieldValue.increment(plan.monthlyCoins),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun rejectVipRequestAsAdmin(request: VipPaymentRequest): Result<Unit> {
        val path = "vip_requests/${request.id}"
        return try {
            requireUserId()
            val reqRef = db.collection("vip_requests").document(request.id)
            val subRef = db.collection("users").document(request.userId)
                .collection("payment_submissions").document(request.id)
            val subSnap = subRef.get().await()
            db.runBatch { batch ->
                batch.update(
                    reqRef,
                    mapOf("status" to "REJECTED", "updatedAt" to FieldValue.serverTimestamp())
                )
                if (subSnap.exists()) {
                    batch.update(
                        subRef,
                        mapOf("status" to "REJECTED", "updatedAt" to FieldValue.serverTimestamp())
                    )
                }
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleVerifyListingAsAdmin(
        listingId: String,
        verified: Boolean,
        featured: Boolean
    ): Result<Unit> {
        val path = "listings/$listingId"
        return try {
            requireUserId()
            db.collection("listings").document(listingId).update(
                mapOf(
                    "verifiedSeller" to verified,
                    "featured" to featured,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
