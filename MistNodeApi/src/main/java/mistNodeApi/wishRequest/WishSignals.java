package mistNodeApi.wishRequest;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonArray;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mistNodeApi.Connection;
import mistNodeApi.Errors;
import mistNodeApi.RequestInterface;

import static mistNodeApi.RequestInterface.bsonException;

/**
 * Created by jeppe on 11/30/16.
 */

class WishSignals {
    static int request(Connection connection, Wish.SignalsCb callback) {
        final String signalsOp = "signals";
        String op = signalsOp;

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");
        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        if (connection != null) {
            op = ConnectionRequest.getOp();
            buffer = ConnectionRequest.getBuffer(connection, signalsOp, new ConnectionRequest.GetRequestArgs() {
                @Override
                public void args(BsonWriter writer) {}
            });
        }


        final int id = RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Wish.SignalsCb callback;

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
                String signalData = "";
                if (bson.isString("data")){
                    signalData = bson.getString("data").getValue();
                } else if (bson.isArray("data")) {
                    BsonArray bsonArray = bson.getArray("data");
                    signalData = bsonArray.get(0).asString().getValue();
                }
                callback.cb(signalData);
            } catch (BSONException e) {
                Errors.wishError(signalsOp, bsonException, e.getMessage(), dataBson);
                callback.err(bsonException, "bson error: " + e.getMessage());
            }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(signalsOp, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Wish.SignalsCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));

        return id;
    }
}
