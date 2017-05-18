package org.ovirt.mobile.movirt.ui;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.zxing.Result;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.camera.AmbientLightManager;
import org.ovirt.mobile.movirt.camera.BeepManager;
import org.ovirt.mobile.movirt.camera.CameraManager;
import org.ovirt.mobile.movirt.camera.CaptureActivityHandler;
import org.ovirt.mobile.movirt.camera.InactivityTimer;
import org.ovirt.mobile.movirt.camera.PreferencesActivity_;
import org.ovirt.mobile.movirt.camera.ViewfinderView;
import org.ovirt.mobile.movirt.facade.intent.HostIntentResolver;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.enums.HostStatus;
import org.ovirt.mobile.movirt.model.enums.VmStatus;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.dialogs.ErrorDialogFragment;
import org.ovirt.mobile.movirt.ui.events.EventsCursorAdapter;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import java.io.IOException;

import static org.ovirt.mobile.movirt.provider.OVirtContract.BaseEntity.ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.HOST_ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.STATUS;

@EActivity(R.layout.activity_camera)
@OptionsMenu(R.menu.camera)
public class CameraActivity extends SyncableActivity implements SurfaceHolder.Callback {

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final long SCAN_DELAY_MS = 100L;
    private static final int EVENTS_LOADER = FIRST_CHILD_LOADER;
    private static final int VMS_LOADER = FIRST_CHILD_LOADER + 1;
    private static final int HOSTS_LOADER = FIRST_CHILD_LOADER + 2;
    @Bean
    HostIntentResolver hostIntentResolver;
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
    @ViewById
    ListView panelEvents;
    @ViewById
    LinearLayout panelParent;
    @ViewById
    ImageView imageStatus;
    @ViewById
    LinearLayout panelVms;
    @ViewById
    ListView listVms;
    @ViewById
    ProgressBar progress;
    @ViewById(R.id.result_text)
    TextView resultTextView;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private BeepManager beepManager;
    private InactivityTimer inactivityTimer;
    private AmbientLightManager ambientLightManager;
    private String lastResult;
    private boolean hasSurface;
    private Host lastHost;
    private CursorAdapterLoader cursorEventsAdapterLoader;
    private CursorAdapterLoader cursorVmsAdapterLoader;
    private LoaderManager loaderManager;
    private View currentView;
    private int eventsPage = 1;
    private int vmsPage = 1;
    private HostsLoader hostsLoader;

    @AfterViews
    void init() {
        //set visibility
        panelEvents.setVisibility(View.GONE);
        panelParent.setVisibility(View.GONE);
        panelVms.setVisibility(View.GONE);
        currentView = panelDetails;

        setProgressBar(progress);

        loaderManager = getSupportLoaderManager();
        hostsLoader = new HostsLoader();
        loaderManager.initLoader(HOSTS_LOADER, null, hostsLoader);
        //init events
        SimpleCursorAdapter eventListAdapter = new EventsCursorAdapter(this);
        panelEvents.setAdapter(eventListAdapter);
        panelEvents.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                eventsPage = page;
                loaderManager.restartLoader(EVENTS_LOADER, null, cursorEventsAdapterLoader);
            }
        });
        cursorEventsAdapterLoader = new CursorAdapterLoader(eventListAdapter) {
            @Override
            public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
                final ProviderFacade.QueryBuilder<Event> query = providerFacade.query(Event.class);
                if (lastHost != null) {
                    query.where(HOST_ID, lastHost.getId());
                }
                return query.orderByDescending(ID).limit(eventsPage * 20).asLoader();
            }
        };
        loaderManager.initLoader(EVENTS_LOADER, null, cursorEventsAdapterLoader);

        //init vms
        SimpleCursorAdapter vmListAdapter = new SimpleCursorAdapter(this,
                R.layout.vm_list_item_small,
                null,
                new String[]{NAME, STATUS},
                new int[]{R.id.vm_name, R.id.vm_status}, 0);
        vmListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(NAME)) {
                    TextView textView = (TextView) view;
                    String vmName = cursor.getString(cursor.getColumnIndex(NAME));
                    textView.setText(vmName);
                } else if (columnIndex == cursor.getColumnIndex(STATUS)) {
                    ImageView imageView = (ImageView) view;
                    VmStatus status = VmStatus.valueOf(cursor.getString(cursor.getColumnIndex(STATUS)));
                    imageView.setImageResource(status.getResource());
                }
                return true;
            }
        });
        listVms.setAdapter(vmListAdapter);
        listVms.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                vmsPage = page;
                loaderManager.restartLoader(VMS_LOADER, null, cursorVmsAdapterLoader);
            }
        });
        cursorVmsAdapterLoader = new CursorAdapterLoader(vmListAdapter) {
            @Override
            public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
                final ProviderFacade.QueryBuilder<Vm> query = providerFacade.query(Vm.class);

                if (lastHost == null) {
                    return query.where(HOST_ID, "0").asLoader();
                } else {
                    query.where(HOST_ID, lastHost.getId());
                }
                return query.orderByAscending(NAME).limit(vmsPage * 20).asLoader();
            }
        };
        loaderManager.initLoader(VMS_LOADER, null, cursorVmsAdapterLoader);
    }

    @Override
    public void restartLoader() {
        super.restartLoader();
        loaderManager.restartLoader(HOSTS_LOADER, null, hostsLoader);
    }

    @Override
    public void destroyLoader() {
        super.destroyLoader();
        loaderManager.destroyLoader(HOSTS_LOADER);
        loaderManager.destroyLoader(EVENTS_LOADER);
        loaderManager.destroyLoader(VMS_LOADER);
    }

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
        inactivityTimer = new InactivityTimer(this);
        ambientLightManager = new AmbientLightManager(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences_zxing, false);
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

        lastResult = null;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        beepManager.updatePrefs();
        inactivityTimer.onResume();
        ambientLightManager.start(cameraManager);

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
        inactivityTimer.onPause();
        ambientLightManager.stop();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
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
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessage(ioe.getMessage());
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessage(e.getMessage());
        }
    }

    private void displayFrameworkBugMessage(String errorDetails) {
        DialogFragment dialogFragment = ErrorDialogFragment.newInstance(
                getString(com.google.zxing.client.android.R.string.zxing_msg_camera_framework_bug) +
                        "\n\n" + errorDetails
        );
        dialogFragment.show(getFragmentManager(), "camera_error");
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode) {
        inactivityTimer.onActivity();
        String result = rawResult.getText();
        if (result != null) {
            result = result.trim().toLowerCase();

            viewfinderView.drawResultBitmap(barcode, rawResult.getResultPoints());

            if (!result.equals(lastResult)) {
                lastResult = result;
                beepManager.playBeepSoundAndVibrate();
                resultTextView.setText(lastResult);
                loaderManager.restartLoader(HOSTS_LOADER, null, hostsLoader);
            }
        }
        restartPreviewAfterDelay(SCAN_DELAY_MS);
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(
                    CaptureActivityHandler.ZXING_RESTART, delayMS);
        }
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    @OptionsItem(R.id.menu_settings)
    public void openSettings() {
        Intent intent = new Intent(this, PreferencesActivity_.class);
        startActivity(intent);
    }

    @Click
    void buttonOpenHostDetails() {
        if (lastHost != null) {
            startActivity(hostIntentResolver.getDetailIntent(lastHost, this));
        }
    }

    @Click
    void buttonSwitch(View view) {
        currentView.setVisibility(View.GONE);
        if (currentView == panelDetails) {
            ((Button) view).setText(R.string.buttonSwitchDetails);
            currentView = panelEvents;
        } else {
            ((Button) view).setText(R.string.buttonSwitchEvents);
            currentView = panelDetails;
        }
        currentView.setVisibility(View.VISIBLE);
    }

    private void renderDetails(Host host) {
        textHostName.setText(host.getName());
        textStatus.setText(host.getStatus().toString());
        textCpuUsage.setText(String.format("%.2f%%", host.getCpuUsage()));
        textMemoryUsage.setText(String.format("%.2f%%", host.getMemoryUsage()));
        //show status icon
        HostStatus status = host.getStatus();
        imageStatus.setImageResource(status.getResource());

        panelParent.setVisibility(View.VISIBLE);
        currentView.setVisibility(View.VISIBLE);
        panelVms.setVisibility(View.VISIBLE);
    }

    private class HostsLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (lastResult == null) {
                return providerFacade.query(Host.class).id("0").asLoader();
            } else {
                return providerFacade.query(Host.class).id(lastResult).asLoader();
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null && data.getCount() > 0) {
                data.moveToFirst();
                lastHost = new Host();
                lastHost.initFromCursor(data);
                loaderManager.restartLoader(EVENTS_LOADER, null, cursorEventsAdapterLoader);
                loaderManager.restartLoader(VMS_LOADER, null, cursorVmsAdapterLoader);
                renderDetails(lastHost);
            } else if (lastResult != null) {
                resultTextView.setText(lastResult + '\n' + getString(R.string.no_such_host));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
