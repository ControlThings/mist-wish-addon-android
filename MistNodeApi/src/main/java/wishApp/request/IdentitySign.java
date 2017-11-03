package wishApp.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import wishApp.Connection;
import mistNode.Errors;
import node.RequestInterface;

import static node.RequestInterface.bsonException;

/**
 * Created by jeppe on 11/28/16.
 */

class IdentitySign {
    static void request(Connection connection, byte[] id, BsonDocument cert, Identity.SignCb callback) {
        final String signOp = "identity.sign";
        String op = signOp;

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");

        writer.writeBinaryData(new BsonBinary(id));

        BsonDocumentReader bsonDocumentReader = new BsonDocumentReader(cert);
        writer.pipe(bsonDocumentReader);

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        if (connection != null) {
            op = ConnectionRequest.getOp();
            buffer = ConnectionRequest.getBuffer(connection, signOp, new ConnectionRequest.GetRequestArgs() {

                private byte[] id;
                private BsonDocument cert;

                @Override
                public void args(BsonWriter writer) {
                    writer.writeBinaryData(new BsonBinary(id));
                    BsonDocumentReader bsonDocumentReader1 = new BsonDocumentReader(cert);
                    writer.pipe(bsonDocumentReader1);
                }

                private ConnectionRequest.GetRequestArgs init (byte[] id, BsonDocument cert) {
                    this.id = id;
                    this.cert = cert;
                    return this;
                }

            }.init(id, cert));
        }
        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Identity.SignCb callback;

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

                    //hack to get rid of double data fields
                    BsonDocumentReader reader = new BsonDocumentReader(bsonData);
                    BasicOutputBuffer buffer = new BasicOutputBuffer();
                    BsonWriter writer = new BsonBinaryWriter(buffer);
                    writer.pipe(reader);
                    writer.flush();

                    callback.cb(buffer.toByteArray());
                } catch (BSONException e) {
                    Errors.wishError(signOp, bsonException, e.getMessage(), dataBson);
                    callback.err(bsonException, "bson error: " + e.getMessage());
                }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(signOp, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Identity.SignCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }



}
