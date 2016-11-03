package fi.ct.mist.referencelibrary.api.node;

import android.util.Log;

import org.bson.BsonArray;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jan on 6/2/16.
 */
public class NodeApiJni {

    private final String TAG = "NodeApiJni";

    private Map<Integer, byte[]> followMap = new HashMap<Integer, byte[]>();


    NodeApi nodeApi;

    static {
        System.loadLibrary("mist");
    }


    public synchronized native void register();

    public synchronized native void result(byte[] data, byte[] peerDoc);


    public NodeApiJni() {
        Log.d(TAG, "DeviceApiJni started");
        nodeApi = new NodeApi(this);
        register();
    }

    public void deviceOnFrame(byte[] request, byte[] peerDoc) {
        Log.d(TAG, "in deviceOnFrame");
        try {
            BsonDocument bson = new RawBsonDocument(request);
            String op = bson.get("op").asString().getValue();
            switch (op) {
                case "control.write":
                    onWrite(bson, peerDoc);
                    break;
                case "control.model":
                    onModel(bson, peerDoc);
                    break;
                case "control.read":
                    onRead(bson, peerDoc);
                    break;
                case "control.follow":
                    onFollow(bson, peerDoc);
                    break;
                default:
                    Log.d(TAG, "op " + op + " not implemented");
            }


        } catch (Exception e) {
            Log.d(TAG, "Bson serialize error in deviceOnFrame: " + e);
            Log.d(TAG, "Stack trace:" + Log.getStackTraceString(e));
            return;
        }
    }

    public void notifyValueChange(String epName) {

        Endpoint endpoint = nodeApi.getEndpointByName(epName);


        for (Map.Entry<Integer, byte[]> entry: followMap.entrySet()) {
            Integer id = entry.getKey();
            byte[] peer = entry.getValue();
            BasicOutputBuffer buffer = new BasicOutputBuffer();
            try {
                BsonWriter writer = new BsonBinaryWriter(buffer);
                writer.writeStartDocument();
                writer.writeInt32("sig", id);
                writer.writeStartDocument("data");
                writer.writeString("id", epName);
        /* Encode the value of the endpoint as element named 'data' */
                if (endpoint.getType() == Endpoint.bool) {
                   // Log.d(TAG, "notify bool");
                    writer.writeBoolean("data", endpoint.getBoolean());
                }
                if (endpoint.getType() == Endpoint.integer) {
                   // Log.d(TAG, "notify integer");
                    writer.writeInt32("data", endpoint.getInt());
                }
                if (endpoint.getType() == Endpoint._float) {
                   // Log.d(TAG, "notify float");
                    writer.writeDouble("data", endpoint.getFloat());
                }
                writer.writeEndDocument();
                writer.writeEndDocument();
                writer.flush();
            } catch (Exception e) {
                Log.d(TAG, "Bson serialize error in notify: " + e);
                e.printStackTrace();
                return;
            }
            Log.d(TAG, "sending notify to sig " + id + " peer: " + peer);
        /* Send the result downwards */
            result(buffer.toByteArray(), peer);

        }
    }

    /**
     * This function is called by Mist C99, when it needs to fulfill a control.read request sent by an other peer.
     *
     * @param readRequest is a BSON document, something like:
     *                    { args: [ 'lamp.state' ],
     *                    id: 3
     *                    }
     *                    <p/>
     *                    When returning the result, we call DeviceApiJni.result(data), where data is a BSON document with the id, and the value of the endpoint., like:
     *                    {
     *                    data:  true,   // Assuming that lamp.state is a boolean endpoint, whose value is 'true', Note that 'data' element will directly have the value encoded. (and not packed inside an array)
     *                    ack: 3
     *                    }
     * @param peerDoc     a byte array containing a BSON document representing the peer which originated the request
     */
    private void onRead(BsonDocument readRequest, byte[] peerDoc) {
        Log.d(TAG, "On read");
        int id;
        String endpointName;
        try {
            id = readRequest.get("id").asInt32().getValue();
            BsonArray bsonArgs = new BsonArray(readRequest.getArray("args"));
            endpointName = bsonArgs.get(0).asString().getValue();
        } catch (Exception e) {
            Log.d(TAG, "Bson serialize error in onWrite: " + e);
            return;
        }

        Endpoint endpoint = nodeApi.getEndpointByName(endpointName);

        if (endpoint == null) {
            Log.d(TAG, "could not find endpoint: " + endpointName);
            return;
        }

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);

        writer.writeStartDocument();
        writer.writeInt32("ack", id);

        /* Encode the value of the endpoint as element named 'data' */
        if (endpoint.getType() == Endpoint.bool) {
            writer.writeBoolean("data", endpoint.getBoolean());
        }
        if (endpoint.getType() == Endpoint.integer) {
            writer.writeInt32("data", endpoint.getInt());
        }
        if (endpoint.getType() == Endpoint._float) {
            writer.writeDouble(endpoint.getFloat());
        }
        writer.writeEndDocument();
        writer.flush();

        /* Send the result downwards */
        result(buffer.toByteArray(), peerDoc);
    }


    /**
     * This function is called by Mist C99 when it needs to fulfill a control.write request.
     *
     * @param writeRequest a BSON document, something like:
     *                     {
     *                     args: [ 'lamp.state', true ]
     *                     id: 45 //optional.
     *                     }
     *                     <p/>
     *                     The id is optional. If id is present, then call DeviceApiJni.result(data), where data is a BSON document:
     *                     {
     *                     ack: 45
     *                     }
     * @param peerDoc      a byte array containing a BSON document representing the peer which originated the request
     */
    private void onWrite(BsonDocument writeRequest, byte[] peerDoc) {

        Integer id = null;
        String endpointName;
        BsonArray bsonArgs;
        try {
            if (writeRequest.containsKey("id")) {
                id = writeRequest.get("id").asInt32().getValue();
            }
            bsonArgs = new BsonArray(writeRequest.getArray("args"));
            endpointName = bsonArgs.get(0).asString().getValue();
        } catch (Exception e) {
            Log.d(TAG, "Bson deserialize error in onWrite: " + e);
            return;
        }

        Endpoint endpoint = nodeApi.getEndpointByName(endpointName);
        if (endpoint == null) {
            Log.d(TAG, "could not find endpoint: " + endpointName);
            return;
        }

        if (endpoint.getType() == Endpoint.bool) {
            try {
                boolean data = bsonArgs.get(1).asBoolean().getValue();
                endpoint.updateBoolean(data);
            } catch (Exception e) {
                Log.d(TAG, "Bson deserialize error in onWrite: " + e);
                return;
            }
        }
        if (endpoint.getType() == Endpoint.integer) {
            try {
                int data = bsonArgs.get(1).asInt32().getValue();
                endpoint.updateInt(data);
            } catch (Exception e) {
                Log.d(TAG, "Bson deserialize error3 in onWrite: " + e);
                return;
            }
        }
        if (endpoint.getType() == Endpoint._float) {
            try {
                float data = (float) bsonArgs.get(1).asDouble().getValue();
                endpoint.updateFloat(data);
            } catch (Exception e) {
                Log.d(TAG, "Bson deserialize error4 in onWrite: " + e);
                return;
            }
        }

        if (id != null) {
            try {
                BasicOutputBuffer buffer = new BasicOutputBuffer();
                BsonWriter writer = new BsonBinaryWriter(buffer);
                writer.writeStartDocument();
                writer.writeInt32("ack", id);
                writer.writeStartArray("args");
                writer.writeBoolean(true);
                writer.writeEndArray();
                writer.writeEndDocument();
                writer.flush();
                result(buffer.toByteArray(), peerDoc);
            } catch (Exception e) {
                Log.d(TAG, "Bson serialize error5: " + e);
                return;
            }
        }
    }

    private void onModel(BsonDocument modelRequest, byte[] peerDoc) {

        int id;
        try {
            id = modelRequest.get("id").asInt32().getValue();
        } catch (Exception e) {
            Log.d(TAG, "Bson serialize error in onWrite: " + e);
            return;
        }

        Map<String, Endpoint> endpointMap = nodeApi.getEndpoints();

        BasicOutputBuffer buffer;
        buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeInt32("ack", id);
        writer.writeStartDocument("data");
        writer.writeString("device", nodeApi.getName());
        writer.writeStartDocument("model");
        if (nodeApi.getUrl() != null) {
            writer.writeStartDocument("mist");
            writer.writeString("type", "meta");
            writer.writeString("tag", "mist-ui-package");
            writer.writeString("value", nodeApi.getUrl());
            writer.writeEndDocument();
        }
        for (String key : endpointMap.keySet()) {
            Endpoint endpoint = endpointMap.get(key);
            try {
                writer.writeStartDocument(key);
                writer.writeString("type", endpoint.getTypeAsString());
                if (endpoint._lable != null) {
                    writer.writeString("label", endpoint._lable);
                }
                if (endpoint._unit != null) {
                    writer.writeString("unit", endpoint._unit);
                }
                writer.writeBoolean("read", endpoint._read);
                writer.writeBoolean("write", endpoint._write);
                writer.writeBoolean("invoke", endpoint._invoke);
                writer.writeEndDocument();

            } catch (Exception e) {
                Log.d(TAG, "Bson serialize error (onModel): " + e);
                return;
            }
        }
        writer.writeEndDocument();
        writer.writeEndDocument();
        writer.writeEndDocument();

        writer.flush();
        Log.d(TAG, "sending model result id: " + id);
        result(buffer.toByteArray(), peerDoc);

        // Pars pars = new Pars();
        // MistModel model = pars.parseModel(new RawBsonDocument(buffer.toByteArray()));

        // Log.d(TAG, "TEST MODEL: " + model);
    }

    private void onFollow(BsonDocument followRequest, byte[] peerDoc) {
        Log.d(TAG, "On follow");

        int id;
        String endpointName;

        try {
            id = followRequest.get("id").asInt32().getValue();
        } catch (Exception e) {
            Log.d(TAG, "Bson serialize error in onFollow: " + e);
            e.printStackTrace();
            return;
        }

        followMap.put(id, peerDoc);
        //peerMap.put(endpointName, peerDoc);



        for (String epid : nodeApi.getEndpoints().keySet()){
            notifyValueChange(epid);
        }

       // notifyValueChange(endpointName);
    }


    public NodeApi getNodeApi() {
        return nodeApi;
    }

    public void cleanup() {

    }
}
