package mistNodeApi.wishRequest;


import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mistNodeApi.MistIdentity;
import mistNodeApi.Errors;
import mistNodeApi.RequestInterface;

/**
 * Created by jeppe on 8/23/16.
 */
class IdentityCreate {
    static void request(String alias, Identity.CreateCb callback) {
        final String op = "identity.create";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();



        writer.writeStartArray("args");

        writer.writeString(alias);

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Identity.CreateCb callback;

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
                    BsonDocument bsonIdentity = bson.get("data").asDocument();
                    MistIdentity identity = new MistIdentity();
                    identity.setAlias(bsonIdentity.get("alias").asString().getValue());
                    identity.setUid(bsonIdentity.get("uid").asBinary().getData());
                    identity.setPrivkey(bsonIdentity.get("privkey").asBoolean().getValue());
                    callback.cb(identity);
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

            private RequestInterface.Callback init(Identity.CreateCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}
