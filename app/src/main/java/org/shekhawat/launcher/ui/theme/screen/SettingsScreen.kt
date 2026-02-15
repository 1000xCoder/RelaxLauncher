package org.shekhawat.launcher.ui.theme.screen

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import org.shekhawat.launcher.AppInfo
import org.shekhawat.launcher.SharedPrefManager
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import org.shekhawat.launcher.ui.theme.FONT_OPTIONS
import org.shekhawat.launcher.ui.theme.transition.TRANSITION_OPTIONS
import org.shekhawat.launcher.viewmodel.SettingsViewModel

/**
 * Resolves a package name to a user-visible app label.
 * Returns the package name itself if resolution fails.
 */
private fun resolveAppName(pm: PackageManager, packageName: String): String {
    return try {
        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.getApplicationInfo(packageName, 0)
        }
        pm.getApplicationLabel(appInfo).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        packageName
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val pm = context.packageManager
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }
    val viewModel = remember(sharedPrefManager) { SettingsViewModel(sharedPrefManager) }

    // Theme
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(viewModel.getTheme()) }
    val options = listOf("LIGHT", "DARK", "PURPLE", "BLUE")

    // Dynamic Color
    var dynamicColorChecked by remember {
        mutableStateOf(viewModel.getBoolean("dynamic_color", false))
    }

    // Clock
    var use24HourClock by remember {
        mutableStateOf(viewModel.getBoolean("use_24h_clock", true))
    }
    var showBatteryPercentage by remember {
        mutableStateOf(viewModel.getBoolean("show_battery_percentage", true))
    }
    var showSeconds by remember {
        mutableStateOf(viewModel.getBoolean("show_seconds", true))
    }

    // Hidden Apps
    val hiddenAppsStr = remember { sharedPrefManager.getString("hidden_apps", "") }
    var hiddenApps by remember {
        mutableStateOf(hiddenAppsStr.split(",").filter { it.isNotBlank() })
    }

    // Font
    var fontExpanded by remember { mutableStateOf(false) }
    val savedFont = remember { sharedPrefManager.getString("font_family", "Default") }
    var selectedFont by remember { mutableStateOf(savedFont) }

    // Music animation
    var musicAnimExpanded by remember { mutableStateOf(false) }
    val savedMusicAnim = remember { sharedPrefManager.getString("music_anim_style", "Floating Notes") }
    var selectedMusicAnim by remember { mutableStateOf(savedMusicAnim) }

    // Wallpaper
    var wallpaperExpanded by remember { mutableStateOf(false) }
    val savedWallpaper = remember { sharedPrefManager.getString("wallpaper", "none") }
    var selectedWallpaper by remember { mutableStateOf(savedWallpaper) }
    val wallpaperOptions = listOf(
        "none", "wallpaper1", "wallpaper2", "wallpaper3", "wallpaper4",
        "wallpaper5", "wallpaper6", "wallpaper7", "wallpaper8"
    )

    // Double-tap lock
    var doubleTapLock by remember {
        mutableStateOf(viewModel.getBoolean("double_tap_lock", true))
    }

    // Swipe-down notification shade
    var swipeDownNotif by remember {
        mutableStateOf(viewModel.getBoolean("swipe_down_notif", true))
    }

    // Transition
    var transitionExpanded by remember { mutableStateOf(false) }
    val savedTransition = remember { sharedPrefManager.getString("pager_transition", "Depth") }
    var selectedTransition by remember { mutableStateOf(savedTransition) }

    // Favorite apps
    val favoriteAppsStr = remember { sharedPrefManager.getString("favorite_apps", "") }
    var favoriteApps by remember {
        mutableStateOf(favoriteAppsStr.split(",").filter { it.isNotBlank() })
    }

    // Date format on home
    var dateFormatExpanded by remember { mutableStateOf(false) }
    val savedDateFormat = remember { sharedPrefManager.getString("date_format", "MMM, d EEE") }
    var selectedDateFormat by remember { mutableStateOf(savedDateFormat) }
    val dateFormatOptions = listOf("MMM, d EEE", "d MMM yyyy", "EEE, MMM d", "yyyy-MM-dd", "dd/MM/yyyy")

    // ── Minimalist / Wellbeing ──
    var minimalistMode by remember {
        mutableStateOf(viewModel.getBoolean("minimalist_mode", false))
    }
    var grayscaleMode by remember {
        mutableStateOf(viewModel.getBoolean("grayscale_mode", false))
    }

    // Notifications
    var showNotifBadge by remember {
        mutableStateOf(viewModel.getBoolean("show_notif_badge", true))
    }
    var batchNotifications by remember {
        mutableStateOf(viewModel.getBoolean("batch_notifications", false))
    }

    // Screen time
    var screenTimeGoalExpanded by remember { mutableStateOf(false) }
    val savedGoal = remember { sharedPrefManager.getString("screen_time_goal", "180") }
    var selectedGoal by remember { mutableStateOf(savedGoal) }
    val goalOptions = listOf("60", "90", "120", "180", "240", "300", "Unlimited")

    var breakReminderExpanded by remember { mutableStateOf(false) }
    val savedBreakReminder = remember { sharedPrefManager.getString("break_reminder_minutes", "120") }
    var selectedBreakReminder by remember { mutableStateOf(savedBreakReminder) }
    val breakReminderOptions = listOf("30", "60", "90", "120", "180", "Off")

    // Quick settings customization
    val defaultQuickSettings = "wifi,bluetooth,torch,ringer,alarm,mobile_data"
    var quickSettingsStr by remember {
        mutableStateOf(sharedPrefManager.getString("quick_settings_items", defaultQuickSettings))
    }
    val allQuickOptions = listOf("wifi", "bluetooth", "torch", "ringer", "alarm", "mobile_data", "dnd")
    val quickLabels = mapOf(
        "wifi" to "Wi-Fi", "bluetooth" to "Bluetooth", "torch" to "Torch",
        "ringer" to "Ringer", "alarm" to "Alarm", "mobile_data" to "Mobile Data", "dnd" to "DND"
    )

    // Pomodoro completion sound
    var pomodoroSound by remember {
        mutableStateOf(viewModel.getBoolean("pomodoro_sound", true))
    }

    // App limits
    val appLimitsStr = remember { sharedPrefManager.getString("app_limits", "{}") }
    var appLimitsJson by remember { mutableStateOf(appLimitsStr) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp)
        )

        // ── Mode ──
        SectionHeader(title = "Mode")

        SettingsToggleRow(
            label = "Minimalist Mode",
            subtitle = "Text-only, distraction-free home screen",
            checked = minimalistMode,
            onCheckedChange = {
                minimalistMode = it
                viewModel.saveBoolean("minimalist_mode", it)
            }
        )

        SettingsToggleRow(
            label = "Grayscale Mode",
            subtitle = "Remove all color to reduce screen appeal",
            checked = grayscaleMode,
            onCheckedChange = {
                grayscaleMode = it
                viewModel.saveBoolean("grayscale_mode", it)
            }
        )

        SettingsDivider()

        // ── Appearance ──
        SectionHeader(title = "Appearance")

        // Theme
        SettingsRow(label = "Theme") {
            DropdownSelector(
                selectedValue = selectedOption,
                expanded = expanded,
                onExpandChange = { expanded = it },
                options = options,
                onSelect = { option ->
                    selectedOption = option
                    viewModel.setTheme(option)
                },
                optionContent = { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when (option) {
                                        "LIGHT" -> androidx.compose.ui.graphics.Color(0xFFF5F5F5)
                                        "DARK" -> androidx.compose.ui.graphics.Color(0xFF121212)
                                        "PURPLE" -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
                                        "BLUE" -> androidx.compose.ui.graphics.Color(0xFF03A9F4)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                                .border(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    CircleShape
                                )
                        )
                        Text(text = option, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }

        // Dynamic Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsToggleRow(
                label = "Dynamic Color",
                subtitle = "Use wallpaper-based colors (Android 12+)",
                checked = dynamicColorChecked,
                onCheckedChange = {
                    dynamicColorChecked = it
                    viewModel.saveBoolean("dynamic_color", it)
                }
            )
        }

        // Wallpaper
        SettingsRow(label = "Wallpaper") {
            DropdownSelector(
                selectedValue = if (selectedWallpaper == "none") "None" else selectedWallpaper,
                expanded = wallpaperExpanded,
                onExpandChange = { wallpaperExpanded = it },
                options = wallpaperOptions,
                onSelect = { option ->
                    selectedWallpaper = option
                    sharedPrefManager.saveString("wallpaper", option)
                },
                optionContent = { option ->
                    Text(
                        text = if (option == "none") "None" else option.replaceFirstChar { it.uppercase() },
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        // Font
        SettingsRow(label = "Font") {
            DropdownSelector(
                selectedValue = selectedFont,
                expanded = fontExpanded,
                onExpandChange = { fontExpanded = it },
                options = FONT_OPTIONS,
                onSelect = { option ->
                    selectedFont = option
                    sharedPrefManager.saveString("font_family", option)
                },
                optionContent = { option ->
                    Text(text = option, color = MaterialTheme.colorScheme.onSurface)
                }
            )
        }

        // Music Animation
        SettingsRow(label = "Music Animation") {
            DropdownSelector(
                selectedValue = selectedMusicAnim,
                expanded = musicAnimExpanded,
                onExpandChange = { musicAnimExpanded = it },
                options = MUSIC_ANIM_STYLES,
                onSelect = { option ->
                    selectedMusicAnim = option
                    sharedPrefManager.saveString("music_anim_style", option)
                },
                optionContent = { option ->
                    Text(text = option, color = MaterialTheme.colorScheme.onSurface)
                }
            )
        }

        SettingsDivider()

        // ── Clock & Date ──
        SectionHeader(title = "Clock & Date")

        SettingsToggleRow(
            label = "24-Hour Clock",
            subtitle = "Use 24-hour format instead of 12-hour",
            checked = use24HourClock,
            onCheckedChange = {
                use24HourClock = it
                viewModel.saveBoolean("use_24h_clock", it)
            }
        )

        SettingsToggleRow(
            label = "Show Seconds",
            subtitle = "Display seconds on the time screen",
            checked = showSeconds,
            onCheckedChange = {
                showSeconds = it
                viewModel.saveBoolean("show_seconds", it)
            }
        )

        SettingsToggleRow(
            label = "Show Battery",
            subtitle = "Show battery percentage on home screen",
            checked = showBatteryPercentage,
            onCheckedChange = {
                showBatteryPercentage = it
                viewModel.saveBoolean("show_battery_percentage", it)
            }
        )

        SettingsRow(label = "Date Format") {
            DropdownSelector(
                selectedValue = selectedDateFormat,
                expanded = dateFormatExpanded,
                onExpandChange = { dateFormatExpanded = it },
                options = dateFormatOptions,
                onSelect = { option ->
                    selectedDateFormat = option
                    sharedPrefManager.saveString("date_format", option)
                },
                optionContent = { option ->
                    Text(text = option, color = MaterialTheme.colorScheme.onSurface)
                }
            )
        }

        SettingsDivider()

        // ── Transitions ──
        SectionHeader(title = "Transitions")

        SettingsRow(label = "Page Transition") {
            DropdownSelector(
                selectedValue = selectedTransition,
                expanded = transitionExpanded,
                onExpandChange = { transitionExpanded = it },
                options = TRANSITION_OPTIONS,
                onSelect = { option ->
                    selectedTransition = option
                    sharedPrefManager.saveString("pager_transition", option)
                },
                optionContent = { option ->
                    Text(text = option, color = MaterialTheme.colorScheme.onSurface)
                }
            )
        }

        SettingsDivider()

        // ── Gestures ──
        SectionHeader(title = "Gestures")

        SettingsToggleRow(
            label = "Double-Tap to Lock",
            subtitle = "Double-tap home screen to lock device",
            checked = doubleTapLock,
            onCheckedChange = {
                doubleTapLock = it
                viewModel.saveBoolean("double_tap_lock", it)
            }
        )

        SettingsToggleRow(
            label = "Swipe Down for Notifications",
            subtitle = "Swipe down on home screen to open notification shade",
            checked = swipeDownNotif,
            onCheckedChange = {
                swipeDownNotif = it
                viewModel.saveBoolean("swipe_down_notif", it)
            }
        )

        SettingsDivider()

        // ── Quick Settings ──
        SectionHeader(title = "Quick Settings")

        Text(
            text = "Choose which toggles appear on the home screen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        QuickSettingsCustomizer(
            allOptions = allQuickOptions,
            labels = quickLabels,
            selectedStr = quickSettingsStr,
            onChanged = { newStr ->
                quickSettingsStr = newStr
                sharedPrefManager.saveString("quick_settings_items", newStr)
            }
        )

        SettingsDivider()

        // ── Favorite Apps ──
        SectionHeader(title = "Favorite Apps")

        if (favoriteApps.isEmpty()) {
            Text(
                text = "No favorite apps. Long-press an app and select \"Add to Favorites\".",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.05f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                favoriteApps.forEachIndexed { index, pkg ->
                    val appName = remember(pkg) { resolveAppName(pm, pkg) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = pkg,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                            )
                        }
                        Text(
                            text = "Remove",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    val newList = favoriteApps.toMutableList()
                                    newList.remove(pkg)
                                    favoriteApps = newList
                                    sharedPrefManager.saveString(
                                        "favorite_apps",
                                        newList.joinToString(",")
                                    )
                                }
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    if (index < favoriteApps.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f)
                        )
                    }
                }
            }
        }

        SettingsDivider()

        // ── Hidden Apps ──
        SectionHeader(title = "Hidden Apps")

        if (hiddenApps.isEmpty()) {
            Text(
                text = "No hidden apps. Long-press an app to hide it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.05f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                hiddenApps.forEachIndexed { index, pkg ->
                    val appName = remember(pkg) { resolveAppName(pm, pkg) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = pkg,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                            )
                        }
                        Text(
                            text = "Unhide",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    val newList = hiddenApps.toMutableList()
                                    newList.remove(pkg)
                                    hiddenApps = newList
                                    sharedPrefManager.saveString(
                                        "hidden_apps",
                                        newList.joinToString(",")
                                    )
                                }
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    if (index < hiddenApps.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f)
                        )
                    }
                }
            }
        }

        SettingsDivider()

        // ── Notifications ──
        SectionHeader(title = "Notifications")

        SettingsToggleRow(
            label = "Show Notification Badge",
            subtitle = "Display unread count on home screen",
            checked = showNotifBadge,
            onCheckedChange = {
                showNotifBadge = it
                viewModel.saveBoolean("show_notif_badge", it)
            }
        )

        SettingsToggleRow(
            label = "Batch Notifications",
            subtitle = "Silently collect notifications for digest view",
            checked = batchNotifications,
            onCheckedChange = {
                batchNotifications = it
                viewModel.saveBoolean("batch_notifications", it)
            }
        )

        SettingsDivider()

        // ── Home Widgets ──
        SectionHeader(title = "Home Widgets")

        Text(
            text = "Manage widgets from the Widgets page — tap the gear icon in the top-right corner.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        SettingsDivider()

        // ── Screen Time & Wellbeing ──
        SectionHeader(title = "Screen Time & Wellbeing")

        // Grant usage access
        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = if (hasUsageStatsPermission(context)) "Usage Access Granted" else "Grant Usage Access",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.titleSmall
            )
        }

        SettingsRow(label = "Daily Screen Time Goal") {
            DropdownSelector(
                selectedValue = if (selectedGoal == "Unlimited") "Unlimited" else "${selectedGoal} min",
                expanded = screenTimeGoalExpanded,
                onExpandChange = { screenTimeGoalExpanded = it },
                options = goalOptions,
                onSelect = { option ->
                    selectedGoal = option
                    sharedPrefManager.saveString("screen_time_goal", option)
                },
                optionContent = { option ->
                    Text(
                        text = if (option == "Unlimited") "Unlimited" else "$option min",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        SettingsRow(label = "Break Reminder After") {
            DropdownSelector(
                selectedValue = if (selectedBreakReminder == "Off") "Off" else "${selectedBreakReminder} min",
                expanded = breakReminderExpanded,
                onExpandChange = { breakReminderExpanded = it },
                options = breakReminderOptions,
                onSelect = { option ->
                    selectedBreakReminder = option
                    sharedPrefManager.saveString("break_reminder_minutes", option)
                },
                optionContent = { option ->
                    Text(
                        text = if (option == "Off") "Off" else "$option min",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        SettingsToggleRow(
            label = "Pomodoro Completion Sound",
            subtitle = "Play a sound when Pomodoro timer completes",
            checked = pomodoroSound,
            onCheckedChange = {
                pomodoroSound = it
                viewModel.saveBoolean("pomodoro_sound", it)
            }
        )

        SettingsDivider()

        // ── App Limits ──
        SectionHeader(title = "App Time Limits")

        Text(
            text = "Set daily usage limits for apps. When exceeded, a reminder screen appears.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        AppLimitsSection(
            sharedPrefManager = sharedPrefManager,
            pm = pm,
            appLimitsJson = appLimitsJson,
            onLimitsChanged = { newJson ->
                appLimitsJson = newJson
                sharedPrefManager.saveString("app_limits", newJson)
            }
        )

        SettingsDivider()

        // ── Actions ──
        SectionHeader(title = "Actions")

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Exit Launcher",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.titleSmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsDivider()

        // ── About ──
        SectionHeader(title = "About")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.05f),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = "Relax Launcher",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version 3.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A minimal, distraction-free launcher designed to help you focus on what matters.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Features: Minimalist mode, grayscale mode, screen time tracking, app time limits, notification digest, break reminders, Pomodoro timer, voice commands, now playing, quick toggles, search, grid/list layout, customizable rows/columns/icons, show/hide labels, hidden apps, favorite apps, wallpapers, page transitions, app sorting, date format, double-tap lock, swipe-down notifications.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Built with Jetpack Compose",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── Reusable Components ──

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.weight(1f)
        )
        content()
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                uncheckedTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                uncheckedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
    )
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

@Composable
private fun AppLimitsSection(
    sharedPrefManager: SharedPrefManager,
    pm: PackageManager,
    appLimitsJson: String,
    onLimitsChanged: (String) -> Unit
) {
    val limits = remember(appLimitsJson) {
        try {
            val json = JSONObject(appLimitsJson)
            json.keys().asSequence().map { key ->
                key to json.optInt(key, 0)
            }.toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    val limitOptions = listOf("15", "30", "45", "60", "90", "120", "Remove")

    if (limits.isEmpty()) {
        Text(
            text = "No app limits set. Use the app drawer context menu to add limits.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.05f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            limits.forEachIndexed { index, (pkg, minutes) ->
                val appName = remember(pkg) { resolveAppName(pm, pkg) }
                var limitExpanded by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "$minutes min/day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        )
                    }
                    DropdownSelector(
                        selectedValue = "${minutes}m",
                        expanded = limitExpanded,
                        onExpandChange = { limitExpanded = it },
                        options = limitOptions,
                        onSelect = { option ->
                            val json = try { JSONObject(appLimitsJson) } catch (_: Exception) { JSONObject() }
                            if (option == "Remove") {
                                json.remove(pkg)
                            } else {
                                json.put(pkg, option.toIntOrNull() ?: 60)
                            }
                            onLimitsChanged(json.toString())
                        },
                        optionContent = { option ->
                            Text(
                                text = if (option == "Remove") "Remove Limit" else "$option min",
                                color = if (option == "Remove") MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
                if (index < limits.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickSettingsCustomizer(
    allOptions: List<String>,
    labels: Map<String, String>,
    selectedStr: String,
    onChanged: (String) -> Unit
) {
    val selected = remember(selectedStr) {
        selectedStr.split(",").filter { it.isNotBlank() }.toMutableList()
    }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allOptions.forEach { option ->
            val isSelected = option in selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f)
                    )
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        if (isSelected) {
                            selected.remove(option)
                        } else {
                            selected.add(option)
                        }
                        onChanged(selected.joinToString(","))
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labels[option] ?: option,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun <T> DropdownSelector(
    selectedValue: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    options: List<T>,
    onSelect: (T) -> Unit,
    optionContent: @Composable (T) -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = { onExpandChange(!expanded) })
            .background(
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = selectedValue,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) },
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onSelect(option)
                        onExpandChange(false)
                    },
                    text = { optionContent(option) }
                )
            }
        }
    }
}
