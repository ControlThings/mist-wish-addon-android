package newApi.request;

import org.bson.BSONException;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import newApi.Peer;
import mistNodeApi.node.MistNode;

import static newApi.request.Callback.BSON_ERROR_CODE;
import static newApi.request.Callback.BSON_ERROR_STRING;

/**
 * Created by jeppe on 25/07/16.
 */
class ControlModel {
    static int request(Peer peer, Control.ModelCb callback) {
        final String op = "control.model";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");
        writer.writeEndArray();

        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();

        return MistNode.getInstance().request(peer.toBson(), buffer.toByteArray(), new MistNode.RequestCb() {
            Control.ModelCb cb;

            @Override
            public void response(byte[] data) {
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    BsonDocument bsonDocument = bson.get("data").asDocument();

                    BsonReader bsonReader = new BsonDocumentReader(bsonDocument);

                    BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
                    BsonWriter bsonWriter = new BsonBinaryWriter(outputBuffer);
                    bsonWriter.pipe(bsonReader);

                    cb.cb(outputBuffer.toByteArray());
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

            private MistNode.RequestCb init(Control.ModelCb callback) {
                this.cb = callback;
                return this;
            }

        }.init(callback));
    }
}
