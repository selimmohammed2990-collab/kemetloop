package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.model.MarketplaceListing
import com.example.model.SenderWalletType
import com.example.model.SubscriptionPlan
import com.example.model.VipPaymentRequest
import com.example.ui.components.GlassLuxuryCard
import com.example.ui.components.GoldTagPill
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.MidnightGlassSoft
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TerracottaError
import com.example.viewmodel.UiState

@Composable
fun DeveloperAdminDialog(
    isPinConfigured: Boolean,
    isAdminUnlocked: Boolean,
    isAdminEmail: Boolean,
    isGuestMode: Boolean,
    vipRequestsState: UiState<List<VipPaymentRequest>>,
    listingsState: UiState<List<MarketplaceListing>>,
    isActionLoading: Boolean,
    onSetupPin: (String) -> Boolean,
    onUnlockWithPin: (String) -> Boolean,
    onApproveRequest: (VipPaymentRequest) -> Unit,
    onRejectRequest: (VipPaymentRequest) -> Unit,
    onToggleVerifyListing: (MarketplaceListing) -> Unit,
    onDismiss: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var adminTab by remember { mutableIntStateOf(0) } // 0: VIP Requests, 1: Verify Businesses

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isAdminUnlocked) Icons.Filled.AdminPanelSettings else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = RoyalGoldLight
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "لوحة تحكم المطور (Selim Mohamed Selim)",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (!isAdminUnlocked) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (!isPinConfigured) {
                            "مرحباً أيها المطور! قم بتعيين الرمز السري لأول مرة لحماية بوابة مراجعة مدفوعات فودافون كاش (01006256085) وتوثيق الأنشطة التجارية."
                        } else {
                            "أدخل الرمز السري للمطور للوصول إلى مراجعة طلبات VIP وتوثيق التجار."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoolSilver
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text(if (!isPinConfigured) "إنشاء الرمز السري (4-12 خانة)" else "الرمز السري") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input")
                    )
                    if (!isPinConfigured) {
                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { confirmPinInput = it },
                            label = { Text("تأكيد الرمز السري") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_pin_confirm_input")
                        )
                    }
                    Button(
                        onClick = {
                            if (!isPinConfigured) {
                                if (pinInput.trim() == confirmPinInput.trim()) {
                                    onSetupPin(pinInput)
                                }
                            } else {
                                onUnlockWithPin(pinInput)
                            }
                        },
                        enabled = pinInput.trim().length >= 4 && (isPinConfigured || pinInput.trim() == confirmPinInput.trim()),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_unlock_button")
                    ) {
                        Text(if (!isPinConfigured) "حفظ وفتح لوحة الإدارة" else "فتح لوحة الإدارة", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val vipRequests = (vipRequestsState as? UiState.Success)?.data.orEmpty()
                val listings = (listingsState as? UiState.Success)?.data.orEmpty()

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!isGuestMode && !isAdminEmail) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RoyalGold.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "تنبيه أمان السحابة: لتفعيل طلبات المستخدمين الآخرين سحابياً، تأكد من تسجيل الدخول ببريد المطور الرسمي (selimmmohammed@gmail.com).",
                                style = MaterialTheme.typography.labelSmall,
                                color = RoyalGoldLight,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = adminTab == 0,
                            onClick = { adminTab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors = SegmentedButtonDefaults.colors(activeContainerColor = RoyalGold, activeContentColor = ObsidianDeep)
                        ) {
                            Text("طلبات VIP (${vipRequests.size})", maxLines = 1)
                        }
                        SegmentedButton(
                            selected = adminTab == 1,
                            onClick = { adminTab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors = SegmentedButtonDefaults.colors(activeContainerColor = RoyalGold, activeContentColor = ObsidianDeep)
                        ) {
                            Text("توثيق التجار (${listings.size})", maxLines = 1)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (adminTab == 0) {
                            if (vipRequests.isEmpty()) {
                                item {
                                    Text("لا توجد طلبات دفع VIP حالياً.", color = CoolSilver)
                                }
                            } else {
                                items(vipRequests, key = { it.id }) { req ->
                                    AdminVipRequestRow(
                                        req = req,
                                        isLoading = isActionLoading,
                                        onApprove = { onApproveRequest(req) },
                                        onReject = { onRejectRequest(req) }
                                    )
                                }
                            }
                        } else {
                            items(listings, key = { it.id }) { listing ->
                                AdminListingVerifyRow(
                                    listing = listing,
                                    isLoading = isActionLoading,
                                    onToggleVerify = { onToggleVerifyListing(listing) }
                                )
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

@Composable
private fun AdminVipRequestRow(
    req: VipPaymentRequest,
    isLoading: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    GlassLuxuryCard(
        cornerRadius = 14.dp,
        containerColor = MidnightGlassSoft,
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(req.userName, style = MaterialTheme.typography.titleSmall, color = CrispWhite, fontWeight = FontWeight.Bold)
                GoldTagPill(
                    backgroundColor = when (req.status) {
                        "APPROVED" -> EmeraldVerified.copy(alpha = 0.25f)
                        "REJECTED" -> TerracottaError.copy(alpha = 0.25f)
                        else -> RoyalGold.copy(alpha = 0.25f)
                    }
                ) {
                    Text(
                        text = when (req.status) {
                            "APPROVED" -> "مفعّل ✓"
                            "REJECTED" -> "مرفوض"
                            else -> "قيد المراجعة"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = CrispWhite,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = "${SubscriptionPlan.fromRaw(req.targetPlan).labelAr} · ${req.amountEgp} ج.م · ${SenderWalletType.fromRaw(req.senderWalletType).labelAr}",
                style = MaterialTheme.typography.labelMedium,
                color = RoyalGoldLight
            )
            Text(
                text = "مرجع التحويل: ${req.txReference.ifBlank { "إيصال مرفق" }}",
                style = MaterialTheme.typography.labelSmall,
                color = CoolSilver
            )
            if (req.status == "PENDING") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onApprove,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldVerified, contentColor = CrispWhite),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تفعيل VIP ✓", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isLoading,
                        border = BorderStroke(1.dp, TerracottaError.copy(alpha = 0.7f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = TerracottaError, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("رفض", color = TerracottaError, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminListingVerifyRow(
    listing: MarketplaceListing,
    isLoading: Boolean,
    onToggleVerify: () -> Unit
) {
    GlassLuxuryCard(
        cornerRadius = 14.dp,
        containerColor = MidnightGlassSoft,
        contentPadding = PaddingValues(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(listing.sellerName, style = MaterialTheme.typography.titleSmall, color = CrispWhite, fontWeight = FontWeight.Bold)
                    if (listing.verifiedSeller) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(15.dp))
                    }
                }
                Text(listing.title, style = MaterialTheme.typography.labelSmall, color = CoolSilver, maxLines = 1)
            }
            Button(
                onClick = onToggleVerify,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (listing.verifiedSeller) MidnightGlass else RoyalGold,
                    contentColor = if (listing.verifiedSeller) CrispWhite else ObsidianDeep
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (listing.verifiedSeller) "إلغاء التوثيق" else "توثيق ✓",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
