package mistNodeApi.wishRequest;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mistNodeApi.Connection;
import mistNodeApi.Errors;
import mistNodeApi.RequestInterface;

import static mistNodeApi.RequestInterface.bsonException;

class IdentityRemove {
    static void request(Connection connection, byte[] uid, Identity.RemoveCb callback) {
        final String removeOp = "identity.remove";
        String op = removeOp;

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");

        writer.writeBinaryData(new BsonBinary(uid));

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        if (connection != null) {
            op = ConnectionRequest.getOp();
            buffer = ConnectionRequest.getBuffer(connection, removeOp, new ConnectionRequest.GetRequestArgs() {

                private byte[] uid;

                @Override
                public void args(BsonWriter writer) {
                    writer.writeBinaryData(new BsonBinary(uid));
                }

                private ConnectionRequest.GetRequestArgs init (byte[] uid) {
                    this.uid = uid;
                    return this;
                }

            }.init(uid));
        }

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Identity.RemoveCb callback;

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
                    Errors.wishError(removeOp, bsonException, e.getMessage(), dataBson);
                    callback.err(bsonException, "bson error: " + e.getMessage());
                }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(removeOp, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Identity.RemoveCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}
