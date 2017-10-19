package mist;

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

    private byte[] localId;
    private byte[] remoteId;
    private byte[] remoteHostId;
    private byte[] remoteServiceId;
    private String protocol;
    private boolean online;

    public Peer(byte[] peerBson) {
        BsonDocument bson = new RawBsonDocument(peerBson);
        localId = bson.get("luid").asBinary().getData();
        remoteId = bson.get("ruid").asBinary().getData();
        remoteHostId = bson.get("rhid").asBinary().getData();
        remoteServiceId = bson.get("rsid").asBinary().getData();
        protocol = bson.get("protocol").asString().getValue();
        online = bson.get("online").asBoolean().getValue();
    }

    public byte[] toBson() {
        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);

        writer.writeStartDocument();
        writer.writeBinaryData("luid", new BsonBinary(getLocalId()));
        writer.writeBinaryData("ruid", new BsonBinary(getRemoteId()));
        writer.writeBinaryData("rhid", new BsonBinary(getRemoteHostId()));
        writer.writeBinaryData("rsid", new BsonBinary(getRemoteServiceId()));
        writer.writeString("protocol", getProtocol());
        writer.writeBoolean("online", isOnline());
        writer.writeEndDocument();
        writer.flush();

        return buffer.toByteArray();
    }

    public byte[] getLocalId() {
        return localId;
    }

    public void setLocalId(byte[] localId) {
        this.localId = localId;
    }

    public byte[] getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(byte[] remoteId) {
        this.remoteId = remoteId;
    }

    public byte[] getRemoteHostId() {
        return remoteHostId;
    }

    public void setRemoteHostId(byte[] remoteHostId) {
        this.remoteHostId = remoteHostId;
    }

    public byte[] getRemoteServiceId() {
        return remoteServiceId;
    }

    public void setRemoteServiceId(byte[] remoteServiceId) {
        this.remoteServiceId = remoteServiceId;
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
        s = "luid: " + byteArrayAsString(localId);
        s += " ruid: " + byteArrayAsString(remoteId);
        s += " rhid: " + byteArrayAsString(remoteHostId);
        s += " rsid: " + byteArrayAsString(remoteServiceId);
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

        return Arrays.equals(localId, peer.getLocalId())
                && Arrays.equals(remoteHostId, peer.getRemoteId())
                && Arrays.equals(remoteHostId, peer.getRemoteHostId())
                && Arrays.equals(remoteServiceId, peer.getRemoteHostId())
                && protocol.equals(peer.getProtocol());
    }
}
