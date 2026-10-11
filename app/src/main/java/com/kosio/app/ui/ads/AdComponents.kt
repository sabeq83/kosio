package com.kosio.app.ui.ads

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosio.app.core.designsystem.BorderSubtle
import com.kosio.app.core.designsystem.CharcoalText
import com.kosio.app.core.designsystem.CharcoalTextMuted
import com.kosio.app.core.designsystem.CharcoalTextSecondary
import com.kosio.app.core.designsystem.DeepIndigo
import com.kosio.app.core.designsystem.LightAmber
import com.kosio.app.core.designsystem.LightIndigo
import com.kosio.app.core.designsystem.LightTeal
import com.kosio.app.core.designsystem.PrimaryIndigo
import com.kosio.app.core.designsystem.PureWhiteSurface
import com.kosio.app.core.designsystem.SuccessTeal
import com.kosio.app.core.designsystem.WarningAmber
import com.kosio.app.data.ads.AdMobManager
import com.kosio.app.data.ads.OcrQuotaState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrQuotaExhaustedDialog(
    quotaState: OcrQuotaState,
    onDismiss: () -> Unit,
    onRewardEarned: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhiteSurface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = CharcoalTextMuted
                        )
                    }
                }

                // Golden Gift Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(LightAmber, LightIndigo)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tambah Kuota Scan Struk",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                        fontSize = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Kuota gratis harian Anda (${quotaState.dailyUsed}/${quotaState.dailyFreeQuota}) telah terpakai. Tonton video singkat (15 dtk) untuk mendapatkan +3 Kuota Scan Tambahan secara GRATIS!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CharcoalTextSecondary,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Benefit summary card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LightTeal.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = SuccessTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "+3 Kuota OCR Struk Baru",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                            )
                            Text(
                                text = "Bisa langsung dipakai scan struk belanja",
                                style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextMuted)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Watch Video Action Button
                Button(
                    onClick = {
                        if (activity != null) {
                            AdMobManager.showRewardedAd(
                                activity = activity,
                                onUserEarnedReward = {
                                    onRewardEarned()
                                },
                                onAdDismissed = onDismiss
                            )
                        } else {
                            onRewardEarned()
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryIndigo,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tonton Video (+3 Scan)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Nanti Saja",
                        style = MaterialTheme.typography.labelMedium.copy(color = CharcoalTextMuted)
                    )
                }
            }
        }
    }
}

@Composable
fun OcrQuotaBadge(
    totalAvailable: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (totalAvailable > 0) LightTeal.copy(alpha = 0.6f) else LightAmber.copy(alpha = 0.6f),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DocumentScanner,
                contentDescription = null,
                tint = if (totalAvailable > 0) SuccessTeal else WarningAmber,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Sisa Scan: $totalAvailable",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (totalAvailable > 0) SuccessTeal else WarningAmber
                )
            )
        }
    }
}

@Composable
fun KosioNativeAdCard(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PureWhiteSurface,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LightIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = DeepIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Peralatan Dapur F&B",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                        )
                        Text(
                            text = "Mitra Usaha Kuliner Kosio",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CharcoalTextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Ad Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = CharcoalTextMuted.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "IKLAN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = CharcoalTextSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tingkatkan efisiensi produksi kuliner Anda dengan peralatan commercial kitchen & packaging premium.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CharcoalTextSecondary,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* Open partner / AdMob CTA */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightIndigo,
                    contentColor = DeepIndigo
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Text(
                    text = "Lihat Rekomendasi Partner",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * In-Line Adaptive Banner Card embedded within ingredient list LazyColumn.
 * Compliant with AdMob policies: clear "IKLAN" label and clean container styling.
 */
@Composable
fun KosioAdaptiveBannerCard(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.TEST_BANNER_AD_UNIT_ID
) {
    var isAdFailed by remember { mutableStateOf(false) }

    if (!isAdFailed) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = PureWhiteSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rekomendasi Supplier & Bisnis",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CharcoalTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CharcoalTextMuted.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "IKLAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = CharcoalTextSecondary
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        factory = { ctx ->
                            AdView(ctx).apply {
                                setAdSize(AdSize.BANNER)
                                setAdUnitId(adUnitId)
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                adListener = object : AdListener() {
                                    override fun onAdLoaded() {
                                        Log.d("AdMobBanner", "Banner loaded successfully")
                                    }
                                    override fun onAdFailedToLoad(error: LoadAdError) {
                                        Log.e("AdMobBanner", "Banner failed to load: ${error.message}")
                                        isAdFailed = true
                                    }
                                }
                                loadAd(AdRequest.Builder().build())
                            }
                        }
                    )
                }
            }
        }
    }
}
