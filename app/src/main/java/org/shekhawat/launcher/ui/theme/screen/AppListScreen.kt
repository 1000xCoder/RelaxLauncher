package org.shekhawat.launcher.ui.theme.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.shekhawat.launcher.AppInfo
import org.shekhawat.launcher.R
import org.shekhawat.launcher.utils.AppNavigation
import kotlin.math.ceil

private val RECENTLY_OPENED = 5

@Composable
fun AppListScreen(appList: List<AppInfo>, navController: NavHostController) {
    var searchAppName by remember {
        mutableStateOf("")
    }
    var recentlyOpenedApps by remember {
        mutableStateOf<Set<AppInfo>>(emptySet())
    }

    Column {
        // show search bar
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            value = searchAppName,
            onValueChange = { searchAppName = it },
            placeholder = {
                Text(
                    text = "Search App",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search Icon",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            trailingIcon = {
                if (searchAppName.isNotEmpty()) {
                    Icon(
                        painter = painterResource(id = R.drawable.close),
                        contentDescription = "Search Icon",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                searchAppName = ""
                            },
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.setting),
                        contentDescription = "Search Icon",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                // open the settings
                                navController.navigate(AppNavigation.SETTINGS.name)
                            },
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )

        if (searchAppName.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp
            )
        }

        val filteredAppList = appList.filter { appInfo ->
            appInfo.name.contains(searchAppName, ignoreCase = true)
        }

        if (filteredAppList.isEmpty()) {
            Text(
                text = "No app found",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            return
        }

        GridPageWiseAppList(
            filteredAppList = filteredAppList,
            recentlyOpenedApps = recentlyOpenedApps
        ) {
            if (recentlyOpenedApps.contains(it)) {
                recentlyOpenedApps =
                    recentlyOpenedApps - it // re-indexing
            }
            if (recentlyOpenedApps.size >= RECENTLY_OPENED) {
                recentlyOpenedApps = recentlyOpenedApps.drop(1).toSet()
            }
            recentlyOpenedApps = recentlyOpenedApps + it
        }

//        GridWiseAppList(filteredAppList) {}
//        RowWiseAppList(filteredAppList) {}
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridPageWiseAppList(
    filteredAppList: List<AppInfo>,
    recentlyOpenedApps: Set<AppInfo>,
    onAppClicked: (AppInfo) -> Unit
) {
    val mergedList: List<AppInfo> = recentlyOpenedApps.toList().plus(filteredAppList)

    // show the app in pager view
    val row = 5
    val col = 3
    // first page will contain recently opened apps as well

    val pagerState =
        rememberPagerState(
            pageCount = { ceil(mergedList.size.div((row * col).toDouble())).toInt() }
        )

    HorizontalPager(
        state = pagerState,
    ) {
        val startIndex = it * row * col
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (i in 0..<row) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (j in 0..<col) {
                            val index = i * col + j
                            if (index >= mergedList.size - startIndex) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                        .padding(12.dp)
                                ) {
                                    // fill empty space to align apps left
                                }
                            } else {
                                AppGridItem(
                                    appInfo = mergedList[startIndex + index],
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                ) {
                                    onAppClicked(mergedList[startIndex + index])
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridWiseAppList(
    filteredAppList: List<AppInfo>,
    onAppClicked: (AppInfo) -> Unit
) {
    val scrollState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val firstCharIndexMap = mutableMapOf<Char, Int>()
    filteredAppList.forEachIndexed { index, appInfo ->
        val firstChar = appInfo.name.first().uppercaseChar()
        if (!firstCharIndexMap.containsKey(firstChar)) {
            firstCharIndexMap[firstChar] = index
        }
    }

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // show the app list with their icons
        LazyVerticalGrid(
            modifier = Modifier.weight(1f),
            state = scrollState,
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            items(filteredAppList.size) { index ->
                AppGridItem(
                    appInfo = filteredAppList[index],
                    Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    onAppClicked(filteredAppList[index])
                }
            }
        }
    }

    // show a-z character and then shift the app list to that character
    LazyColumn(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        items(26) { index ->
            val char = ('A' + index).toString()
            Text(
                text = char,
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable {
                        // scroll to the app starting with the character
                        scope.launch {
                            scrollState.scrollToItem(
                                firstCharIndexMap[char.first()] ?: 0,
                            )
                        }
                    }
                    .padding(start = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
private fun RowWiseAppList(
    filteredAppList: List<AppInfo>,
    onAppClicked: (AppInfo) -> Unit
) {
    val scope = rememberCoroutineScope()
    val firstCharIndexMap = mutableMapOf<Char, Int>()
    filteredAppList.forEachIndexed { index, appInfo ->
        val firstChar = appInfo.name.first().uppercaseChar()
        if (!firstCharIndexMap.containsKey(firstChar)) {
            firstCharIndexMap[firstChar] = index
        }
    }
    val scrollState = rememberLazyListState()
    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        // show the app list with their icons
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = scrollState,
            verticalArrangement = Arrangement.Top
        ) {
            items(filteredAppList.size) { index ->
                AppItem(filteredAppList[index]) {
                    onAppClicked(filteredAppList[index])
                }
            }
        }
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // show a-z character and then shift the app list to that character
            items(26) { index ->
                val char = ('A' + index).toString()
                Text(
                    text = char,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            // scroll to the app starting with the character
                            scope.launch {
                                scrollState.animateScrollToItem(
                                    firstCharIndexMap[char.first()] ?: 0,
                                    0
                                )
                            }
                        }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItem(appInfo: AppInfo, onClick: () -> Unit) {
    val context = LocalContext.current
    val expanded = remember { mutableStateOf(false) }
    // show the app icon and name
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .combinedClickable(
                onClick = {
                    // open the app when clicked
                    appInfo.intent?.let { intent ->
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                    onClick()
                },
                onLongClick = {
                    // show the app info when long clicked
                    // show the quick action items like app info, uninstall, etc
                    expanded.value = true
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = "App Info") },
                onClick = {
                    expanded.value = false
                    showAppInfo(context, appInfo.packageName)
                }
            )
        }
        // show the app icon ( commenting, app is lagging )
        Image(
            modifier = Modifier
                .size(64.dp)
                .padding(4.dp),
            bitmap = appInfo.icon.toBitmap().asImageBitmap(),
            contentDescription = "App Icon"
        )
        // show the app name
        Text(
            text = appInfo.name,
            modifier = Modifier.padding(start = 24.dp),
            fontSize = 32.sp,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridItem(
    appInfo: AppInfo, modifier: Modifier, onClick: () -> Unit
) {
    val context = LocalContext.current
    val expanded = remember { mutableStateOf(false) }
    // show the app icon and name
    Column(
        modifier = modifier
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier
                .size(64.dp)
                .padding(4.dp)
                .combinedClickable(
                    onClick = {
                        // open the app when clicked
                        appInfo.intent?.let { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                        onClick()
                    },
                    onLongClick = {
                        // show the app info when long clicked
                        // show the quick action items like app info, uninstall, etc
                        expanded.value = true
                    }
                ),
            bitmap = appInfo.icon.toBitmap().asImageBitmap(),
            contentDescription = "App Icon"
        )
        // show the app name
        Text(
            modifier = Modifier
                .padding(4.dp)
                .combinedClickable(
                    onClick = {
                        // open the app when clicked
                        appInfo.intent?.let { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                        onClick()
                    },
                    onLongClick = {
                        // show the app info when long clicked
                        // show the quick action items like app info, uninstall, etc
                        expanded.value = true
                    }
                ),
            text = appInfo.name,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

fun showAppInfo(context: Context, packageName: String) {
    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    context.startActivity(intent)
}