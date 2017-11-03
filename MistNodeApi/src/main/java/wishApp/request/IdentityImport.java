package wishApp.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mistNode.Errors;
import node.RequestInterface;

import static node.RequestInterface.bsonException;

class IdentityImport {
    static void request(byte[] identity, byte[] localUid, Identity.ImportCb callback) {
        final String op = "identity.import";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");

        writer.writeBinaryData(new BsonBinary(identity));

        writer.writeBinaryData(new BsonBinary(localUid));

        writer.writeString("binary");

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Identity.ImportCb callback;

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
                    BsonDocument bsonData = bson.get("data").asDocument();
                    String importAlias = bsonData.get("alias").asString().getValue();
                    byte[] userId = bsonData.get("uid").asBinary().getData();
                    callback.cb(importAlias, userId);
                } catch (BSONException e) {
                    Errors.wishError(op, bsonException, e.getMessage(), dataBson);
                    callback.err(bsonException, "bson error: " + e.getMessage());
                }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(op, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Identity.ImportCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}
