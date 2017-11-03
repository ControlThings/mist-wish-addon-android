package wishApp.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mistNode.Errors;
import node.RequestInterface;


/**
 * Created by jan on 8/30/17.
 */

class HostConfig {
    static void request(Host.ConfigCb callback) {
        final String op = "host.config";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");
        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        final int id = RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Host.ConfigCb callback;

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
                    BsonDocument configDocument = bson.get("data").asDocument();
                    callback.cb(configDocument.get("version").asString().getValue(), configDocument.get("hid").asBinary().getData());
                } catch (BSONException e) {
                    Errors.wishError(op, 333, e.getMessage(), dataBson);
                    callback.err(333, "bson error: " + e.getMessage());
                }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(op, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Host.ConfigCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}
