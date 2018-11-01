package com.panasonic.cdd.cddandroidlib;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.UiModeManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import static android.content.Context.UI_MODE_SERVICE;
import static android.content.Context.WINDOW_SERVICE;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
import static org.junit.Assert.assertTrue;

public class DeviceUtils {

    public static boolean isLock(Context context) {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.isDeviceSecure();
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void changeWallpaper(Context context, int res) {
        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(context);
        try {
            myWallpaperManager.setResource(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openIntent(Intent intent) {
        if (intent == null) return;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        InstrumentationRegistry.getTargetContext().startActivity(intent);
    }

    public static boolean hasActivityForIntent(Intent intent) {
        PackageManager packageManager = InstrumentationRegistry.getTargetContext().getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            return true;
        } else {
            return false;
        }
    }

    public static String getLabelOfIntent(Intent intent) {
        String label = null;
        try {
            PackageManager packageManager = InstrumentationRegistry.getTargetContext().getPackageManager();
            label = String.valueOf(packageManager.getActivityInfo(intent.getComponent(), 0)
                    .loadLabel(packageManager));
            Log.e("label", label);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return label;
    }

    public static boolean hasConnect(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean hasPermission(Context context, String manifest) {
        return ContextCompat.checkSelfPermission(context, manifest) == PackageManager.PERMISSION_GRANTED;
    }


    public static void copyRawToFile(Context context, int id, File path) throws IOException {
        InputStream in = context.getResources().openRawResource(id);
        FileOutputStream out = new FileOutputStream(path);
        byte[] buff = new byte[1024];
        int read = 0;
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    public static boolean hasFeature(String feature) {
        try {
            return InstrumentationRegistry.getTargetContext().getPackageManager().hasSystemFeature(feature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void openApp(String packageName) {
        Intent launchIntent = InstrumentationRegistry.getContext().getPackageManager()
                .getLaunchIntentForPackage(packageName);
        if (launchIntent != null)
            openIntent(launchIntent);
    }

    public static File copyAssets(String filename) {
        Context context = InstrumentationRegistry.getTargetContext();
        AssetManager assetManager = context.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            File newFileName = new File(Environment.getExternalStorageDirectory(), filename);
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return newFileName;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
        return null;
    }


    public static void openFile(File file, String mime, String author) {
        Context context = InstrumentationRegistry.getTargetContext();
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(mime);
        Uri photoURI = FileProvider.getUriForFile(context, author, file);
        newIntent.setDataAndType(photoURI, mimeType);
        newIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            openIntent(newIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void openAPKFile(File file) {
        Context context = InstrumentationRegistry.getTargetContext();
        Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        Uri photoURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName()
                + ".provider", file);
        installIntent.setData(photoURI);
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        openIntent(installIntent);
    }

    public static void clearAllowUnknownApp(String appName) {
        try {
            Context context = InstrumentationRegistry.getTargetContext();
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            DeviceUtils.openIntent(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
            UiObject app = uiDevice.findObject(new UiSelector().textContains(appName));
            app.waitForExists(1000);
            if (app.exists()) {
                app.click();
                UiObject btnSwitch = uiDevice.findObject(new UiSelector().checkable(true).checked(true));
                btnSwitch.waitForExists(1000);
                if (btnSwitch.exists()) btnSwitch.click();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uninstallApp(String packageName) {
        try {
            Context context = InstrumentationRegistry.getTargetContext();
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            openAppDetailSettings(packageName);

            UiObject btnUninstall = uiDevice.findObject(new UiSelector().textContains("uninstall").clickable(true));
            btnUninstall.waitForExists(1000);
            if (btnUninstall.exists()) {
                btnUninstall.click();
                UiObject btnOk = uiDevice.findObject(new UiSelector().resourceId("android:id/button1"));
                btnOk.waitForExists(1000);
                if (btnOk.exists()) btnOk.click();
            }
            uiDevice.waitForIdle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openAppDetailSettings(String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        openIntent(intent);
    }

    public static boolean changeAllPermissionState(String packageName, boolean enable) {
        boolean availableUI = false;
        try {
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            openAppDetailSettings(packageName);
            UiObject btnPermission = uiDevice.findObject(new UiSelector().textContains("permission"));
            btnPermission.waitForExists(1000);
            if (btnPermission.exists()) {
                btnPermission.click();
                uiDevice.waitForIdle();
            } else {
                return false;
            }
            while (true) {
                UiObject btnSwitchable = uiDevice.findObject(new UiSelector().checkable(true).checked(!enable));
                btnSwitchable.waitForExists(1000);
                if (btnSwitchable.exists()) {
                    btnSwitchable.click();
                    uiDevice.waitForIdle();
                    availableUI = true;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return availableUI;
    }

    public static boolean requestPermission() {
        boolean availableUI = false;
        try {
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            openAppDetailSettings(InstrumentationRegistry.getContext().getPackageName());
            UiObject btnPermission = uiDevice.findObject(new UiSelector().textContains("permission"));
            btnPermission.waitForExists(1000);
            if (btnPermission.exists()) {
                btnPermission.click();
                uiDevice.waitForIdle();
            } else {
                return false;
            }
            while (true) {
                UiObject btnSwitchable = uiDevice.findObject(new UiSelector().checkable(true).checked(false));
                btnSwitchable.waitForExists(1000);
                if (btnSwitchable.exists()) {
                    btnSwitchable.click();
                    availableUI = true;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return availableUI;
    }

    public static void clearLogcat() {
        try {
            Runtime.getRuntime().exec("logcat -c -d");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean exeCmdWithExpectedContent(String cmd, String content)
            throws IOException {
        Process logProcess = Runtime.getRuntime().exec(cmd);
        BufferedReader input = new BufferedReader(new InputStreamReader(
                logProcess.getInputStream()));
        String line = null;
        while ((line = input.readLine()) != null) {
            Log.e("CMD", line);
            if (line.toLowerCase().trim()
                    .contains(content.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    public static void connectWifi() {
        try {
            WifiManager manager = (WifiManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.WIFI_SERVICE);
            if (!manager.isWifiEnabled()) {
                manager.setWifiEnabled(true);
                sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float getScreenDiagonalSize() {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            ((WindowManager) InstrumentationRegistry.getTargetContext().getSystemService(WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(dm);
            double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
            double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
            double screenInches = Math.sqrt(x + y);
            Log.d("debug", "Screen inches : " + screenInches);
            return (float) screenInches;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static float getAspectRatio() {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            ((WindowManager) InstrumentationRegistry.getTargetContext().getSystemService(WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(dm);
            double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
            double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
            if (x > y) return (float) (x / y);
            else return (float) (y / x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean clickOptionInScrollView(String key) {
        try {
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            int count = 0;
            boolean exist = false;
            while (count < 5) {
                UiObject option = uiDevice.findObject(new UiSelector().textContains(key));
                option.waitForExists(1000);
                if (option.exists()) {
                    option.click();
                    DeviceUtils.sleep(1000);
                    exist = true;
                    break;
                } else {
                    uiDevice.swipe(uiDevice.getDisplayWidth() / 2, uiDevice.getDisplayHeight() / 2,
                            uiDevice.getDisplayWidth() / 2, uiDevice.getDisplayHeight() / 2 - 50, 2);
                    count++;
                }
            }
            return exist;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static UiObject getOptionInScrollView(String key) {
        try {
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            int count = 0;
            while (count < 5) {
                UiObject option = uiDevice.findObject(new UiSelector().textContains(key));
                option.waitForExists(1000);
                if (option.exists()) {
                    return option;
                } else {
                    uiDevice.swipe(uiDevice.getDisplayWidth() / 2, uiDevice.getDisplayHeight() / 2,
                            uiDevice.getDisplayWidth() / 2, uiDevice.getDisplayHeight() / 2 - 50, 2);
                    count++;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clickOkIfNeed() {
        try {
            while (true) {
                UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
                UiObject ok = uiDevice.findObject(new UiSelector().resourceId("android:id/button1"));
                ok.waitForExists(1000);
                if (ok.exists()) ok.click();
                else break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clickCancelIfNeed() {
        try {
            while (true) {
                UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
                UiObject ok = uiDevice.findObject(new UiSelector().resourceId("android:id/button2"));
                ok.waitForExists(1000);
                if (ok.exists()) ok.click();
                else break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clickAllowIfNeed() {
        try {
            while (true) {
                UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
                UiObject ok = uiDevice.findObject(new UiSelector().textContains("allow").clickable(true));
                ok.waitForExists(1000);
                if (ok.exists()) ok.click();
                else break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deviceIsTeleVision() {
        try {
            UiModeManager uiModeManager = (UiModeManager) InstrumentationRegistry.getTargetContext().getSystemService(UI_MODE_SERVICE);
            return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deviceIsWatch() {
        try {
            UiModeManager uiModeManager = (UiModeManager) InstrumentationRegistry.getTargetContext().getSystemService(UI_MODE_SERVICE);
            return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_WATCH;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("NewApi")
    public static boolean hasSoftNavigation(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH &&
                !ViewConfiguration.get(context).hasPermanentMenuKey();
    }

    public static float convertPixelsToDp(float px) {
        return px / ((float) InstrumentationRegistry.getTargetContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static boolean hasDPad() {
        return InstrumentationRegistry.getTargetContext().getResources().getConfiguration().navigation
                == Configuration.NAVIGATION_DPAD;
    }

    public static boolean hasTrackBall() {
        return InstrumentationRegistry.getTargetContext().getResources().getConfiguration().navigation
                == Configuration.NAVIGATION_TRACKBALL;
    }

    public static boolean hasWheel() {
        return InstrumentationRegistry.getTargetContext().getResources().getConfiguration().navigation
                == Configuration.NAVIGATION_WHEEL;
    }


    public static boolean isHandheld() {
        Boolean isHandheld = false;
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) InstrumentationRegistry.getTargetContext()
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        double x = Math.pow(metrics.widthPixels / metrics.xdpi, 2);
        double y = Math.pow(metrics.heightPixels / metrics.ydpi, 2);
        double screenInches = Math.sqrt(x + y);
        if (2.5 <= screenInches && screenInches < 8) {
            isHandheld = true;
        }
        return isHandheld;
    }


    public static boolean hasPhysicalMenuButton() {
        return ViewConfiguration.get(InstrumentationRegistry.getTargetContext()).hasPermanentMenuKey();
    }

    public static UiObject findObjectContainId(String id) {
        return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                .findObject(new UiSelector().resourceIdMatches("^.*" + id + ".*$"));
    }

    public static String regexContain(String id) {
        return "^.*" + id + ".*$";
    }

    public static UiObject getEditText() {
        try {
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            DeviceUtils.openIntent(new Intent(Settings.ACTION_SETTINGS));
            UiObject search = findObjectContainId("search");
            search.waitForExists(1000);
            if (search.exists()) search.click();

            UiObject editText = uiDevice.findObject(new UiSelector().className(EditText.class));
            editText.waitForExists(2000);
            if (editText.exists()) {
                editText.click();
                return editText;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static void clearRecentApp() {
//        try {
//            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
//            uiDevice.pressKeyCode(KeyEvent.KEYCODE_APP_SWITCH);
//            uiDevice.waitForIdle();
//            while (true) {
//                UiObject option = uiDevice.findObject(new UiSelector().descriptionContains("dismiss"));
//                option.waitForExists(1000);
//                if (option.exists()) {
//                    option.click();
//                } else {
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static long getTotalMemoryMB() {
        ActivityManager actManager = (ActivityManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
        }
        long size = memInfo.totalMem / (1024 * 1024);
        Log.e("TotalMemorySize", size + "");
        return size;
    }

    public static boolean is32bit() {
        if (Build.SUPPORTED_64_BIT_ABIS == null || Build.SUPPORTED_64_BIT_ABIS.length == 0) {
            return Build.SUPPORTED_32_BIT_ABIS != null && Build.SUPPORTED_32_BIT_ABIS.length > 0;
        }
        return false;
    }

    public static boolean is64bit() {
        return Build.SUPPORTED_64_BIT_ABIS != null && Build.SUPPORTED_64_BIT_ABIS.length > 0;
    }

    public static ResolutionDisplay getResolutionType() {
        Context context = InstrumentationRegistry.getTargetContext();
        if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == SCREENLAYOUT_SIZE_LARGE) {
            return ResolutionDisplay.FHD;
        } else if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            return ResolutionDisplay.HDPlus;
        } else if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            return ResolutionDisplay.qHD;
        } else if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            return ResolutionDisplay.QHD;
        }
        return null;
    }


    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long size = availableBlocks * blockSize / (1024 * 1024);
        Log.e("internalAvailableSize", size + "");
        return size;
    }


    public static String formatSize(long size) {
//        String suffix = null;
//
//        if (size >= 1024) {
//            suffix = "KB";
//            size /= 1024;
//            if (size >= 1024) {
//                suffix = "MB";
//                size /= 1024;
//            }
//        }

//        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

//        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    public static String getAppByKey(String key) {
        List<ApplicationInfo> list = InstrumentationRegistry.getContext().getPackageManager().getInstalledApplications(Integer.MAX_VALUE);
        for (ApplicationInfo info : list) {
            if (info.packageName.contains(key) && !info.packageName.contains("provider"))
                return info.packageName;
        }
        return null;
    }

    public static void clearRecentApps() {
        try {
            UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            uiDevice.pressHome();
            uiDevice.waitForIdle();
            uiDevice.pressKeyCode(KeyEvent.KEYCODE_APP_SWITCH);
            DeviceUtils.sleep(1000);
            int count = 0;
            while (count < 3) {
                UiObject btnClear = uiDevice.findObject(new UiSelector().descriptionContains("clear"));
                if (!btnClear.exists()) {
                    btnClear = uiDevice.findObject(new UiSelector().textContains("clear"));
                }
                if (!btnClear.exists()) {
                    btnClear = findObjectContainId("clear");
                }
                if (btnClear.exists()) {
                    btnClear.click();
                    break;
                } else {
                    uiDevice.swipe(uiDevice.getDisplayWidth() / 2, 100,
                            uiDevice.getDisplayWidth() / 2, uiDevice.getDisplayHeight() - 100, 2);
                    uiDevice.waitForIdle();
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkHasRequestAccessPermission() {
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        UiObject btnAllow = uiDevice.findObject(new UiSelector().textContains("allow").clickable(true));
        UiObject btnDeny = uiDevice.findObject(new UiSelector().textContains("deny").clickable(true));

        btnAllow.waitForExists(2000);
        btnDeny.waitForExists(2000);
        assertTrue("CAN not found button ALLOW, current is not exiting", btnAllow.exists());
        assertTrue("CAN not found button DENY, current is not exiting", btnDeny.exists());
    }

    public static void wakeup() {
       try {
           UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
           if(!uiDevice.isScreenOn()) uiDevice.wakeUp();
       } catch (Exception e){
           e.printStackTrace();
       }
    }
}
