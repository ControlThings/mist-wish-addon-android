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

/**
 * Created by jeppe on 11/28/16.
 */

class IdentityExport {
    static void request(Connection connection, byte[] id, Identity.ExportCb callback) {
        final String exportOp = "identity.export";
        String op = exportOp;

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");

        writer.writeBinaryData(new BsonBinary(id));

        writer.writeString("binary");

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        if (connection != null) {
            op = ConnectionRequest.getOp();
            buffer = ConnectionRequest.getBuffer(connection, exportOp, new ConnectionRequest.GetRequestArgs() {

                private byte[] id;

                @Override
                public void args(BsonWriter writer) {
                    writer.writeBinaryData(new BsonBinary(id));
                }

                private ConnectionRequest.GetRequestArgs init (byte[] id) {
                    this.id = id;
                    return this;
                }

            }.init(id));
        }

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Identity.ExportCb callback;

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
                    BsonDocument bsonData = bson.getDocument("data");
                    byte[] data = bsonData.get("data").asBinary().getData();

                    callback.cb(data, dataBson);
                } catch (BSONException e) {
                    Errors.wishError(exportOp, 333, e.getMessage(), dataBson);
                    callback.err(333, "bson error..: " + e.getMessage());
                }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(exportOp, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Identity.ExportCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}
