package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.LinkCategory
import com.example.model.ListingCategory
import com.example.model.ListingReview
import com.example.model.MarketplaceListing
import com.example.model.SavedLink
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.MidnightGlassSoft
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.viewmodel.UiState
import kotlinx.coroutines.flow.Flow

@Composable
fun CreateListingDialog(
    isBusinessVip: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        description: String,
        category: ListingCategory,
        priceEgp: Long,
        location: String,
        featured: Boolean,
        whatsappPhone: String,
        socialUrl: String
    ) -> Unit,
    onUpgradeClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("القاهرة") }
    var category by remember { mutableStateOf(ListingCategory.PRODUCT) }
    var featured by remember { mutableStateOf(isBusinessVip) }
    var whatsappPhone by remember { mutableStateOf("") }
    var socialUrl by remember { mutableStateOf("") }

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = { Text("إضافة عرض للسوق", color = CrispWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ListingCategory.entries) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.labelAr) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المنتج / الرحلة / الخدمة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("الوصف") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                        label = { Text("السعر (ج.م)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("المدينة") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isBusinessVip) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تثبيت في أعلى القائمة (Featured)", color = CrispWhite, style = MaterialTheme.typography.labelLarge)
                        Switch(checked = featured, onCheckedChange = { featured = it })
                    }
                    OutlinedTextField(
                        value = whatsappPhone,
                        onValueChange = { whatsappPhone = it },
                        label = { Text("رقم واتساب المباشر") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = socialUrl,
                        onValueChange = { socialUrl = it },
                        label = { Text("رابط منصة تواصل") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RoyalGold.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onUpgradeClick)
                    ) {
                        Text(
                            text = "لتثبيت الإعلان في الأعلى وربط واتساب والشارة الذهبية: قم بالترقية إلى Business VIP.",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoyalGoldLight,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toLongOrNull() ?: 0L
                    onSubmit(title, description, category, price, location, featured, whatsappPhone, socialUrl)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                enabled = !isLoading && title.trim().length >= 2 && description.trim().length >= 2
            ) {
                Text(if (isLoading) "جارٍ النشر..." else "نشر", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = CrispWhite) }
        }
    )
}

@Composable
fun LinkBoxSheetContent(
    linksState: UiState<List<SavedLink>>,
    isActionLoading: Boolean,
    onAddLink: (String, String, LinkCategory) -> Unit,
    onDeleteLink: (String) -> Unit,
    onOpenUrl: (title: String, url: String) -> Unit
) {
    var linkTitle by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }
    var linkCategory by remember { mutableStateOf(LinkCategory.TRAVEL) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.BookmarkAdd, contentDescription = null, tint = RoyalGoldLight)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "صندوق الأحلام والروابط (يعمل بدون إنترنت)",
                style = MaterialTheme.typography.titleLarge,
                color = CrispWhite,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "احفظ روابط متاجرك، حجوزات السفر، ومنصات التواصل لتبقى محفوظة محلياً وسحابياً.",
            style = MaterialTheme.typography.bodyMedium,
            color = CoolSilver
        )

        OutlinedTextField(
            value = linkTitle,
            onValueChange = { linkTitle = it },
            label = { Text("اسم الرابط") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = linkUrl,
            onValueChange = { linkUrl = it },
            label = { Text("الرابط (URL)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(LinkCategory.entries) { cat ->
                FilterChip(
                    selected = linkCategory == cat,
                    onClick = { linkCategory = cat },
                    label = { Text(cat.labelAr) }
                )
            }
        }
        Button(
            onClick = {
                onAddLink(linkTitle, linkUrl, linkCategory)
                linkTitle = ""
                linkUrl = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
            enabled = !isActionLoading && linkTitle.isNotBlank() && linkUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حفظ في صندوق الأحلام والروابط", fontWeight = FontWeight.Bold)
        }

        val links = (linksState as? UiState.Success)?.data.orEmpty()
        links.take(8).forEach { item ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MidnightGlassSoft,
                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenUrl(item.title, item.url) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium, color = CrispWhite, fontWeight = FontWeight.SemiBold)
                        Text(item.url, style = MaterialTheme.typography.labelSmall, color = CoolSilver, maxLines = 1)
                    }
                    Row {
                        IconButton(onClick = { onOpenUrl(item.title, item.url) }) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = "فتح الرابط", tint = RoyalGoldLight)
                        }
                        IconButton(onClick = { onDeleteLink(item.id) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف الرابط", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ListingReviewsDialog(
    listing: MarketplaceListing,
    reviewsFlow: Flow<List<ListingReview>>,
    isActionLoading: Boolean,
    onSubmitReview: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    val reviews by reviewsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedStars by remember { mutableIntStateOf(5) }
    var commentText by remember { mutableStateOf("") }

    val avgRating = if (reviews.isEmpty()) 5.0 else reviews.map { it.rating.toDouble() }.average()

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("تقييمات 5 نجوم ★", color = CrispWhite, fontWeight = FontWeight.Bold)
                Text(
                    text = "${listing.title} (${String.format("%.1f", avgRating)} / 5 · ${reviews.size} تقييم)",
                    style = MaterialTheme.typography.labelMedium,
                    color = RoyalGoldLight
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { selectedStars = star },
                            modifier = Modifier.testTag("star_rating_$star")
                        ) {
                            Icon(
                                imageVector = if (star <= selectedStars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$star نجوم",
                                tint = RoyalGoldLight,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("اكتب تجربتك مع هذا العرض أو التاجر...") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_comment_input")
                )

                Button(
                    onClick = {
                        onSubmitReview(selectedStars, commentText)
                        commentText = ""
                    },
                    enabled = !isActionLoading && commentText.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_review_button")
                ) {
                    Text(if (isActionLoading) "جارٍ الحفظ..." else "إرسال التقييم ($selectedStars نجوم)", fontWeight = FontWeight.Bold)
                }

                if (reviews.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(reviews, key = { it.id }) { rev ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MidnightGlassSoft,
                                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(rev.reviewerName, style = MaterialTheme.typography.labelLarge, color = CrispWhite, fontWeight = FontWeight.Bold)
                                        Text("★".repeat(rev.rating.toInt().coerceIn(1, 5)), color = RoyalGoldLight)
                                    }
                                    Text(rev.comment, style = MaterialTheme.typography.bodySmall, color = CoolSilver)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = RoyalGoldLight, fontWeight = FontWeight.Bold)
            }
        }
    )
}
