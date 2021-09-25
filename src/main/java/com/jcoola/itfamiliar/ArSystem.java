// jcoola technologies, copyright @2013-2020
// www.jcoola.com
// -----------------------------------------

package com.jcoola.itfamiliar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Build;

import android.util.Log;
import androidx.core.content.FileProvider;



import java.io.File;


public class ArSystem {

    public static final String googlePlayServicesForArApkName = "Google_Play_Services_for_AR_1.17.apk";


    public static int activityStackNumber=0;

    public static boolean justCalled_installGooglePlayServicesForAR =false;


    private ArSystem() {

    }



    @SuppressWarnings("deprecation")
    public static void installAPK(Context ctx, File updateFile) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d("ARCore", ">> installAPK 1_1");
                Log.d("InstallAPK", "Install APK, installing apk step4.. exists:" + updateFile.exists());
                Uri apkUri = FileProvider.getUriForFile(
                        ctx,"com.jcoola.itfamiliar.provider" //BuildConfig.APPLICATION_ID+ ".provider"
                                , updateFile);
                //Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ctx.startActivity(intent);
                Log.d("ARCore", ">> installAPK 1_2");
            }
        }catch
        (Exception e){
            Log.d("InstallAPK", "Install APK, installing apk exception:" + e.getMessage());
        }
    }

    // 1: not first time , else: first time
    public static void saveToDevicePref_FirstTimeRunStat(Context ctx, int firstTimeRunStat) {
        String _key = ctx.getResources().getString(R.string.ar_pref_firsttimerunstat);
        SharedPreferences sharedPref = ctx.getSharedPreferences(
                _key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(_key, firstTimeRunStat);
        editor.commit();
    }

    public static boolean isFirstTimeRun(Context ctx){
        if(readFromDevicePref_FirstTimeRunStat(ctx) != 1)
            return true;
        else
            return false;
    }

    public static int readFromDevicePref_FirstTimeRunStat(Context ctx)
    {
        String _key = ctx.getResources().getString(R.string.ar_pref_firsttimerunstat);
        SharedPreferences sharedPref = ctx.getSharedPreferences(_key, Context.MODE_PRIVATE);
        int defaultValue = 0;
        return sharedPref.getInt(_key, defaultValue);
    }



}

