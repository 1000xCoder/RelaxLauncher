package org.shekhawat.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.UserManager
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.shekhawat.launcher.service.AppUsageMonitorService
import org.shekhawat.launcher.ui.theme.RelaxLauncherTheme
import org.shekhawat.launcher.ui.theme.screen.RootScreen
import org.shekhawat.launcher.utils.AppNavigation
import org.shekhawat.launcher.utils.UsageStatsHelper
import org.shekhawat.launcher.viewmodel.SettingsViewModel

class AppInfo(
    var name: String,
    val icon: ImageBitmap,
    val intent: Intent?,
    val packageName: String,
    val isWorkProfile: Boolean = false,
    val userHandle: android.os.UserHandle? = null,
    val componentName: android.content.ComponentName? = null
)

class MainActivity : ComponentActivity() {

    private fun loadAppList(): List<AppInfo> {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val queryResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val personalApps = queryResult.map {
            val name = it.loadLabel(pm).toString()
            val icon = drawableToBitmap(it.loadIcon(pm)).asImageBitmap()
            val intent = pm.getLaunchIntentForPackage(it.activityInfo.packageName)
            val packageName = it.activityInfo.packageName
            AppInfo(name, icon, intent, packageName, isWorkProfile = false)
        }
            .map {
                (
                        if (it.name == resources.getString(R.string.app_name))
                            AppInfo("App Settings", it.icon, it.intent, it.packageName, isWorkProfile = false)
                        else
                            it
                        )
            }

        val workProfileApps = getWorkProfileApps(pm)
        return (personalApps + workProfileApps).sortedBy { it.name }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shouldGrantNotificationListenerServicePermission()

        // Start app usage monitor if limits are configured
        startAppUsageMonitorIfNeeded()

        val appListState = mutableStateOf(loadAppList())

        // Listen for package installs / uninstalls / changes
        val packageReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Rebuild app list when packages change
                appListState.value = loadAppList()
            }
        }
        val packageFilter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, packageFilter)

        setContent {
            val appList by appListState
            val sharedPrefManager = SharedPrefManager(this)
            val settingViewModel = SettingsViewModel(sharedPrefManager)
            val dynamicColor = sharedPrefManager.getBoolean("dynamic_color", false)
            val theme by settingViewModel.theme.collectAsState(settingViewModel.getTheme())

            val themeType = ThemeType.entries.find { it.name == theme } ?: ThemeType.LIGHT

            val fontPref by sharedPrefManager.observeString("font_family", "Default")
                .collectAsState(initial = sharedPrefManager.getString("font_family", "Default"))
            val selectedFontFamily = remember(fontPref) {
                org.shekhawat.launcher.ui.theme.fontFamilyFromName(fontPref)
            }

            val wallpaper by sharedPrefManager.observeString("wallpaper", "none")
                .collectAsState(initial = sharedPrefManager.getString("wallpaper", "none"))

            val grayscaleMode by sharedPrefManager.observeBoolean("grayscale_mode", false)
                .collectAsState(initial = sharedPrefManager.getBoolean("grayscale_mode", false))

            val grayscaleMatrix = remember {
                ColorMatrix().apply { setToSaturation(0f) }
            }

            RelaxLauncherTheme(
                theme = themeType,
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = dynamicColor,
                fontFamily = selectedFontFamily
            ) {
                val animatedSurfaceColor by animateColorAsState(
                    targetValue = MaterialTheme.colorScheme.primary,
                    animationSpec = tween(durationMillis = 300),
                    label = "mainSurfaceBg"
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = animatedSurfaceColor
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (grayscaleMode) Modifier.drawWithContent {
                                    val paint = Paint().apply {
                                        this.colorFilter = ColorFilter.colorMatrix(grayscaleMatrix)
                                    }
                                    drawContext.canvas.saveLayer(
                                        androidx.compose.ui.geometry.Rect(
                                            0f, 0f, size.width, size.height
                                        ),
                                        paint
                                    )
                                    drawContent()
                                    drawContext.canvas.restore()
                                } else Modifier
                            )
                    ) {
                        // Wallpaper background
                        val wallpaperRes = getWallpaperResource(wallpaper)
                        if (wallpaperRes != 0) {
                            Image(
                                painter = painterResource(id = wallpaperRes),
                                contentDescription = "Wallpaper",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.3f
                            )
                        }

                        val navController = rememberNavController()
                        val isDefault = isDefaultLauncher()
                        if (isDefault) {
                            handleBackButton(navController)
                        }

                        var bannerDismissed by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.fillMaxSize()) {
                            // "Set as default launcher" banner
                            AnimatedVisibility(
                                visible = !isDefault && !bannerDismissed,
                                enter = fadeIn() + slideInVertically { -it },
                                exit = fadeOut() + slideOutVertically { -it }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .windowInsetsPadding(WindowInsets.statusBars)
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
                                        .clickable {
                                            try {
                                                // Open the system "Default home app" picker
                                                startActivity(
                                                    Intent(Settings.ACTION_HOME_SETTINGS)
                                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                )
                                            } catch (_: Exception) {
                                                try {
                                                    // Fallback: open general app settings
                                                    startActivity(
                                                        Intent(Settings.ACTION_SETTINGS)
                                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    )
                                                } catch (_: Exception) { }
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Set as default launcher",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Text(
                                            text = "Tap to choose Relax Launcher as your home screen",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "✕",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .clickable { bannerDismissed = true }
                                            .padding(4.dp)
                                    )
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                RootScreen(appList = appList)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun shouldGrantNotificationListenerServicePermission() {
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }
    }

    private fun setDefaultLauncher(mainActivity: MainActivity) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val currentHomePackage = resolveInfo?.activityInfo?.packageName ?: return
        if (currentHomePackage != packageName) {
            mainActivity.startActivity(intent)
        }
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val currentHomePackage = resolveInfo?.activityInfo?.packageName ?: return false
        return currentHomePackage == packageName
    }

    private fun handleBackButton(navController: NavHostController) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentBackStackEntry?.destination?.route != AppNavigation.HOME.name) {
                    navController.popBackStack()
                }
            }
        }
        this.onBackPressedDispatcher.addCallback(callback)
    }

    private fun askDrawOverOtherAppsPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            // send user to the device settings
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(myIntent)
        }
    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(
                window,
                window.decorView.findViewById(android.R.id.content)
            ).let { controller ->
                controller.hide(WindowInsetsCompat.Type.navigationBars())

                // When the screen is swiped up at the bottom
                // of the application, the navigationBar shall
                // appear for some time
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(pkgName) == true
    }

    private fun getWorkProfileApps(pm: PackageManager): List<AppInfo> {
        val result = mutableListOf<AppInfo>()
        try {
            val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val userManager = getSystemService(Context.USER_SERVICE) as UserManager
            val profiles = userManager.userProfiles
            val currentUser = Process.myUserHandle()

            for (profile in profiles) {
                if (profile == currentUser) continue // Skip personal profile
                val activities = launcherApps.getActivityList(null, profile)
                for (activityInfo in activities) {
                    val name = activityInfo.label.toString()
                    val icon = drawableToBitmap(activityInfo.getBadgedIcon(0)).asImageBitmap()
                    val pkgName = activityInfo.applicationInfo.packageName
                    val component = activityInfo.componentName
                    result.add(
                        AppInfo(
                            name = "$name (Work)",
                            icon = icon,
                            intent = null, // Work apps must be launched via LauncherApps
                            packageName = "$pkgName#work",
                            isWorkProfile = true,
                            userHandle = profile,
                            componentName = component
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Work profile not available or permission denied
        }
        return result
    }

    private fun startAppUsageMonitorIfNeeded() {
        val sharedPrefManager = SharedPrefManager(this)
        val limitsStr = sharedPrefManager.getString("app_limits", "{}")
        val hasLimits = try {
            val json = org.json.JSONObject(limitsStr)
            json.length() > 0
        } catch (_: Exception) {
            false
        }
        if (hasLimits && UsageStatsHelper.hasPermission(this)) {
            AppUsageMonitorService.start(this)
        }
    }

    private fun getWallpaperResource(name: String): Int {
        return when (name) {
            "wallpaper1" -> R.drawable.wallpaper1
            "wallpaper2" -> R.drawable.wallpaper2
            "wallpaper3" -> R.drawable.wallpaper3
            "wallpaper4" -> R.drawable.wallpaper4
            "wallpaper5" -> R.drawable.wallpaper5
            "wallpaper6" -> R.drawable.wallpaper6
            "wallpaper7" -> R.drawable.wallpaper7
            "wallpaper8" -> R.drawable.wallpaper8
            else -> 0
        }
    }

}
