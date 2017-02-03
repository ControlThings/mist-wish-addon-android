package mist;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

import mist.node.EndpointInt;
import mist.node.EndpointString;
import mist.node.NodeModel;
import mist.node.EndpointBoolean;

/**
 * Created by jeppe on 9/12/16.
 */
public class FlashLight {

    private final String TAG = "FlashLight";

    Context _context;

    // Camera API
    private CameraManager cameraManager;

    // old Camera API
    private Camera camera = null;
    private Camera.Parameters parameters;

    private Boolean isTorchOn = false;
    private String cameraId;

    // Mist device/node model
    NodeModel model;
    // Mist endpoint
    private EndpointBoolean lightOn;
    private EndpointString myStringEndpoint;
    private EndpointInt myIntEndpoint;


    public FlashLight(Context context) {
        this._context = context;
        initCameraId();

        model = new NodeModel("Light", context);

        lightOn = new EndpointBoolean("torch", "Torch");

        lightOn.setWritable(new EndpointBoolean.Writable() {
            @Override
            public void write(boolean value) {
                if (value) {
                    turnOnFlashLight();
                } else {
                    turnOffFlashLight();
                }

                // update value in Mist model
                lightOn.update(value);
            }
        });




        myStringEndpoint = new EndpointString("test_str", "Test String");
        myStringEndpoint.setWritable(new EndpointString.Writable() {
            @Override
            public void write(String newValue) {
                Log.d(TAG, "Writing string here:" + newValue);
                myStringEndpoint.update(newValue);
            }
        });
        myStringEndpoint.setReadable(true);
        lightOn.addNext(myStringEndpoint);

        myIntEndpoint = new EndpointInt("test_int", "Test int");
        myIntEndpoint.setWritable(new EndpointInt.Writable() {
            @Override
            public void write(int newValue) {
                Log.d(TAG, "Writing string here:" + newValue);
                myIntEndpoint.update(newValue);
            }
        });
        myIntEndpoint.setReadable(true);
        lightOn.addNext(myIntEndpoint);


        model.setRootEndpoint(lightOn);

    }

    private void turnOnFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, true);
            } else {
                camera = Camera.open();
                parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                camera.startPreview();
            }
            isTorchOn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void turnOffFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, false);
            } else {
                if (camera != null) {
                    parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
            isTorchOn = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initCameraId() {
        Boolean isFlashAvailable = _context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (isFlashAvailable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager = (CameraManager) _context.getSystemService(Context.CAMERA_SERVICE);
                try {
                    cameraId = cameraManager.getCameraIdList()[0];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // use old api, no need for camera id
            }
        } else {
            cleanup();
            return;
        }
    }

    public void cleanup() {
        if (isTorchOn) {
            turnOffFlashLight();
        }
        if (camera != null) {
            camera.release();
            camera = null;
            parameters = null;
        }
    }
}
