package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.SafeIntentHelper
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.MidnightGlassSoft
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.viewmodel.VoiceAssistantUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVoiceAssistantSheet(
    state: VoiceAssistantUiState,
    autoSpeak: Boolean,
    onAsk: (String) -> Unit,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var inputQuery by remember { mutableStateOf("") }
    var ttsReady by remember { mutableStateOf(false) }

    val tts = remember(context) {
        TextToSpeech(context) { status ->
            ttsReady = (status == TextToSpeech.SUCCESS)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakArabicOrAuto(text: String) {
        if (!ttsReady) return
        val arabicLocale = Locale.forLanguageTag("ar-EG")
        val res = tts.setLanguage(arabicLocale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.language = Locale.US
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kemet_ai_voice")
    }

    LaunchedEffect(state) {
        if (autoSpeak && state is VoiceAssistantUiState.Success) {
            speakArabicOrAuto(state.reply)
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                inputQuery = spoken
                onAsk(spoken)
            }
        }
    }

    fun launchSpeech() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث إلى مساعد KemetLoop الذكي...")
        }
        runCatching { speechLauncher.launch(intent) }
            .onFailure { onShowMessage("الإملاء الصوتي غير متاح هنا؛ اكتب سؤالك أدناه وسيرد عليك المتحدث الذكي.") }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchSpeech()
        } else {
            onShowMessage("تم رفض صلاحية الميكروفون؛ يمكنك كتابة سؤالك للمتحدث الذكي.")
        }
    }

    val quickPrompts = remember {
        listOf(
            "أفضل رحلة يخت في البحر الأحمر",
            "خطة يوم واحد في أهرامات الجيزة",
            "كيف أفاوض التاجر بالإنجليزية؟",
            "نصائح زيارة الأقصر وأسوان"
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MidnightGlass,
        modifier = Modifier.testTag("ai_voice_assistant_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RoyalGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RecordVoiceOver,
                            contentDescription = null,
                            tint = RoyalGoldLight
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "المتحدث الذكي (AI Voice Assistant)",
                            style = MaterialTheme.typography.titleMedium,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اسأل صوتياً أو كتابياً عن السياحة واليخوت والأسعار",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoolSilver
                        )
                    }
                }
                IconButton(onClick = { tts.stop() }) {
                    Icon(Icons.Filled.Stop, contentDescription = "إيقاف الصوت", tint = RoyalGoldLight)
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickPrompts) { prompt ->
                    AssistChip(
                        onClick = {
                            inputQuery = prompt
                            onAsk(prompt)
                        },
                        label = { Text(prompt, color = CrispWhite, maxLines = 1) },
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f))
                    )
                }
            }

            when (state) {
                is VoiceAssistantUiState.Idle -> {
                    GlassLuxuryCard(containerColor = MidnightGlassSoft) {
                        Text(
                            text = "أهلاً بك في KemetLoop! اضغط على أيقونة الميكروفون للتحدث أو اختر سؤالاً سريعاً وسأجيبك صوتياً فوراً.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CoolSilver
                        )
                    }
                }

                is VoiceAssistantUiState.Loading -> {
                    GlassLuxuryCard(containerColor = MidnightGlassSoft) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = RoyalGold, strokeWidth = 2.dp)
                            Text("جارٍ تجهيز الإجابة الذكية...", color = RoyalGoldLight)
                        }
                    }
                }

                is VoiceAssistantUiState.Error -> {
                    GlassLuxuryCard(borderColor = MaterialTheme.colorScheme.error) {
                        Text(state.message, color = CrispWhite, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                is VoiceAssistantUiState.Success -> {
                    GlassLuxuryCard(
                        borderColor = RoyalGold,
                        containerColor = MidnightGlassSoft,
                        modifier = Modifier.testTag("ai_voice_response_card")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "رد المساعد الذكي:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RoyalGoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { speakArabicOrAuto(state.reply) },
                                    modifier = Modifier.testTag("ai_voice_replay_btn")
                                ) {
                                    Icon(Icons.Filled.VolumeUp, contentDescription = "تشغيل الصوت", tint = RoyalGoldLight)
                                }
                            }
                            Text(
                                text = state.reply,
                                style = MaterialTheme.typography.bodyLarge,
                                color = CrispWhite
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = RoyalGold,
                    modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                                PackageManager.PERMISSION_GRANTED
                            if (granted) launchSpeech() else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        .testTag("ai_voice_mic_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Mic, contentDescription = "تحدث الآن", tint = ObsidianDeep)
                    }
                }

                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("اكتب سؤالك هنا...", color = CoolSilver) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_voice_input_field")
                )

                Button(
                    onClick = { onAsk(inputQuery) },
                    enabled = inputQuery.isNotBlank() && state !is VoiceAssistantUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricAzure, contentColor = CrispWhite),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                    modifier = Modifier.testTag("ai_voice_send_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun LiveRoomsDialog(
    onOpenAiVoiceRoom: () -> Unit,
    onOpenTranslatorRoom: () -> Unit,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val whatsappUrl = stringResource(R.string.developer_whatsapp_url)

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.VideoCall, contentDescription = null, tint = RoyalGoldLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text("غرف المحادثة والفيديو المباشر", color = CrispWhite, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "اختر الغرفة المناسبة للتواصل المباشر مع مستشاري السفر وتجار VIP أو الترجمة الفورية.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolSilver
                )

                LiveRoomOptionCard(
                    icon = Icons.Filled.AutoAwesome,
                    title = "غرفة المستشار السياحي الذكي (AI)",
                    subtitle = "محادثة صوتية وفورية على مدار 24 ساعة",
                    badgeText = "مباشر الآن",
                    accent = RoyalGold,
                    onClick = {
                        onDismiss()
                        onOpenAiVoiceRoom()
                    }
                )

                LiveRoomOptionCard(
                    icon = Icons.Filled.VideoCall,
                    title = "غرفة الفيديو المباشر مع VIP والدعم",
                    subtitle = "مكالمة فيديو ودردشة مباشرة عبر واتساب",
                    badgeText = "فيديو HD",
                    accent = EmeraldVerified,
                    onClick = {
                        onDismiss()
                        SafeIntentHelper.openWhatsApp(context, whatsappUrl, onShowMessage)
                    }
                )

                LiveRoomOptionCard(
                    icon = Icons.Filled.Translate,
                    title = "غرفة الترجمة الفورية للتجار والسياح",
                    subtitle = "ترجمة حية بالصوت والكاميرا بـ 8 لغات",
                    badgeText = "تفاعلي",
                    accent = ElectricAzure,
                    onClick = {
                        onDismiss()
                        onOpenTranslatorRoom()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = RoyalGoldLight, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun LiveRoomOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MidnightGlassSoft,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = CrispWhite, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = CoolSilver)
            }
            GoldTagPill(backgroundColor = accent.copy(alpha = 0.2f), borderColor = accent.copy(alpha = 0.6f)) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = CrispWhite,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
