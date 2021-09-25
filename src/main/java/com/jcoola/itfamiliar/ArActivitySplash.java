// jcoola technologies, copyright @2013-2020
// www.jcoola.com
// -----------------------------------------

package com.jcoola.itfamiliar;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.jcoola.ar.unitybinding.CoolaUContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class ArActivitySplash extends AppCompatActivity {

    TextView textMessage;
    TextView textCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ar_activity_splash);

        ar_setupAvailability();

        textMessage = (TextView) findViewById(R.id.textMessage);
        textCode = (TextView) findViewById(R.id.textCode);

        textMessage.setText(getApplicationContext().getResources().getString(R.string.ar_init_text));
        textCode.setText("");

        consumerFullClassName =  getClass().getName();



        handleIntent(getIntent());

    }


    boolean isUnityLoaded = false;

    String consumerFullClassName;


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
        setIntent(intent);
    }
    void handleIntent(Intent intent) {
        if(intent == null || intent.getExtras() == null) return;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1) isUnityLoaded = false;
    }
    @Override
    public void onBackPressed() {

        finishActivity(1);
        finish();
    }

    public void loadUnity() {
        isUnityLoaded = true;
        Intent intent = new Intent(ArActivitySplash.this, CoolaUContainer.class);
        intent.putExtra("consumer", consumerFullClassName);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        startActivityForResult(intent, 1);
    }

    public volatile ArCoreApk.Availability arAvailability;

    private void ar_setupAvailability() {

        arAvailability = ArCoreApk.getInstance().checkAvailability(ArActivitySplash.this);

    }

    public void arCheckAvailibility() {

        arAvailability = ArCoreApk.getInstance().checkAvailability(ArActivitySplash.this);


        if (arAvailability.isTransient()) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    arCheckAvailibility();
                }
            }, 200);
        }
    }


    @Override
    protected void onResume() {

        super.onResume();


        if(ArSystem.activityStackNumber >1)
        {
            ArSystem.activityStackNumber = 0;

            ArActivitySplash.this.finish();
            return;
        }
        else
            ArSystem.activityStackNumber = 1;


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {



            arCheckAvailibility();


            if(ArSystem.isFirstTimeRun(getApplicationContext())){
                ArSystem.saveToDevicePref_FirstTimeRunStat(getApplicationContext(), 1);

                textMessage.setText(getApplicationContext().getResources().getString(R.string.ar_installgpsar_text));
                textCode.setText("");

                installGooglePlayServicesForAR();
            }else

                {


                if(arAvailability == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE){

                    notSupported_Device();
                }
                else{

                    if(
                       arAvailability == ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED
                    || arAvailability == ArCoreApk.Availability.UNKNOWN_CHECKING
                    || arAvailability == ArCoreApk.Availability.UNKNOWN_TIMED_OUT){
                        textMessage.setText(getApplicationContext().getResources().getString(R.string.ar_installgpsar_text));
                        textCode.setText("");
                        installGooglePlayServicesForAR();
                    }
                    else {


                        if(arAvailability != ArCoreApk.Availability.SUPPORTED_INSTALLED)
                            Toast.makeText(ArActivitySplash.this,
                                    "Warning: AR may not be supported on this phone!" , Toast.LENGTH_LONG)
                                    .show();


                        ArSystem.activityStackNumber = 2;
                        loadUnity();
                    }
                }


            }
        }
        else
        {
            notSupported_RequiresAndroidNPlus();
        }
    }

    private void notSupported_RequiresAndroidNPlus() {
        textMessage.setText(getApplicationContext().getResources().getString(R.string.ar_requiresandroid7plus_text));
        textCode.setText("");

    }

    private void notSupported_Device() {
        textMessage.setText(getApplicationContext().getResources().getString(R.string.ar_notsupported_text));
        textCode.setText("");


    }



    private void installGooglePlayServicesForAR() {

        ArSystem.justCalled_installGooglePlayServicesForAR = true;


            File folder = getFilesDir();
            File f = new File(folder, "ar_gpsar");
            if(f.exists()==false)
                f.mkdirs();

            Log.d("InstallAPK", "Install APK, Copying apk..");
            copyFileOrDirFromAssetsToInternalStorage("ar_gpsar", false);

            Log.d("InstallAPK", "Install APK, apk copied.");

            Toast.makeText(ArActivitySplash.this,
                    getApplicationContext().getResources().getString(R.string.ar_installgpsar_text), Toast.LENGTH_LONG)
                    .show();

            //Log.d("InstallAPK", "Install APK, apk will install in 1.5 second..");

        //installGooglePlayServicesMainRepoForAR();



            // Let the 'installing google play services..' message be seen and then install the apk.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Run local 'Google Play Services for AR' APK
                    Log.d("InstallAPK", "Install APK, installing apk step1..");

                    installGooglePlayServicesMainRepoForAR();

                }
            }, 1500);
    }




    private void installGooglePlayServicesMainRepoForAR()
    {
        //Log.d("InstallAPK", "Install APK, installing apk step2..");

        String apkName = ArSystem.googlePlayServicesForArApkName;
        String apkPath = getFilesDir() + "/" + "ar_gpsar/"+ apkName;
        //Log.d("InstallAPK", "Install APK, installing apk step3.. with file:" + apkPath);

        ArSystem.installAPK(ArActivitySplash.this, new File(apkPath));

        //Log.d("InstallAPK", "Install APK, apk installed..");
    }



    private void copyFileOrDirFromAssetsToInternalStorage(String path, boolean overwrite) {
        AssetManager assetManager = this.getAssets();
        String[] assets = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFileFromAssetsToInternalStorage(path, overwrite);
            } else {
                String fullPath = getFilesDir() + "/" + path ;//getDataDir().getPath() + "/" + path ;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDirFromAssetsToInternalStorage(path + "/" + assets[i], overwrite);
                }
            }
        } catch (IOException ex) {
            Log.e("arfiles", "I/O Exception", ex);
        }
    }

    private void copyFileFromAssetsToInternalStorage(String filename, boolean overwrite) {

        String newFileName = getFilesDir() +  "/" + filename; //getDataDir().getPath() +  "/" + filename;

        try {

            File f = new File(newFileName);
            if (overwrite == false) {

                if (f.exists()) {
                    Log.d("arfiles", "Duplicate Found: " + newFileName);
                    return;
                }
            } else {
                if (f.exists())
                f.delete();
            }

        }catch (Exception e){
            Log.e("arfiles", e.getMessage());
        }


        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {

            in = assetManager.open(filename);
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
        } catch (Exception e) {
            Log.e("arfiles", e.getMessage());
        }

    }






}
