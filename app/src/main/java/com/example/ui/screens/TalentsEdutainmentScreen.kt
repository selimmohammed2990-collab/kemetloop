package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.SubscriptionPlan
import com.example.model.TalentCategory
import com.example.model.TalentShowcase
import com.example.model.UserProfile
import com.example.ui.components.AdMobRewardedCallout
import com.example.ui.components.GlassLuxuryCard
import com.example.ui.components.GoldTagPill
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.MidnightGlassSoft
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TerracottaError
import com.example.viewmodel.EdutainmentState
import com.example.viewmodel.UiState

@Composable
fun TalentsEdutainmentScreen(
    profile: UserProfile?,
    edutainmentState: EdutainmentState,
    talentsState: UiState<List<TalentShowcase>>,
    isActionLoading: Boolean,
    onSelectRiddleOption: (Int) -> Unit,
    onNextRiddle: () -> Unit,
    onPublishTalent: (String, String, TalentCategory, String, String, String, () -> Unit) -> Unit,
    onOpenInAppBrowser: (title: String, url: String) -> Unit,
    onWatchRewardedAd: () -> Unit
) {
    val activePlan = profile?.subscriptionPlan ?: SubscriptionPlan.FREE_STARTER
    var selectedSection by remember { mutableIntStateOf(0) }
    var showAddTalentDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
                colors = CardDefaults.cardColors(containerColor = ObsidianDeep)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_talents_stage),
                        contentDescription = "الألعاب والمواهب",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, ObsidianDeep.copy(alpha = 0.88f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "الألعاب والتعليم واكتشاف المواهب",
                            style = MaterialTheme.typography.headlineSmall,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ألغاز ثقافية ممتعة للجميع ومنصة مواهب مدعومة بيوتيوب",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CoolSilver
                        )
                    }
                }
            }
        }

        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = RoyalGold,
                        activeContentColor = ObsidianDeep
                    ),
                    modifier = Modifier.testTag("tab_edutainment_games")
                ) {
                    Text("ألعاب وألغاز ثقافية", fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = RoyalGold,
                        activeContentColor = ObsidianDeep
                    ),
                    modifier = Modifier.testTag("tab_talents_showcase")
                ) {
                    Text("اكتشاف المواهب", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (selectedSection == 0) {
            item {
                EdutainmentGameCard(
                    state = edutainmentState,
                    totalWins = profile?.quizWins ?: 0L,
                    onSelectOption = onSelectRiddleOption,
                    onNextRiddle = onNextRiddle
                )
            }

            item {
                AdMobRewardedCallout(
                    activePlan = activePlan,
                    onWatchRewardedAd = onWatchRewardedAd
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "منصة المواهب المصرية",
                        style = MaterialTheme.typography.titleLarge,
                        color = CrispWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddTalentDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalGold,
                            contentColor = ObsidianDeep
                        ),
                        modifier = Modifier.testTag("add_talent_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اعرض موهبتك", fontWeight = FontWeight.Bold)
                    }
                }
            }

            when (talentsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = RoyalGold)
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        Text(text = talentsState.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is UiState.Success -> {
                    items(talentsState.data, key = { it.id }) { talent ->
                        TalentCard(
                            talent = talent,
                            onOpenLink = { url ->
                                onOpenInAppBrowser(talent.talentTitle, url)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddTalentDialog) {
        AddTalentDialog(
            defaultCreator = profile?.displayName ?: "",
            isLoading = isActionLoading,
            onDismiss = { showAddTalentDialog = false },
            onSubmit = { creator, title, cat, bio, yt, social ->
                onPublishTalent(creator, title, cat, bio, yt, social) {
                    showAddTalentDialog = false
                }
            }
        )
    }
}

@Composable
private fun EdutainmentGameCard(
    state: EdutainmentState,
    totalWins: Long,
    onSelectOption: (Int) -> Unit,
    onNextRiddle: () -> Unit
) {
    val riddle = state.currentRiddle
    GlassLuxuryCard(borderColor = RoyalGold.copy(alpha = 0.55f)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoldTagPill {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = RoyalGoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = riddle.audienceTag,
                            style = MaterialTheme.typography.labelMedium,
                            color = RoyalGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoldTagPill(backgroundColor = RoyalGold, borderColor = RoyalGoldLight) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.MonetizationOn,
                                contentDescription = null,
                                tint = ObsidianDeep,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${riddle.rewardCoins}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ObsidianDeep,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    GoldTagPill(
                        backgroundColor = ElectricAzure.copy(alpha = 0.2f),
                        borderColor = ElectricAzure.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = RoyalGoldLight,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$totalWins فوز",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                text = riddle.question,
                style = MaterialTheme.typography.titleLarge,
                color = CrispWhite,
                fontWeight = FontWeight.Bold
            )

            riddle.options.forEachIndexed { idx, option ->
                val isSelected = state.selectedOptionIndex == idx
                val isCorrectOption = idx == riddle.correctIndex
                val answered = state.selectedOptionIndex != null

                val bgColor = when {
                    !answered -> MidnightGlassSoft
                    isCorrectOption -> EmeraldVerified.copy(alpha = 0.24f)
                    isSelected -> TerracottaError.copy(alpha = 0.24f)
                    else -> MidnightGlassSoft.copy(alpha = 0.5f)
                }
                val borderColor = when {
                    !answered -> RoyalGold.copy(alpha = 0.35f)
                    isCorrectOption -> EmeraldVerified
                    isSelected -> TerracottaError
                    else -> RoyalGold.copy(alpha = 0.2f)
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = bgColor,
                    border = BorderStroke(1.2.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !answered) { onSelectOption(idx) }
                        .testTag("riddle_option_$idx")
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        color = CrispWhite,
                        fontWeight = if (isSelected || (answered && isCorrectOption)) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                }
            }

            if (state.selectedOptionIndex != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (state.isCorrect == true) EmeraldVerified.copy(alpha = 0.16f) else RoyalGold.copy(alpha = 0.16f),
                    border = BorderStroke(
                        1.dp,
                        if (state.isCorrect == true) EmeraldVerified.copy(alpha = 0.6f) else RoyalGold.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = if (state.isCorrect == true) EmeraldVerified else RoyalGoldLight
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (state.isCorrect == true) "أحسنت! إجابة صحيحة (+${riddle.rewardCoins} عملة)" else "معلومة ثقافية لك:",
                                style = MaterialTheme.typography.labelLarge,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = riddle.fact,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CoolSilver
                            )
                        }
                    }
                }

                Button(
                    onClick = onNextRiddle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = ObsidianDeep
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("next_riddle_button")
                ) {
                    Text("اللغز التالي", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TalentCard(
    talent: TalentShowcase,
    onOpenLink: (String) -> Unit
) {
    GlassLuxuryCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Stars, contentDescription = null, tint = RoyalGoldLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = talent.creatorName,
                        style = MaterialTheme.typography.titleMedium,
                        color = CrispWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                GoldTagPill {
                    Text(
                        text = talent.talentCategory.labelAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalGoldLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = talent.talentTitle,
                style = MaterialTheme.typography.titleLarge,
                color = CrispWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = talent.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = CoolSilver
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onOpenLink(talent.youtubeUrl) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD92323),
                        contentColor = CrispWhite
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("شاهد على يوتيوب")
                }
                if (talent.socialUrl.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onOpenLink(talent.socialUrl) },
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("التواصل", color = CrispWhite)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTalentDialog(
    defaultCreator: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, TalentCategory, String, String, String) -> Unit
) {
    var creatorName by remember { mutableStateOf(defaultCreator) }
    var talentTitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(TalentCategory.MUSIC) }
    var bio by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }
    var socialUrl by remember { mutableStateOf("") }

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = { Text("شارك موهبتك في KemetLoop", color = CrispWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TalentCategory.entries) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.labelAr) }
                        )
                    }
                }
                OutlinedTextField(
                    value = creatorName,
                    onValueChange = { creatorName = it },
                    label = { Text("اسم المبدع") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = talentTitle,
                    onValueChange = { talentTitle = it },
                    label = { Text("عنوان الموهبة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("نبذة قصيرة") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = youtubeUrl,
                    onValueChange = { youtubeUrl = it },
                    label = { Text("رابط فيديو يوتيوب") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = socialUrl,
                    onValueChange = { socialUrl = it },
                    label = { Text("رابط منصة تواصل (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(creatorName, talentTitle, category, bio, youtubeUrl, socialUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                enabled = !isLoading && talentTitle.trim().length >= 2 && bio.trim().length >= 2 && youtubeUrl.trim().length >= 4
            ) {
                Text(if (isLoading) "جارٍ النشر..." else "نشر الموهبة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = CrispWhite) }
        }
    )
}
