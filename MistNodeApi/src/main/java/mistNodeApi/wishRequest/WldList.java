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

import mistNodeApi.LocalDiscovery;
import mistNodeApi.Errors;
import mistNodeApi.RequestInterface;

import static mistNodeApi.RequestInterface.bsonException;

/**
 * Created by jeppe on 9/28/16.
 */
class WldList {
    static void request(Wld.ListCb callback) {
        final String op = "wld.list";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");
        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Wld.ListCb callback;

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
                    BsonArray bsonArray = bson.get("data").asArray();
                    ArrayList<LocalDiscovery> connections = new ArrayList<LocalDiscovery>();

                    for (BsonValue listValue : bsonArray) {
                        LocalDiscovery connection = new LocalDiscovery();
                        connection.setType(listValue.asDocument().get("type").asString().getValue());
                        connection.setAlias(listValue.asDocument().get("alias").asString().getValue());
                        if (listValue.asDocument().containsKey("luid")) {
                            connection.setLuid(listValue.asDocument().get("luid").asBinary().getData());
                        }
                        connection.setRuid(listValue.asDocument().get("ruid").asBinary().getData());
                        connection.setRhid(listValue.asDocument().get("rhid").asBinary().getData());
                        connection.setPubkey(listValue.asDocument().get("pubkey").asBinary().getData());
                        if (listValue.asDocument().containsKey("claim")) {
                            connection.setClaim(listValue.asDocument().get("claim").asBoolean().getValue());
                        } else {
                            connection.setClaim(false);
                        }

                        connections.add(connection);
                    }
                    callback.cb(connections);
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

            private RequestInterface.Callback init(Wld.ListCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}
