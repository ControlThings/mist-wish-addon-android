package fi.ct.mist.reference;


import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;

import fi.ct.mist.referencelibrary.api.mistNode.DeviceModel;
import fi.ct.mist.referencelibrary.api.mistNode.EndpointBoolean;

public class Torch {

    private final String TAG = "Torch";


    Context _context;
    private Camera camera = null;
    private boolean isFlashOn;
    Parameters parameters;

    private EndpointBoolean endpointBoolean;

    public Torch(Context context) {
        this._context = context;

        DeviceModel model = new DeviceModel("Torch");
        final EndpointBoolean reflectorEndpoint = new EndpointBoolean("reflection", "Reflection1");


        endpointBoolean = new EndpointBoolean("torch", "Torch");
        endpointBoolean.setWritable(new EndpointBoolean.Writable() {
            @Override
            public void write(boolean value) {
                if (value) {
                    //turnFlashOn();
                    Log.d(TAG, "Turning torch on!");

                }
                else  {
                    //turnFlashOff();
                    Log.d(TAG, "Turning torch on!");
                }
                endpointBoolean.update(value);
                reflectorEndpoint.update(value);
            }
        });
        endpointBoolean.addNext(reflectorEndpoint);
        model.setRootEndpoint(endpointBoolean);


        boolean hasFlash = context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            cleanup();
            return;
        }
    }

    private void turnFlashOn() {
        camera = Camera.open();
        parameters= camera.getParameters();
        parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
        camera.startPreview();
        isFlashOn = true;
        endpointBoolean.update(true);
    }

    private void turnFlashOff() {
       // camera = Camera.open();
        if (camera != null) {
            parameters = camera.getParameters();
            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.stopPreview();
            camera.release();
            camera = null;
            isFlashOn = false;
            endpointBoolean.update(false);
        }
    }

    public void cleanup() {
        if (camera != null) {
            camera.release();
            camera = null;
            parameters = null;
        }
        //_deviceApi.deleteEndpoint(endpoint.getName());
    }


}
