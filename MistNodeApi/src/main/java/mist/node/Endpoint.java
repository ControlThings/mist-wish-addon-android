package mist.node;

import mist.Peer;

/**
 * Created by jan on 11/1/16.
 */

public class Endpoint {
    public enum Type {
        MIST_TYPE_BOOL,     /* a true/false value (BSON bool) */
        MIST_TYPE_FLOAT,    /* A floating point value (actually a BSON double) */
        MIST_TYPE_INT,       /* A 32-bit signed integer (BSON int32) */
        MIST_TYPE_STRING,   /* A string, which can be MIST_STRING_EP_MAX_LEN bytes long at most */
        MIST_TYPE_INVOKE,   /* An endpoint which represent a function you can call, "invoke" so to speak */
    }

    private String parent;
    private String id;

    public String epid;
    private String label;
    private String unit;
    private Type type;

    private boolean readable;
    protected boolean writable;
    private boolean invokable;

    public Endpoint(String epid) {
        /* Note: fullPath must be evaluated and parent field set accordingly */
        this.epid = epid;
        id = epid.substring(epid.lastIndexOf('.')+1);
        parent = epid.substring(0, epid.lastIndexOf('.')+1);
    }

    public Readable readCb;

    public Writable writeCb;

    public Invokable invokeCb;

    private void setType(Type type) {
        if (this.type != null && this.type != type) {
            throw new MistException("Type is already set to: " + type);
        }
        this.type = type;
    }

    public Endpoint setRead(ReadableInt readableCb) {
        setType(Type.MIST_TYPE_INT);
        readCb = readableCb;
        readable = true;

        return this;
    }

    public Endpoint setRead(ReadableBool readableCb) {
        setType(Type.MIST_TYPE_BOOL);
        readCb = readableCb;
        readable = true;

        return this;
    }

    public Endpoint setRead(ReadableString readableCb) {
        setType(Type.MIST_TYPE_STRING);
        readCb = readableCb;
        readable = true;

        return this;
    }

    public Endpoint setWrite(WritableBool writableCb) {
        setType(Type.MIST_TYPE_BOOL);
        writeCb = writableCb;
        writable = true;

        return this;
    }

    public Endpoint setWrite(WritableInt writableCb) {
        setType(Type.MIST_TYPE_BOOL);
        writeCb = writableCb;
        writable = true;

        return this;
    }

    public Endpoint setInvoke(Invokable invokableCb) {
        setType(Type.MIST_TYPE_INVOKE); // FIXME this type must only be used for datatypes, must be refactored out from mist-c99
        invokeCb = invokableCb;
        invokable = true;

        return this;
    }

    public void changed() {
        MistNode.getInstance().changed(epid);
    }

    public String getLabel() {
        return label;
    }

    public Endpoint setLabel(String label) {
        this.label = label;

        return this;
    }





    public static class Readable {

    }

    public static class Response {
        protected int requestId;
        protected String epid;

        public Response(String epid, int requestId) {
            this.epid = epid;
            this.requestId = requestId;
        }

        public void error(int code, String msg) {
            MistNode.getInstance().readError(epid, requestId, code, msg);
        }
    }

    public static abstract class ReadableInt extends Readable {
        public abstract void read(Peer peer, ReadableIntResponse response);
    }

    public static class ReadableIntResponse extends Response {

        public ReadableIntResponse(String epid, int requestId) {
            super(epid, requestId);
        }
        public void send(int value) {
            /* The value must be marshalled into BSON document: { data: <value> } */

            byte[] valueAsBson = null;

            MistNode.getInstance().readResponse(epid, requestId, valueAsBson);
        }
    }

    public static abstract class ReadableBool extends Readable {
        public abstract void read(Peer peer, ReadableBoolResponse response);
    }

    public static class ReadableBoolResponse extends Response {
        public ReadableBoolResponse(String epid, int requestId) {
            super(epid, requestId);
        }
        public void send(boolean value) {
            /* The value must be marshalled into BSON document: { data: <value> } */
            byte[] valueAsBson = null;

            MistNode.getInstance().readResponse(epid, requestId, valueAsBson);
        }
    }

    public static abstract class ReadableString extends Readable {
        public abstract void read(Peer peer, ReadableStringResponse response);
    }

    public static class ReadableStringResponse extends Response {
        public ReadableStringResponse(String epid, int requestId) {
            super(epid, requestId);
        }
        public void send(String value) {
            /* The value must be marshalled into BSON document: { data: <value> } */
            byte[] valueAsBson = null;

            MistNode.getInstance().readResponse(epid, requestId, valueAsBson);
        }
    }


    public static class Writable {

    }

    public static abstract class WritableBool extends Writable {
        public abstract void write(boolean value, Peer peer, WriteResponse response);
    }

    public static abstract class WritableInt extends Writable {
        public abstract void write(int value, Peer peer, WriteResponse response);
    }

    public static class WriteResponse extends Response {
        public WriteResponse(String epid, int requestId) {
            super(epid, requestId);
        }

        public void send() {
            MistNode.getInstance().writeResponse(epid, requestId);
        }
    }

    public static abstract class Invokable {
        public abstract void invoke(byte[] args, Peer peer, InvokeResponse response);
    }

    public static class InvokeResponse extends Response {
        public InvokeResponse(String epid, int requestId) {
            super(epid, requestId);
        }

        public void send(byte[] response) {
            MistNode.getInstance().invokeResponse(epid, requestId, response);
        }
    }

}
