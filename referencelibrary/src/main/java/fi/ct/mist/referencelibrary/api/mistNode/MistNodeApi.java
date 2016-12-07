package fi.ct.mist.referencelibrary.api.mistNode;

import android.content.Context;

/**
 * Created by jan on 11/1/16.
 */

class MistNodeApi {
    private Context context;
    private WishFile file;

    static {
        System.loadLibrary("mist");
    }

    /* Private constructor must exist to enforce Singleton pattern */
    private MistNodeApi() {}


    private static class MistNodeApiHolder {
        private static final MistNodeApi INSTANCE = new MistNodeApi();
    }

    public static MistNodeApi getInstance() {
        return MistNodeApiHolder.INSTANCE;
    }

    void startMistApp(String appName, Context context) {
        this.context = context;
        file = new WishFile(context);
        startMistApp(appName);
    }

    synchronized native void updateBool(String epName, boolean newValue);
    synchronized native void updateInt(String epName, int newValue);
    synchronized native void updateString(String epName, String newValue);
    synchronized native void updateFloat(String epName, double newValue);
    synchronized native void addEndpoint(Endpoint ep);
    private synchronized native void startMistApp(String appName);


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
