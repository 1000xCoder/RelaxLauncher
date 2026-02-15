package org.shekhawat.launcher.ui.theme.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import org.shekhawat.launcher.AppInfo
import org.shekhawat.launcher.SettingActivity
import org.shekhawat.launcher.SharedPrefManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Minimalist home screen: text-only, distraction-free.
 * Shows time, date, favorite apps as text, and an "All Apps" text-list.
 */
@Composable
fun MinimalistHomeScreen(appList: List<AppInfo>) {
    val context = LocalContext.current
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }

    val use24h by sharedPrefManager.observeBoolean("use_24h_clock", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("use_24h_clock", true))
    val dateFormat by sharedPrefManager.observeString("date_format", "EEE, MMM d")
        .collectAsState(initial = sharedPrefManager.getString("date_format", "EEE, MMM d"))

    val favoriteAppsStr by sharedPrefManager.observeString("favorite_apps", "")
        .collectAsState(initial = sharedPrefManager.getString("favorite_apps", ""))
    val favoriteApps = remember(favoriteAppsStr, appList) {
        val favPkgs = favoriteAppsStr.split(",").filter { it.isNotBlank() }.toSet()
        appList.filter { it.packageName in favPkgs }
    }

    val hiddenAppsStr by sharedPrefManager.observeString("hidden_apps", "")
        .collectAsState(initial = sharedPrefManager.getString("hidden_apps", ""))
    val visibleApps = remember(appList, hiddenAppsStr) {
        val hidden = hiddenAppsStr.split(",").filter { it.isNotBlank() }.toSet()
        appList.filter { it.packageName !in hidden }.sortedBy { it.name.lowercase() }
    }

    var showAllApps by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val now = remember { mutableStateOf(LocalDateTime.now()) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            now.value = LocalDateTime.now()
            kotlinx.coroutines.delay(1000)
        }
    }

    val time = now.value
    val hourStr = if (use24h) {
        String.format("%02d", time.hour)
    } else {
        val h = time.hour % 12
        String.format("%d", if (h == 0) 12 else h)
    }
    val amPm = if (!use24h) { if (time.hour < 12) " AM" else " PM" } else ""
    val timeText = "$hourStr:${String.format("%02d", time.minute)}$amPm"

    val formattedDate = try {
        DateTimeFormatter.ofPattern(dateFormat).format(time)
    } catch (_: Exception) {
        "${time.dayOfWeek.toString().substring(0, 3)}, ${time.month.toString().substring(0, 3)} ${time.dayOfMonth}"
    }

    if (showAllApps) {
        // Text-only alphabetical app list
        MinimalistAppList(
            apps = visibleApps,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onAppClick = { launchMinimalistApp(context, it) },
            onBack = { showAllApps = false; searchQuery = "" }
        )
    } else {
        // Main minimalist home
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 32.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.Center
            ) {
                // Time
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Thin,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                // Date
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                // Favorite apps as text
                val appsToShow = if (favoriteApps.isNotEmpty()) {
                    favoriteApps.take(6)
                } else {
                    visibleApps.take(4)
                }
                appsToShow.forEach { app ->
                    MinimalistAppItem(
                        app = app,
                        onTap = { launchMinimalistApp(context, app) },
                        context = context
                    )
                }
            }

            // Bottom: All Apps + Settings
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "All Apps",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable { showAllApps = true }
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f),
                    modifier = Modifier
                        .clickable {
                            context.startActivity(Intent(context, SettingActivity::class.java))
                        }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun MinimalistAppList(
    apps: List<AppInfo>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onBack: () -> Unit
) {
    val filtered = if (searchQuery.isBlank()) apps
    else apps.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        // Back
        Text(
            text = "< Back",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            modifier = Modifier
                .clickable { onBack() }
                .padding(bottom = 12.dp)
        )

        // Search
        BasicTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
            decorationBox = { innerTextField ->
                Box {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // App list
        val listContext = LocalContext.current
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filtered) { app ->
                MinimalistAppListItem(
                    app = app,
                    onTap = { onAppClick(app) },
                    context = listContext
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MinimalistAppItem(
    app: AppInfo,
    onTap: () -> Unit,
    context: Context
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(
            text = app.name,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Light
            ),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            modifier = Modifier
                .combinedClickable(
                    onClick = onTap,
                    onLongClick = { expanded = true }
                )
                .padding(vertical = 8.dp)
        )
        MinimalistContextMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            appInfo = app,
            context = context
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MinimalistAppListItem(
    app: AppInfo,
    onTap: () -> Unit,
    context: Context
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onTap,
                    onLongClick = { expanded = true }
                )
                .padding(vertical = 10.dp)
        )
        MinimalistContextMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            appInfo = app,
            context = context
        )
    }
}

@Composable
private fun MinimalistContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    appInfo: AppInfo,
    context: Context
) {
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }
    var showLimitOptions by remember { mutableStateOf(false) }
    var showCustomInput by remember { mutableStateOf(false) }
    val realPkg = appInfo.packageName.removeSuffix("#work")
    val currentLimit = remember(realPkg) {
        val limitsStr = sharedPrefManager.getString("app_limits", "{}")
        try {
            val json = org.json.JSONObject(limitsStr)
            if (json.has(realPkg)) json.getInt(realPkg) else 0
        } catch (_: Exception) { 0 }
    }

    val isSelf = appInfo.name == "App Settings"

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            showLimitOptions = false
            onDismiss()
        },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        properties = PopupProperties(focusable = true)
    ) {
        if (!showLimitOptions) {
            DropdownMenuItem(
                text = { Text("App Info", color = MaterialTheme.colorScheme.onSurface) },
                onClick = {
                    onDismiss()
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", realPkg, null)
                    context.startActivity(intent)
                }
            )
            // Only show these options for other apps, not for the launcher itself
            if (!isSelf) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                DropdownMenuItem(
                    text = { Text("Uninstall", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onDismiss()
                        try {
                            val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                            uninstallIntent.data = Uri.parse("package:$realPkg")
                            uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                            uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(uninstallIntent)
                        } catch (_: Exception) {
                            try {
                                val fallback = Intent(Intent.ACTION_DELETE)
                                fallback.data = Uri.parse("package:$realPkg")
                                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(fallback)
                            } catch (_: Exception) { }
                        }
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                DropdownMenuItem(
                    text = { Text("Hide App", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onDismiss()
                        val current = sharedPrefManager.getString("hidden_apps", "")
                        val hiddenSet = current.split(",").filter { it.isNotBlank() }.toMutableSet()
                        hiddenSet.add(appInfo.packageName)
                        sharedPrefManager.saveString("hidden_apps", hiddenSet.joinToString(","))
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )

                val isFav = remember(appInfo.packageName) {
                    val current = sharedPrefManager.getString("favorite_apps", "")
                    appInfo.packageName in current.split(",").filter { it.isNotBlank() }.toSet()
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            if (isFav) "Remove from Favorites" else "Add to Favorites",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onDismiss()
                        val current = sharedPrefManager.getString("favorite_apps", "")
                        val favSet = current.split(",").filter { it.isNotBlank() }.toMutableSet()
                        if (appInfo.packageName in favSet) {
                            favSet.remove(appInfo.packageName)
                        } else {
                            favSet.add(appInfo.packageName)
                        }
                        sharedPrefManager.saveString("favorite_apps", favSet.joinToString(","))
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (currentLimit > 0) "Time Limit: ${currentLimit}m" else "Set Time Limit",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = { showLimitOptions = true }
                )
            }
        } else {
            // Time limit sub-menu
            DropdownMenuItem(
                text = { Text("← Back", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                onClick = { showLimitOptions = false }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            val limitOptions = listOf(15, 30, 45, 60, 90, 120)
            limitOptions.forEach { minutes ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (currentLimit == minutes) "$minutes min ✓" else "$minutes min",
                            color = if (currentLimit == minutes) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        showLimitOptions = false
                        onDismiss()
                        val limitsStr = sharedPrefManager.getString("app_limits", "{}")
                        val json = try { org.json.JSONObject(limitsStr) } catch (_: Exception) { org.json.JSONObject() }
                        json.put(realPkg, minutes)
                        sharedPrefManager.saveString("app_limits", json.toString())
                        // Ensure the monitor service is running
                        if (org.shekhawat.launcher.utils.UsageStatsHelper.hasPermission(context)) {
                            org.shekhawat.launcher.service.AppUsageMonitorService.start(context)
                        }
                    }
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            DropdownMenuItem(
                text = { Text("Custom...", color = MaterialTheme.colorScheme.onSurface) },
                onClick = {
                    showCustomInput = true
                    showLimitOptions = false
                    onDismiss()
                }
            )
            if (currentLimit > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                DropdownMenuItem(
                    text = { Text("Remove Limit", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showLimitOptions = false
                        onDismiss()
                        val limitsStr = sharedPrefManager.getString("app_limits", "{}")
                        val json = try { org.json.JSONObject(limitsStr) } catch (_: Exception) { org.json.JSONObject() }
                        json.remove(realPkg)
                        sharedPrefManager.saveString("app_limits", json.toString())
                    }
                )
            }
        }
    }

    // Custom time limit dialog
    if (showCustomInput) {
        var customMinutesText by remember { mutableStateOf(if (currentLimit > 0) currentLimit.toString() else "") }
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showCustomInput = false }
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Custom Time Limit",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = customMinutesText,
                    onValueChange = { newVal ->
                        customMinutesText = newVal.filter { it.isDigit() }
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    singleLine = true,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (customMinutesText.isEmpty()) {
                                Text(
                                    text = "Minutes (e.g. 25)",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { showCustomInput = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    androidx.compose.material3.Button(
                        onClick = {
                            val mins = customMinutesText.toIntOrNull()
                            if (mins != null && mins > 0) {
                                val limitsStr = sharedPrefManager.getString("app_limits", "{}")
                                val json = try { org.json.JSONObject(limitsStr) } catch (_: Exception) { org.json.JSONObject() }
                                json.put(realPkg, mins)
                                sharedPrefManager.saveString("app_limits", json.toString())
                                // Ensure the monitor service is running
                                if (org.shekhawat.launcher.utils.UsageStatsHelper.hasPermission(context)) {
                                    org.shekhawat.launcher.service.AppUsageMonitorService.start(context)
                                }
                                showCustomInput = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = (customMinutesText.toIntOrNull() ?: 0) > 0
                    ) {
                        Text("Set")
                    }
                }
            }
        }
    }
}

private fun launchMinimalistApp(context: Context, appInfo: AppInfo) {
    if (appInfo.name == "App Settings") {
        context.startActivity(Intent(context, SettingActivity::class.java))
        return
    }

    // Block check — no exceptions
    if (!org.shekhawat.launcher.utils.AppBlocker.canLaunch(context, appInfo.packageName)) return

    if (appInfo.isWorkProfile && appInfo.componentName != null && appInfo.userHandle != null) {
        try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps
            launcherApps.startMainActivity(appInfo.componentName, appInfo.userHandle, null, null)
        } catch (_: Exception) { }
    } else {
        appInfo.intent?.let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
