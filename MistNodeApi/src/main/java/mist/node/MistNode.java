package mist.node;

import android.content.Context;

import mist.Peer;

import mist.node.EndpointRaw.*;


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

    public synchronized native void addEndpoint(EndpointRaw ep);
    public synchronized native void removeEndpoint(EndpointRaw ep);

    public synchronized native void readResponse(String fullPath, int requestId, byte[] bson);
    public synchronized native void readError(String fullPath, int requestId, int code, String msg);

    public synchronized native void writeResponse(String fullPath, int requestId);
    public synchronized native void writeError(String fullPath, int requestId, int code, String msg);

    public synchronized native void invokeResponse(String fullPath, int requestId, byte[] bson);
    public synchronized native void invokeError(String fullPath, int requestId, int code, String msg);

    public synchronized native void changed(String fullPath);

    void read(final EndpointRaw ep, Peer peer, final int requestId) {

        /* Check data type of endpoint */
        if (ep.readCb instanceof ReadableInt) {
            ReadableInt readCb = (ReadableInt) ep.readCb;

            readCb.read(peer, new EndpointRaw.ReadableIntResponse(ep.epid, requestId) {

            });
        } else if (ep.readCb instanceof  ReadableBool) {
            ((ReadableBool) ep.readCb).read(peer, new EndpointRaw.ReadableBoolResponse(ep.epid, requestId));
        }

    }

    void write(EndpointRaw ep, Peer peer, int requestId, byte[] args) {
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

    void invoke(EndpointRaw ep, Peer peer, int requestId, byte[] args) {
        ep.invokeCb.invoke(args, peer, new InvokeResponse(ep.epid, requestId));
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
