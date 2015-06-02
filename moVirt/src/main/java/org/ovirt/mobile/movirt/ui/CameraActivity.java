package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.camera.CaptureActivityHandler;
import org.ovirt.mobile.movirt.camera.zxing.Result;
import org.ovirt.mobile.movirt.camera.zxing.client.CameraManager;
import org.ovirt.mobile.movirt.camera.zxing.client.PreferencesActivity;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.hosts.HostDetailActivity;

import java.io.IOException;

@EActivity(R.layout.activity_camera)
public class CameraActivity extends Activity implements SurfaceHolder.Callback {

    @Bean
    ProviderFacade provider;

    @RootContext
    Context context;

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private Button buttonDetails;
    private String lastFoundID;
    //private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Result savedResultToShow;

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    /*ViewfinderView getViewfinderView() {
        return viewfinderView;
    }*/

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        hasSurface = false;

/*      inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        ambientLightManager = new AmbientLightManager(this);*/

        PreferenceManager.setDefaultValues(this, R.xml.zxing_preferences, false);

/*      // Create an instance of Camera
        mCamera = getCameraInstance(CameraActivity.this);

        if(mCamera == null)
        {
            Toast.makeText(CameraActivity.this, "Can't detect camera", Toast.LENGTH_LONG).show();
        }
        else {
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());
        //provider = new ProviderFacade();

        lastFoundID = null;
        buttonDetails = (Button)findViewById(R.id.button_openhotstdetails);
        buttonDetails.setVisibility(View.GONE);
        buttonDetails.setOnClickListener(
            new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HostDetailActivity.class);
                intent.putExtra("EXTRA_HOST_ID", lastFoundID);
                startActivity(intent);
            }
        });

//        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
//        viewfinderView.setCameraManager(cameraManager);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getBoolean(PreferencesActivity.KEY_DISABLE_AUTO_ORIENTATION, true)) {
            setRequestedOrientation(getCurrentOrientation());
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
//        inactivityTimer.onPause();
//        ambientLightManager.stop();
//        beepManager.close();
        cameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            //displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            //displayFrameworkBugMessageAndExit();
        }
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        TextView tw = (TextView)findViewById(R.id.result_text);
        String result = rawResult.getText();
        if (provider.query(Host.class).id(result).all().size() == 0) {
            String message = ". Can't find or not a proper host ID.";
            tw.setText(result + message);
            buttonDetails.setVisibility(View.GONE);
        } else
        {
            String message = ". Found host ID. Open details page?";
            tw.setText(result + message);
            lastFoundID = result;
            buttonDetails.setVisibility(View.VISIBLE);
        }
        restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        //resetStatusView();
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    private int getCurrentOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_90:
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            default:
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        }
    }
}
