package org.shekhawat.launcher

import android.R.id
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.shekhawat.launcher.ui.theme.RelaxLauncherTheme
import org.shekhawat.launcher.ui.theme.screen.RootScreen
import org.shekhawat.launcher.utils.AppNavigation
import org.shekhawat.launcher.viewmodel.SettingsViewModel

class AppInfo(var name: String, val icon: ImageBitmap, val intent: Intent?, val packageName: String)

class MainActivity : ComponentActivity() {

    private lateinit var musicBroadcastReceiver: MusicBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // check if the app has overlay permission
        // askDrawOverOtherAppsPermissions()
        // showWhenLockedAndTurnScreenOn()
        // hideSystemUI()
        // setDefaultLauncher(this)

        // Register the broadcast receiver
        musicBroadcastReceiver = MusicBroadcastReceiver()

        val filter = IntentFilter("org.shekhawat.launcher.NotificationListener")
        registerReceiver(musicBroadcastReceiver, filter, RECEIVER_EXPORTED)

        shouldGrantNotificationListenerServicePermission()

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
            val icon = drawableToBitmap(it.loadIcon(pm)).asImageBitmap()
            val intent = pm.getLaunchIntentForPackage(it.activityInfo.packageName)
            val packageName = it.activityInfo.packageName
            AppInfo(name, icon, intent, packageName)
        }
            .map {
                (
                        if (it.name == resources.getString(R.string.app_name))
                            AppInfo("App Settings", it.icon, it.intent, it.packageName)
                        else
                            it
                        )
            }
            .sortedBy { it.name }

        setContent {
            val sharedPrefManager = SharedPrefManager(this)
            val settingViewModel = SettingsViewModel(sharedPrefManager)
            val dynamicColor = sharedPrefManager.getBoolean("dynamic_color", false)
            val theme by settingViewModel.theme.collectAsState(settingViewModel.getTheme())

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
                    if (isDefaultLauncher()) {
                        handleBackButton(navController)
                    }
                    RootScreen(appList = appList)
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

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.width)
        drawable.draw(canvas)
        return bitmap
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(pkgName) == true
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(musicBroadcastReceiver)
    }
}
