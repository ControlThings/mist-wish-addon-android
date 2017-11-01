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

/**
 * Created by jeppe on 26/07/16.
 */
class ControlRead {
    static void request(Peer peer, String epid, Control.ReadCb callback) {
        final String op = "mist.control.read";

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

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().mistApiRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Control.ReadCb callback;

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
                    BsonDocument bson = new RawBsonDocument(dataBson);
                    if (bson.get("data").isBoolean()) {
                        callback.cbBoolean(bson.get("data").asBoolean().getValue());
                    } else if (bson.get("data").isInt32()) {
                        callback.cbInt(bson.get("data").asInt32().getValue());
                    } else if (bson.get("data").isDouble()) {
                        float value = (float) bson.get("data").asDouble().getValue();
                        callback.cbFloat(value);
                    } else if (bson.get("data").isString()) {
                        callback.cbString(bson.get("data").asString().getValue());
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

            private RequestInterface.Callback init(Control.ReadCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}
