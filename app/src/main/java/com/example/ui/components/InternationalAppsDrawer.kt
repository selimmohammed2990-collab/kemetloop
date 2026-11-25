package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.InternationalAppCategory
import com.example.model.InternationalAppItem
import com.example.model.InternationalAppsCatalog
import com.example.model.LinkCategory
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.MidnightGlassSoft
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight

@Composable
fun InternationalAppsDrawerSheet(
    onOpenAppInWebView: (title: String, url: String) -> Unit,
    onAddCustomLinkClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<InternationalAppCategory?>(null) }

    val filteredApps = remember(searchQuery, selectedCategory) {
        val q = searchQuery.trim()
        InternationalAppsCatalog.apps.filter { app ->
            val catMatch = selectedCategory == null || app.category == selectedCategory
            val queryMatch = q.isEmpty() ||
                app.name.contains(q, ignoreCase = true) ||
                app.subtitleAr.contains(q, ignoreCase = true) ||
                app.category.labelAr.contains(q, ignoreCase = true)
            catMatch && queryMatch
        }
    }

    val groupedApps = remember(filteredApps) {
        filteredApps.groupBy { it.category }
    }

    ModalDrawerSheet(
        drawerContainerColor = ObsidianDeep,
        drawerContentColor = CrispWhite,
        modifier = Modifier
            .widthIn(max = 336.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MidnightNavy, ObsidianDeep, MidnightNavy.copy(alpha = 0.92f))
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Drawer Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoyalGold),
                        color = ObsidianDeep
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_kemetloop_royal_emblem),
                            contentDescription = "شعار KemetLoop",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "التطبيقات الدولية",
                            style = MaterialTheme.typography.titleLarge,
                            color = RoyalGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "فتح فوري داخل متصفح KemetLoop",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoolSilver
                        )
                    }
                }
                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.testTag("close_international_drawer_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق القائمة الجانبية",
                        tint = CrispWhite
                    )
                }
            }

            // Quick Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن تطبيق دولي أو منصة...", color = CoolSilver) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = RoyalGoldLight) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "مسح البحث", tint = CoolSilver)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .testTag("drawer_search_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("الكل (${InternationalAppsCatalog.apps.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalGold,
                            selectedLabelColor = ObsidianDeep
                        )
                    )
                }
                items(InternationalAppCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text(cat.labelAr, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalGold,
                            selectedLabelColor = ObsidianDeep
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = RoyalGold.copy(alpha = 0.25f))

            // Categorized International Apps List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (groupedApps.isEmpty()) {
                    item {
                        GlassLuxuryCard(containerColor = MidnightGlassSoft) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.Public, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(28.dp))
                                Text(
                                    text = "لم يتم العثور على تطبيق مطابق",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CrispWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "يمكنك إضافته يدوياً إلى صندوق الأحلام بالأسفل.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CoolSilver
                                )
                            }
                        }
                    }
                } else {
                    groupedApps.forEach { (category, appsInCat) ->
                        item(key = "header_${category.name}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Language,
                                    contentDescription = null,
                                    tint = RoyalGoldLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category.labelAr,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RoyalGoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        items(appsInCat, key = { it.id }) { app ->
                            InternationalAppRowCard(
                                app = app,
                                onClick = {
                                    onCloseDrawer()
                                    onOpenAppInWebView(app.name, app.url)
                                }
                            )
                        }
                    }
                }
            }

            // Pinned Footer: + إضافة تطبيق أو رابط دولي (Dream Box integration)
            Surface(
                color = MidnightGlass,
                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        onCloseDrawer()
                        onAddCustomLinkClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = ObsidianDeep
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .height(48.dp)
                        .testTag("drawer_add_dream_link_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ إضافة تطبيق أو رابط دولي",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun InternationalAppRowCard(
    app: InternationalAppItem,
    onClick: () -> Unit
) {
    val accent = Color(app.accentColorHex)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MidnightGlassSoft,
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("intl_app_${app.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.subtitleAr,
                    style = MaterialTheme.typography.labelSmall,
                    color = CoolSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Filled.OpenInBrowser,
                contentDescription = "فتح ${app.name}",
                tint = RoyalGoldLight,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Sleek glassmorphic floating handle anchored to the screen edge so users can open/close
 * the International Apps Sidebar from anywhere without sacrificing screen space.
 */
@Composable
fun FloatingEdgeDrawerHandle(
    isDrawerOpen: Boolean,
    onToggleDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 96.dp)
            .clickable(onClick = onToggleDrawer)
            .testTag("floating_edge_drawer_handle"),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
            color = MidnightElevated.copy(alpha = 0.92f),
            border = BorderStroke(1.2.dp, RoyalGold.copy(alpha = 0.78f)),
            shadowElevation = 10.dp,
            modifier = Modifier
                .width(26.dp)
                .height(82.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Apps,
                    contentDescription = if (isDrawerOpen) "إغلاق قائمة التطبيقات الدولية" else "فتح قائمة التطبيقات الدولية",
                    tint = RoyalGoldLight,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ElectricAzure,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AddDreamLinkDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSaveLink: (title: String, url: String, category: LinkCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LinkCategory.TRAVEL) }

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Public, contentDescription = null, tint = RoyalGoldLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة تطبيق أو رابط دولي", color = CrispWhite, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "سيُحفظ هذا الرابط مباشرة في صندوق الأحلام الخاص بك ليعمل في أي وقت.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolSilver
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم التطبيق أو الموقع") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dream_link_title_input")
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("الرابط (مثال: https://example.com)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dream_link_url_input")
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(LinkCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.labelAr) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveLink(title, url, selectedCategory) },
                enabled = !isLoading && title.isNotBlank() && url.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                modifier = Modifier.testTag("save_dream_link_confirm_btn")
            ) {
                Text(if (isLoading) "جارٍ الحفظ..." else "حفظ في صندوق الأحلام", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = CrispWhite)
            }
        }
    )
}
