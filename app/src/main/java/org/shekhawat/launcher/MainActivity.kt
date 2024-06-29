package org.shekhawat.launcher

import android.R.id
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.shekhawat.launcher.ui.theme.RelaxLauncherTheme
import org.shekhawat.launcher.ui.theme.screen.AppListScreen
import org.shekhawat.launcher.ui.theme.screen.PomodoroScreen
import org.shekhawat.launcher.ui.theme.screen.RootScreen
import org.shekhawat.launcher.ui.theme.screen.SettingsScreen
import org.shekhawat.launcher.ui.theme.screen.TimeScreen
import org.shekhawat.launcher.ui.theme.screen.TimerScreen
import org.shekhawat.launcher.utils.AppNavigation

class AppInfo(val name: String, val icon: Drawable, val intent: Intent?, val packageName: String)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // check if the app has overlay permission
        // askDrawOverOtherAppsPermissions()
        // showWhenLockedAndTurnScreenOn()
        // hideSystemUI()
        // setDefaultLauncher(this)

        val sharedPrefManager = SharedPrefManager(this)
        val dynamicColor = sharedPrefManager.getBoolean("dynamic_color", false)
        val theme = sharedPrefManager.getString("theme", "PURPLE")

        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val queryResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            pm.queryIntentActivities(mainIntent, 0)
        }

        val appList = queryResult.map {
            val name = it.loadLabel(pm).toString()
            val icon = it.loadIcon(pm)
            val intent = pm.getLaunchIntentForPackage(it.activityInfo.packageName)
            val packageName = it.activityInfo.packageName
            AppInfo(name, icon, intent, packageName)
        }
            .filter { it.name != resources.getString(R.string.app_name) }
            .sortedBy { it.name }

        setContent {
            RelaxLauncherTheme(
                theme = ThemeType.valueOf(theme),
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = dynamicColor
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    val navController = rememberNavController()
                    if(isDefaultLauncher()) {
                        handleBackButton(navController)
                    }

                    NavHost(
                        navController = navController, startDestination = AppNavigation.HOME.name) {
                        composable(AppNavigation.HOME.name) {
                            RootScreen(
                                appList = appList, navController = navController
                            )
                        }
                        composable(AppNavigation.APP_LIST.name) {
                            AppListScreen(appList = appList, navController = navController)
                        }
                        composable(AppNavigation.SETTINGS.name) {
                            SettingsScreen()
                        }
                        composable(AppNavigation.POMODORO.name) {
                            PomodoroScreen()
                        }
                        composable(AppNavigation.TIME.name) {
                            TimeScreen()
                        }
                        composable(AppNavigation.TIMER.name) {
                            TimerScreen()
                        }
                        composable(AppNavigation.SEARCH.name) {
                            // SearchScreen()
                        }
                        composable(AppNavigation.NOTIFICATIONS.name) {
                            // NotificationsScreen()
                        }
                        composable(AppNavigation.RECENT_APPS.name) {
                            // RecentAppsScreen()
                        }
                        composable(AppNavigation.THEME.name) {
                            // ThemeScreen()
                        }
                        composable(AppNavigation.WALLPAPER.name) {
                            // WallpaperScreen()
                        }
                        composable(AppNavigation.LOCK_SCREEN.name) {
                            // LockScreenScreen()
                        }
                        composable(AppNavigation.SCREEN_TIME.name) {
                            // ScreenTimeScreen()
                        }
                        composable(AppNavigation.APP_LOCK.name) {
                            // AppLockScreen()
                        }
                        composable(AppNavigation.APP_INFO.name) {
                            // AppInfoScreen()
                        }
                        composable(AppNavigation.APP_PERMISSIONS.name) {
                            // AppPermissionsScreen()
                        }
                        composable(AppNavigation.APP_ABOUT.name) {
                            // AppAboutScreen()
                        }
                    }
                }
            }
        }
    }

    private fun setDefaultLauncher(mainActivity: MainActivity) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val currentHomePackage = resolveInfo!!.activityInfo.packageName
        if (currentHomePackage != packageName) {
            mainActivity.startActivity(intent)
        }
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val currentHomePackage = resolveInfo!!.activityInfo.packageName
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
                window.decorView.findViewById(id.content)
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
}
