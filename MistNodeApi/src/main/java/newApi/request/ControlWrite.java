package newApi.request;

import org.bson.BsonBinaryWriter;
import org.bson.BsonWriter;
import org.bson.io.BasicOutputBuffer;

import newApi.Peer;
import mistNodeApi.node.MistNode;

class ControlWrite {

    static int request(Peer peer, String epid, Boolean state, Control.WriteCb callback) {
        return send(peer, epid, state, null, null, null, callback);
    }

    static int request(Peer peer, String epid, int state, Control.WriteCb callback) {
        return send(peer, epid, null, state, null, null, callback);
    }

    static int request(Peer peer, String epid, float state, Control.WriteCb callback) {
        return send(peer, epid, null, null, state, null, callback);
    }

    static int request(Peer peer, String epid, String state, Control.WriteCb callback) {
        return send(peer, epid, null, null, null, state, callback);
    }


    private static int send(Peer peer, String epid, Boolean boolState, Integer intState, Float floatState, String stringState, Control.WriteCb callback) {
        final String op = "control.write";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);
        writer.writeStartArray("args");

        writer.writeString(epid);

        if (boolState != null) {
            writer.writeBoolean(boolState);
        } else if (intState != null) {
            writer.writeInt32(intState);
        } else if (floatState != null) {
            writer.writeDouble(floatState);
        } else if (stringState != null) {
            writer.writeString(stringState);
        }

        writer.writeEndArray();
        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();

        return MistNode.getInstance().request(peer.toBson(), buffer.toByteArray(), new MistNode.RequestCb() {
            private Control.WriteCb cb;

            @Override
            public void response(byte[] data) {
                cb.cb();
            }

            @Override
            public void end() {
                cb.end();
            }

            @Override
            public void err(int code, String msg) {
                cb.err(code, msg);
            }

            private MistNode.RequestCb init(Control.WriteCb callback) {
                this.cb = callback;
                return this;
            }

        }.init(callback));

    }
}
