package mist.node;

import android.content.Context;

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
    public synchronized native int nodeRequest(byte[] peer, byte[] req); //will call mist_app_request

    /**
     * Send a Wish request to the local Wish core
     * @param req the request in BSON format
     * @return the RPC id
     */
    public synchronized native int wishRequest(byte[] req); //will call  wish_app_core_with_cb_context

    void read(Endpoint ep, byte[] peerBson, final int requestId) {
        Peer peer = new Peer(peerBson);

        /* Check data type of endpoint */
        if (ep.readCb instanceof ReadableInt) {
            ((ReadableInt) ep.readCb).read(peer, new ReadableIntResponse(ep.epid, requestId));
        } else if (ep.readCb instanceof  ReadableBool) {
            ((ReadableBool) ep.readCb).read(peer, new ReadableBoolResponse(ep.epid, requestId));
        } else if (ep.readCb instanceof  ReadableString) {
            ((ReadableString) ep.readCb).read(peer, new ReadableStringResponse(ep.epid, requestId));
        }

    }

    void write(Endpoint ep, byte[] peerBson, int requestId, byte[] args) {
        Peer peer = new Peer(peerBson);

        if (ep.writeCb instanceof WritableBool) {
            /* Read the value from BSON args.
            * { args: <value> }
            *
            * if there is a BSON error, call writeError(ep.epid, requestId, 45, "Bad BSON structure");
            * */


            boolean value = false;
            ((WritableBool) ep.writeCb).write(value, peer, new WriteResponse(ep.epid, requestId));
        } else if (ep.writeCb instanceof WritableInt) {
            /* Read the value from BSON args. */

            int value = 0;
            ((WritableInt) ep.writeCb).write(value, peer, new WriteResponse(ep.epid, requestId));
        }
    }

    void invoke(Endpoint ep, byte[] peerBson, int requestId, byte[] args) {
        Peer peer = new Peer(peerBson);
        ep.invokeCb.invoke(args, peer, new InvokeResponse(ep.epid, requestId));
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


}
