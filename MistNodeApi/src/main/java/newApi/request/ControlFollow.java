package newApi.request;

import org.bson.BSONException;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import newApi.Peer;
import node.MistNode;

import static newApi.request.Callback.BSON_ERROR_CODE;
import static newApi.request.Callback.BSON_ERROR_STRING;

class ControlFollow {

    static int request(Peer peer, Control.FollowCb callback) {
        final String op = "control.follow";

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
            Control.FollowCb cb;

            @Override
            public void response(byte[] data) {
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    BsonDocument bsonData = bson.get("data").asDocument();

                    String epid = bsonData.getString("id").getValue();

                    BsonValue bsonValue = bsonData.get("data");
                    if (bsonValue.isBoolean()) {
                        cb.cbBool(epid, bsonValue.asBoolean().getValue());
                    }
                    if (bsonValue.isInt32()) {
                        cb.cbInt(epid, bsonValue.asInt32().getValue());
                    }
                    if (bsonValue.isDouble()) {
                        cb.cbFloat(epid, (float) bsonValue.asDouble().getValue());
                    }
                    if (bsonValue.isString()) {
                        cb.cbString(epid, bsonValue.asString().getValue());
                    }
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

            private MistNode.RequestCb init(Control.FollowCb callback) {
                this.cb = callback;
                return this;
            }

        }.init(callback));
    }
}










