package mistNodeApi.wishRequest;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonArray;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.ArrayList;

import mistNodeApi.Connection;
import mistNodeApi.Errors;
import mistNodeApi.MistIdentity;
import mistNodeApi.RequestInterface;

import static mistNodeApi.RequestInterface.bsonException;

class IdentityList {
    static void request(Connection connection, Identity.ListCb callback) {
        final String listOp = "identity.list";
        String op = listOp;

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");
        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        if (connection != null) {
            op = ConnectionRequest.getOp();
            buffer = ConnectionRequest.getBuffer(connection, listOp, new ConnectionRequest.GetRequestArgs() {
                @Override
                public void args(BsonWriter writer) {
                }
             });
        }

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Identity.ListCb callback;

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
                    ArrayList<MistIdentity> identityList = new ArrayList<MistIdentity>();
                    BsonArray bsonIdentityList = new BsonArray(bson.getArray("data"));
                    for (BsonValue listValue : bsonIdentityList) {
                        MistIdentity identity = new MistIdentity();
                        identity.setAlias(listValue.asDocument().get("alias").asString().getValue());
                        identity.setUid(listValue.asDocument().get("uid").asBinary().getData());
                        identity.setPrivkey(listValue.asDocument().get("privkey").asBoolean().getValue());
                        identityList.add(identity);
                    }
                    callback.cb(identityList);
                } catch (BSONException e) {
                    Errors.wishError(listOp, bsonException, e.getMessage(), dataBson);
                    callback.err(bsonException, "bson error: " + e.getMessage());
                }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(listOp, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Identity.ListCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}
