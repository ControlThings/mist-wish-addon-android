package mistNodeApi.wishRequest;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mistNodeApi.MistIdentity;
import mistNodeApi.Errors;
import mistNodeApi.RequestInterface;

import static mistNodeApi.RequestInterface.bsonException;

class IdentityGet {
    static void request(byte[] id, Identity.GetCb callback) {
        final String op = "identity.get";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");
        writer.writeBinaryData(new BsonBinary(id));
        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Identity.GetCb callback;

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
                    BsonDocument bsonDocument = bson.getDocument("data");
                    MistIdentity identity = new MistIdentity();
                    identity.setAlias(bsonDocument.get("alias").asString().getValue());
                    identity.setUid(bsonDocument.get("uid").asBinary().getData());
                    identity.setPrivkey(bsonDocument.get("privkey").asBoolean().getValue());
                    callback.cb(identity);
                } catch (BSONException e) {
                    Errors.wishError(op, bsonException, e.getMessage(), dataBson);
                    callback.err(bsonException, "bson error: " + e.getMessage());
                }


            }

            @Override
            public void err(int code, String msg) {
                Log.e(op, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Identity.GetCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}
