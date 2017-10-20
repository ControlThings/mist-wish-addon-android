package mist;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.io.Serializable;
import java.util.Arrays;


/**
 * Created by jeppe on 11/16/16.
 */

public class Peer implements Serializable {

    private byte[] luid;
    private byte[] ruid;
    private byte[] rhid;
    private byte[] rsid;
    private String protocol;
    private boolean online;

    private static int WISH_UID_LEN = 32;

    public static Peer fromBson(byte[] data) {

        Peer peer = new Peer();
        try {
            BsonDocument bsonDocument = new RawBsonDocument(data);
            peer.luid = bsonDocument.get("luid").asBinary().getData();
            peer.ruid = bsonDocument.get("ruid").asBinary().getData();
            peer.rhid = bsonDocument.get("rhid").asBinary().getData();
            peer.rsid = bsonDocument.get("rsid").asBinary().getData();
            peer.protocol = bsonDocument.get("protocol").asString().getValue();
            peer.online = bsonDocument.get("online").asBoolean().getValue();

            if (peer.luid.length != WISH_UID_LEN) { return null; }
            if (peer.ruid.length != WISH_UID_LEN) { return null; }
            if (peer.rhid.length != WISH_UID_LEN) { return null; }
            if (peer.rsid.length != WISH_UID_LEN) { return null; }

        } catch (BSONException e) {
            return null;
        }

        return peer;
    }

    public byte[] toBson() {
        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);

        writer.writeStartDocument();
        writer.writeBinaryData("luid", new BsonBinary(getLuid()));
        writer.writeBinaryData("ruid", new BsonBinary(getRuid()));
        writer.writeBinaryData("rhid", new BsonBinary(getRhid()));
        writer.writeBinaryData("rsid", new BsonBinary(getRsid()));
        writer.writeString("protocol", getProtocol());
        writer.writeBoolean("online", isOnline());
        writer.writeEndDocument();
        writer.flush();

        return buffer.toByteArray();
    }

    public byte[] getLuid() {
        return luid;
    }

    public void setLuid(byte[] luid) {
        this.luid = luid;
    }

    public byte[] getRuid() {
        return ruid;
    }

    public void setRuid(byte[] ruid) {
        this.ruid = ruid;
    }

    public byte[] getRhid() {
        return rhid;
    }

    public void setRhid(byte[] rhid) {
        this.rhid = rhid;
    }

    public byte[] getRsid() {
        return rsid;
    }

    public void setRsid(byte[] rsid) {
        this.rsid = rsid;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    private String byteArrayAsString(byte[] array) {
        String ret = new String();
        if (array == null) {
            ret = "null";
        } else {
            for (int i = 0; i < array.length; i++) {
                ret += "0x" + Integer.toHexString(array[i]) + ", ";
            }
        }
        return ret;
    }

    public String toString() {
        String s = new String();
        s = "luid: " + byteArrayAsString(luid);
        s += " ruid: " + byteArrayAsString(ruid);
        s += " rhid: " + byteArrayAsString(rhid);
        s += " rsid: " + byteArrayAsString(rsid);
        s += " protocol: " + protocol;
        s += online ? " online" : " offline";
        return s;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null) {
            return false;
        }

        if (getClass() != object.getClass()) {
            return false;
        }

        Peer peer = (Peer) object;

        return Arrays.equals(luid, peer.getLuid())
                && Arrays.equals(rhid, peer.getRuid())
                && Arrays.equals(rhid, peer.getRhid())
                && Arrays.equals(rsid, peer.getRhid())
                && protocol.equals(peer.getProtocol());
    }
}
