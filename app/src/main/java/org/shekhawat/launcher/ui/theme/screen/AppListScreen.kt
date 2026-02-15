package org.shekhawat.launcher.ui.theme.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import org.shekhawat.launcher.AppInfo
import org.shekhawat.launcher.SettingActivity
import org.shekhawat.launcher.SharedPrefManager

// ═══════════════════════════════════════════════════════════════════
//  Single-page App Drawer with search bar + settings icon
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppDrawerPage(appList: List<AppInfo>) {
    val context = LocalContext.current
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }

    // ── Drawer settings (reactive) ──
    val layoutMode by sharedPrefManager.observeString("app_layout", "Grid")
        .collectAsState(initial = sharedPrefManager.getString("app_layout", "Grid"))
    val colString by sharedPrefManager.observeString("grid_cols", "$DEFAULT_COL")
        .collectAsState(initial = sharedPrefManager.getString("grid_cols", "$DEFAULT_COL"))
    val col = colString.toIntOrNull() ?: DEFAULT_COL
    val iconSizeStr by sharedPrefManager.observeString("icon_size", "48")
        .collectAsState(initial = sharedPrefManager.getString("icon_size", "48"))
    val iconSize = iconSizeStr.toIntOrNull() ?: 48
    val showLabels by sharedPrefManager.observeBoolean("show_app_labels", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_app_labels", true))
    val vertPadStr by sharedPrefManager.observeString("grid_vertical_padding", "4")
        .collectAsState(initial = sharedPrefManager.getString("grid_vertical_padding", "4"))
    val verticalPad = vertPadStr.toIntOrNull() ?: 4
    val iconShapePref by sharedPrefManager.observeString("icon_shape", "Default")
        .collectAsState(initial = sharedPrefManager.getString("icon_shape", "Default"))
    val hiddenAppsStr by sharedPrefManager.observeString("hidden_apps", "")
        .collectAsState(initial = sharedPrefManager.getString("hidden_apps", ""))
    val hiddenApps = hiddenAppsStr.split(",").filter { it.isNotBlank() }.toSet()

    val filteredAppList = remember(appList, hiddenApps) {
        appList.filter { it.packageName !in hiddenApps }
    }

    var searchQuery by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }

    val displayList = if (searchQuery.isBlank()) {
        filteredAppList
    } else {
        filteredAppList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // Deduplicate by packageName within each category to avoid LazyGrid key collisions
    val personalApps = remember(displayList) {
        displayList.filter { !it.isWorkProfile }.distinctBy { it.packageName }
    }
    val workApps = remember(displayList) {
        displayList.filter { it.isWorkProfile }.distinctBy { it.packageName }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 12.dp)
    ) {
        // ── Search bar with settings icon ──
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search apps...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 14.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }
            IconButton(onClick = { showSettings = true }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Drawer Settings",
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // ── App list / grid ──
        if (layoutMode == "List") {
            AppListViewWithWorkSection(
                personalApps = personalApps,
                workApps = workApps,
                iconSize = iconSize,
                showLabels = showLabels,
                context = context,
                iconShape = iconShapePref
            )
        } else {
            // Scrollable grid — all apps on one page
            LazyVerticalGrid(
                columns = GridCells.Fixed(col),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(verticalPad.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    personalApps,
                    key = { "p_${it.packageName}" }
                ) { appInfo ->
                    AppGridItem(
                        appInfo = appInfo,
                        iconSize = iconSize,
                        showLabel = showLabels,
                        iconShape = iconShapePref,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        launchApp(context, appInfo)
                    }
                }
                if (workApps.isNotEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                            )
                            Text(
                                text = "  Work  ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                            )
                        }
                    }
                    items(
                        workApps,
                        key = { "w_${it.packageName}" }
                    ) { appInfo ->
                        AppGridItem(
                            appInfo = appInfo,
                            iconSize = iconSize,
                            showLabel = showLabels,
                            iconShape = iconShapePref,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            launchApp(context, appInfo)
                        }
                    }
                }
            }
        }
    }

    // ── Settings bottom sheet ──
    if (showSettings) {
        DrawerSettingsSheet(
            sharedPrefManager = sharedPrefManager,
            onDismiss = { showSettings = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Drawer Settings Bottom Sheet
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerSettingsSheet(
    sharedPrefManager: SharedPrefManager,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Local state backed by prefs
    var layoutMode by remember { mutableStateOf(sharedPrefManager.getString("app_layout", "Grid")) }
    var gridCols by remember { mutableStateOf(sharedPrefManager.getString("grid_cols", "$DEFAULT_COL")) }
    var iconSize by remember { mutableStateOf(sharedPrefManager.getString("icon_size", "48")) }
    var iconShape by remember { mutableStateOf(sharedPrefManager.getString("icon_shape", "Default")) }
    var vertPad by remember { mutableStateOf(sharedPrefManager.getString("grid_vertical_padding", "4")) }
    var showLabels by remember { mutableStateOf(sharedPrefManager.getBoolean("show_app_labels", true)) }
    var sortOrder by remember { mutableStateOf(sharedPrefManager.getString("app_sort", "A-Z")) }
    var autoKeyboard by remember { mutableStateOf(sharedPrefManager.getBoolean("auto_keyboard_search", false)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "App Drawer Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Layout
            DrawerSettingRow(label = "Layout") {
                DrawerChipGroup(
                    options = listOf("Grid", "List"),
                    selected = layoutMode,
                    onSelect = {
                        layoutMode = it
                        sharedPrefManager.saveString("app_layout", it)
                    }
                )
            }

            // Columns (only for Grid)
            if (layoutMode == "Grid") {
                DrawerSettingRow(label = "Columns") {
                    DrawerChipGroup(
                        options = listOf("3", "4", "5", "6"),
                        selected = gridCols,
                        onSelect = {
                            gridCols = it
                            sharedPrefManager.saveString("grid_cols", it)
                        }
                    )
                }
            }

            // Icon Size
            DrawerSettingRow(label = "Icon Size") {
                DrawerChipGroup(
                    options = listOf("36", "42", "48", "54", "60"),
                    selected = iconSize,
                    onSelect = {
                        iconSize = it
                        sharedPrefManager.saveString("icon_size", it)
                    },
                    labelTransform = { "${it}dp" }
                )
            }

            // Icon Shape
            DrawerSettingRow(label = "Icon Shape") {
                DrawerChipGroup(
                    options = listOf("Default", "Circle", "Rounded", "Squircle"),
                    selected = iconShape,
                    onSelect = {
                        iconShape = it
                        sharedPrefManager.saveString("icon_shape", it)
                    }
                )
            }

            // Vertical Padding (only for Grid)
            if (layoutMode == "Grid") {
                DrawerSettingRow(label = "Vertical Spacing") {
                    DrawerChipGroup(
                        options = listOf("0", "4", "8", "12", "16", "24"),
                        selected = vertPad,
                        onSelect = {
                            vertPad = it
                            sharedPrefManager.saveString("grid_vertical_padding", it)
                        },
                        labelTransform = { "${it}dp" }
                    )
                }
            }

            // Sort
            DrawerSettingRow(label = "Sort") {
                DrawerChipGroup(
                    options = listOf("A-Z", "Z-A", "Recent"),
                    selected = sortOrder,
                    onSelect = {
                        sortOrder = it
                        sharedPrefManager.saveString("app_sort", it)
                    }
                )
            }

            // Show Labels toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Show Labels",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = showLabels,
                    onCheckedChange = {
                        showLabels = it
                        sharedPrefManager.saveBoolean("show_app_labels", it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )
            }

            // Auto keyboard toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Auto-open Keyboard",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = autoKeyboard,
                    onCheckedChange = {
                        autoKeyboard = it
                        sharedPrefManager.saveBoolean("auto_keyboard_search", it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

@Composable
private fun DrawerSettingRow(
    label: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DrawerChipGroup(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    labelTransform: (String) -> String = { it }
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labelTransform(option),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Legacy AppListScreen (kept for compatibility)
// ═══════════════════════════════════════════════════════════════════

@Composable
fun AppListScreen(
    appList: List<AppInfo>,
    pageNo: Int,
    row: Int,
    col: Int,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    showSearchBar: Boolean = true
) {
    val context = LocalContext.current
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }

    // Read icon size setting
    val iconSizeStr by sharedPrefManager.observeString("icon_size", "48")
        .collectAsState(initial = sharedPrefManager.getString("icon_size", "48"))
    val iconSize = iconSizeStr.toIntOrNull() ?: 48

    // Read hidden apps
    val hiddenAppsStr by sharedPrefManager.observeString("hidden_apps", "")
        .collectAsState(initial = sharedPrefManager.getString("hidden_apps", ""))
    val hiddenApps = hiddenAppsStr.split(",").filter { it.isNotBlank() }.toSet()

    // Read layout mode
    val layoutMode by sharedPrefManager.observeString("app_layout", "Grid")
        .collectAsState(initial = sharedPrefManager.getString("app_layout", "Grid"))

    // Read show labels
    val showLabels by sharedPrefManager.observeBoolean("show_app_labels", true)
        .collectAsState(initial = sharedPrefManager.getBoolean("show_app_labels", true))

    // Read auto-keyboard
    val autoKeyboard by sharedPrefManager.observeBoolean("auto_keyboard_search", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("auto_keyboard_search", false))

    val filteredAppList = remember(appList, hiddenApps) {
        appList.filter { it.packageName !in hiddenApps }
    }

    val startIndex = pageNo * row * col

    val displayList = if (searchQuery.isBlank()) {
        filteredAppList
    } else {
        filteredAppList.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    // Separate personal and work profile apps
    val personalApps = remember(displayList) { displayList.filter { !it.isWorkProfile } }
    val workApps = remember(displayList) { displayList.filter { it.isWorkProfile } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (!showSearchBar) {
                    // When search bar is overlaid from RootScreen, add top padding
                    // so content doesn't sit behind it (status bar + search bar height)
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(start = 12.dp, end = 12.dp, bottom = 4.dp, top = 48.dp)
                } else {
                    Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                }
            )
    ) {
        // Search bar (only if showSearchBar is true — otherwise it's fixed in RootScreen)
        if (showSearchBar) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                autoFocus = autoKeyboard && pageNo == 0
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (searchQuery.isNotBlank()) {
            // Show search results — always show all matching results
            if (layoutMode == "List") {
                AppListViewWithWorkSection(
                    personalApps = personalApps,
                    workApps = workApps,
                    iconSize = iconSize,
                    showLabels = showLabels,
                    context = context
                )
            } else {
                AppGridView(
                    apps = displayList,
                    startIndex = 0,
                    row = row,
                    col = col,
                    iconSize = iconSize,
                    showLabels = showLabels,
                    context = context,
                    showAll = true
                )
            }
        } else {
            // Normal view
            if (layoutMode == "List") {
                // List view: show ALL apps in one scrollable page (no pagination)
                AppListViewWithWorkSection(
                    personalApps = personalApps,
                    workApps = workApps,
                    iconSize = iconSize,
                    showLabels = showLabels,
                    context = context
                )
            } else {
                AppGridView(
                    apps = displayList,
                    startIndex = startIndex,
                    row = row,
                    col = col,
                    iconSize = iconSize,
                    showLabels = showLabels,
                    context = context,
                    showAll = false
                )
            }
        }
    }
}

// ── Grid View ──

@Composable
private fun AppGridView(
    apps: List<AppInfo>,
    startIndex: Int,
    row: Int,
    col: Int,
    iconSize: Int,
    showLabels: Boolean,
    context: Context,
    showAll: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = if (showAll) Arrangement.Top else Arrangement.SpaceEvenly,
    ) {
        val totalItems = if (showAll) apps.size else apps.size
        for (i in 0 until row) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (j in 0 until col) {
                    val index = i * col + j
                    val actualIndex = if (showAll) index else startIndex + index
                    if (actualIndex >= apps.size) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(4.dp)
                        )
                    } else {
                        val appInfo = apps[actualIndex]
                        AppGridItem(
                            appInfo = appInfo,
                            iconSize = iconSize,
                            showLabel = showLabels,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        ) {
                            launchApp(context, appInfo)
                        }
                    }
                }
            }
        }
    }
}

// ── List View with Work Profile Section ──

@Composable
private fun AppListViewWithWorkSection(
    personalApps: List<AppInfo>,
    workApps: List<AppInfo>,
    iconSize: Int,
    showLabels: Boolean,
    context: Context,
    iconShape: String = "Default"
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Personal apps
        items(personalApps) { appInfo ->
            AppListItem(
                appInfo = appInfo,
                iconSize = iconSize,
                showLabel = showLabels,
                iconShape = iconShape,
                onClick = { launchApp(context, appInfo) }
            )
        }

        // Work profile section
        if (workApps.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "  Work  ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    )
                }
            }
            items(workApps) { appInfo ->
                AppListItem(
                    appInfo = appInfo,
                    iconSize = iconSize,
                    showLabel = showLabels,
                    iconShape = iconShape,
                    onClick = { launchApp(context, appInfo) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppListItem(
    appInfo: AppInfo,
    iconSize: Int,
    showLabel: Boolean,
    iconShape: String = "Default",
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val shape = iconShapeFor(iconShape)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { expanded = true }
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = appInfo.icon,
            contentDescription = appInfo.name,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(iconSize.dp)
                .aspectRatio(1f)
                .then(if (shape != null) Modifier.clip(shape) else Modifier)
        )

        if (showLabel) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = appInfo.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        AppContextMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            appInfo = appInfo,
            context = context
        )
    }
}

// ── Search Bar ──

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    autoFocus: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    if (autoFocus) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Search apps...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear search",
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onQueryChange("") }
            )
        }
    }
}

// ── Grid Item ──

/**
 * Returns the [Shape] for the given icon shape preference name.
 */
internal fun iconShapeFor(name: String): androidx.compose.ui.graphics.Shape? {
    return when (name) {
        "Circle" -> CircleShape
        "Rounded" -> RoundedCornerShape(14.dp)
        "Squircle" -> RoundedCornerShape(22)  // percentage-based for squircle look
        else -> null  // "Default" — no clipping
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridItem(
    appInfo: AppInfo,
    iconSize: Int = 48,
    showLabel: Boolean = true,
    iconShape: String = "Default",
    modifier: Modifier,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val shape = iconShapeFor(iconShape)

    Box(
        modifier = modifier
            .padding(4.dp)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { expanded = true }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppContextMenu(
                expanded = expanded,
                onDismiss = { expanded = false },
                appInfo = appInfo,
                context = context
            )

            Image(
                bitmap = appInfo.icon,
                contentDescription = appInfo.name,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                    .size(iconSize.dp)
                    .aspectRatio(1f)
                    .then(if (shape != null) Modifier.clip(shape) else Modifier)
            )

            if (showLabel) {
                Text(
                    text = appInfo.name,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

// ── Shared Context Menu ──

@Composable
private fun AppContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    appInfo: AppInfo,
    context: Context
) {
    var showLimitOptions by remember { mutableStateOf(false) }
    var showCustomInput by remember { mutableStateOf(false) }
    var customMinutesText by remember { mutableStateOf("") }
    val realPkg = appInfo.packageName.removeSuffix("#work")
    val currentLimit = remember(realPkg) { getAppLimit(context, realPkg) }

    // Custom time limit dialog
    if (showCustomInput) {
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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                BasicTextField(
                    value = customMinutesText,
                    onValueChange = { newVal ->
                        // Only allow digits
                        customMinutesText = newVal.filter { it.isDigit() }
                    },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
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
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
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
                                setAppLimit(context, realPkg, mins)
                                showCustomInput = false
                                showLimitOptions = false
                                onDismiss()
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
            // Main menu
            DropdownMenuItem(
                text = {
                    Text(text = "App Info", color = MaterialTheme.colorScheme.onSurface)
                },
                onClick = {
                    onDismiss()
                    showAppInfo(context, appInfo.packageName)
                }
            )
            // Only show these options for other apps, not for the launcher itself
            if (!isSelf) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                DropdownMenuItem(
                    text = {
                        Text(text = "Uninstall", color = MaterialTheme.colorScheme.onSurface)
                    },
                    onClick = {
                        onDismiss()
                        uninstallApp(context, appInfo.packageName)
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                DropdownMenuItem(
                    text = {
                        Text(text = "Hide App", color = MaterialTheme.colorScheme.onSurface)
                    },
                    onClick = {
                        onDismiss()
                        hideApp(context, appInfo.packageName)
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isFavorite(context, appInfo.packageName)) "Remove from Favorites" else "Add to Favorites",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onDismiss()
                        toggleFavorite(context, appInfo.packageName)
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
                text = {
                    Text(text = "← Back", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                },
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
                        setAppLimit(context, realPkg, minutes)
                    }
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            DropdownMenuItem(
                text = {
                    Text(text = "Custom...", color = MaterialTheme.colorScheme.onSurface)
                },
                onClick = {
                    customMinutesText = if (currentLimit > 0) currentLimit.toString() else ""
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
                    text = {
                        Text(text = "Remove Limit", color = MaterialTheme.colorScheme.error)
                    },
                    onClick = {
                        showLimitOptions = false
                        onDismiss()
                        setAppLimit(context, realPkg, 0)
                    }
                )
            }
        }
    }
}

// ── Helper Functions ──

private fun launchApp(context: Context, appInfo: AppInfo) {
    if (appInfo.name == "App Settings") {
        val intent = Intent(context, SettingActivity::class.java)
        context.startActivity(intent)
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

private fun showAppInfo(context: Context, packageName: String) {
    val realPkg = packageName.removeSuffix("#work")
    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", realPkg, null)
    intent.data = uri
    context.startActivity(intent)
}

private fun uninstallApp(context: Context, packageName: String) {
    val realPkg = packageName.removeSuffix("#work")
    try {
        // ACTION_UNINSTALL_PACKAGE is the reliable way to trigger the system uninstall dialog
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
        intent.data = Uri.parse("package:$realPkg")
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback to ACTION_DELETE if ACTION_UNINSTALL_PACKAGE is not available
        try {
            val fallback = Intent(Intent.ACTION_DELETE)
            fallback.data = Uri.parse("package:$realPkg")
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallback)
        } catch (_: Exception) { }
    }
}

private fun hideApp(context: Context, packageName: String) {
    val sharedPrefManager = SharedPrefManager(context)
    val current = sharedPrefManager.getString("hidden_apps", "")
    val hiddenSet = current.split(",").filter { it.isNotBlank() }.toMutableSet()
    hiddenSet.add(packageName)
    sharedPrefManager.saveString("hidden_apps", hiddenSet.joinToString(","))
}

private fun isFavorite(context: Context, packageName: String): Boolean {
    val sharedPrefManager = SharedPrefManager(context)
    val current = sharedPrefManager.getString("favorite_apps", "")
    return packageName in current.split(",").filter { it.isNotBlank() }.toSet()
}

private fun toggleFavorite(context: Context, packageName: String) {
    val sharedPrefManager = SharedPrefManager(context)
    val current = sharedPrefManager.getString("favorite_apps", "")
    val favSet = current.split(",").filter { it.isNotBlank() }.toMutableSet()
    if (packageName in favSet) {
        favSet.remove(packageName)
    } else {
        favSet.add(packageName)
    }
    sharedPrefManager.saveString("favorite_apps", favSet.joinToString(","))
}

private fun getAppLimit(context: Context, packageName: String): Int {
    val prefs = SharedPrefManager(context)
    val limitsStr = prefs.getString("app_limits", "{}")
    return try {
        val json = org.json.JSONObject(limitsStr)
        if (json.has(packageName)) json.getInt(packageName) else 0
    } catch (_: Exception) {
        0
    }
}

private fun setAppLimit(context: Context, packageName: String, minutes: Int) {
    val prefs = SharedPrefManager(context)
    val limitsStr = prefs.getString("app_limits", "{}")
    val json = try { org.json.JSONObject(limitsStr) } catch (_: Exception) { org.json.JSONObject() }
    if (minutes <= 0) {
        json.remove(packageName)
    } else {
        json.put(packageName, minutes)
    }
    prefs.saveString("app_limits", json.toString())

    // Ensure the monitor service is running whenever limits exist
    if (json.length() > 0 && org.shekhawat.launcher.utils.UsageStatsHelper.hasPermission(context)) {
        org.shekhawat.launcher.service.AppUsageMonitorService.start(context)
    }
}
