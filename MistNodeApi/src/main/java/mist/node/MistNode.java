package mist.node;

import android.content.Context;

import org.bson.BSONException;
import org.bson.BsonDocument;
import org.bson.RawBsonDocument;

import mist.Peer;

import mist.node.Endpoint.*;


/**
 * Created by jan on 11/1/16.
 */

public class MistNode {
    private Context context;
    private WishFile file;

    static {
        System.loadLibrary("mist");
    }

    /* Private constructor must exist to enforce Singleton pattern */
    private MistNode() {}


    private static class MistNodeApiHolder {
        private static final MistNode INSTANCE = new MistNode();
    }

    public static MistNode getInstance() {
        return MistNodeApiHolder.INSTANCE;
    }

    public void startMistApp(String appName, Context context) {
        this.context = context;
        file = new WishFile(context);
        startMistApp(appName);
    }

    synchronized native void startMistApp(String appName);
    synchronized native void stopMistApp();

    public synchronized native void addEndpoint(Endpoint ep);
    public synchronized native void removeEndpoint(Endpoint ep);

    public synchronized native void readResponse(String fullPath, int requestId, byte[] bson);
    public synchronized native void readError(String fullPath, int requestId, int code, String msg);

    public synchronized native void writeResponse(String fullPath, int requestId);
    public synchronized native void writeError(String fullPath, int requestId, int code, String msg);

    public synchronized native void invokeResponse(String fullPath, int requestId, byte[] bson);
    public synchronized native void invokeError(String fullPath, int requestId, int code, String msg);

    public synchronized native void changed(String fullPath);

    /**
     * Send a Mist request to remote peer.
     *
     * @param peer a BSON representation of the protocol peer
     * @param req a BSON representation of the RPC request, {op, args}
     * @return the RPC id
     */
    public synchronized native int request(byte[] peer, byte[] req, RequestCb cb); //will call mist_app_request

    /**
     * Send a Wish request to the local Wish core
     * @param req the request in BSON format
     * @return the RPC id
     */
    public synchronized native int wishRequest(byte[] req); //will call  wish_app_core_with_cb_context

    /**
     * Read function called by JNI code. This function is called when handling control.read, or whenever an update of the Endpoint's value is
     * to be read, when satisfying a control.follow request perhaps after the Endpoint's value has been declared to have changed via MistNode.changed().
     *
     * @param ep Endpoint object to be read
     * @param peerBson the peer in BSON format in case of control.read, or null if this is an internal read.
     * @param requestId the request id
     */
    protected void read(Endpoint ep, byte[] peerBson, final int requestId) {
        Peer peer = Peer.fromBson(peerBson);

        /* Check data type of endpoint */
        if (ep.getReadCb() instanceof ReadableInt) {
            ((ReadableInt) ep.getReadCb()).read(peer, new ReadableIntResponse(ep.getEpid(), requestId));
        } else if (ep.getReadCb() instanceof  ReadableBool) {
            ((ReadableBool) ep.getReadCb()).read(peer, new ReadableBoolResponse(ep.getEpid(), requestId));
        } else if (ep.getReadCb() instanceof  ReadableString) {
            ((ReadableString) ep.getReadCb()).read(peer, new ReadableStringResponse(ep.getEpid(), requestId));
        } else if (ep.getReadCb() instanceof ReadableFloat) {
            ((ReadableFloat) ep.getReadCb()).read(peer, new ReadableFloatResponse(ep.getEpid(), requestId));
        } else {
            if (ep.getReadCb() == null) { readError(ep.getEpid(), requestId, 346, "No callback function registered"); }
            else { readError(ep.getEpid(), requestId, 346, "Not supported callback function"); }
            return;
        }

    }

    protected void write(Endpoint ep, byte[] peerBson, int requestId, byte[] args) {
        Peer peer = Peer.fromBson(peerBson);

        if (ep.getWriteCb() instanceof WritableBool) {

            boolean value;
            try {
                BsonDocument bson = new RawBsonDocument(args);
                value = bson.get("args").asBoolean().getValue();
            } catch (BSONException e) {
                writeError(ep.getEpid(), requestId, 452, "Bad BSON structure");
                return;
            }

            ((WritableBool) ep.getWriteCb()).write(value, peer, new WriteResponse(ep.getEpid(), requestId));

        } else if (ep.getWriteCb() instanceof WritableInt) {

            int value;
            try {
                BsonDocument bson = new RawBsonDocument(args);
                value = bson.get("args").asInt32().getValue();
            } catch (BSONException e) {
                writeError(ep.getEpid(), requestId, 452, "Bad BSON structure");
                return;
            }

            ((WritableInt) ep.getWriteCb()).write(value, peer, new WriteResponse(ep.getEpid(), requestId));

        } else if (ep.getWriteCb() instanceof WritableFloat) {

            float value;
            try {
                BsonDocument bson = new RawBsonDocument(args);
                value = (float) bson.get("args").asDouble().getValue();
            } catch (BSONException e) {
                writeError(ep.getEpid(), requestId, 452, "Bad BSON structure");
                return;
            }

            ((WritableFloat) ep.getWriteCb()).write(value, peer, new WriteResponse(ep.getEpid(), requestId));

        } else if (ep.getWriteCb() instanceof WritableString) {

            String value;
            try {
                BsonDocument bson = new RawBsonDocument(args);
                value = bson.get("args").asString().getValue();
            } catch (BSONException e) {
                writeError(ep.getEpid(), requestId, 452, "Bad BSON structure");
                return;
            }

            ((WritableString) ep.getWriteCb()).write(value, peer, new WriteResponse(ep.getEpid(), requestId));

        } else {
            if (ep.getWriteCb() == null) { writeError(ep.getEpid(), requestId, 346, "No callback function registered"); }
            else { writeError(ep.getEpid(), requestId, 346, "Not supported callback function"); }
            return;
        }
    }

    void invoke(Endpoint ep, byte[] peerBson, int requestId, byte[] args) {

        Peer peer = Peer.fromBson(peerBson);

        if (peer == null) {
            writeError(ep.getEpid(), requestId, 347, "Invalid peer");
            return;
        }

        if (ep.getInvokeCb() == null) {
            writeError(ep.getEpid(), requestId, 346, "No callback function registered");
            return;
        }

        ep.getInvokeCb().invoke(args, peer, new InvokeResponse(ep.getEpid(), requestId));
    }

    void online(byte[] peerBson) {

    }

    void offline(byte[] peerBson) {

    }



    /**
     * Open a file
     *
     * @param filename the filename
     * @return the file ID number which can be used with read, write..., or -1 for an error
     */
    public int openFile(String filename) {
        return file.open(filename);
    }

    /**
     * Close a fileId
     *
     * @param fileId the fileId to close
     * @return 0 for success, -1 for an error
     */
    public int closeFile(int fileId) {
        return file.close(fileId);
    }

    /**
     * Read from a file
     *
     * @param fileId the fileId, obtained with open()
     * @param buffer The buffer to place the bytes into
     * @param count  the number of bytes to read
     * @return the amount of bytes read, or 0 if EOF, or -1 for an error
     */
    public int readFile(int fileId, byte[] buffer, int count) {
        if (count != buffer.length) {
            return -1;
        }
        return file.read(fileId, buffer, count);
    }

    /**
     * Write to a file in internal storage
     *
     * @param fileId the fileId
     * @param buffer the databuffer to be written
     * @param count  The
     * @return
     */
    public int writeFile(int fileId, byte[] buffer, int count) {
        if (count != buffer.length) {
            return -1;
        } else {
            return file.write(fileId, buffer);
        }
    }

    public int seekFile(int fileId, int offset, int whence) {
        return (int) file.seek(fileId, offset, whence);
    }

    public int rename(String oldName, String newName) {
        return file.rename(oldName, newName);
    }

    public int remove(String filename) {
        return file.remove(filename);
    }


    public abstract static class RequestCb {

        /**
         * The callback invoked when "ack" is received for a RPC request
         *
         * @param data a document containing RPC return value as 'data' element
         */
        public void ack(byte[] data) {
            response(data);
            end();
        };

        /**
         * The callback invoked when "sig" is received for a RPC request
         *
         * @param data the contents of 'data' element of the RPC reply
         */
        public void sig(byte[] data) {
            response(data);
        };

        public abstract void response(byte[] data);
        public abstract void end();

        /**
         * The callback invoked when "err" is received for a failed RPC request
         *
         * @param code the error code
         * @param msg  a free-text error message
         */
        public abstract void err(int code, String msg);
    }
}
