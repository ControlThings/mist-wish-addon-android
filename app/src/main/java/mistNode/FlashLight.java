package mistNode;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mistNode.request.Control;
import newApi.Peer;


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
    private Endpoint testInvoke;
    private Endpoint readingWillProduceError;
    private Endpoint invokingSendsControlRead;
    private Endpoint testWishRequest;

    public FlashLight(Context context) {
        this._context = context;
        initCameraId();

        /* FIXME this might be moved to Mist.java:onCreate */
       // node.MistNode.getInstance().startMistApp("Flashlight", context);

        mist = new Endpoint("mist").setRead(new Endpoint.ReadableString() {
            @Override
            public void read(Peer peer, Endpoint.ReadableStringResponse response) {
                response.send("foo");
            }
        });
        mistName = new Endpoint("mist.name").setRead(new Endpoint.ReadableString() {
            @Override
            public void read(Peer peer, Endpoint.ReadableStringResponse response) {
                response.send("Flashlight");
            }
        });
        MistNode.getInstance().addEndpoint(mist);
        MistNode.getInstance().addEndpoint(mistName);

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
        MistNode.getInstance().addEndpoint(lightEndpoint);
        testInvoke = new Endpoint("testInvoke").setInvoke(new Endpoint.Invokable() {
            @Override
            public void invoke(byte[] args, Peer peer, Endpoint.InvokeResponse response) {
                BasicOutputBuffer buffer = new BasicOutputBuffer();
                BsonWriter writer = new BsonBinaryWriter(buffer);

                writer.writeStartDocument();
                writer.writeBinaryData("testArray", new BsonBinary(new byte[3]));
                if (args != null) {
                    writer.writeString("feedback", "The args you supplied was " + args.length + " bytes long.");
                }
                else {
                    writer.writeString("feedback", "The args you supplied was null!");
                }
                writer.writeEndDocument();
                writer.flush();

                response.send(buffer.toByteArray());
            }
        });
        MistNode.getInstance().addEndpoint(testInvoke);
        readingWillProduceError = new Endpoint("readingWillProduceError").setRead(new Endpoint.ReadableFloat() {
            @Override
            public void read(Peer peer, Endpoint.ReadableFloatResponse response) {
                response.error(67, "Error 67 - I am so sorry.");
            }
        });
        MistNode.getInstance().addEndpoint(readingWillProduceError);
        invokingSendsControlRead = new Endpoint("reqTest").setInvoke(new Endpoint.Invokable() {
            @Override
            public void invoke(byte[] args, Peer peer, final Endpoint.InvokeResponse response) {
                Log.d(TAG, "now testing request api");

                mistNode.Peer p = new mistNode.Peer();


                p.setLocalId(peer.getLuid());
                p.setRemoteHostId(peer.getRhid());
                p.setRemoteServiceId(peer.getRsid());
                p.setRemoteId(peer.getRuid());
                p.setOnline(peer.isOnline());
                p.setProtocol(peer.getProtocol());


                Control.read(p, "mist.name", new Control.ReadCb() {
                    @Override
                    public void cbBoolean(Boolean data) {

                    }

                    @Override
                    public void cbInt(int data) {

                    }

                    @Override
                    public void cbFloat(double data) {

                    }

                    @Override
                    public void cbString(String data) {
                        Log.d(TAG, "cbString: " + data);
                        BasicOutputBuffer buffer = new BasicOutputBuffer();
                        BsonWriter writer = new BsonBinaryWriter(buffer);

                        writer.writeStartDocument();
                        writer.writeString("data", "mist.name is: " + data);
                        writer.writeEndDocument();
                        writer.flush();
                        response.send(buffer.toByteArray());
                    }

                    @Override
                    public void err(int code, String msg) {

                    }

                    @Override
                    public void end() {

                    }
                });

              /*  Control.read(peer, "mist.name", new Control.ReadCb() {
                    @Override
                    public void cbString(String data) {
                        Log.d(TAG, "cbString: " + data);
                        BasicOutputBuffer buffer = new BasicOutputBuffer();
                        BsonWriter writer = new BsonBinaryWriter(buffer);

                        writer.writeStartDocument();
                        writer.writeString("data", "mist.name is: " + data);
                        writer.writeEndDocument();
                        writer.flush();
                        response.send(buffer.toByteArray());
                    }

                }); */

            }
        });

        MistNode.getInstance().addEndpoint(invokingSendsControlRead);
        testWishRequest = new Endpoint("wishReqTest").setRead(new Endpoint.ReadableString() {
            @Override
            public void read(Peer peer, final Endpoint.ReadableStringResponse response) {
                BasicOutputBuffer buffer = new BasicOutputBuffer();
                BsonWriter writer = new BsonBinaryWriter(buffer);

                if (peer != null) {


                    writer.writeStartDocument();
                    writer.writeString("op", "identity.get");
                    writer.writeStartArray("args");
                    writer.writeBinaryData(new BsonBinary(peer.getRuid()));
                    writer.writeEndArray();
                    writer.writeInt32("id", 0);
                    writer.writeEndDocument();
                    writer.flush();
                    MistNode.getInstance().wishRequest(buffer.toByteArray(), new MistNode.RequestCb() {
                        @Override
                        public void response(byte[] data) {
                            Log.d(TAG, "we got a response of len " + data.length);

                            BsonDocument bsonDocument = new RawBsonDocument(data);
                            if (bsonDocument.containsKey("data")) {
                                BsonDocument inner = bsonDocument.get("data").asDocument();
                                String alias = inner.get("alias").asString().getValue();
                                response.send("Your alias is " + alias);
                            }
                        }

                        @Override
                        public void end() {
                            Log.d(TAG, "we (end)");
                        }

                        @Override
                        public void err(int code, String msg) {
                            Log.d(TAG, "we got error: " + msg + " code " + code);
                        }
                    });

                }
            }
        });
        MistNode.getInstance().addEndpoint(testWishRequest);
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
