package org.shekhawat.launcher.ui.theme.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import org.shekhawat.launcher.AppInfo
import org.shekhawat.launcher.SettingActivity

@Composable
fun AppListScreen(appList: List<AppInfo>, pageNo: Int, row: Int, col: Int) {
    val context = LocalContext.current
    val startIndex = pageNo * row * col

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
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
                        if (index >= appList.size - startIndex) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .padding(12.dp)
                            )
                        } else {
                            val appInfo = appList[startIndex + index]
                            AppGridItem(
                                appInfo = appInfo,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            ) {
                                if (appInfo.name == "App Settings") {
                                    val intent = Intent(context, SettingActivity::class.java)
                                    context.startActivity(intent)
                                } else {
                                    appInfo.intent?.let { intent ->
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridItem(
    appInfo: AppInfo, modifier: Modifier, onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = modifier
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White),
                properties = PopupProperties(focusable = true)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Show App Info",
                            color = Color.Black
                        )
                    },
                    onClick = {
                        expanded = false
                        // Show App Info logic
                        showAppInfo(context, appInfo.packageName)
                    })
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Uninstall",
                            color = Color.Black
                        )
                    },
                    onClick = {
                        expanded = false
                        // Show App Info logic
                    })
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Add to favourites",
                            color = Color.Black
                        )
                    },
                    onClick = {
                        expanded = false
                        // Show App Info logic
                    })
            }

            Image(
                bitmap = appInfo.icon,
                contentDescription = "App Icon",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    .size(64.dp)
                    .aspectRatio(1f)
//                    .clip(RoundedCornerShape(12.dp))
//                    .clickable(onClick = onClick)
//                    .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
//                    .background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(16.dp))
//                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(16.dp), clip = true)
//                    .rotate(0.2f)
            )

            Text(
                modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            onClick()
                        },
                        onLongClick = {
                            expanded = true
                        }
                    ),
                text = appInfo.name,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

fun showAppInfo(context: Context, packageName: String) {
    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    context.startActivity(intent)
}