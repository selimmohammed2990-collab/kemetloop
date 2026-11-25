package com.example.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.SafeIntentHelper
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SmartWebViewScreen(
    initialUrl: String,
    initialTitle: String,
    onClose: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val safeStartUrl = remember(initialUrl) {
        SafeIntentHelper.normalizeWebUrl(initialUrl) ?: "https://kemet.io"
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(safeStartUrl) }
    var pageTitle by remember { mutableStateOf(initialTitle.ifBlank { "متصفح KemetLoop الذكي" }) }
    var progress by remember { mutableIntStateOf(15) }
    var canGoBack by remember { mutableStateOf(false) }

    // System back button returns inside page history first, then exits WebView
    BackHandler(enabled = true) {
        val wv = webViewRef
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            onClose()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDeep)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Smart Top Control Bar (Back, Title, Refresh, Open External, Close)
        Surface(
            color = MidnightGlass,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            val wv = webViewRef
                            if (wv != null && wv.canGoBack()) wv.goBack() else onClose()
                        },
                        modifier = Modifier.testTag("webview_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (canGoBack) "رجوع داخل الصفحة" else "رجوع للتطبيق",
                            tint = RoyalGoldLight
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pageTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = Uri.parse(currentUrl).host ?: currentUrl,
                            style = MaterialTheme.typography.labelSmall,
                            color = CoolSilver,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { webViewRef?.reload() },
                        modifier = Modifier.testTag("webview_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "تحديث الصفحة",
                            tint = CrispWhite
                        )
                    }

                    IconButton(
                        onClick = {
                            SafeIntentHelper.openExternalBrowser(context, currentUrl, onShowMessage)
                        },
                        modifier = Modifier.testTag("webview_external_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInNew,
                            contentDescription = "فتح في المتصفح الخارجي",
                            tint = RoyalGoldLight
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("webview_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "إغلاق المتصفح",
                            tint = CrispWhite
                        )
                    }
                }

                if (progress in 1..99) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        color = RoyalGold,
                        trackColor = MidnightNavy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("webview_progress_bar")
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                                canGoBack = view?.canGoBack() == true
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                if (!title.isNullOrBlank()) {
                                    pageTitle = title
                                }
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val target = request?.url?.toString().orEmpty()
                                val scheme = request?.url?.scheme?.lowercase().orEmpty()
                                return when {
                                    scheme == "http" || scheme == "https" -> {
                                        if (target.contains("wa.me/")) {
                                            SafeIntentHelper.openWhatsApp(ctx, target, onShowMessage)
                                            true
                                        } else {
                                            currentUrl = target
                                            false
                                        }
                                    }
                                    scheme == "tel" -> {
                                        SafeIntentHelper.dialPhone(ctx, target.removePrefix("tel:"), onShowMessage)
                                        true
                                    }
                                    scheme == "mailto" || scheme == "whatsapp" -> {
                                        SafeIntentHelper.openWhatsApp(ctx, target, onShowMessage)
                                        true
                                    }
                                    else -> true // Block unsafe schemes like javascript:, file:, content:
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                if (!url.isNullOrBlank()) currentUrl = url
                                canGoBack = view?.canGoBack() == true
                                progress = 100
                            }
                        }

                        loadUrl(safeStartUrl)
                        webViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
