package mistNodeApi.wishRequest;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mistNodeApi.Errors;
import mistNodeApi.RequestInterface;



/**
 * Created by jeppe on 10/18/16.
 */
class ConnectionDisconnect {
    static void request(int cid, Connection.DisconectCb callback) {
        final String op = "connections.disconnect";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");

        writer.writeInt32(cid);

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Connection.DisconectCb callback;

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
                    callback.cb(bson.get("data").asBoolean().getValue());
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

            private RequestInterface.Callback init(Connection.DisconectCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }


}
