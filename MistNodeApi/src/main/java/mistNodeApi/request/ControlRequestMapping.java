package mistNodeApi.request;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import mistNodeApi.Peer;
import mistNodeApi.Errors;
import mistNodeApi.RequestInterface;

import static mistNodeApi.RequestInterface.bsonException;

/**
 * Created by jeppe on 16/12/2016.
 */

public class ControlRequestMapping {

    static void request(Peer peer, Peer from, String epFrom, String epTo, Control.RequestMappingCB callback) {
        final String op = "mist.control.requestMapping";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");

        // which peer to send the requestMapping command to
        writer.writeStartDocument();
        writer.writeBinaryData("luid", new BsonBinary(peer.getLocalId()));
        writer.writeBinaryData("ruid", new BsonBinary(peer.getRemoteId()));
        writer.writeBinaryData("rhid", new BsonBinary(peer.getRemoteHostId()));
        writer.writeBinaryData("rsid", new BsonBinary(peer.getRemoteServiceId()));
        writer.writeString("protocol", peer.getProtocol());
        writer.writeEndDocument();

        // which peer the signal will come from
        writer.writeStartDocument();
        writer.writeBinaryData("luid", new BsonBinary(peer.getRemoteId()));
        writer.writeBinaryData("ruid", new BsonBinary(from.getRemoteId()));
        writer.writeBinaryData("rhid", new BsonBinary(from.getRemoteHostId()));
        writer.writeBinaryData("rsid", new BsonBinary(from.getRemoteServiceId()));
        writer.writeString("protocol", from.getProtocol());
        writer.writeEndDocument();

        writer.writeString(epFrom);

        writer.writeStartDocument();
        writer.writeString("type", "direct");
        writer.writeString("interval", "change");
        writer.writeEndDocument();

        writer.writeString(epTo);

        writer.writeStartDocument();
        writer.writeString("type", "write");
        writer.writeEndDocument();

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().mistApiRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Control.RequestMappingCB callback;

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
                    callback.cb();
                } catch (BSONException e) {
                    Errors.mistError(op, bsonException, e.getMessage(), dataBson);
                    callback.err(bsonException, "bson error: " + e.getMessage());
                }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(op, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Control.RequestMappingCB callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }

}
