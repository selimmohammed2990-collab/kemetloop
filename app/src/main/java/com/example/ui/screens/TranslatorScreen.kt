package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.KemetCatalogDefaults
import com.example.model.SubscriptionPlan
import com.example.model.TranslatorLanguage
import com.example.model.UserProfile
import com.example.ui.components.AdMobBannerSlot
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
import com.example.viewmodel.KemetViewModel
import com.example.viewmodel.TranslatorUiState
import java.util.Locale

@Composable
fun TranslatorScreen(
    profile: UserProfile?,
    isOnline: Boolean,
    translatorState: TranslatorUiState,
    onTranslate: (String, TranslatorLanguage, TranslatorLanguage) -> Unit,
    onTranslateImage: (Bitmap, TranslatorLanguage) -> Unit,
    onNavigateToVip: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val languages = KemetCatalogDefaults.languages
    val activePlan = profile?.subscriptionPlan ?: SubscriptionPlan.FREE_STARTER
    val usedToday = profile?.dailyTranslationsUsed ?: 0L
    val remaining = (KemetViewModel.FREE_DAILY_TRANSLATION_LIMIT - usedToday).coerceAtLeast(0L)

    var sourceLang by remember { mutableStateOf(languages[0]) }
    var targetLang by remember { mutableStateOf(languages[1]) }
    var inputText by remember { mutableStateOf("") }
    var ttsReady by remember { mutableStateOf(false) }

    val tts = remember(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            ttsReady = (status == TextToSpeech.SUCCESS)
        }
        engine
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakOut(text: String, bcp47: String) {
        if (!ttsReady) {
            onShowMessage("محرك النطق الصوتي قيد التجهيز، حاول بعد لحظة.")
            return
        }
        val locale = Locale.forLanguageTag(bcp47)
        val res = tts.setLanguage(locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.language = Locale.US
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kemet_tts")
    }

    // Safe Camera Preview capture for Menu/Sign OCR
    val cameraCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onTranslateImage(bitmap, targetLang)
        } else {
            onShowMessage("لم يتم التقاط صورة.")
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            runCatching { cameraCaptureLauncher.launch(null) }
                .onFailure { onShowMessage("تعذر تشغيل تطبيق الكاميرا على هذا الجهاز.") }
        } else {
            onShowMessage("تم رفض صلاحية الكاميرا؛ يمكنك اختيار صورة قائمة الطعام من المعرض بدون صلاحيات.")
        }
    }

    // Zero-permission Photo Picker for Menu/Sign OCR
    val galleryOcrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }.copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            }.getOrNull()
            if (bmp != null) {
                onTranslateImage(bmp, targetLang)
            } else {
                onShowMessage("تعذر قراءة الصورة المختارة.")
            }
        }
    }

    // Safe Voice Dictation (Microphone)
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                inputText = spoken
                onTranslate(spoken, sourceLang, targetLang)
            }
        }
    }

    fun launchSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLang.bcp47)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن باللغة ${sourceLang.nameAr}...")
        }
        runCatching { speechLauncher.launch(intent) }
            .onFailure { onShowMessage("الإملاء الصوتي غير مدعوم على هذا الجهاز/المحاكي؛ يمكنك كتابة النص أو استخدام الكاميرا.") }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchSpeechRecognizer()
        } else {
            onShowMessage("تم رفض صلاحية الميكروفون. يمكنك الكتابة أو ترجمة الصور بالكاميرا.")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassLuxuryCard(borderColor = RoyalGold.copy(alpha = 0.65f)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = null,
                                tint = RoyalGoldLight,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "المترجم الذكي والكاميرا OCR",
                                style = MaterialTheme.typography.titleLarge,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!isOnline) {
                            GoldTagPill(backgroundColor = EmeraldVerified.copy(alpha = 0.2f)) {
                                Text(
                                    text = "وضع الأوفلاين",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldVerified,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "ترجمة النصوص، الصوت، وقوائم الطعام واللافتات بالكاميرا مع قاموس محفوظ بدون إنترنت.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoolSilver
                    )
                    GoldTagPill {
                        Text(
                            text = if (activePlan == SubscriptionPlan.FREE_STARTER) {
                                "الرصيد المجاني المتبقي: $remaining من ${KemetViewModel.FREE_DAILY_TRANSLATION_LIMIT} ترجمات"
                            } else {
                                "ترجمة صوتية ومرئية غير محدودة (${activePlan.labelAr})"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = RoyalGoldLight,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Camera OCR & Voice Dictation Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            runCatching { cameraCaptureLauncher.launch(null) }
                                .onFailure { onShowMessage("تعذر تشغيل الكاميرا.") }
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("camera_ocr_button")
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ترجمة كاميرا", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        galleryOcrLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gallery_ocr_button")
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("صورة قائمة", style = MaterialTheme.typography.labelMedium, color = CrispWhite)
                }

                OutlinedButton(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            launchSpeechRecognizer()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    border = BorderStroke(1.dp, ElectricAzure.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("mic_dictation_button")
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = "تحدث للترجمة", tint = ElectricAzure, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Language picker row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LanguageSelectorBox(
                    label = "من",
                    selected = sourceLang,
                    languages = languages,
                    onSelect = { sourceLang = it },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val temp = sourceLang
                        sourceLang = targetLang
                        targetLang = temp
                    },
                    modifier = Modifier.testTag("swap_languages_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "تبديل اللغتين",
                        tint = RoyalGoldLight
                    )
                }

                LanguageSelectorBox(
                    label = "إلى",
                    selected = targetLang,
                    languages = languages,
                    onSelect = { targetLang = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Phrases
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "عبارات سريعة للمسافرين والتجار",
                    style = MaterialTheme.typography.labelLarge,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(KemetCatalogDefaults.quickPhrases) { phrase ->
                        AssistChip(
                            onClick = { inputText = phrase },
                            label = { Text(phrase, color = CrispWhite, maxLines = 1) },
                            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }

        // Input Box + Translate Button
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("اكتب الجملة أو استخدم الكاميرا لقوائم الطعام...") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("translator_input_field")
                )

                Button(
                    onClick = { onTranslate(inputText, sourceLang, targetLang) },
                    enabled = inputText.isNotBlank() && translatorState !is TranslatorUiState.Loading,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = ObsidianDeep
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("translate_submit_button")
                ) {
                    if (translatorState is TranslatorUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = ObsidianDeep,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ترجمة فورية ذكية", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Translation Output
        item {
            when (translatorState) {
                is TranslatorUiState.Idle -> {
                    GlassLuxuryCard(containerColor = MidnightGlassSoft) {
                        Text(
                            text = "اختر اللغتين وأدخل النص أو التقط صورة للافتة/قائمة الطعام لترجمتها فوراً.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CoolSilver
                        )
                    }
                }

                is TranslatorUiState.Loading -> Unit

                is TranslatorUiState.Error -> {
                    GlassLuxuryCard(borderColor = MaterialTheme.colorScheme.error) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = translatorState.message,
                                color = CrispWhite,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (activePlan == SubscriptionPlan.FREE_STARTER) {
                                OutlinedButton(onClick = onNavigateToVip) {
                                    Text("ترقية إلى Pro لترجمة غير محدودة", color = RoyalGoldLight)
                                }
                            }
                        }
                    }
                }

                is TranslatorUiState.Success -> {
                    val result = translatorState.result
                    GlassLuxuryCard(
                        borderColor = RoyalGold,
                        modifier = Modifier.testTag("translation_result_card")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "الترجمة (${targetLang.nameAr})",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = RoyalGoldLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (result.fromOfflineCache) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        GoldTagPill(backgroundColor = EmeraldVerified.copy(alpha = 0.2f)) {
                                            Text(
                                                "محلي بدون إنترنت",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EmeraldVerified,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Row {
                                    IconButton(
                                        onClick = { speakOut(result.translatedText, targetLang.bcp47) },
                                        modifier = Modifier.testTag("tts_speak_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.VolumeUp,
                                            contentDescription = "نطق صوتي",
                                            tint = RoyalGoldLight
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(result.translatedText))
                                            onShowMessage("تم نسخ الترجمة.")
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "نسخ الترجمة",
                                            tint = CrispWhite
                                        )
                                    }
                                }
                            }

                            if (result.extractedSourceText.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = ObsidianDeep.copy(alpha = 0.65f),
                                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "النص المستخرج من الكاميرا: ${result.extractedSourceText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CoolSilver,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Text(
                                text = result.translatedText,
                                style = MaterialTheme.typography.headlineSmall,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )

                            if (result.pronunciationGuide.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MidnightGlassSoft,
                                    border = BorderStroke(1.dp, ElectricAzure.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "النطق التقريبي: ${result.pronunciationGuide}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CrispWhite,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            if (result.culturalTip.isNotBlank()) {
                                Text(
                                    text = "نصيحة للمسافر/التاجر: ${result.culturalTip}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CoolSilver
                                )
                            }

                            Button(
                                onClick = { speakOut(result.translatedText, targetLang.bcp47) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricAzure,
                                    contentColor = CrispWhite
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("استمع للنطق الصوتي (TTS)")
                            }
                        }
                    }
                }
            }
        }

        // Offline Saved Phrasebook Section (always works without internet)
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.OfflinePin, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "أشهر الجمل المترجمة (تعمل بدون إنترنت)",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(KemetCatalogDefaults.offlineTopPhrases, key = { it.id }) { phrase ->
            val translatedOffline = phrase.textForCode(targetLang.code)
            GlassLuxuryCard(
                containerColor = MidnightGlassSoft,
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(phrase.categoryAr, style = MaterialTheme.typography.labelSmall, color = RoyalGoldLight)
                        Text(phrase.arabic, style = MaterialTheme.typography.bodyMedium, color = CrispWhite, fontWeight = FontWeight.Bold)
                        Text(translatedOffline, style = MaterialTheme.typography.bodySmall, color = EmeraldVerified)
                    }
                    IconButton(onClick = { speakOut(translatedOffline, targetLang.bcp47) }) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = "نطق العبارة", tint = RoyalGoldLight)
                    }
                }
            }
        }

        item {
            AdMobBannerSlot(
                activePlan = activePlan,
                onUpgradeClick = onNavigateToVip
            )
        }
    }
}

@Composable
private fun LanguageSelectorBox(
    label: String,
    selected: TranslatorLanguage,
    languages: List<TranslatorLanguage>,
    onSelect: (TranslatorLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MidnightGlass,
            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = CoolSilver
                )
                Text(
                    text = selected.nameAr,
                    style = MaterialTheme.typography.titleMedium,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.nameAr) },
                    onClick = {
                        onSelect(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}
