package mistNode.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import mistNode.Peer;
import mistNode.Errors;
import node.RequestInterface;

import static node.RequestInterface.bsonException;

/**
 * Created by jeppe on 25/07/16.
 */
class ControlModel {
    static void request(Peer peer, Control.ModelCb callback) {
        final String op = "mist.control.model";

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

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().mistApiRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Control.ModelCb callback;

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
                    BsonDocument bsonDocument = bson.get("data").asDocument();
                    try {
                        callback.cb(new JSONObject(bsonDocument.toJson()));
                    } catch (JSONException e) {
                        Log.d(op, "Json parsing error: " + e);
                        return;
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

            private RequestInterface.Callback init(Control.ModelCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }


}
