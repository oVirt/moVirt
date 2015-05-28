package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.camera.CameraPreview;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
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
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCamera != null) mCamera.release();
    }

    public static Camera getCameraInstance(Context context){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}
