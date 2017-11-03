package wishApp.request;

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

import mistNode.Errors;
import node.RequestInterface;


/**
 * Created by jeppe on 9/28/16.
 */
class ConnectionList {
    static void request(Connection.ListCb callback) {
        final String op = "connections.list";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");
        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Connection.ListCb callback;

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
                    ArrayList<wishApp.Connection> connections = new ArrayList<wishApp.Connection>();
                    BsonArray bsonArray = bson.get("data").asArray();
                    for (BsonValue bsonValue : bsonArray) {
                        wishApp.Connection connection = new wishApp.Connection();
                        connection.setCid(bsonValue.asDocument().get("cid").asInt32().getValue());
                        connection.setLuid(bsonValue.asDocument().get("luid").asBinary().getData());
                        connection.setRuid(bsonValue.asDocument().get("ruid").asBinary().getData());
                        connection.setRhid(bsonValue.asDocument().get("rhid").asBinary().getData());
                        connection.setOutgoing(bsonValue.asDocument().get("outgoing").asBoolean().getValue());
                        connections.add(connection);

                    }
                    callback.cb(connections);
                } catch (BSONException e) {
                    Errors.wishError(op, 333, e.getMessage(), dataBson);
                    callback.err(333, "bson error: " + e.getMessage());
                }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(op, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Connection.ListCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }
}
