package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
import com.example.ui.components.AmbientLuxuryBackground
import com.example.ui.components.GlassLuxuryCard
import com.example.ui.components.GoldTagPill
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.MidnightGlassSoft
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    AmbientLuxuryBackground(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Official KemetLoop Brand Emblem with subtle azure + royal gold halo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(112.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                RoyalGold.copy(alpha = 0.28f),
                                ElectricAzure.copy(alpha = 0.16f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = ObsidianDeep,
                    border = BorderStroke(1.75.dp, RoyalGold),
                    shadowElevation = 14.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_kemetloop_royal_emblem),
                        contentDescription = "شعار KemetLoop",
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(26.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Text(
                text = "KemetLoop",
                style = MaterialTheme.typography.displaySmall,
                color = RoyalGoldLight,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "السياحة الذكية والتجارة الفاخرة والترجمة الفورية والمواهب",
                style = MaterialTheme.typography.bodyLarge,
                color = CoolSilver,
                textAlign = TextAlign.Center
            )

            // Panoramic luxury tourism + technology card
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
                colors = CardDefaults.cardColors(containerColor = ObsidianDeep)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_luxury_tourism_tech),
                        contentDescription = "السياحة والتكنولوجيا في KemetLoop",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(185.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, ObsidianDeep.copy(alpha = 0.85f))
                                )
                            )
                    )
                    GoldTagPill(
                        backgroundColor = ObsidianDeep.copy(alpha = 0.85f),
                        borderColor = RoyalGold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CardGiftcard,
                                contentDescription = null,
                                tint = RoyalGoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "هدية الترحيب: 100 عملة مجانية فور الدخول",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Glass Bento Highlights
            GlassLuxuryCard(contentPadding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AuthFeatureRow(
                        icon = Icons.Filled.Storefront,
                        title = "سوق النخبة والتسويق بالعمولة (10%)",
                        subtitle = "عروض منتجات ورحلات وخدمات بشارات VIP ذهبية"
                    )
                    AuthFeatureRow(
                        icon = Icons.Filled.Translate,
                        title = "مترجم فوري ذكي + نطق صوتي",
                        subtitle = "ترجمة ذكية فورية للمسافرين والتجار بلغات متعددة"
                    )
                    AuthFeatureRow(
                        icon = Icons.Filled.WorkspacePremium,
                        title = "ألعاب ثقافية، مواهب، وباقات VIP",
                        subtitle = "دفع مرن عبر فودافون كاش أو إنستا باي"
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                GlassLuxuryCard(
                    borderColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = CrispWhite,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Google Sign-In Primary Gold CTA
            Button(
                onClick = {
                    errorMessage = null
                    isLoading = true
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isLoading = false
                            onAuthSuccess()
                        },
                        onAuthError = { msg ->
                            isLoading = false
                            errorMessage = msg
                        },
                        scope = scope,
                        onAuthCancelled = {
                            isLoading = false
                        }
                    )
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGold,
                    contentColor = ObsidianDeep
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("google_sign_in_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = ObsidianDeep,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(imageVector = Icons.Filled.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "تسجيل الدخول عبر Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Instant Guest Mode Button
            OutlinedButton(
                onClick = onContinueAsGuest,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.65f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MidnightGlassSoft,
                    contentColor = CrispWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("continue_as_guest_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Explore,
                    contentDescription = null,
                    tint = RoyalGoldLight
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "الدخول كزائر / تصفح فوري",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(RoyalGold.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RoyalGoldLight,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = CrispWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CoolSilver
            )
        }
    }
}
