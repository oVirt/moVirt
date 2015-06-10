package org.ovirt.mobile.movirt.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.Result;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.camera.BeepManager;
import org.ovirt.mobile.movirt.camera.CameraManager;
import org.ovirt.mobile.movirt.camera.CaptureActivityHandler;
import org.ovirt.mobile.movirt.camera.PreferencesActivity;
import org.ovirt.mobile.movirt.camera.ViewfinderView;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import java.io.IOException;
import java.util.Collection;

@EActivity(R.layout.activity_camera)
public class CameraActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final long BULK_MODE_SCAN_DELAY_MS = 100L;
    @Bean
    ProviderFacade provider;
    @Bean
    HostFacade hostFacade;
    @ViewById
    TextView textHostName;
    @ViewById
    TextView textStatus;
    @ViewById
    TextView textCpuUsage;
    @ViewById
    TextView textMemoryUsage;
    @ViewById
    LinearLayout panelDetails;
    @ViewById(R.id.viewfinder_view)
    ViewfinderView viewfinderView;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private BeepManager beepManager;
    private String lastFoundID;
    private boolean hasSurface;
    private Result savedResultToShow;
    private Host lastHost;

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

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
        beepManager = new BeepManager(this);

        PreferenceManager.setDefaultValues(this, R.xml.zxing_preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());
        viewfinderView.setCameraManager(cameraManager);

        lastFoundID = null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getBoolean(PreferencesActivity.KEY_DISABLE_AUTO_ORIENTATION, true)) {
            setRequestedOrientation(getCurrentOrientation());
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        beepManager.updatePrefs();

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
        cameraManager.closeDriver();
        beepManager.close();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.camera, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        switch (item.getItemId()) {
            case R.id.menu_settings:
                intent.setClassName(this, PreferencesActivity.class.getName());
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
     * @param rawResult   The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode     A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        TextView tw = (TextView) findViewById(R.id.result_text);
        String result = rawResult.getText();
        viewfinderView.drawResultBitmap(barcode, rawResult.getResultPoints());
        if (!result.equals(lastFoundID)) {
            lastFoundID = result;
            beepManager.playBeepSoundAndVibrate();
            Collection<Host> col = provider.query(Host.class).id(result).all();
            if (col.size() == 0) {
                String message = ". Can't find or not a proper host ID.";
                tw.setText(result + message);
            } else {
                tw.setText(result);
                renderDetails(col.iterator().next());
            }
        }
        restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
    }

    private void renderDetails(Host host) {
        lastHost = host;
        panelDetails.setVisibility(View.VISIBLE);
        textHostName.setText(host.getName());
        textStatus.setText(host.getStatus().toString());
        textCpuUsage.setText(String.format("%.2f%%", host.getCpuUsage()));
        textMemoryUsage.setText(String.format("%.2f%%", host.getMemoryUsage()));
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(
                    CaptureActivityHandler.ZXING_RESTART, delayMS);
        }
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
                Message message = Message.obtain(
                        handler, com.google.zxing.client.android.R.id.zxing_decode_succeeded, savedResultToShow);
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

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    //Button event
    public void openHostDetails(View view) {
        if (lastHost != null) {
            startActivity(hostFacade.getDetailIntent(lastHost, this));
        }
    }
}
