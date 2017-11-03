package node;

import android.util.Log;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.ArrayList;
import java.util.List;

import bson.BsonExtendedBinaryWriter;
import bson.BsonExtendedWriter;


/**
 * Created by jan on 11/25/16.
 */

public class RequestInterface {

    private static final String TAG = "RequestInterface";

    public static final int bsonError = 233;
    public static final int bsonException = 244;

    private static RequestInterface instance;
    private List<Error> mistErrorHandlerList;
    private List<Error> wishErrorHandlerList;

    private boolean debug = false;

    private RequestInterface() {
        mistErrorHandlerList = new ArrayList<>();
        wishErrorHandlerList = new ArrayList<>();
    }

    public static RequestInterface getInstance() {
        if (instance == null) {
            instance = new RequestInterface();
        }
        return instance;
    }

    /**
     * Make a Wish Api request, such as "identity.list"
     *
     * @param op       The name of the Wish RPC request
     * @param argsBson the arguments in BSON format of the request
     * @param cb       the callback to be invoked when a reply arrives
     * @return the RPC id of the request, or 0 for fail
     */
    public int wishRequest(final String op, byte[] argsBson, final RequestInterface.Callback cb) {

        BsonDocument bson = new RawBsonDocument(argsBson);
        BsonArray array = bson.getArray("args");


        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonExtendedWriter writer = new BsonExtendedBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");
        writer.pipeArray(array);
        writer.writeEndArray();

        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();

        MistNode.RequestCb callback = new MistNode.RequestCb() {

            @Override
            public void ack(byte[] data) {
                super.ack(data);
                cb.ack(data);
            }

            @Override
            public void sig(byte[] data) {
                super.sig(data);
                cb.sig(data);
            }

            @Override
            public void response(byte[] data) {}

            @Override
            public void end() {}

            @Override
            public void err(int code, String msg) {
                synchronized (wishErrorHandlerList) {
                    for (Error error : wishErrorHandlerList) {
                        error.cb(op, code, msg);
                    }
                }
                cb.err(code, msg);
            }
        };

        return MistNode.getInstance().wishRequest(buffer.toByteArray(), callback);

    }

    public void wishRequestCancel(int id) {
        Log.d(TAG, "No cancel implementation");
        //node.MistNode.getInstance().wishRequestCancel(id);
    }

    /**
     * Make a Mist Api request, such as "control.model"
     *
     * @param op       The name of the Wish RPC request
     * @param argsBson the arguments in BSON format of the request, that is an array named args, for example for "control.model": { args: [0: {luid, ruid, rsid, rhid} ] }"
     * @param cb       the callback to be invoked when a reply arrives
     * @return the RPC id of the request, or 0 for fail
     */
    public int mistApiRequest(final String op, byte[] argsBson, final RequestInterface.Callback cb) {
        //Callback callback = cb;

        BsonDocument bsonDocument = new RawBsonDocument(argsBson);

        BasicOutputBuffer peerBuffer = new BasicOutputBuffer();
        BsonExtendedWriter peerWriter = new BsonExtendedBinaryWriter(peerBuffer);

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonExtendedWriter writer = new BsonExtendedBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op.replace("mist.", ""));

        writer.writeStartArray("args");


        BsonReader reader = new BsonDocumentReader(bsonDocument);

        reader.readStartDocument();
        reader.readStartArray();

        peerWriter.pipe(reader);
        peerWriter.flush();

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            switch (reader.getCurrentBsonType()) {
                case BOOLEAN:
                    writer.writeBoolean(reader.readBoolean());
                    break;
                case INT32:
                    writer.writeInt32(reader.readInt32());
                    break;
                case INT64:
                    writer.writeInt64(reader.readInt64());
                    break;
                case DOUBLE:
                    writer.writeDouble(reader.readDouble());
                    break;
                case STRING:;
                    writer.writeString(reader.readString());
                    break;
                case BINARY:
                    writer.writeBinaryData(reader.readBinaryData());
                    break;
                case DOCUMENT:
                    writer.pipe(reader);
                    break;
            }
        }
        reader.readEndArray();
        reader.readEndDocument();

        writer.writeEndArray();

        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();

        MistNode.RequestCb callback = new MistNode.RequestCb() {

            @Override
            public void ack(byte[] data) {
                super.ack(data);
                cb.ack(data);
            }

            @Override
            public void sig(byte[] data) {
                super.sig(data);
                cb.sig(data);
            }

            @Override
            public void response(byte[] data) {

            }

            @Override
            public void end() {

            }

            @Override
            public void err(int code, String msg) {
                synchronized (mistErrorHandlerList) {
                    for (Error error : mistErrorHandlerList) {
                        error.cb(op, code, msg);
                    }
                }
                cb.err(code, msg);
            }
        };

        return MistNode.getInstance().request(peerBuffer.toByteArray(), buffer.toByteArray(), callback);
    }

    public void mistApiRequestCancel(int id) {
        Log.d(TAG, "No cancel implementation");
        //node.MistNode.getInstance().wishRequestCancel(id);
    }

    public void registerMistRpcErrorHandler(Error error) {
        synchronized (mistErrorHandlerList) {
            this.mistErrorHandlerList.add(error);
        }
    }

    public void removeMistRpcErrorHandler(Error error) {
        synchronized (mistErrorHandlerList) {
            if (mistErrorHandlerList.contains(error)) {
                mistErrorHandlerList.remove(error);
            }
        }
    }

    public void registerWishRpcErrorHandler(Error error) {
        synchronized (wishErrorHandlerList) {
            this.wishErrorHandlerList.add(error);
        }
    }

    public void removeWishRpcErrorHandler(Error error) {
        synchronized (wishErrorHandlerList) {
            if (wishErrorHandlerList.contains(error)) {
                wishErrorHandlerList.remove(error);
            }
        }
    }

    public interface Error {
        public void cb(String op, int code, String msg);
    }

    public interface Callback {
        /**
         * The callback invoked when "ack" is received for a RPC request
         *
         * @param dataBson a document containing RPC return value as 'data' element
         */
        public void ack(byte[] dataBson);

        /**
         * The callback invoked when "sig" is received for a RPC request
         *
         * @param dataBson the contents of 'data' element of the RPC reply
         */
        public void sig(byte[] dataBson);

        /**
         * The callback invoked when "err" is received for a failed RPC request
         *
         * @param code the error code
         * @param msg  a free-text error message
         */
        public void err(int code, String msg);
    }
}
