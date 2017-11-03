package newApi.wishRequest;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import node.MistNode;

import static newApi.wishRequest.Callback.BSON_ERROR_CODE;
import static newApi.wishRequest.Callback.BSON_ERROR_STRING;

/**
 * Created by jeppe on 11/28/16.
 */

class IdentityExport {
    static int request(byte[] uid, Identity.ExportCb callback) {
        final String op = "identity.export";

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
            Identity.ExportCb cb;

            @Override
            public void response(byte[] data) {
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    BsonDocument bsonDocument = bson.getDocument("data");

                    byte[] bsonData = bsonDocument.getBinary("data").getData();

                    BsonReader bsonReader = new BsonDocumentReader(bsonDocument);

                    BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
                    BsonWriter bsonWriter = new BsonBinaryWriter(outputBuffer);
                    bsonWriter.pipe(bsonReader);

                    cb.cb(bsonData, outputBuffer.toByteArray());
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

            private MistNode.RequestCb init(Identity.ExportCb callback) {
                this.cb = callback;
                return this;
            }

        }.init(callback));

    }
}
