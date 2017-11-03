package wishApp.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import wishApp.Connection;
import mistNode.Errors;
import node.RequestInterface;


class IdentityFriendRequestAccept {
    static void request(Connection connection, byte[] luid, byte[] ruid, Identity.FriendRequestAcceptCb callback) {
        final String acceptOp = "identity.friendRequestAccept";
        String op = acceptOp;

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");

        writer.writeBinaryData(new BsonBinary(luid));
        writer.writeBinaryData(new BsonBinary(ruid));

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();


        if (connection != null) {
            op = ConnectionRequest.getOp();
            buffer = ConnectionRequest.getBuffer(connection, acceptOp, new ConnectionRequest.GetRequestArgs() {

                private byte[] luid;
                private byte[] ruid;

                @Override
                public void args(BsonWriter writer) {
                    writer.writeBinaryData(new BsonBinary(luid));
                    writer.writeBinaryData(new BsonBinary(ruid));
                }

                private ConnectionRequest.GetRequestArgs init (byte[] luid, byte[] ruid) {
                    this.luid = luid;
                    this.ruid = ruid;
                    return this;
                }

            }.init(luid, ruid));
        }

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Identity.FriendRequestAcceptCb callback;

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
                    boolean state = bson.getBoolean("data").getValue();
                    callback.cb(state);
                } catch (BSONException e) {
                    Errors.wishError(acceptOp, 333, e.getMessage(), dataBson);
                    callback.err(333, "bson error: " + e.getMessage());
                }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(acceptOp, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Identity.FriendRequestAcceptCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}

