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
import mistNodeApi.Friend;
import mistNodeApi.RequestInterface;

import static mistNodeApi.RequestInterface.bsonException;

class IdentityFriendRequestList {
    static void request(Connection connection, Identity.FriendRequestListCb callback) {
        final String listOp = "identity.friendRequestList";
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
                public void args(BsonWriter writer) {}

            });
        }

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Identity.FriendRequestListCb callback;

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
                    ArrayList<Friend> identitys = new ArrayList<>();
                    BsonArray bsonArray = new BsonArray(bson.getArray("data"));
                    for (BsonValue listValue : bsonArray) {
                        Friend identity = new Friend();
                        BsonDocument document = listValue.asDocument();
                        identity.setLuid(document.get("luid").asBinary().getData());
                        identity.setRuid(document.get("ruid").asBinary().getData());
                        identity.setAlias(document.get("alias").asString().getValue());
                        identity.setPubkey(document.get("pubkey").asBinary().getData());

                        if (document.containsKey("meta")) {
                            identity.setMeta(document.getDocument("meta").asDocument());
                        }

                        identitys.add(identity);
                    }
                    callback.cb(identitys);
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

            private RequestInterface.Callback init(Identity.FriendRequestListCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}

