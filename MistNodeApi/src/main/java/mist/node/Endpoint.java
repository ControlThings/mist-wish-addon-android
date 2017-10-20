package mist.node;

import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonWriter;
import org.bson.io.BasicOutputBuffer;

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

    private String epid;
    private String label;
    private Type type;
    private String unit;

    private boolean readable;
    private boolean writable;
    private boolean invokable;

    private Readable readCb;
    private Writable writeCb;
    private Invokable invokeCb;

    public Endpoint(String epid) {
        /* Note: fullPath must be evaluated and parent field set accordingly */
        this.epid = epid;
        id = epid.substring(epid.lastIndexOf('.')+1);
        parent = epid.substring(0, epid.lastIndexOf('.')+1);
    }

    public String getEpid() {
        return epid;
    }

    public Endpoint setLabel(String label) {
        this.label = label;

        return this;
    }

    public String getLabel() {
        return label;
    }

    private void setType(Type type) {
        if (this.type != null && this.type != type) {
            throw new MistException("Type is already set to: " + type);
        }
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Endpoint setUnit(String unit) {
        this.unit = unit;

        return this;
    }

    public String getUnit() {
        return unit;
    }

    public Endpoint setRead(ReadableBool readableCb) {
        setType(Type.MIST_TYPE_BOOL);
        readCb = readableCb;
        readable = true;

        return this;
    }

    public Endpoint setRead(ReadableInt readableCb) {
        setType(Type.MIST_TYPE_INT);
        readCb = readableCb;
        readable = true;

        return this;
    }

    public Endpoint setRead(ReadableFloat readableCb) {
        setType(Type.MIST_TYPE_FLOAT);
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

    public Readable getReadCb() {
        return readCb;
    }

    public boolean isReadable() {
        return readable;
    }

    public Endpoint setWrite(WritableBool writableCb) {
        setType(Type.MIST_TYPE_BOOL);
        writeCb = writableCb;
        writable = true;

        return this;
    }

    public Endpoint setWrite(WritableInt writableCb) {
        setType(Type.MIST_TYPE_INT);
        writeCb = writableCb;
        writable = true;

        return this;
    }

    public Endpoint setWrite(WritableFloat writableCb) {
        setType(Type.MIST_TYPE_FLOAT);
        writeCb = writableCb;
        writable = true;

        return this;
    }

    public Endpoint setWrite(WritableString writableCb) {
        setType(Type.MIST_TYPE_STRING);
        writeCb = writableCb;
        writable = true;

        return this;
    }

    public Writable getWriteCb() {
        return writeCb;
    }

    public boolean isWritable() {
        return writable;
    }

    public Endpoint setInvoke(Invokable invokableCb) {
        setType(Type.MIST_TYPE_INVOKE); // FIXME this type must only be used for datatypes, must be refactored out from mist-c99
        invokeCb = invokableCb;
        invokable = true;

        return this;
    }

    public Invokable getInvokeCb() {
        return invokeCb;
    }

    public boolean isInvokable() {
        return invokable;
    }

    public void changed() {
        MistNode.getInstance().changed(epid);
    }


    private static class Response {
        protected int requestId;
        protected String epid;

        public Response(String epid, int requestId) {
            this.epid = epid;
            this.requestId = requestId;
        }
    }

    private static class Readable {
    }

    private static class ReadResponse extends Response{

        public ReadResponse(String epid, int requestId) {
            super(epid, requestId);
            if (epid == null) {
                error(453, "Response without epid");
                return;
            }

            if (requestId == 0) {
                error(453, "Response without requestId");
                return;
            }
        }

        public void error(int code, String msg) {
            MistNode.getInstance().readError(epid, requestId, code, msg);
        }
    }

    public static abstract class ReadableBool extends Readable {
        public abstract void read(Peer peer, ReadableBoolResponse response);
    }

    public static class ReadableBoolResponse extends ReadResponse {
        public ReadableBoolResponse(String epid, int requestId) {
            super(epid, requestId);
        }
        public void send(boolean value) {

            BasicOutputBuffer buffer = new BasicOutputBuffer();
            BsonWriter writer = new BsonBinaryWriter(buffer);
            writer.writeStartDocument();
            writer.writeBoolean("data", value);
            writer.writeEndDocument();
            writer.flush();

            MistNode.getInstance().readResponse(epid, requestId, buffer.toByteArray());
        }
    }

    public static abstract class ReadableInt extends Readable {
        public abstract void read(Peer peer, ReadableIntResponse response);
    }

    public static class ReadableIntResponse extends ReadResponse {
        public ReadableIntResponse(String epid, int requestId) {
            super(epid, requestId);
        }
        public void send(int value) {

            BasicOutputBuffer buffer = new BasicOutputBuffer();
            BsonWriter writer = new BsonBinaryWriter(buffer);
            writer.writeStartDocument();
                writer.writeInt32("data", value);
            writer.writeEndDocument();
            writer.flush();

            MistNode.getInstance().readResponse(epid, requestId, buffer.toByteArray());
        }
    }

    public static abstract class ReadableFloat extends Readable {
        public abstract void read(Peer peer, ReadableFloatResponse response);
    }

    public static class ReadableFloatResponse extends ReadResponse {
        public ReadableFloatResponse(String epid, int requestId) {
            super(epid, requestId);
        }
        public void send(float value) {

            BasicOutputBuffer buffer = new BasicOutputBuffer();
            BsonWriter writer = new BsonBinaryWriter(buffer);
            writer.writeStartDocument();
            writer.writeDouble("data", value);
            writer.writeEndDocument();
            writer.flush();

            MistNode.getInstance().readResponse(epid, requestId, buffer.toByteArray());
        }
    }

    public static abstract class ReadableString extends Readable {
        public abstract void read(Peer peer, ReadableStringResponse response);
    }

    public static class ReadableStringResponse extends ReadResponse {
        public ReadableStringResponse(String epid, int requestId) {
            super(epid, requestId);
        }
        public void send(String value) {

            BasicOutputBuffer buffer = new BasicOutputBuffer();
            BsonWriter writer = new BsonBinaryWriter(buffer);
            writer.writeStartDocument();
                writer.writeString("data", value);
            writer.writeEndDocument();
            writer.flush();

            MistNode.getInstance().readResponse(epid, requestId, buffer.toByteArray());
        }
    }


    private static class Writable {

    }

    public static abstract class WritableBool extends Writable {
        public abstract void write(boolean value, Peer peer, WriteResponse response);
    }

    public static abstract class WritableInt extends Writable {
        public abstract void write(int value, Peer peer, WriteResponse response);
    }

    public static abstract class WritableFloat extends Writable {
        public abstract void write(float value, Peer peer, WriteResponse response);
    }

    public static abstract class WritableString extends Writable {
        public abstract void write(String value, Peer peer, WriteResponse response);
    }

    public static class WriteResponse extends Response {
        public WriteResponse(String epid, int requestId) {
            super(epid, requestId);
            if (epid == null) {
                error(454, "Response without epid");
                return;
            }

            if (requestId == 0) {
                error(454, "Response without requestId");
                return;
            }
        }

        public void send() {
            MistNode.getInstance().writeResponse(epid, requestId);
        }

        public void error(int code, String msg) {
            MistNode.getInstance().writeError(epid, requestId, code, msg);
        }
    }

    public static abstract class Invokable {
        public abstract void invoke(byte[] args, Peer peer, InvokeResponse response);
    }

    public static class InvokeResponse extends Response {
        public InvokeResponse(String epid, int requestId) {
            super(epid, requestId);
            if (epid == null) {
                error(455, "Response without epid");
                return;
            }

            if (requestId == 0) {
                error(455, "Response without requestId");
                return;
            }
        }

        public void send(byte[] response) {
            MistNode.getInstance().invokeResponse(epid, requestId, response);
        }

        public void error(int code, String msg) {
            MistNode.getInstance().invokeError(epid, requestId, code, msg);
        }
    }

}
