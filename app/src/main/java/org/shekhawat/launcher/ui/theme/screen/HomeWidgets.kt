package org.shekhawat.launcher.ui.theme.screen

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.ContactsContract
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import org.shekhawat.launcher.SharedPrefManager
import org.shekhawat.launcher.utils.DailyQuotes
import org.shekhawat.launcher.utils.UsageStatsHelper
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════════
//  Widgets Page — standalone left-swipe page with all home widgets
// ═══════════════════════════════════════════════════════════════════

@Composable
fun WidgetsPage() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }

    val showDailyQuote by sharedPrefManager.observeBoolean("show_daily_quote", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_daily_quote", true))
    val showNextAlarm by sharedPrefManager.observeBoolean("show_next_alarm", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_next_alarm", true))
    val showQuickNote by sharedPrefManager.observeBoolean("show_quick_note", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_quick_note", false))
    val showUsageSummary by sharedPrefManager.observeBoolean("show_usage_summary", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_usage_summary", true))
    val showHabitTracker by sharedPrefManager.observeBoolean("show_habit_tracker", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_habit_tracker", false))
    val showQuickContacts by sharedPrefManager.observeBoolean("show_quick_contacts", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_quick_contacts", false))
    val showCountdown by sharedPrefManager.observeBoolean("show_countdown", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_countdown", false))
    val showBattery by sharedPrefManager.observeBoolean("show_battery_widget", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_battery_widget", false))
    val showBreathing by sharedPrefManager.observeBoolean("show_breathing", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_breathing", false))
    val showStepCounter by sharedPrefManager.observeBoolean("show_step_counter", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_step_counter", false))
    val showHydration by sharedPrefManager.observeBoolean("show_hydration", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_hydration", false))
    val showSleepTracker by sharedPrefManager.observeBoolean("show_sleep_tracker", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_sleep_tracker", false))
    val showWorkoutLog by sharedPrefManager.observeBoolean("show_workout_log", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_workout_log", false))
    val showChallenge by sharedPrefManager.observeBoolean("show_challenge", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_challenge", false))
    val use24h by sharedPrefManager.observeBoolean("use_24h_clock", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("use_24h_clock", true))

    var showScreenTimeSheet by remember { mutableStateOf(false) }
    var showWidgetSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(28.dp)) // balance the icon
            Text(
                text = "Widgets",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Widget Settings",
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showWidgetSettings = true }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (showDailyQuote) {
            DailyQuoteWidget()
        }
        if (showNextAlarm) {
            NextAlarmWidget(context = context, use24h = use24h)
        }
        if (showQuickNote) {
            QuickNoteWidget(sharedPrefManager = sharedPrefManager)
        }
        if (showUsageSummary) {
            AppUsageSummaryWidget(
                context = context,
                onOpenScreenTime = { showScreenTimeSheet = true }
            )
        }
        if (showHabitTracker) {
            HabitTrackerWidget(sharedPrefManager = sharedPrefManager)
        }
        if (showQuickContacts) {
            QuickContactsWidget(sharedPrefManager = sharedPrefManager)
        }
        if (showCountdown) {
            CountdownWidget(sharedPrefManager = sharedPrefManager)
        }
        if (showBattery) {
            BatteryWidget()
        }
        if (showBreathing) {
            BreathingWidget()
        }
        if (showStepCounter) {
            StepCounterWidget(sharedPrefManager = sharedPrefManager)
        }
        if (showHydration) {
            HydrationWidget(sharedPrefManager = sharedPrefManager)
        }
        if (showSleepTracker) {
            SleepTrackerWidget(sharedPrefManager = sharedPrefManager)
        }
        if (showWorkoutLog) {
            WorkoutLogWidget(sharedPrefManager = sharedPrefManager)
        }
        if (showChallenge) {
            ChallengeWidget(sharedPrefManager = sharedPrefManager)
        }

        // Fallback when all widgets are disabled
        if (!showDailyQuote && !showNextAlarm && !showQuickNote && !showUsageSummary
            && !showHabitTracker && !showQuickContacts && !showCountdown && !showBattery
            && !showBreathing && !showStepCounter && !showHydration && !showSleepTracker
            && !showWorkoutLog && !showChallenge
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "No widgets enabled",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
            )
            Text(
                text = "Enable widgets in Settings → Home Widgets",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    // Screen Time Sheet
    if (showScreenTimeSheet) {
        ScreenTimeSheet(onDismiss = { showScreenTimeSheet = false })
    }

    // Widget Settings Sheet
    if (showWidgetSettings) {
        WidgetSettingsSheet(
            sharedPrefManager = sharedPrefManager,
            onDismiss = { showWidgetSettings = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Widget Settings Sheet — quick toggle all widgets
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetSettingsSheet(
    sharedPrefManager: SharedPrefManager,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    data class WidgetOption(
        val key: String,
        val label: String,
        val subtitle: String,
        val defaultValue: Boolean,
        val configKey: String? = null // extra config field key
    )

    val widgets = listOf(
        WidgetOption("show_daily_quote", "Daily Quote", "A calming quote that changes daily", true),
        WidgetOption("show_next_alarm", "Upcoming Alarm", "Next scheduled alarm time", true),
        WidgetOption("show_quick_note", "Quick Note", "A small sticky note", false),
        WidgetOption("show_usage_summary", "App Usage", "Top 3 most-used apps today", true),
        WidgetOption("show_habit_tracker", "Habit Tracker", "Track daily habits", false, "habit_names"),
        WidgetOption("show_quick_contacts", "Quick Contacts", "Speed-dial favorites", false, "quick_contacts"),
        WidgetOption("show_countdown", "Countdown", "Days until an event", false, "countdown_config"),
        WidgetOption("show_battery_widget", "Battery Status", "Visual battery gauge", false),
        WidgetOption("show_breathing", "Breathing Exercise", "Guided 4-4-4 breathing", false),
        WidgetOption("show_step_counter", "Step Counter", "Steps, calories & distance", false, "step_counter_goal"),
        WidgetOption("show_hydration", "Hydration Tracker", "Track daily water intake", false),
        WidgetOption("show_sleep_tracker", "Sleep Tracker", "Log hours of sleep", false),
        WidgetOption("show_workout_log", "Workout Log", "Track daily workouts", false),
        WidgetOption("show_challenge", "Challenge Tracker", "Day challenges (e.g. Day 12/30)", false),
    )

    // Track toggle states
    val states = remember {
        widgets.map { w ->
            mutableStateOf(sharedPrefManager.getBoolean(w.key, w.defaultValue))
        }
    }

    // Config field states
    var habitNamesText by remember {
        mutableStateOf(sharedPrefManager.getString("habit_names", "Water,Exercise,Read,Meditate"))
    }
    var quickContactsText by remember {
        mutableStateOf(sharedPrefManager.getString("quick_contacts", ""))
    }
    var countdownEventName by remember {
        mutableStateOf(sharedPrefManager.getString("countdown_event_name", ""))
    }
    var countdownEventDate by remember {
        mutableStateOf(sharedPrefManager.getString("countdown_event_date", ""))
    }
    var stepGoalText by remember {
        mutableStateOf(sharedPrefManager.getString("step_counter_goal", "6000"))
    }

    val surfaceColor = MaterialTheme.colorScheme.onSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = surfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Widget Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Toggle widgets on or off. Changes apply immediately.",
                style = MaterialTheme.typography.bodySmall,
                color = surfaceColor.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            widgets.forEachIndexed { index, widget ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newVal = !states[index].value
                            states[index].value = newVal
                            sharedPrefManager.saveBoolean(widget.key, newVal)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = widget.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = surfaceColor
                        )
                        Text(
                            text = widget.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = surfaceColor.copy(alpha = 0.45f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = states[index].value,
                        onCheckedChange = { newVal ->
                            states[index].value = newVal
                            sharedPrefManager.saveBoolean(widget.key, newVal)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = surfaceColor.copy(alpha = 0.7f),
                            uncheckedThumbColor = surfaceColor.copy(alpha = 0.5f),
                            uncheckedTrackColor = surfaceColor.copy(alpha = 0.1f),
                            uncheckedBorderColor = surfaceColor.copy(alpha = 0.2f)
                        )
                    )
                }

                // Inline config for widgets that need it
                if (states[index].value) {
                    when (widget.configKey) {
                        "habit_names" -> {
                            WidgetConfigField(
                                label = "Habit names (comma-separated, max 6)",
                                value = habitNamesText,
                                placeholder = "Water,Exercise,Read,Meditate",
                                onValueChange = {
                                    habitNamesText = it
                                    sharedPrefManager.saveString("habit_names", it)
                                }
                            )
                        }
                        "quick_contacts" -> {
                            WidgetConfigField(
                                label = "Contacts (Name:Number, comma-separated)",
                                value = quickContactsText,
                                placeholder = "Mom:+1234567890,Dad:+0987654321",
                                singleLine = false,
                                onValueChange = {
                                    quickContactsText = it
                                    sharedPrefManager.saveString("quick_contacts", it)
                                }
                            )
                        }
                        "countdown_config" -> {
                            WidgetConfigField(
                                label = "Event name",
                                value = countdownEventName,
                                placeholder = "Birthday, Vacation...",
                                onValueChange = {
                                    countdownEventName = it
                                    sharedPrefManager.saveString("countdown_event_name", it)
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            WidgetConfigField(
                                label = "Event date (yyyy-MM-dd)",
                                value = countdownEventDate,
                                placeholder = "2026-12-25",
                                onValueChange = {
                                    countdownEventDate = it
                                    sharedPrefManager.saveString("countdown_event_date", it)
                                }
                            )
                        }
                        "step_counter_goal" -> {
                            WidgetConfigField(
                                label = "Daily step goal",
                                value = stepGoalText,
                                placeholder = "6000",
                                onValueChange = { newVal ->
                                    val filtered = newVal.filter { it.isDigit() }.take(6)
                                    stepGoalText = filtered
                                    if (filtered.isNotEmpty()) {
                                        sharedPrefManager.saveString("step_counter_goal", filtered)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetConfigField(
    label: String,
    value: String,
    placeholder: String,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.onSurface
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = surfaceColor.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = surfaceColor,
                fontSize = 13.sp
            ),
            singleLine = singleLine,
            cursorBrush = SolidColor(surfaceColor),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(surfaceColor.copy(alpha = 0.06f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = surfaceColor.copy(alpha = 0.3f),
                        fontSize = 13.sp
                    )
                }
                inner()
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Shared Widget Card wrapper — consistent zen styling
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun WidgetCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.06f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════
//  1. Daily Quote Widget
// ═══════════════════════════════════════════════════════════════════

@Composable
fun DailyQuoteWidget() {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    var quoteIndex by remember { mutableIntStateOf(DailyQuotes.getQuoteIndex()) }
    var quote by remember { mutableStateOf(DailyQuotes.getQuoteForToday()) }

    WidgetCard(onClick = {
        val (newQuote, newIndex) = DailyQuotes.getRandomQuote(quoteIndex)
        quote = newQuote
        quoteIndex = newIndex
    }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.2f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quote.first,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = contentColor.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "— ${quote.second}",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  2. Upcoming Alarm Widget
// ═══════════════════════════════════════════════════════════════════

@Composable
fun NextAlarmWidget(context: Context, use24h: Boolean) {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    var alarmText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            try {
                val nextAlarm = alarmManager.nextAlarmClock
                if (nextAlarm != null) {
                    val time = Date(nextAlarm.triggerTime)
                    val pattern = if (use24h) "HH:mm" else "hh:mm a"
                    alarmText = SimpleDateFormat(pattern, Locale.getDefault()).format(time)
                }
            } catch (_: Exception) { }
        }
    }

    // Only show if there is an alarm
    if (alarmText != null) {
        WidgetCard(onClick = {
            try {
                context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
            } catch (_: Exception) { }
        }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Alarm",
                    tint = contentColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Next alarm",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = alarmText ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  3. Quick Notes Widget
// ═══════════════════════════════════════════════════════════════════

@Composable
fun QuickNoteWidget(sharedPrefManager: SharedPrefManager) {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    var noteText by remember {
        mutableStateOf(sharedPrefManager.getString("quick_note_text", ""))
    }
    var isEditing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    WidgetCard(onClick = {
        if (!isEditing) isEditing = true
    }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Note",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                contentDescription = if (isEditing) "Done" else "Edit",
                tint = contentColor.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        if (isEditing) {
                            isEditing = false
                            focusManager.clearFocus()
                            sharedPrefManager.saveString("quick_note_text", noteText)
                        } else {
                            isEditing = true
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isEditing) {
            BasicTextField(
                value = noteText,
                onValueChange = {
                    if (it.length <= 200) {
                        noteText = it
                        sharedPrefManager.saveString("quick_note_text", it)
                    }
                },
                textStyle = TextStyle(
                    color = contentColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                cursorBrush = SolidColor(contentColor),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    isEditing = false
                    focusManager.clearFocus()
                }),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (noteText.isEmpty()) {
                            Text(
                                text = "Tap to add a note...",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.3f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        } else {
            Text(
                text = noteText.ifEmpty { "Tap to add a note..." },
                style = MaterialTheme.typography.bodySmall,
                color = if (noteText.isEmpty()) contentColor.copy(alpha = 0.3f)
                else contentColor.copy(alpha = 0.75f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  4. App Usage Summary Widget
// ═══════════════════════════════════════════════════════════════════

private data class AppUsageDisplay(
    val name: String,
    val timeMs: Long,
    val icon: android.graphics.Bitmap?
)

@Composable
fun AppUsageSummaryWidget(context: Context, onOpenScreenTime: () -> Unit) {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val topApps = remember { mutableStateListOf<AppUsageDisplay>() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            if (UsageStatsHelper.hasPermission(context)) {
                val usage = UsageStatsHelper.getTodayPerAppUsage(context).take(3)
                val pm = context.packageManager
                val displays = usage.map { info ->
                    val icon = try {
                        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pm.getApplicationInfo(info.packageName, PackageManager.ApplicationInfoFlags.of(0L))
                        } else {
                            @Suppress("DEPRECATION")
                            pm.getApplicationInfo(info.packageName, 0)
                        }
                        pm.getApplicationIcon(appInfo).toBitmap(48, 48)
                    } catch (_: Exception) { null }
                    AppUsageDisplay(info.appName, info.totalTimeMs, icon)
                }
                topApps.clear()
                topApps.addAll(displays)
            }
        }
    }

    if (topApps.isNotEmpty()) {
        WidgetCard(onClick = onOpenScreenTime) {
            Text(
                text = "Today's usage",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            val maxTime = topApps.maxOf { it.timeMs }.coerceAtLeast(1L)

            topApps.forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App icon
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(contentColor.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (app.icon != null) {
                            Image(
                                bitmap = app.icon.asImageBitmap(),
                                contentDescription = app.name,
                                modifier = Modifier.size(22.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // App name
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(72.dp),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { (app.timeMs.toFloat() / maxTime).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = contentColor.copy(alpha = 0.35f),
                        trackColor = contentColor.copy(alpha = 0.08f),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Duration
                    Text(
                        text = UsageStatsHelper.formatDuration(app.timeMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  5. Habit Tracker Widget
// ═══════════════════════════════════════════════════════════════════

@Composable
fun HabitTrackerWidget(sharedPrefManager: SharedPrefManager) {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val todayKey = "habits_${LocalDate.now()}"

    val habitNames = remember {
        sharedPrefManager.getString("habit_names", "Water,Exercise,Read,Meditate")
            .split(",")
            .filter { it.isNotBlank() }
            .take(6)
    }

    val completions = remember {
        val saved = sharedPrefManager.getString(todayKey, "")
        val list = saved.split(",").map { it == "true" }
        mutableStateListOf(*Array(habitNames.size) { i -> list.getOrElse(i) { false } })
    }

    fun saveCompletions() {
        sharedPrefManager.saveString(todayKey, completions.joinToString(","))
    }

    if (habitNames.isNotEmpty()) {
        WidgetCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily habits",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                val doneCount = completions.count { it }
                Text(
                    text = "$doneCount/${habitNames.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.3f),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                habitNames.forEachIndexed { index, name ->
                    val done = completions.getOrElse(index) { false }
                    val bgColor by animateColorAsState(
                        targetValue = if (done) contentColor.copy(alpha = 0.2f)
                        else contentColor.copy(alpha = 0.0f),
                        label = "habit_bg_$index"
                    )
                    val borderColor = if (done) contentColor.copy(alpha = 0.3f)
                    else contentColor.copy(alpha = 0.15f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            if (index < completions.size) {
                                completions[index] = !completions[index]
                                saveCompletions()
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .border(1.dp, borderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (done) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = name.take(1).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (done) contentColor.copy(alpha = 0.6f)
                            else contentColor.copy(alpha = 0.35f),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  6. Quick Contacts Widget
// ═══════════════════════════════════════════════════════════════════

private data class QuickContact(
    val name: String,
    val phoneNumber: String,
    val initial: String
)

@Composable
fun QuickContactsWidget(sharedPrefManager: SharedPrefManager) {
    val context = LocalContext.current
    val contentColor = MaterialTheme.colorScheme.onPrimary

    // Stored as "name:number,name:number,..."
    val contactsStr by sharedPrefManager.observeString("quick_contacts", "")
        .collectAsState(initial = sharedPrefManager.getString("quick_contacts", ""))

    val contacts = remember(contactsStr) {
        contactsStr.split(",").filter { it.contains(":") }.mapNotNull { entry ->
            val parts = entry.split(":", limit = 2)
            if (parts.size == 2 && parts[1].isNotBlank()) {
                QuickContact(
                    name = parts[0].trim(),
                    phoneNumber = parts[1].trim(),
                    initial = parts[0].trim().take(1).uppercase()
                )
            } else null
        }.take(8)
    }

    // If no contacts configured, try to load starred contacts from the system
    var systemContacts by remember { mutableStateOf<List<QuickContact>>(emptyList()) }
    LaunchedEffect(contacts) {
        if (contacts.isEmpty()) {
            systemContacts = withContext(Dispatchers.IO) {
                loadStarredContacts(context)
            }
        }
    }

    val displayContacts = if (contacts.isNotEmpty()) contacts else systemContacts

    val accentColors = listOf(
        contentColor.copy(alpha = 0.15f),
        contentColor.copy(alpha = 0.10f),
        contentColor.copy(alpha = 0.12f),
        contentColor.copy(alpha = 0.08f),
    )

    WidgetCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quick Contacts",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (displayContacts.isEmpty()) {
            Text(
                text = "Star contacts in your phone app, or add them in Settings → Home Widgets",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.3f)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                displayContacts.forEach { contact ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                // Try direct call first, fall back to dialer
                                try {
                                    val callIntent = Intent(Intent.ACTION_CALL).apply {
                                        data = Uri.parse("tel:${contact.phoneNumber}")
                                    }
                                    context.startActivity(callIntent)
                                } catch (_: SecurityException) {
                                    // CALL_PHONE permission not granted — fall back to dialer
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${contact.phoneNumber}")
                                    }
                                    try { context.startActivity(dialIntent) } catch (_: Exception) {}
                                } catch (_: Exception) {}
                            }
                            .padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(accentColors[displayContacts.indexOf(contact) % accentColors.size]),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.initial,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = contact.name.split(" ").first(),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

private fun loadStarredContacts(context: Context): List<QuickContact> {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED
    ) return emptyList()

    val contacts = mutableListOf<QuickContact>()
    try {
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.STARRED
            ),
            "${ContactsContract.CommonDataKinds.Phone.STARRED} = 1",
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val seen = mutableSetOf<String>()
            while (it.moveToNext() && contacts.size < 6) {
                val name = it.getString(nameIdx) ?: continue
                val number = it.getString(numIdx) ?: continue
                if (name in seen) continue
                seen.add(name)
                contacts.add(
                    QuickContact(name = name, phoneNumber = number, initial = name.take(1).uppercase())
                )
            }
        }
    } catch (_: Exception) {}
    return contacts
}

// ═══════════════════════════════════════════════════════════════════
//  7. Countdown Widget
// ═══════════════════════════════════════════════════════════════════

@Composable
fun CountdownWidget(sharedPrefManager: SharedPrefManager) {
    val contentColor = MaterialTheme.colorScheme.onPrimary

    val eventName by sharedPrefManager.observeString("countdown_event_name", "")
        .collectAsState(initial = sharedPrefManager.getString("countdown_event_name", ""))
    val eventDateStr by sharedPrefManager.observeString("countdown_event_date", "")
        .collectAsState(initial = sharedPrefManager.getString("countdown_event_date", ""))

    // Parse date as yyyy-MM-dd
    val daysLeft = remember(eventDateStr) {
        try {
            val target = LocalDate.parse(eventDateStr)
            val today = LocalDate.now()
            ChronoUnit.DAYS.between(today, target)
        } catch (_: Exception) { null }
    }

    if (eventName.isBlank() || daysLeft == null) {
        WidgetCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set a countdown in Settings → Home Widgets",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.3f)
                )
            }
        }
        return
    }

    WidgetCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = eventName,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = when {
                    daysLeft > 0 -> "$daysLeft"
                    daysLeft == 0L -> "Today!"
                    else -> "${-daysLeft}"
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when {
                    daysLeft > 1 -> "days to go"
                    daysLeft == 1L -> "day to go"
                    daysLeft == 0L -> ""
                    else -> "days ago"
                },
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.4f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  8. Battery Status Widget
// ═══════════════════════════════════════════════════════════════════

@Composable
fun BatteryWidget() {
    val context = LocalContext.current
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val bm = remember { context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager }

    var batteryPct by remember { mutableIntStateOf(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) }
    var isCharging by remember { mutableStateOf(bm.isCharging) }

    LaunchedEffect(Unit) {
        while (true) {
            batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            isCharging = bm.isCharging
            delay(10_000)
        }
    }

    val accentColor = when {
        batteryPct <= 15 -> contentColor.copy(red = 0.9f, green = 0.3f, blue = 0.3f, alpha = 0.8f)
        batteryPct <= 30 -> contentColor.copy(red = 0.9f, green = 0.7f, blue = 0.3f, alpha = 0.8f)
        else -> contentColor.copy(alpha = 0.5f)
    }

    WidgetCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular battery gauge
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 5.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val topLeft = Offset(
                        (size.width - radius * 2) / 2,
                        (size.height - radius * 2) / 2
                    )
                    // Background arc
                    drawArc(
                        color = contentColor.copy(alpha = 0.08f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Progress arc
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = 360f * batteryPct / 100f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$batteryPct%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCharging) Icons.Default.BatteryChargingFull
                        else Icons.Default.BatteryFull,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCharging) "Charging" else "Battery",
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        isCharging && batteryPct >= 95 -> "Almost full"
                        isCharging -> "Plugged in"
                        batteryPct >= 80 -> "Healthy charge"
                        batteryPct >= 50 -> "Moderate"
                        batteryPct >= 20 -> "Getting low"
                        else -> "Charge soon"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.35f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  9. Breathing Exercise Widget
// ═══════════════════════════════════════════════════════════════════

@Composable
fun BreathingWidget() {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    var isActive by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf("Tap to begin") }
    var elapsed by remember { mutableLongStateOf(0L) }
    var breathCount by remember { mutableIntStateOf(0) }

    // Breathing cycle: 4s inhale, 4s hold, 4s exhale, 2s rest = 14s total
    LaunchedEffect(isActive) {
        if (!isActive) {
            phase = "Tap to begin"
            elapsed = 0L
            breathCount = 0
            return@LaunchedEffect
        }
        breathCount = 0
        while (isActive && breathCount < 5) {
            phase = "Breathe in..."
            repeat(40) { if (isActive) { delay(100); elapsed += 100 } }
            phase = "Hold..."
            repeat(40) { if (isActive) { delay(100); elapsed += 100 } }
            phase = "Breathe out..."
            repeat(40) { if (isActive) { delay(100); elapsed += 100 } }
            phase = "Rest..."
            repeat(20) { if (isActive) { delay(100); elapsed += 100 } }
            breathCount++
        }
        if (isActive) {
            phase = "Well done"
            delay(2000)
            isActive = false
        }
    }

    // Gentle pulsing animation when active
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val circleAlpha = if (isActive) 0.06f + pulse * 0.08f else 0.06f
    val circleSize = if (isActive) (44 + pulse * 12).dp else 44.dp

    WidgetCard(onClick = { isActive = !isActive }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Breathe",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.5f)
            )
            if (isActive && breathCount < 5) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${breathCount + 1}/5",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.3f)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = circleAlpha)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isActive && phase != "Well done") phase
                    else if (phase == "Well done") "✓"
                    else "●",
                    style = if (isActive) MaterialTheme.typography.bodySmall
                    else MaterialTheme.typography.titleMedium,
                    color = contentColor.copy(alpha = if (isActive) 0.7f else 0.25f),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (!isActive && phase != "Well done") {
            Text(
                text = "4-4-4 breathing · 5 cycles",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  10. Step Counter Widget
// ═══════════════════════════════════════════════════════════════════

@Composable
fun StepCounterWidget(sharedPrefManager: SharedPrefManager) {
    val context = LocalContext.current
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val todayKey = "steps_${LocalDate.now()}"

    val goalStr by sharedPrefManager.observeString("step_counter_goal", "6000")
        .collectAsState(initial = sharedPrefManager.getString("step_counter_goal", "6000"))
    val stepGoal = goalStr.toIntOrNull() ?: 6000

    // Persisted steps for today (survives recomposition and app restarts)
    var steps by remember { mutableIntStateOf(sharedPrefManager.getInt(todayKey, 0)) }

    // Baseline from the TYPE_STEP_COUNTER sensor (cumulative since last reboot)
    var sensorBaseline by remember { mutableIntStateOf(-1) }

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val stepSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    val hasPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required before Q
        }
    }

    // Register sensor listener
    DisposableEffect(hasPermission, stepSensor) {
        if (!hasPermission || stepSensor == null) {
            return@DisposableEffect onDispose { }
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return
                val totalStepsSinceBoot = event.values[0].toInt()

                if (sensorBaseline < 0) {
                    // First reading — calculate baseline
                    // If we already have saved steps for today, the baseline is
                    // (current sensor value - saved steps). Otherwise, baseline = current value.
                    val savedToday = sharedPrefManager.getInt(todayKey, 0)
                    sensorBaseline = totalStepsSinceBoot - savedToday
                }

                val newSteps = (totalStepsSinceBoot - sensorBaseline).coerceAtLeast(0)
                if (newSteps != steps) {
                    steps = newSteps
                    sharedPrefManager.saveInt(todayKey, newSteps)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener, stepSensor, SensorManager.SENSOR_DELAY_UI
        )

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val progress = (steps.toFloat() / stepGoal).coerceIn(0f, 1f)
    val caloriesBurned = (steps * 0.04f).toInt() // rough estimate: ~0.04 kcal per step
    val distanceKm = steps * 0.000762f // rough estimate: ~0.762m per step

    val accentColor = when {
        progress >= 1f -> contentColor.copy(
            red = 0.3f, green = 0.85f, blue = 0.4f, alpha = 0.8f
        )
        progress >= 0.6f -> contentColor.copy(alpha = 0.55f)
        else -> contentColor.copy(alpha = 0.4f)
    }

    WidgetCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular step gauge
            Box(
                modifier = Modifier.size(58.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 5.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val topLeft = Offset(
                        (size.width - radius * 2) / 2,
                        (size.height - radius * 2) / 2
                    )
                    // Background arc
                    drawArc(
                        color = contentColor.copy(alpha = 0.08f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Progress arc
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formatSteps(steps),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Steps today",
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        !hasPermission -> "Permission needed"
                        stepSensor == null -> "No step sensor"
                        progress >= 1f -> "Goal reached!"
                        progress >= 0.75f -> "Almost there!"
                        progress >= 0.5f -> "Halfway done"
                        steps == 0 -> "Start walking"
                        else -> "${(progress * 100).toInt()}% of goal"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.35f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Calories
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.25f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${caloriesBurned} kcal",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.35f),
                            fontSize = 10.sp
                        )
                    }
                    // Distance
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.25f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "%.1f km".format(distanceKm),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.35f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatSteps(steps: Int): String {
    return if (steps >= 1000) {
        "%.1fk".format(steps / 1000f)
    } else {
        steps.toString()
    }
}

// ═══════════════════════════════════════════════════════════════════
//  11. Hydration Widget
// ═══════════════════════════════════════════════════════════════════

@Composable
fun HydrationWidget(sharedPrefManager: SharedPrefManager) {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val todayKey = "hydration_${LocalDate.now()}"

    val goalStr by sharedPrefManager.observeString("hydration_goal", "8")
        .collectAsState(initial = sharedPrefManager.getString("hydration_goal", "8"))
    val goal = goalStr.toIntOrNull() ?: 8

    var cups by remember { mutableIntStateOf(sharedPrefManager.getInt(todayKey, 0)) }
    var isEditingGoal by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf(goalStr) }

    val progress = (cups.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val waterColor = contentColor.copy(
        red = 0.3f, green = 0.6f, blue = 0.95f, alpha = 0.7f
    )

    WidgetCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = waterColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Hydration",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.weight(1f))
            // Edit goal
            Text(
                text = if (isEditingGoal) "Done" else "Goal: $goal",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.3f),
                modifier = Modifier.clickable {
                    if (isEditingGoal) {
                        val newGoal = goalInput.toIntOrNull()
                        if (newGoal != null && newGoal > 0) {
                            sharedPrefManager.saveString("hydration_goal", newGoal.toString())
                        }
                    }
                    isEditingGoal = !isEditingGoal
                }
            )
        }

        if (isEditingGoal) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily goal (cups):",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it.filter { c -> c.isDigit() }.take(2) },
                    textStyle = TextStyle(
                        color = contentColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(contentColor),
                    modifier = Modifier
                        .width(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(contentColor.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Water cups visual
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..goal.coerceAtMost(12)) {
                val filled = i <= cups
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (filled) waterColor
                            else contentColor.copy(alpha = 0.06f)
                        )
                        .border(
                            1.dp,
                            if (filled) waterColor.copy(alpha = 0.5f)
                            else contentColor.copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            cups = if (cups == i) i - 1 else i
                            sharedPrefManager.saveInt(todayKey, cups)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (filled) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.9f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$cups / $goal cups",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = when {
                    progress >= 1f -> "Goal reached!"
                    progress >= 0.75f -> "Almost there!"
                    progress >= 0.5f -> "Keep going!"
                    cups == 0 -> "Tap to log water"
                    else -> "${(progress * 100).toInt()}%"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (progress >= 1f) waterColor else contentColor.copy(alpha = 0.35f)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  12. Sleep Tracker Widget
// ═══════════════════════════════════════════════════════════════════

@Composable
fun SleepTrackerWidget(sharedPrefManager: SharedPrefManager) {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val todayKey = "sleep_${LocalDate.now()}"

    var sleepHours by remember {
        mutableStateOf(sharedPrefManager.getString(todayKey, ""))
    }
    var isEditing by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf(sleepHours) }

    val hours = sleepHours.toFloatOrNull() ?: 0f
    val goalHours = 8f
    val progress = (hours / goalHours).coerceIn(0f, 1f)

    val sleepColor = contentColor.copy(
        red = 0.4f, green = 0.3f, blue = 0.8f, alpha = 0.7f
    )

    WidgetCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = null,
                tint = sleepColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sleep",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (isEditing) "Save" else if (hours > 0) "Edit" else "Log",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.3f),
                modifier = Modifier.clickable {
                    if (isEditing) {
                        val h = inputText.toFloatOrNull()
                        if (h != null && h >= 0) {
                            sleepHours = inputText
                            sharedPrefManager.saveString(todayKey, inputText)
                        }
                    } else {
                        inputText = sleepHours
                    }
                    isEditing = !isEditing
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hours slept:",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it.filter { c -> c.isDigit() || c == '.' }.take(4) },
                    textStyle = TextStyle(
                        color = contentColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(contentColor),
                    modifier = Modifier
                        .width(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(contentColor.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleep gauge
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 4.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val topLeft = Offset(
                            (size.width - radius * 2) / 2,
                            (size.height - radius * 2) / 2
                        )
                        drawArc(
                            color = contentColor.copy(alpha = 0.08f),
                            startAngle = -90f, sweepAngle = 360f,
                            useCenter = false, topLeft = topLeft,
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = sleepColor,
                            startAngle = -90f, sweepAngle = 360f * progress,
                            useCenter = false, topLeft = topLeft,
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = if (hours > 0) "${hours}h" else "—",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Last night",
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.5f)
                    )
                    Text(
                        text = when {
                            hours == 0f -> "Tap Log to record"
                            hours >= 8f -> "Great sleep!"
                            hours >= 6f -> "Could be better"
                            else -> "Need more rest"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  13. Workout Log Widget
// ═══════════════════════════════════════════════════════════════════

@Composable
fun WorkoutLogWidget(sharedPrefManager: SharedPrefManager) {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val todayKey = "workout_${LocalDate.now()}"
    val weekKey = "workout_week_${LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())}"

    var workedOut by remember { mutableStateOf(sharedPrefManager.getBoolean(todayKey, false)) }
    var weekDays by remember { mutableIntStateOf(sharedPrefManager.getInt(weekKey, 0)) }

    val fitnessColor = contentColor.copy(
        red = 0.9f, green = 0.4f, blue = 0.3f, alpha = 0.7f
    )

    WidgetCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = fitnessColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Workout",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$weekDays days this week",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.3f),
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (workedOut) fitnessColor.copy(alpha = 0.2f)
                        else contentColor.copy(alpha = 0.06f)
                    )
                    .border(
                        1.dp,
                        if (workedOut) fitnessColor.copy(alpha = 0.4f)
                        else contentColor.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        workedOut = !workedOut
                        sharedPrefManager.saveBoolean(todayKey, workedOut)
                        weekDays = if (workedOut) weekDays + 1 else (weekDays - 1).coerceAtLeast(0)
                        sharedPrefManager.saveInt(weekKey, weekDays)
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (workedOut) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = fitnessColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (workedOut) "Done today!" else "Mark as done",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (workedOut) fitnessColor else contentColor.copy(alpha = 0.5f),
                        fontWeight = if (workedOut) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ── Challenge Widget ──

@Composable
fun ChallengeWidget(sharedPrefManager: SharedPrefManager) {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val challengeColor = contentColor.copy(red = 1f, green = 0.7f, blue = 0.2f, alpha = 0.8f)

    // Load saved challenges: stored as "name|currentDay|totalDays|startDate"
    val challengeListKey = "challenge_list"
    val savedChallenges = remember {
        val raw = sharedPrefManager.getString(challengeListKey, "")
        mutableStateListOf<ChallengeItem>().apply {
            if (raw.isNotEmpty()) {
                raw.split(";;").filter { it.isNotBlank() }.forEach { entry ->
                    val parts = entry.split("|")
                    if (parts.size >= 4) {
                        add(
                            ChallengeItem(
                                name = parts[0],
                                currentDay = parts[1].toIntOrNull() ?: 0,
                                totalDays = parts[2].toIntOrNull() ?: 30,
                                startDate = parts[3]
                            )
                        )
                    }
                }
            }
        }
    }

    fun saveChallenges() {
        val serialized = savedChallenges.joinToString(";;") {
            "${it.name}|${it.currentDay}|${it.totalDays}|${it.startDate}"
        }
        sharedPrefManager.saveString(challengeListKey, serialized)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    WidgetCard {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = challengeColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Challenges",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${savedChallenges.size} active",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.3f),
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (savedChallenges.isEmpty()) {
            // Empty state
            Text(
                text = "No active challenges",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
        } else {
            // Show first challenge always, rest when expanded
            val displayList = if (expanded) savedChallenges else savedChallenges.take(1)
            displayList.forEachIndexed { index, challenge ->
                ChallengeRow(
                    challenge = challenge,
                    accentColor = challengeColor,
                    contentColor = contentColor,
                    onIncrement = {
                        if (challenge.currentDay < challenge.totalDays) {
                            savedChallenges[index] = challenge.copy(currentDay = challenge.currentDay + 1)
                            saveChallenges()
                        }
                    },
                    onReset = {
                        savedChallenges[index] = challenge.copy(
                            currentDay = 0,
                            startDate = LocalDate.now().toString()
                        )
                        saveChallenges()
                    },
                    onDelete = {
                        savedChallenges.removeAt(index)
                        saveChallenges()
                    }
                )
                if (index < displayList.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Show more / less toggle
            if (savedChallenges.size > 1) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (expanded) "Show less" else "Show all (${savedChallenges.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = challengeColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Add new challenge button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(contentColor.copy(alpha = 0.06f))
                .border(1.dp, contentColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .clickable { showAddDialog = true }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add challenge",
                    tint = challengeColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "New Challenge",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.5f)
                )
            }
        }
    }

    // Add challenge dialog
    if (showAddDialog) {
        AddChallengeDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, days ->
                savedChallenges.add(
                    ChallengeItem(
                        name = name,
                        currentDay = 0,
                        totalDays = days,
                        startDate = LocalDate.now().toString()
                    )
                )
                saveChallenges()
                showAddDialog = false
            }
        )
    }
}

private data class ChallengeItem(
    val name: String,
    val currentDay: Int,
    val totalDays: Int,
    val startDate: String
)

@Composable
private fun ChallengeRow(
    challenge: ChallengeItem,
    accentColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (challenge.totalDays > 0) challenge.currentDay.toFloat() / challenge.totalDays else 0f
    val isComplete = challenge.currentDay >= challenge.totalDays
    var showActions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isComplete) accentColor.copy(alpha = 0.1f)
                else contentColor.copy(alpha = 0.04f)
            )
            .clickable {
                if (isComplete) {
                    showActions = !showActions
                } else {
                    onIncrement()
                }
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isComplete) accentColor else contentColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isComplete) "Completed!" else "Day ${challenge.currentDay}/${challenge.totalDays}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isComplete) accentColor.copy(alpha = 0.7f) else contentColor.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }

            // Circular progress
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(32.dp)) {
                    // Background arc
                    drawArc(
                        color = contentColor.copy(alpha = 0.08f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Progress arc
                    drawArc(
                        color = if (isComplete) accentColor else accentColor.copy(alpha = 0.6f),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isComplete) accentColor else contentColor.copy(alpha = 0.5f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Progress bar
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (isComplete) accentColor else accentColor.copy(alpha = 0.5f),
            trackColor = contentColor.copy(alpha = 0.06f),
        )

        // Actions row (shown on tap when complete, or long-press style)
        if (showActions || isComplete) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = contentColor.copy(alpha = 0.3f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onReset() }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = contentColor.copy(alpha = 0.3f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDelete() }
                )
            }
        }
    }
}

@Composable
private fun AddChallengeDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, days: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("30") }
    val focusManager = LocalFocusManager.current

    val presets = listOf(
        "No Sugar" to 30,
        "Meditation" to 21,
        "Reading" to 30,
        "Exercise" to 30,
        "No Phone Before Bed" to 14,
        "Gratitude Journal" to 21,
        "Cold Shower" to 30,
        "Wake Up Early" to 21
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
        ) {
            Text(
                text = "New Challenge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Presets
            Text(
                text = "Quick start",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { (presetName, presetDays) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .clickable {
                                name = presetName
                                daysText = presetDays.toString()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$presetName ($presetDays d)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom name
            Text(
                text = "Challenge name",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                decorationBox = { inner ->
                    if (name.isEmpty()) {
                        Text(
                            text = "e.g. No Sugar Challenge",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    inner()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Duration
            Text(
                text = "Duration (days)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("7", "14", "21", "30", "60", "90").forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (daysText == preset) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            )
                            .clickable { daysText = preset }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (daysText == preset) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = if (daysText == preset) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val days = daysText.toIntOrNull() ?: 30
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (name.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        .clickable(enabled = name.isNotBlank()) {
                            focusManager.clearFocus()
                            onAdd(name.trim(), days)
                        }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Start Challenge",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (name.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
