package mist.request;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mist.Peer;
import mist.node.MistNode;

import static mist.request.Callback.BSON_ERROR_CODE;
import static mist.request.Callback.BSON_ERROR_STRING;

/**
 * Created by jeppe on 26/07/16.
 */
class ControlInvoke {
    static int request(Peer peer, String epid, byte[] bson, final Control.InvokeCb callback) {
        final String op = "control.invoke";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");
        writer.writeString(epid);
        writer.writeBinaryData(new BsonBinary(bson));
        writer.writeEndArray();

        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();

        return MistNode.getInstance().request(peer.toBson(), buffer.toByteArray(), new MistNode.RequestCb() {
            private Control.InvokeCb cb;

            @Override
            public void response(byte[] data) {
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                     cb.cb(bson.get("data").asBinary().getData());
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

            private MistNode.RequestCb init(Control.InvokeCb callback) {
                this.cb = callback;
                return this;
            }

        }.init(callback));
    }
}
