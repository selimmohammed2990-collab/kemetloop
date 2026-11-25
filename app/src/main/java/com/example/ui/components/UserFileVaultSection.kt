package com.example.ui.components

import android.net.Uri
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.PickedUriMeta
import com.example.data.SafeIntentHelper
import com.example.model.VaultCategory
import com.example.model.VaultFileItem
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepSkyBlue
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.MidnightGlassSoft
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TerracottaError
import java.io.File

@Composable
fun UserFileVaultSection(
    vaultFiles: List<VaultFileItem>,
    autoCloudSync: Boolean,
    isGuestMode: Boolean,
    isActionLoading: Boolean,
    onInspectPickedUri: (Uri) -> PickedUriMeta,
    onResolveLocalFile: (VaultFileItem) -> File?,
    onGetShareableUri: (VaultFileItem) -> Uri?,
    onSavePickedFile: (uri: Uri, title: String, category: VaultCategory, note: String, onDone: () -> Unit) -> Unit,
    onDeleteVaultFile: (VaultFileItem) -> Unit,
    onAutoCloudSyncChange: (Boolean) -> Unit,
    onSyncNowClick: () -> Unit,
    onLinkGoogleClick: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf<VaultCategory?>(null) }
    var pendingPicked by remember { mutableStateOf<Pair<Uri, PickedUriMeta>?>(null) }
    var previewItem by remember { mutableStateOf<VaultFileItem?>(null) }

    val filteredFiles = remember(vaultFiles, selectedFilter) {
        if (selectedFilter == null) vaultFiles
        else vaultFiles.filter { it.vaultCategory == selectedFilter }
    }
    val unsyncedCount = remember(vaultFiles) { vaultFiles.count { !it.cloudSynced } }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingPicked = uri to onInspectPickedUri(uri)
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingPicked = uri to onInspectPickedUri(uri)
        }
    }

    fun openFileItem(item: VaultFileItem) {
        val localFile = onResolveLocalFile(item)
        if (item.isImage && localFile != null) {
            previewItem = item
            return
        }
        val shareUri = onGetShareableUri(item)
        if (shareUri != null) {
            SafeIntentHelper.openVaultUriWithSystem(context, shareUri, item.mimeType, onShowMessage)
        } else {
            onShowMessage("هذا الملف متاح كبيانات وصفية سحابية فقط على هذا الجهاز لأن حجمه كبير.")
        }
    }

    fun shareFileItem(item: VaultFileItem) {
        val shareUri = onGetShareableUri(item)
        if (shareUri != null) {
            SafeIntentHelper.shareVaultUri(context, shareUri, item.mimeType, item.title, onShowMessage)
        } else {
            onShowMessage("لا تتوفر نسخة محلية من الملف للمشاركة على هذا الجهاز.")
        }
    }

    GlassLuxuryCard(
        borderColor = RoyalGold.copy(alpha = 0.72f),
        containerColor = DeepSkyBlue.copy(alpha = 0.22f),
        modifier = Modifier.testTag("profile_file_vault_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(RoyalGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderSpecial,
                            contentDescription = null,
                            tint = RoyalGoldLight
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "حافظة ملفاتي ووثائقي",
                            style = MaterialTheme.typography.titleLarge,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تذاكر · جواز السفر · فواتير · حفظ آمن أوفلاين",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoolSilver
                        )
                    }
                }
                GoldTagPill(backgroundColor = RoyalGold, borderColor = RoyalGoldLight) {
                    Text(
                        text = "${vaultFiles.size} ملفات",
                        style = MaterialTheme.typography.labelMedium,
                        color = ObsidianDeep,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Upload Action Buttons (Zero-permission Photo Picker & SAF Document Picker)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = ObsidianDeep
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("vault_upload_image_btn")
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("رفع صورة", fontWeight = FontWeight.Bold, maxLines = 1)
                }

                OutlinedButton(
                    onClick = {
                        documentPickerLauncher.launch(arrayOf("*/*"))
                    },
                    border = BorderStroke(1.dp, ElectricAzure.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("vault_upload_doc_btn")
                ) {
                    Icon(Icons.Filled.AttachFile, contentDescription = null, tint = ElectricAzure, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مستند / PDF", color = CrispWhite, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            // Offline & Cloud Sync Bar
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MidnightGlassSoft,
                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (autoCloudSync) Icons.Filled.CloudSync else Icons.Filled.CloudOff,
                                contentDescription = null,
                                tint = if (autoCloudSync) EmeraldVerified else CoolSilver,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "المزامنة السحابية التلقائية",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrispWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isGuestMode) {
                                        "محفوظ محلياً بأمان — يتزامن تلقائياً فور ربط Google"
                                    } else {
                                        "نسخ احتياطي مشفّر لملفاتك عبر حساب Google"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CoolSilver
                                )
                            }
                        }
                        Switch(
                            checked = autoCloudSync,
                            onCheckedChange = onAutoCloudSyncChange,
                            modifier = Modifier.testTag("vault_auto_sync_switch")
                        )
                    }

                    if (isGuestMode && vaultFiles.isNotEmpty()) {
                        TextButton(
                            onClick = onLinkGoogleClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("vault_guest_sync_google_btn")
                        ) {
                            Icon(Icons.Filled.CloudSync, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اربط حساب Google الآن لمزامنة ${vaultFiles.size} ملفات سحابياً",
                                style = MaterialTheme.typography.labelMedium,
                                color = RoyalGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (!isGuestMode && unsyncedCount > 0) {
                        TextButton(
                            onClick = onSyncNowClick,
                            enabled = !isActionLoading,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("vault_sync_now_btn")
                        ) {
                            Icon(Icons.Filled.CloudSync, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مزامنة $unsyncedCount ملفات الآن",
                                style = MaterialTheme.typography.labelMedium,
                                color = RoyalGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Category Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("الكل (${vaultFiles.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalGold,
                            selectedLabelColor = ObsidianDeep
                        ),
                        modifier = Modifier.testTag("vault_filter_all")
                    )
                }
                items(VaultCategory.entries) { cat ->
                    val count = vaultFiles.count { it.vaultCategory == cat }
                    FilterChip(
                        selected = selectedFilter == cat,
                        onClick = { selectedFilter = if (selectedFilter == cat) null else cat },
                        label = { Text("${cat.labelAr} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalGold,
                            selectedLabelColor = ObsidianDeep
                        ),
                        modifier = Modifier.testTag("vault_filter_${cat.name.lowercase()}")
                    )
                }
            }

            HorizontalDivider(color = RoyalGold.copy(alpha = 0.25f))

            // File List
            if (filteredFiles.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MidnightGlassSoft,
                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.22f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.FolderSpecial, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(30.dp))
                        Text(
                            text = if (vaultFiles.isEmpty()) {
                                "حافظتك جاهزة لحفظ تذاكر السفر والوثائق المهمة"
                            } else {
                                "لا توجد ملفات في هذا التصنيف حالياً"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ارفع صور الجواز، حجوزات الفنادق، الإيصالات أو PDF للوصول إليها فوراً حتى بدون إنترنت.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoolSilver
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredFiles.forEach { item ->
                        VaultFileItemCard(
                            item = item,
                            localFile = onResolveLocalFile(item),
                            onOpen = { openFileItem(item) },
                            onShare = { shareFileItem(item) },
                            onDelete = { onDeleteVaultFile(item) }
                        )
                    }
                }
            }
        }
    }

    pendingPicked?.let { (uri, meta) ->
        VaultUploadMetaDialog(
            pickedMeta = meta,
            isLoading = isActionLoading,
            onDismiss = { pendingPicked = null },
            onConfirmSave = { title, category, note ->
                onSavePickedFile(uri, title, category, note) {
                    pendingPicked = null
                }
            }
        )
    }

    previewItem?.let { item ->
        VaultImagePreviewDialog(
            item = item,
            localFile = onResolveLocalFile(item),
            onOpenExternal = {
                previewItem = null
                val shareUri = onGetShareableUri(item)
                if (shareUri != null) {
                    SafeIntentHelper.openVaultUriWithSystem(context, shareUri, item.mimeType, onShowMessage)
                }
            },
            onShare = { shareFileItem(item) },
            onDismiss = { previewItem = null }
        )
    }
}

@Composable
private fun VaultFileItemCard(
    item: VaultFileItem,
    localFile: File?,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val catAccent = categoryColor(item.vaultCategory)
    val sizeText = remember(item.sizeBytes) {
        if (item.sizeBytes > 0L) Formatter.formatShortFileSize(context, item.sizeBytes) else "محفوظ"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MidnightGlassSoft,
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("vault_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Thumbnail or Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(catAccent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.isImage && localFile != null) {
                    AsyncImage(
                        model = localFile,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = itemIcon(item),
                        contentDescription = item.vaultCategory.labelAr,
                        tint = catAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Metadata
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.vaultCategory.labelAr} • $sizeText",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoolSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.note.isNotBlank()) {
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalGoldLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (localFile != null) {
                        Text(
                            text = "أوفلاين ✓",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldVerified,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (item.cloudSynced) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CloudDone, contentDescription = null, tint = ElectricAzure, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "سحابي",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricAzure,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // One-tap Open, Share, Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpen,
                    modifier = Modifier.testTag("vault_open_${item.id}")
                ) {
                    Icon(
                        imageVector = if (item.isImage) Icons.Filled.Visibility else Icons.Filled.OpenInNew,
                        contentDescription = "فتح ومعاينة",
                        tint = RoyalGoldLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("vault_share_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "مشاركة الملف",
                        tint = ElectricAzure,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("vault_delete_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "حذف الملف",
                        tint = TerracottaError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultUploadMetaDialog(
    pickedMeta: PickedUriMeta,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirmSave: (title: String, category: VaultCategory, note: String) -> Unit
) {
    val defaultTitle = remember(pickedMeta.displayName) {
        pickedMeta.displayName.substringBeforeLast('.').ifBlank { pickedMeta.displayName }.take(80)
    }
    var titleInput by remember(pickedMeta) { mutableStateOf(defaultTitle) }
    var noteInput by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(
            when {
                pickedMeta.mimeType.startsWith("image/") -> VaultCategory.TRAVEL_TICKETS
                pickedMeta.mimeType == "application/pdf" -> VaultCategory.ID_DOCS
                else -> VaultCategory.MEDIA_FILES
            }
        )
    }

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.FolderSpecial, contentDescription = null, tint = RoyalGoldLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ في حافظة الملفات", color = CrispWhite, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "الملف المختار: ${pickedMeta.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = RoyalGoldLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("عنوان الملف (مثال: تذكرة الطيران / الجواز)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_dialog_title_input")
                )
                Text("اختر التصنيف:", style = MaterialTheme.typography.labelMedium, color = CoolSilver)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(VaultCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.labelAr) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalGold,
                                selectedLabelColor = ObsidianDeep
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("ملاحظة سريعة (اختياري)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_dialog_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmSave(titleInput, selectedCategory, noteInput) },
                enabled = !isLoading && titleInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                modifier = Modifier.testTag("vault_dialog_confirm_save_btn")
            ) {
                Text(if (isLoading) "جارٍ الحفظ..." else "حفظ في الحافظة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = CrispWhite)
            }
        }
    )
}

@Composable
private fun VaultImagePreviewDialog(
    item: VaultFileItem,
    localFile: File?,
    onOpenExternal: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = item.title,
                color = CrispWhite,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (localFile != null) {
                    AsyncImage(
                        model = localFile,
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp, max = 340.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(
                    text = "${item.vaultCategory.labelAr} • ${item.fileName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoolSilver
                )
                if (item.note.isNotBlank()) {
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = RoyalGoldLight
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenExternal,
                        border = BorderStroke(1.dp, RoyalGold),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("فتح خارجي", color = RoyalGoldLight, maxLines = 1)
                    }
                    Button(
                        onClick = onShare,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricAzure, contentColor = CrispWhite),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة", maxLines = 1)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep)
            ) {
                Text("إغلاق", fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun itemIcon(item: VaultFileItem): ImageVector = when {
    item.isPdf -> Icons.Filled.PictureAsPdf
    item.isImage -> Icons.Filled.Image
    item.vaultCategory == VaultCategory.TRAVEL_TICKETS -> Icons.Filled.FlightTakeoff
    item.vaultCategory == VaultCategory.ID_DOCS -> Icons.Filled.Badge
    item.vaultCategory == VaultCategory.INVOICES_RECEIPTS -> Icons.Filled.ReceiptLong
    else -> Icons.Filled.InsertDriveFile
}

private fun categoryColor(category: VaultCategory): Color = when (category) {
    VaultCategory.TRAVEL_TICKETS -> ElectricAzure
    VaultCategory.ID_DOCS -> RoyalGoldLight
    VaultCategory.INVOICES_RECEIPTS -> EmeraldVerified
    VaultCategory.MEDIA_FILES -> CoolSilver
}
