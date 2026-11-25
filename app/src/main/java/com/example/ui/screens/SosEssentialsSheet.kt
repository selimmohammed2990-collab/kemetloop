package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Atm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.SafeIntentHelper
import com.example.model.KemetCatalogDefaults
import com.example.ui.components.GlassLuxuryCard
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosEssentialsSheet(
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationLabel by remember { mutableStateOf("لم يتم تحديد الموقع عبر GPS بعد (يمكنك البحث أو الاتصال مباشرة)") }
    var isLocating by remember { mutableStateOf(false) }

    @SuppressLint("MissingPermission")
    fun fetchGpsCoordinates() {
        isLocating = true
        scope.launch {
            val loc = runCatching {
                fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
                    ?: fusedClient.lastLocation.await()
            }.getOrNull()

            if (loc != null) {
                latitude = loc.latitude
                longitude = loc.longitude
                val addressText = withContext(Dispatchers.IO) {
                    runCatching {
                        @Suppress("DEPRECATION")
                        Geocoder(context, Locale("ar"))
                            .getFromLocation(loc.latitude, loc.longitude, 1)
                            ?.firstOrNull()
                            ?.let { addr ->
                                listOfNotNull(addr.subLocality, addr.locality, addr.adminArea).joinToString(" - ")
                            }
                    }.getOrNull()
                }
                locationLabel = if (!addressText.isNullOrBlank()) {
                    "موقعك الحالي: $addressText"
                } else {
                    "تم تحديد الإحداثيات: ${String.format(Locale.US, "%.4f, %.4f", loc.latitude, loc.longitude)}"
                }
            } else {
                locationLabel = "تعذر الحصول على إشارة GPS حالياً؛ ستفتح الخرائط البحث في مصر."
            }
            isLocating = false
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchGpsCoordinates()
        } else {
            onShowMessage("تم رفض صلاحية الموقع؛ يمكنك الاتصال بأرقام الطوارئ أو فتح الخرائط بالبحث العام.")
        }
    }

    fun requestOrFetchLocation() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            fetchGpsCoordinates()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MidnightGlass
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Sos, contentDescription = null, tint = TerracottaError, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "مساعد الطوارئ والخدمات القريبة (SOS)",
                            style = MaterialTheme.typography.titleLarge,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "أرقام الطوارئ السياحية والخدمات الحيوية بضغطة زر",
                            style = MaterialTheme.typography.labelMedium,
                            color = CoolSilver
                        )
                    }
                }
            }

            // GPS Detection Card
            item {
                GlassLuxuryCard(
                    borderColor = ElectricAzure.copy(alpha = 0.6f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = locationLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrispWhite
                        )
                        Button(
                            onClick = ::requestOrFetchLocation,
                            enabled = !isLocating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalGold,
                                contentColor = ObsidianDeep
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sos_detect_gps_button")
                        ) {
                            if (isLocating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ObsidianDeep, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جارٍ تحديد موقعك عبر GPS...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تحديد موقعي الحالي (GPS) للخدمات القريبة", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Nearby Essentials Quick Finders
            item {
                Text(
                    text = "أقرب الخدمات الحيوية لموقعك",
                    style = MaterialTheme.typography.titleMedium,
                    color = RoyalGoldLight,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NearbyTile(
                            icon = Icons.Filled.LocalHospital,
                            title = "أقرب مستشفى",
                            accent = TerracottaError,
                            modifier = Modifier.weight(1f),
                            onClick = { SafeIntentHelper.openNearbyMapSearch(context, "hospital", latitude, longitude, onShowMessage) }
                        )
                        NearbyTile(
                            icon = Icons.Filled.LocalPharmacy,
                            title = "أقرب صيدلية",
                            accent = EmeraldVerified,
                            modifier = Modifier.weight(1f),
                            onClick = { SafeIntentHelper.openNearbyMapSearch(context, "pharmacy", latitude, longitude, onShowMessage) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NearbyTile(
                            icon = Icons.Filled.Atm,
                            title = "ماكينة صراف ATM",
                            accent = RoyalGoldLight,
                            modifier = Modifier.weight(1f),
                            onClick = { SafeIntentHelper.openNearbyMapSearch(context, "ATM", latitude, longitude, onShowMessage) }
                        )
                        NearbyTile(
                            icon = Icons.Filled.Security,
                            title = "شرطة السياحة",
                            accent = ElectricAzure,
                            modifier = Modifier.weight(1f),
                            onClick = { SafeIntentHelper.openNearbyMapSearch(context, "tourist police", latitude, longitude, onShowMessage) }
                        )
                    }
                }
            }

            // Official Egyptian Emergency Numbers
            item {
                Text(
                    text = "أرقام الطوارئ السياحية والإسعاف في مصر",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            items(KemetCatalogDefaults.emergencyHotlines, key = { it.id }) { hotline ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MidnightGlassSoft,
                    border = BorderStroke(1.dp, TerracottaError.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { SafeIntentHelper.dialPhone(context, hotline.phoneNumber, onShowMessage) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(hotline.titleAr, style = MaterialTheme.typography.titleSmall, color = CrispWhite, fontWeight = FontWeight.Bold)
                            Text(hotline.subtitleAr, style = MaterialTheme.typography.labelSmall, color = CoolSilver)
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TerracottaError
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Call, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(hotline.phoneNumber, color = CrispWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyTile(
    icon: ImageVector,
    title: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MidnightGlassSoft,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = accent, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, color = CrispWhite, fontWeight = FontWeight.Bold)
        }
    }
}
