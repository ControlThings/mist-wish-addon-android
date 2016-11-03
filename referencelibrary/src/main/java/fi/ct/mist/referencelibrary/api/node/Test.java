package fi.ct.mist.referencelibrary.api.node;

/**
 * Created by jeppe on 11/1/16.
 */

public class Test {

    static {
        System.loadLibrary("mist");
    }


    public synchronized native void register();

    public synchronized native void result(byte[] data, byte[] peerDoc);

    public Test() {

    }

}