package mistNode.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.lang.reflect.Field;

import mistNode.Peer;
import mistNode.Errors;
import node.RequestInterface;

import static node.RequestInterface.bsonException;

/**
 * Created by jeppe on 26/07/16.
 */
class ControlInvoke {

    static void request(Peer peer, String epid, Control.InvokeCb callback) {
        send(peer, epid, null, null, null, null, null, null, callback);
    }

    static void request(Peer peer, String epid, String value, Control.InvokeCb callback) {
        send(peer, epid, value, null, null, null, null, null, callback);
    }

    static void request(Peer peer, String epid, Boolean value, Control.InvokeCb callback) {
        send(peer, epid, null, value, null, null, null, null, callback);
    }

    static void request(Peer peer, String epid, int value, Control.InvokeCb callback) {
        send(peer, epid, null, null, value, null, null, null, callback);
    }

    static void request(Peer peer, String epid, float value, Control.InvokeCb callback) {
        send(peer, epid, null, null, null, value, null, null, callback);
    }

    static void request(Peer peer, String epid, byte[] value, Control.InvokeCb callback) {
        send(peer, epid, null, null, null, null, value, null, callback);
    }

    static void request(Peer peer, String epid, Object value, Control.InvokeCb callback) {
        send(peer, epid, null, null, null, null, null, value, callback);
    }

    private static void send(Peer peer, String epid, String stringValue, Boolean boolValue, Integer intValue, Float floatValue, byte[] byteValue, Object objValue, Control.InvokeCb callback) {

        final String op = "mist.control.invoke";

        RequestInterface.Callback requestCb = new RequestInterface.Callback() {
            private Control.InvokeCb callback;

            @Override
            public void ack(byte[] dataBson) {
                Log.d("invokeCb:", "ack");
                response(dataBson);
                callback.end();
            }

            @Override
            public void sig(byte[] dataBson) {
                response(dataBson);
            }

            private void response(byte[] dataBson) {
                try {
                    BsonDocument bson = new RawBsonDocument(dataBson);
                    Log.d("invokeCb:", bson.getBsonType().toString());
                    if (bson.get("data").isBoolean()) {
                        callback.cbBoolean(bson.get("data").asBoolean().getValue());
                    } else if (bson.get("data").isInt32()) {
                        callback.cbInt(bson.get("data").asInt32().getValue());
                    } else if (bson.get("data").isDouble()) {
                        float value = (float) bson.get("data").asDouble().getValue();
                        callback.cbFloat(value);
                    } else if (bson.get("data").isString()) {
                        callback.cbString(bson.get("data").asString().getValue());
                    } else if (bson.get("data").isBinary()) {
                        callback.cbByte(bson.get("data").asBinary().getData());
                    } else if (bson.get("data").isArray()) {
                        callback.cbArray(bson.get("data").asArray());
                    } else if (bson.get("data").isDocument()) {
                        callback.cbDocument(bson.get("data").asDocument());
                    }
                } catch (BSONException e) {
                    Errors.mistError(op, bsonException, e.getMessage(), dataBson);
                    callback.err(bsonException, "bson error: " + e.getMessage());
                }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(op, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Control.InvokeCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback);


        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");

        writer.writeStartDocument();
        writer.writeBinaryData("luid", new BsonBinary(peer.getLocalId()));
        writer.writeBinaryData("ruid", new BsonBinary(peer.getRemoteId()));
        writer.writeBinaryData("rhid", new BsonBinary(peer.getRemoteHostId()));
        writer.writeBinaryData("rsid", new BsonBinary(peer.getRemoteServiceId()));
        writer.writeString("protocol", peer.getProtocol());
        writer.writeBoolean("online", peer.isOnline());
        writer.writeEndDocument();

        writer.writeString(epid);

        if (stringValue != null) {
            writer.writeString(stringValue);
        } else if (boolValue != null) {
            writer.writeBoolean(boolValue);
        } else if (intValue != null) {
            writer.writeInt32(intValue);
        } else if (floatValue != null) {
            writer.writeDouble(floatValue);
        } else if (byteValue != null) {
            writer.writeBinaryData(new BsonBinary(byteValue));
        } else if (objValue != null) {
            //TODO fix this
            writer.writeStartDocument();
            Class aClass = objValue.getClass();
            for (Field field : aClass.getDeclaredFields()) {
                if (field.getType().isPrimitive() || field.getType().getSimpleName().equals("String")) {
                    if (!field.getName().equals("serialVersionUID")) {
                        String type = field.getType().getSimpleName();
                        String name = field.getName();
                        try {
                            Object value = field.get(objValue);
                            switch (type) {
                                case "String":
                                    writer.writeString(name, (String) value);
                                    break;
                                case "boolean":
                                    writer.writeBoolean(name, (boolean) value);
                                    break;
                                case "float":
                                    writer.writeDouble(name, (float) value);
                                    break;
                                case "int":
                                    writer.writeInt32((int) value);
                                    break;
                            }
                        } catch (IllegalAccessException e) {
                            Log.d("filed e", e.toString());
                        }

                    }
                }
            }
            writer.writeEndDocument();
        }

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().mistApiRequest(op, buffer.toByteArray(), requestCb);

    }

}
