package mist.request;

import org.bson.BSONException;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mist.Peer;
import mist.node.*;
import static mist.request.Callback.BSON_ERROR_CODE;
import static mist.request.Callback.BSON_ERROR_STRING;

/**
 * Created by jeppe on 26/07/16.
 */
class ControlRead {
    static void request(Peer peer, String epid, final Control.ReadCb callback) {
        final String op = "control.read";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");
        writer.writeString(epid);
        writer.writeEndArray();

        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();

        MistNode.getInstance().request(peer.toBson(), buffer.toByteArray(), new MistNode.RequestCb() {
            private Control.ReadCb cb;

            @Override
            public void response(byte[] data) {
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    if (bson.get("data").isBoolean()) {
                        cb.cbBoolean(bson.get("data").asBoolean().getValue());
                    } else if (bson.get("data").isInt32()) {
                        cb.cbInt(bson.get("data").asInt32().getValue());
                    } else if (bson.get("data").isDouble()) {
                        cb.cbFloat((float) bson.get("data").asDouble().getValue());
                    } else if (bson.get("data").isString()) {
                        cb.cbString(bson.get("data").asString().getValue());
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

            private MistNode.RequestCb init(Control.ReadCb callback) {
                this.cb = callback;
                return this;
            }

        }.init(callback));
    }
}
