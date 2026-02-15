package org.shekhawat.launcher.ui.theme.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.shekhawat.launcher.SharedPrefManager
import org.shekhawat.launcher.utils.UsageStatsHelper

/**
 * Screen time shown as a ModalBottomSheet, opened from the HomeScreen badge.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTimeSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val goalStr by sharedPrefManager.observeString("screen_time_goal", "180")
        .collectAsState(initial = sharedPrefManager.getString("screen_time_goal", "180"))
    val goalMinutes = goalStr.toIntOrNull()

    var hasPermission by remember { mutableStateOf(UsageStatsHelper.hasPermission(context)) }
    var totalScreenTimeMs by remember { mutableLongStateOf(0L) }
    var unlockCount by remember { mutableIntStateOf(0) }
    var perAppUsage by remember { mutableStateOf<List<UsageStatsHelper.AppUsageInfo>>(emptyList()) }
    var isWeekly by remember { mutableStateOf(false) }

    // Load data on open and when toggling daily/weekly
    LaunchedEffect(isWeekly) {
        hasPermission = UsageStatsHelper.hasPermission(context)
        if (hasPermission) {
            withContext(Dispatchers.IO) {
                perAppUsage = if (isWeekly) {
                    UsageStatsHelper.getWeeklyPerAppUsage(context)
                } else {
                    UsageStatsHelper.getTodayPerAppUsage(context)
                }
                totalScreenTimeMs = perAppUsage.sumOf { it.totalTimeMs }
                unlockCount = UsageStatsHelper.getTodayUnlockCount(context)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Screen Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (!hasPermission) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Usage Access Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Grant usage access to view screen time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Grant Permission")
                    }
                }
            } else {
                // Total screen time card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = if (isWeekly) "This Week" else "Today",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = UsageStatsHelper.formatDuration(totalScreenTimeMs),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Goal progress
                        if (goalMinutes != null && !isWeekly) {
                            val usedMinutes = (totalScreenTimeMs / 60_000).toInt()
                            val progress = (usedMinutes.toFloat() / goalMinutes).coerceIn(0f, 1f)
                            val overLimit = usedMinutes > goalMinutes

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Goal: ${goalMinutes / 60}h ${goalMinutes % 60}m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (overLimit) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                if (overLimit) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Over limit!",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (overLimit) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Unlocks: $unlockCount",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = if (isWeekly) "Show Today" else "Show Week",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { isWeekly = !isWeekly }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Per-App Usage",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .height(300.dp)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(perAppUsage) { appUsage ->
                        val fraction = if (totalScreenTimeMs > 0) {
                            (appUsage.totalTimeMs.toFloat() / totalScreenTimeMs).coerceIn(0f, 1f)
                        } else 0f

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appUsage.appName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = UsageStatsHelper.formatDuration(appUsage.totalTimeMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
