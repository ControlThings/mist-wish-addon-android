package mist;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import mist.node.Endpoint;
import mist.node.MistNode;

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

    private boolean isTorchOn = false;
    private String cameraId;

    private Endpoint mist;
    private Endpoint mistName;
    private Endpoint lightEndpoint;
    private Endpoint myStringEndpoint;
    private Endpoint myIntEndpoint;


    public FlashLight(Context context) {
        this._context = context;
        initCameraId();

        /* FIXME this might be moved to Mist.java:onCreate */
        MistNode.getInstance().startMistApp("Flashlight", context);

        mist = new Endpoint("mist");
        mistName = new Endpoint("mist.name").setRead(new Endpoint.ReadableString() {
            @Override
            public void read(Peer peer, Endpoint.ReadableStringResponse response) {
                response.send("Flashlight");
            }
        });

        lightEndpoint = new Endpoint("light")
                .setRead(new Endpoint.ReadableBool() {
                    @Override
                    public void read(Peer peer, Endpoint.ReadableBoolResponse response) {
                        response.send(isTorchOn);
                    }
                })
                .setLabel("The light's state")
                .setWrite(new Endpoint.WritableBool() {
                    @Override
                    public void write(boolean value, Peer peer, Endpoint.WriteResponse response) {
                        if (value) {
                            turnOnFlashLight();
                        }
                        else {
                            turnOffFlashLight();
                        }

                        response.send();

                    }
                });

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
            lightEndpoint.changed();
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
            lightEndpoint.changed();
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
