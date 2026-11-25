package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.AffiliateLog
import com.example.model.BillingCycle
import com.example.model.CloudVaultFileDoc
import com.example.model.LinkCategory
import com.example.model.ListingCategory
import com.example.model.MarketplaceListing
import com.example.model.PaymentSubmission
import com.example.model.SavedLink
import com.example.model.SenderWalletType
import com.example.model.SubscriptionPlan
import com.example.model.TalentCategory
import com.example.model.TalentShowcase
import com.example.model.UserProfile
import com.example.model.VaultFileItem
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
import java.util.Locale

class KemetRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

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

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid"
        emitAll(
            unwrapSnapshotErrors(
                db.collection("users").document(uid)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    },
                uid,
                OperationType.GET,
                path
            )
        )
    }

    fun observeListings(): Flow<List<MarketplaceListing>> = flow {
        val uid = requireUserId()
        val path = "listings"
        emitAll(
            unwrapSnapshotErrors(
                db.collection("listings")
                    .whereEqualTo("status", "ACTIVE")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            MarketplaceListing::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ).sortedWith(
                            compareByDescending<MarketplaceListing> { it.featured }
                                .thenByDescending { it.createdAt?.seconds ?: 0L }
                        )
                    },
                uid,
                OperationType.LIST,
                path
            )
        )
    }

    fun observeTalents(): Flow<List<TalentShowcase>> = flow {
        val uid = requireUserId()
        val path = "talents"
        emitAll(
            unwrapSnapshotErrors(
                db.collection("talents")
                    .whereEqualTo("status", "PUBLISHED")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            TalentShowcase::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                    },
                uid,
                OperationType.LIST,
                path
            )
        )
    }

    fun observeSavedLinks(userId: String): Flow<List<SavedLink>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/saved_links"
        emitAll(
            unwrapSnapshotErrors(
                db.collection("users").document(uid).collection("saved_links")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            SavedLink::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                    },
                uid,
                OperationType.LIST,
                path
            )
        )
    }

    fun observeVaultFiles(userId: String): Flow<List<CloudVaultFileDoc>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/vault_files"
        emitAll(
            unwrapSnapshotErrors(
                db.collection("users").document(uid).collection("vault_files")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            CloudVaultFileDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                    },
                uid,
                OperationType.LIST,
                path
            )
        )
    }

    fun observePaymentSubmissions(userId: String): Flow<List<PaymentSubmission>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/payment_submissions"
        emitAll(
            unwrapSnapshotErrors(
                db.collection("users").document(uid).collection("payment_submissions")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            PaymentSubmission::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                    },
                uid,
                OperationType.LIST,
                path
            )
        )
    }

    fun observeAffiliateLogs(userId: String): Flow<List<AffiliateLog>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/affiliate_logs"
        emitAll(
            unwrapSnapshotErrors(
                db.collection("users").document(uid).collection("affiliate_logs")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            AffiliateLog::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                    },
                uid,
                OperationType.LIST,
                path
            )
        )
    }

    suspend fun ensureUserProfileAndStarterContent(
        displayName: String,
        email: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Unauthenticated"))
        val userDoc = db.collection("users").document(uid)
        return try {
            val snap = userDoc.get().await()
            if (!snap.exists()) {
                val safeName = displayName.trim().ifBlank { "عضو KemetLoop" }.take(80)
                val codeSuffix = uid.take(6).uppercase(Locale.US)
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "email" to email.trim().take(120),
                    "plan" to SubscriptionPlan.FREE_STARTER.name,
                    "billingCycle" to BillingCycle.NONE.name,
                    "coins" to 100L,
                    "referralCode" to "KEMET$codeSuffix",
                    "referralCount" to 0L,
                    "affiliateEarningsCoins" to 0L,
                    "verifiedBusiness" to false,
                    "dailyTranslationsUsed" to 0L,
                    "quizWins" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userDoc.set(payload).await()

                // Seed starter link box items for immediate utility
                val linkRef = userDoc.collection("saved_links").document()
                linkRef.set(
                    mapOf(
                        "userId" to uid,
                        "title" to "عروض السفر النيلية - KemetLoop",
                        "url" to "https://kemetloop.app/nile-trips",
                        "category" to LinkCategory.TRAVEL.name,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userDoc.path)
            Result.failure(e)
        }
    }

    suspend fun updateDisplayName(newDisplayName: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Unauthenticated"))
        val cleanName = newDisplayName.trim()
        require(cleanName.length in 2..80) { "الاسم يجب أن يكون بين 2 و 80 حرفاً." }
        val userPath = "users/$uid"
        return try {
            db.collection("users").document(uid).update(
                mapOf(
                    "displayName" to cleanName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userPath)
            Result.failure(e)
        }
    }

    suspend fun seedStarterMarketplaceIfEmpty(sellerName: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Unauthenticated"))
        return try {
            val existingListings = db.collection("listings")
                .whereEqualTo("status", "ACTIVE")
                .limit(1)
                .get()
                .await()
            if (existingListings.isEmpty) {
                val starterItems = listOf(
                    mapOf(
                        "userId" to uid,
                        "sellerName" to sellerName.ifBlank { "بازار النيل الذهبي" }.take(60),
                        "title" to "رحلة فلوكة أسوان وغروب النيل الشاملة",
                        "description" to "جولة خاصة لمدة 3 ساعات في النيل مع مرشد سياحي ومشروبات ضيافة مصرية.",
                        "category" to ListingCategory.TRIP.name,
                        "priceEgp" to 1450L,
                        "location" to "أسوان - كورنيش النيل",
                        "featured" to true,
                        "verifiedSeller" to true,
                        "whatsappPhone" to "+201006256085",
                        "socialUrl" to "https://www.youtube.com/@EgyptTravel",
                        "status" to "ACTIVE",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "userId" to uid,
                        "sellerName" to "حرف خان الخليلي",
                        "title" to "فانوس نحاس يدوي مطعّم بالزجاج الملون",
                        "description" to "تصميم شرقي أصيل صناعة يدوية 100% مناسب للديكور والهدايا الفاخرة.",
                        "category" to ListingCategory.PRODUCT.name,
                        "priceEgp" to 680L,
                        "location" to "القاهرة - الحسين",
                        "featured" to false,
                        "verifiedSeller" to false,
                        "status" to "ACTIVE",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "userId" to uid,
                        "sellerName" to "خدمات الاستقبال والترجمة",
                        "title" to "مرافق تجاري ومترجم للمعارض والأسواق",
                        "description" to "خدمة مرافقة للتجار والسياح في القاهرة والإسكندرية للتفاوض والشحن.",
                        "category" to ListingCategory.SERVICE.name,
                        "priceEgp" to 900L,
                        "location" to "القاهرة والجيزة",
                        "featured" to false,
                        "verifiedSeller" to false,
                        "status" to "ACTIVE",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                starterItems.forEach { item ->
                    db.collection("listings").document().set(item).await()
                }
            }

            val existingTalents = db.collection("talents")
                .whereEqualTo("status", "PUBLISHED")
                .limit(1)
                .get()
                .await()
            if (existingTalents.isEmpty) {
                val starterTalents = listOf(
                    mapOf(
                        "userId" to uid,
                        "creatorName" to "كريم النوبي",
                        "talentTitle" to "عزف عود نُوبي ومقطوعات تراثية",
                        "category" to TalentCategory.MUSIC.name,
                        "bio" to "مؤلف موسيقي شاب يمزج ألحان النوبة الأصيلة بالموسيقى المعاصرة.",
                        "youtubeUrl" to "https://www.youtube.com/results?search_query=arabic+oud+music",
                        "socialUrl" to "https://www.instagram.com/",
                        "status" to "PUBLISHED",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "userId" to uid,
                        "creatorName" to "سلمى الخطاطة",
                        "talentTitle" to "الخط العربي المعاصر على ورق البردي",
                        "category" to TalentCategory.ART.name,
                        "bio" to "تصميم لوحات فنية بالخط الديواني والثلث مستوحاة من الحضارة المصرية.",
                        "youtubeUrl" to "https://www.youtube.com/results?search_query=arabic+calligraphy+art",
                        "socialUrl" to "https://www.tiktok.com/",
                        "status" to "PUBLISHED",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                starterTalents.forEach { talent ->
                    db.collection("talents").document().set(talent).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "listings")
            Result.failure(e)
        }
    }

    suspend fun publishListing(
        sellerName: String,
        title: String,
        description: String,
        category: ListingCategory,
        priceEgp: Long,
        location: String,
        featured: Boolean,
        verifiedSeller: Boolean,
        whatsappPhone: String,
        socialUrl: String
    ): Result<Unit> {
        var docPath = "listings"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim()
            val cleanDesc = description.trim()
            val cleanLocation = location.trim()
            require(cleanTitle.length in 2..100) { "عنوان الإعلان يجب أن يكون بين 2 و 100 حرف." }
            require(cleanDesc.length in 2..500) { "الوصف يجب أن يكون بين 2 و 500 حرف." }
            require(cleanLocation.length in 1..80) { "الموقع مطلوب (بحد أقصى 80 حرفاً)." }
            require(priceEgp in 0L..10_000_000L) { "السعر غير صالح." }

            val docRef = db.collection("listings").document()
            docPath = docRef.path
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "sellerName" to sellerName.trim().ifBlank { "تاجر KemetLoop" }.take(60),
                "title" to cleanTitle,
                "description" to cleanDesc,
                "category" to category.name,
                "priceEgp" to priceEgp,
                "location" to cleanLocation,
                "featured" to featured,
                "verifiedSeller" to verifiedSeller,
                "whatsappPhone" to whatsappPhone.trim().take(25).takeIf { it.isNotEmpty() },
                "socialUrl" to socialUrl.trim().take(250).takeIf { it.isNotEmpty() },
                "status" to "ACTIVE",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteListing(listingId: String): Result<Unit> {
        val path = "listings/$listingId"
        return try {
            requireUserId()
            require(listingId.isNotBlank())
            db.collection("listings").document(listingId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun addSavedLink(title: String, url: String, category: LinkCategory): Result<Unit> {
        var path = "saved_links"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim()
            val cleanUrl = url.trim()
            require(cleanTitle.length in 1..80) { "العنوان مطلوب (1-80 حرفاً)." }
            require(cleanUrl.length in 4..300) { "الرابط غير صالح." }
            val normalizedUrl = if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
                cleanUrl
            } else {
                "https://$cleanUrl"
            }.take(300)

            val docRef = db.collection("users").document(uid).collection("saved_links").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "title" to cleanTitle,
                    "url" to normalizedUrl,
                    "category" to category.name,
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

    suspend fun deleteSavedLink(linkId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "unauth"
        val path = "users/$uid/saved_links/$linkId"
        return try {
            val authedUid = requireUserId()
            db.collection("users").document(authedUid).collection("saved_links")
                .document(linkId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertVaultFile(item: VaultFileItem, inlineDataBase64: String?): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "unauth"
        val path = "users/$uid/vault_files/${item.id}"
        return try {
            val authedUid = requireUserId()
            require(item.id.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$"))) { "معرّف الملف غير صالح." }
            val cleanTitle = item.title.trim().take(100).ifBlank { "مستند" }
            val cleanFileName = item.fileName.trim().take(160).ifBlank { "file" }
            val cleanMime = item.mimeType.trim().take(120).ifBlank { "application/octet-stream" }
            val cleanSize = item.sizeBytes.coerceIn(0L, 104_857_600L)
            val docRef = db.collection("users").document(authedUid)
                .collection("vault_files").document(item.id)

            val payload = mutableMapOf<String, Any?>(
                "userId" to authedUid,
                "title" to cleanTitle,
                "fileName" to cleanFileName,
                "mimeType" to cleanMime,
                "category" to item.vaultCategory.name,
                "sizeBytes" to cleanSize,
                "note" to item.note.trim().take(300).takeIf { it.isNotEmpty() },
                "inlineDataBase64" to inlineDataBase64?.takeIf { it.length <= 750_000 && it.isNotEmpty() },
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

    suspend fun deleteVaultFile(fileId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "unauth"
        val path = "users/$uid/vault_files/$fileId"
        return try {
            val authedUid = requireUserId()
            db.collection("users").document(authedUid).collection("vault_files")
                .document(fileId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun publishTalent(
        creatorName: String,
        talentTitle: String,
        category: TalentCategory,
        bio: String,
        youtubeUrl: String,
        socialUrl: String
    ): Result<Unit> {
        var path = "talents"
        return try {
            val uid = requireUserId()
            val cleanTitle = talentTitle.trim()
            val cleanBio = bio.trim()
            val cleanYt = youtubeUrl.trim()
            require(cleanTitle.length in 2..100) { "عنوان الموهبة مطلوب (2-100 حرف)." }
            require(cleanBio.length in 2..400) { "نبذة الموهبة مطلوبة." }
            require(cleanYt.length in 4..250) { "رابط يوتيوب مطلوب." }
            val normalizedYt = if (cleanYt.startsWith("http")) cleanYt else "https://$cleanYt"
            val normalizedSocial = socialUrl.trim().take(250).let {
                if (it.isBlank()) null else if (it.startsWith("http")) it else "https://$it"
            }

            val docRef = db.collection("talents").document()
            path = docRef.path
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "creatorName" to creatorName.trim().ifBlank { "مبدع مصري" }.take(60),
                "talentTitle" to cleanTitle,
                "category" to category.name,
                "bio" to cleanBio,
                "youtubeUrl" to normalizedYt.take(250),
                "socialUrl" to normalizedSocial,
                "status" to "PUBLISHED",
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

    suspend fun recordAffiliateAction(
        actionType: String,
        referenceTitle: String,
        offerPriceEgp: Long,
        earnedCoins: Long
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "unauth"
        val userPath = "users/$uid"
        return try {
            val authedUid = requireUserId()
            val safeCoins = earnedCoins.coerceIn(1L, 1000L)
            val commissionEgp = (offerPriceEgp * 10L) / 100L
            val logRef = db.collection("users").document(authedUid)
                .collection("affiliate_logs").document()
            val userRef = db.collection("users").document(authedUid)

            db.runBatch { batch ->
                batch.set(
                    logRef,
                    mapOf(
                        "userId" to authedUid,
                        "actionType" to actionType,
                        "referenceTitle" to referenceTitle.trim().take(100).ifBlank { "عرض KemetLoop" },
                        "offerPriceEgp" to offerPriceEgp.coerceIn(0L, 10_000_000L),
                        "platformCommissionEgp" to commissionEgp.coerceIn(0L, 1_000_000L),
                        "earnedCoins" to safeCoins,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                val userUpdates = mutableMapOf<String, Any>(
                    "coins" to FieldValue.increment(safeCoins),
                    "affiliateEarningsCoins" to FieldValue.increment(safeCoins),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (actionType == "INVITE_FRIEND") {
                    userUpdates["referralCount"] = FieldValue.increment(1L)
                }
                batch.update(userRef, userUpdates)
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userPath)
            Result.failure(e)
        }
    }

    suspend fun awardCoinsForActivity(coinsToAdd: Long, isQuizWin: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "unauth"
        val userPath = "users/$uid"
        return try {
            val authedUid = requireUserId()
            val safeCoins = coinsToAdd.coerceIn(1L, 500L)
            val updates = mutableMapOf<String, Any>(
                "coins" to FieldValue.increment(safeCoins),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (isQuizWin) {
                updates["quizWins"] = FieldValue.increment(1L)
            }
            db.collection("users").document(authedUid).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userPath)
            Result.failure(e)
        }
    }

    suspend fun incrementDailyTranslations(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "unauth"
        val userPath = "users/$uid"
        return try {
            val authedUid = requireUserId()
            db.collection("users").document(authedUid).update(
                mapOf(
                    "dailyTranslationsUsed" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userPath)
            Result.failure(e)
        }
    }

    suspend fun submitVodafoneCashPayment(
        userName: String,
        userEmail: String,
        targetPlan: SubscriptionPlan,
        billingCycle: BillingCycle,
        senderWalletType: SenderWalletType,
        txReference: String,
        receiptSelected: Boolean,
        receiptUriNote: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "unauth"
        val userPath = "users/$uid"
        return try {
            val authedUid = requireUserId()
            require(targetPlan == SubscriptionPlan.PRO || targetPlan == SubscriptionPlan.BUSINESS_VIP) {
                "يرجى اختيار باقة مدفوعة للتفعيل."
            }
            require(billingCycle == BillingCycle.MONTHLY || billingCycle == BillingCycle.YEARLY) {
                "يرجى اختيار دورة اشتراك (شهري أو سنوي)."
            }
            val cleanRef = txReference.trim()
            require(cleanRef.length >= 4 || receiptSelected) {
                "يرجى إدخال رقم عملية التحويل أو إرفاق صورة الإيصال."
            }

            val amountEgp = if (billingCycle == BillingCycle.YEARLY) {
                targetPlan.yearlyEgp
            } else {
                targetPlan.monthlyEgp
            }

            val vipReqRef = db.collection("vip_requests").document()
            val paymentRef = db.collection("users").document(authedUid)
                .collection("payment_submissions").document(vipReqRef.id)

            val paymentPayload = mutableMapOf<String, Any?>(
                "userId" to authedUid,
                "targetPlan" to targetPlan.name,
                "billingCycle" to billingCycle.name,
                "amountEgp" to amountEgp,
                "walletNumber" to "01006256085",
                "senderWalletType" to senderWalletType.name,
                "txReference" to cleanRef.take(64).takeIf { it.isNotEmpty() },
                "receiptSelected" to receiptSelected,
                "receiptUriNote" to receiptUriNote.trim().take(300).takeIf { it.isNotEmpty() },
                "status" to "PENDING",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            val vipRequestPayload = mutableMapOf<String, Any?>(
                "userId" to authedUid,
                "userName" to userName.trim().ifBlank { "عضو KemetLoop" }.take(80),
                "userEmail" to userEmail.trim().take(120).takeIf { it.isNotEmpty() },
                "targetPlan" to targetPlan.name,
                "billingCycle" to billingCycle.name,
                "amountEgp" to amountEgp,
                "walletNumber" to "01006256085",
                "senderWalletType" to senderWalletType.name,
                "txReference" to cleanRef.take(64).takeIf { it.isNotEmpty() },
                "receiptSelected" to receiptSelected,
                "receiptUriNote" to receiptUriNote.trim().take(300).takeIf { it.isNotEmpty() },
                "status" to "PENDING",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            db.runBatch { batch ->
                batch.set(paymentRef, paymentPayload)
                batch.set(vipReqRef, vipRequestPayload)
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userPath)
            Result.failure(e)
        }
    }
}
