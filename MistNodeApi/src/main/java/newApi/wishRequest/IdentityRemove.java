package newApi.wishRequest;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import node.MistNode;

import static newApi.wishRequest.Callback.BSON_ERROR_CODE;
import static newApi.wishRequest.Callback.BSON_ERROR_STRING;


class IdentityRemove {
    static int request(byte[] uid, Identity.RemoveCb callback) {
        final String op = "identity.remove";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");
        writer.writeBinaryData(new BsonBinary(uid));
        writer.writeEndArray();

        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();

        return MistNode.getInstance().wishRequest(buffer.toByteArray(), new MistNode.RequestCb() {
            Identity.RemoveCb cb;

            @Override
            public void response(byte[] data) {
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    boolean value = bson.getBoolean("data").getValue();
                    cb.cb(value);
                } catch (BSONException e) {
                    cb.err(BSON_ERROR_CODE, BSON_ERROR_STRING);
                }
            }

            @Override
            public void end() {
                cb.end();
            }

            @Override
            public void err(int code, String msg) {
                cb.err(code, msg);
            }

            private MistNode.RequestCb init(Identity.RemoveCb callback) {
                this.cb = callback;
                return this;
            }

        }.init(callback));
    }
}
