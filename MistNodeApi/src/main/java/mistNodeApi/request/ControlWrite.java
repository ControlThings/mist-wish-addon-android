package mistNodeApi.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mistNodeApi.Peer;
import mistNodeApi.Errors;
import mistNodeApi.RequestInterface;

import static mistNodeApi.RequestInterface.bsonException;

class ControlWrite {

    static void request(Peer peer, String epid, Boolean state, Control.WriteCb callback) {
        send(peer, epid, state, null, null, null, callback);
    }

    static void request(Peer peer, String epid, int state, Control.WriteCb callback) {
        send(peer, epid, null, state, null, null, callback);
    }

    static void request(Peer peer, String epid, double state, Control.WriteCb callback) {
        send(peer, epid, null, null, state, null, callback);
    }

    static void request(Peer peer, String epid, String state, Control.WriteCb callback) {
        send(peer, epid, null, null, null, state, callback);
    }

    private static void send(Peer peer, String epid, Boolean boolState, Integer intState, Double floatState, String stringState, Control.WriteCb callback) {
        final String op = "mist.control.write";

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

        if (boolState != null) {
            writer.writeBoolean(boolState);
        } else if (intState != null) {
            writer.writeInt32(intState);
        } else if (floatState != null) {
            writer.writeDouble(floatState);
        } else if (stringState != null) {
            writer.writeString(stringState);
        }

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().mistApiRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Control.WriteCb callback;

            @Override
            public void ack(byte[] dataBson) {
                response(dataBson);
                callback.end();
            }

            @Override
            public void sig(byte[] dataBson) {
                response(dataBson);
            }

            private void response(byte[] dataBson) {
                try {
                    BsonDocument bson = null;
                    try {
                        bson = new RawBsonDocument(dataBson);
                    } catch (Exception e) {
                        Log.d(op, "Error: ControlWrite response not bson, but we will call success callback anyway.");
                        callback.cb(true);
                        return;
                    }

                    if (bson.get("data") != null && bson.get("data").isBoolean()) {
                        callback.cb(bson.get("data").asBoolean().getValue());
                    } else {
                        Log.d(op, "TODO Fix write response according to specs.");
                        callback.cb(true);
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

            private RequestInterface.Callback init(Control.WriteCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}
