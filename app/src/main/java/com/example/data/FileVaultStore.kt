package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.CloudVaultFileDoc
import com.example.model.VaultCategory
import com.example.model.VaultFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

private val Context.vaultDataStore by preferencesDataStore(name = "kemetloop_file_vault_ds")

data class PickedUriMeta(
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long
)

/**
 * Offline-first User File Vault & Document Wallet backed by Android Internal App Storage
 * (filesDir/kemet_vault) and Jetpack DataStore Preferences, with automatic Guest-to-Google
 * migration and Firestore cloud sync support.
 */
class FileVaultStore(context: Context) {

    private val appContext = context.applicationContext

    private val _vaultFiles = MutableStateFlow<List<VaultFileItem>>(emptyList())
    val vaultFiles: StateFlow<List<VaultFileItem>> = _vaultFiles.asStateFlow()

    private val _autoCloudSync = MutableStateFlow(true)
    val autoCloudSync: StateFlow<Boolean> = _autoCloudSync.asStateFlow()

    private var activeOwnerId: String = GuestLocalStore.GUEST_UID

    suspend fun bindUser(ownerId: String) {
        activeOwnerId = ownerId.ifBlank { GuestLocalStore.GUEST_UID }
        val prefs = appContext.vaultDataStore.data.first()
        _autoCloudSync.value = prefs[KEY_AUTO_CLOUD_SYNC] ?: true
        _vaultFiles.value = decodeItems(prefs[itemsKey(activeOwnerId)], activeOwnerId)
    }

    suspend fun setAutoCloudSync(enabled: Boolean) {
        appContext.vaultDataStore.edit { prefs ->
            prefs[KEY_AUTO_CLOUD_SYNC] = enabled
        }
        _autoCloudSync.value = enabled
    }

    fun inspectUri(uri: Uri): PickedUriMeta {
        val resolver = appContext.contentResolver
        var name = ""
        var size = 0L
        runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx >= 0) name = cursor.getString(nameIdx).orEmpty()
                        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
                    }
                }
        }
        val cleanName = name.trim().ifBlank {
            uri.lastPathSegment?.substringAfterLast('/')?.take(120).orEmpty().ifBlank {
                "document_${System.currentTimeMillis()}"
            }
        }
        val mime = resolver.getType(uri) ?: guessMimeType(cleanName)
        return PickedUriMeta(
            displayName = cleanName.take(160),
            mimeType = mime.take(120),
            sizeBytes = size.coerceAtLeast(0L)
        )
    }

    suspend fun importUriToVault(
        ownerId: String,
        uri: Uri,
        title: String,
        category: VaultCategory,
        note: String
    ): VaultFileItem = withContext(Dispatchers.IO) {
        val cleanOwner = ownerId.ifBlank { GuestLocalStore.GUEST_UID }
        val meta = inspectUri(uri)
        val itemId = generateVaultId()
        val safeFileName = sanitizeFileName(meta.displayName)
        val ownerDir = ensureOwnerDir(cleanOwner)
        val destName = "${itemId}_$safeFileName"
        val destFile = File(ownerDir, destName)

        appContext.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("تعذر قراءة الملف المختار من الجهاز.")

        val actualSize = destFile.length().coerceAtLeast(meta.sizeBytes)
        val relativePath = "kemet_vault/${sanitizeOwnerFolder(cleanOwner)}/$destName"
        val cleanTitle = title.trim().ifBlank { meta.displayName.substringBeforeLast('.') }
            .ifBlank { meta.displayName }
            .take(100)

        val item = VaultFileItem(
            id = itemId,
            ownerId = cleanOwner,
            title = cleanTitle,
            fileName = meta.displayName.take(160),
            mimeType = meta.mimeType.take(120),
            category = category.name,
            sizeBytes = actualSize,
            localRelativePath = relativePath,
            note = note.trim().take(300),
            cloudSynced = false,
            hasCloudInlineBackup = actualSize <= MAX_INLINE_BACKUP_BYTES,
            createdAtMillis = System.currentTimeMillis()
        )

        val current = readItemsFor(cleanOwner)
        val updated = listOf(item) + current
        writeItemsFor(cleanOwner, updated)
        if (cleanOwner == activeOwnerId) {
            _vaultFiles.value = updated
        }
        item
    }

    /**
     * Migrates files saved while browsing as a Guest into the newly linked Google account's
     * secure vault and returns items pending cloud upload.
     */
    suspend fun migrateGuestVaultToUser(targetUserId: String): List<VaultFileItem> = withContext(Dispatchers.IO) {
        if (targetUserId.isBlank() || targetUserId == GuestLocalStore.GUEST_UID) {
            return@withContext emptyList()
        }
        val guestItems = readItemsFor(GuestLocalStore.GUEST_UID)
        val targetItems = readItemsFor(targetUserId).toMutableList()
        val existingIds = targetItems.map { it.id }.toMutableSet()
        val newlyMigrated = mutableListOf<VaultFileItem>()

        if (guestItems.isNotEmpty()) {
            val targetDir = ensureOwnerDir(targetUserId)
            guestItems.forEach { guestItem ->
                if (guestItem.id !in existingIds) {
                    val srcFile = resolveLocalFile(guestItem)
                    var targetRelativePath = guestItem.localRelativePath
                    if (srcFile != null && srcFile.exists()) {
                        val destName = srcFile.name
                        val destFile = File(targetDir, destName)
                        if (!destFile.exists()) {
                            runCatching { srcFile.copyTo(destFile, overwrite = false) }
                        }
                        if (destFile.exists()) {
                            targetRelativePath = "kemet_vault/${sanitizeOwnerFolder(targetUserId)}/$destName"
                        }
                    }
                    val migrated = guestItem.copy(
                        ownerId = targetUserId,
                        localRelativePath = targetRelativePath,
                        cloudSynced = false
                    )
                    targetItems.add(0, migrated)
                    existingIds.add(migrated.id)
                    newlyMigrated.add(migrated)
                }
            }
            writeItemsFor(targetUserId, targetItems)
            // Clear guest items once safely copied to authenticated user's vault
            guestItems.forEach { gItem -> resolveLocalFile(gItem)?.delete() }
            writeItemsFor(GuestLocalStore.GUEST_UID, emptyList())
        }

        if (targetUserId == activeOwnerId) {
            _vaultFiles.value = targetItems
        }
        targetItems.filter { !it.cloudSynced }
    }

    suspend fun getUnsyncedItems(ownerId: String): List<VaultFileItem> {
        return readItemsFor(ownerId).filter { !it.cloudSynced }
    }

    suspend fun markItemsCloudSynced(ownerId: String, syncedIds: Set<String>) {
        if (syncedIds.isEmpty()) return
        val current = readItemsFor(ownerId)
        val updated = current.map { item ->
            if (item.id in syncedIds) item.copy(cloudSynced = true) else item
        }
        writeItemsFor(ownerId, updated)
        if (ownerId == activeOwnerId) {
            _vaultFiles.value = updated
        }
    }

    suspend fun mergeCloudDocs(ownerId: String, cloudDocs: List<CloudVaultFileDoc>) = withContext(Dispatchers.IO) {
        if (ownerId.isBlank() || ownerId == GuestLocalStore.GUEST_UID) return@withContext
        val current = readItemsFor(ownerId).toMutableList()
        val byId = current.associateBy { it.id }.toMutableMap()
        val ownerDir = ensureOwnerDir(ownerId)
        var changed = false

        cloudDocs.forEach { doc ->
            if (doc.id.isBlank()) return@forEach
            val existing = byId[doc.id]
            if (existing != null) {
                var updated = existing
                if (!existing.cloudSynced) {
                    updated = updated.copy(cloudSynced = true)
                }
                if (resolveLocalFile(existing) == null && doc.inlineDataBase64.isNotBlank()) {
                    val restoredPath = materializeInlineBackup(ownerId, ownerDir, doc)
                    if (restoredPath != null) {
                        updated = updated.copy(
                            localRelativePath = restoredPath,
                            hasCloudInlineBackup = true
                        )
                    }
                }
                if (updated != existing) {
                    byId[doc.id] = updated
                    changed = true
                }
            } else {
                val restoredPath = if (doc.inlineDataBase64.isNotBlank()) {
                    materializeInlineBackup(ownerId, ownerDir, doc) ?: ""
                } else {
                    ""
                }
                val newItem = VaultFileItem(
                    id = doc.id,
                    ownerId = ownerId,
                    title = doc.title.ifBlank { doc.fileName },
                    fileName = doc.fileName,
                    mimeType = doc.mimeType.ifBlank { "application/octet-stream" },
                    category = doc.category,
                    sizeBytes = doc.sizeBytes,
                    localRelativePath = restoredPath,
                    note = doc.note,
                    cloudSynced = true,
                    hasCloudInlineBackup = doc.inlineDataBase64.isNotBlank(),
                    createdAtMillis = doc.createdAt?.toDate()?.time ?: System.currentTimeMillis()
                )
                byId[doc.id] = newItem
                changed = true
            }
        }

        if (changed) {
            val merged = byId.values.sortedByDescending { it.createdAtMillis }
            writeItemsFor(ownerId, merged)
            if (ownerId == activeOwnerId) {
                _vaultFiles.value = merged
            }
        }
    }

    suspend fun deleteVaultItem(ownerId: String, itemId: String) = withContext(Dispatchers.IO) {
        val cleanOwner = ownerId.ifBlank { GuestLocalStore.GUEST_UID }
        val current = readItemsFor(cleanOwner)
        val target = current.firstOrNull { it.id == itemId }
        if (target != null) {
            resolveLocalFile(target)?.delete()
        }
        val updated = current.filterNot { it.id == itemId }
        writeItemsFor(cleanOwner, updated)
        if (cleanOwner == activeOwnerId) {
            _vaultFiles.value = updated
        }
    }

    fun resolveLocalFile(item: VaultFileItem): File? {
        if (item.localRelativePath.isBlank()) return null
        val candidate = File(appContext.filesDir, item.localRelativePath)
        return if (candidate.exists() && candidate.isFile) candidate else null
    }

    fun createShareableUri(item: VaultFileItem): Uri? {
        val file = resolveLocalFile(item) ?: return null
        return runCatching {
            FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                file
            )
        }.getOrNull()
    }

    fun readInlineBase64IfSmall(item: VaultFileItem): String? {
        val file = resolveLocalFile(item) ?: return null
        if (file.length() <= 0L || file.length() > MAX_INLINE_BACKUP_BYTES) return null
        return runCatching {
            val bytes = file.readBytes()
            val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
            if (encoded.length <= MAX_FIRESTORE_BASE64_CHARS) encoded else null
        }.getOrNull()
    }

    private fun materializeInlineBackup(
        ownerId: String,
        ownerDir: File,
        doc: CloudVaultFileDoc
    ): String? {
        return runCatching {
            val bytes = Base64.decode(doc.inlineDataBase64, Base64.DEFAULT)
            val safeName = sanitizeFileName(doc.fileName.ifBlank { "file" })
            val destName = "${doc.id}_$safeName"
            val destFile = File(ownerDir, destName)
            destFile.writeBytes(bytes)
            "kemet_vault/${sanitizeOwnerFolder(ownerId)}/$destName"
        }.getOrNull()
    }

    private suspend fun readItemsFor(ownerId: String): List<VaultFileItem> {
        val prefs = appContext.vaultDataStore.data.first()
        return decodeItems(prefs[itemsKey(ownerId)], ownerId)
    }

    private suspend fun writeItemsFor(ownerId: String, items: List<VaultFileItem>) {
        val json = encodeItems(items)
        appContext.vaultDataStore.edit { prefs ->
            prefs[itemsKey(ownerId)] = json
        }
    }

    private fun decodeItems(raw: String?, ownerId: String): List<VaultFileItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val obj = arr.getJSONObject(idx)
                VaultFileItem(
                    id = obj.optString("id", generateVaultId()),
                    ownerId = obj.optString("ownerId", ownerId),
                    title = obj.optString("title", ""),
                    fileName = obj.optString("fileName", ""),
                    mimeType = obj.optString("mimeType", "application/octet-stream"),
                    category = obj.optString("category", VaultCategory.TRAVEL_TICKETS.name),
                    sizeBytes = obj.optLong("sizeBytes", 0L),
                    localRelativePath = obj.optString("localRelativePath", ""),
                    note = obj.optString("note", ""),
                    cloudSynced = obj.optBoolean("cloudSynced", false),
                    hasCloudInlineBackup = obj.optBoolean("hasCloudInlineBackup", false),
                    createdAtMillis = obj.optLong("createdAtMillis", 0L)
                )
            }.sortedByDescending { it.createdAtMillis }
        }.getOrElse { emptyList() }
    }

    private fun encodeItems(items: List<VaultFileItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("ownerId", item.ownerId)
                    put("title", item.title)
                    put("fileName", item.fileName)
                    put("mimeType", item.mimeType)
                    put("category", item.category)
                    put("sizeBytes", item.sizeBytes)
                    put("localRelativePath", item.localRelativePath)
                    put("note", item.note)
                    put("cloudSynced", item.cloudSynced)
                    put("hasCloudInlineBackup", item.hasCloudInlineBackup)
                    put("createdAtMillis", item.createdAtMillis)
                }
            )
        }
        return arr.toString()
    }

    private fun ensureOwnerDir(ownerId: String): File {
        val dir = File(appContext.filesDir, "kemet_vault/${sanitizeOwnerFolder(ownerId)}")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun sanitizeOwnerFolder(ownerId: String): String =
        ownerId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { GuestLocalStore.GUEST_UID }

    private fun sanitizeFileName(name: String): String =
        name.replace(Regex("[^a-zA-Z0-9._\\-]"), "_").take(80).ifBlank { "file" }

    private fun generateVaultId(): String =
        "vf_${UUID.randomUUID().toString().replace("-", "").take(20)}"

    private fun guessMimeType(fileName: String): String {
        val lower = fileName.lowercase()
        return when {
            lower.endsWith(".pdf") -> "application/pdf"
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".txt") -> "text/plain"
            lower.endsWith(".doc") || lower.endsWith(".docx") -> "application/msword"
            else -> "application/octet-stream"
        }
    }

    private fun itemsKey(ownerId: String) = stringPreferencesKey("vault_items_${sanitizeOwnerFolder(ownerId)}")

    companion object {
        private val KEY_AUTO_CLOUD_SYNC = booleanPreferencesKey("vault_auto_cloud_sync")
        private const val MAX_INLINE_BACKUP_BYTES = 480_000L
        private const val MAX_FIRESTORE_BASE64_CHARS = 740_000
    }
}
