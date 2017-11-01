package mistNodeApi.wishRequest;

import android.util.Log;

import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.ArrayList;
import java.util.Arrays;

import mistNodeApi.Cert;
import mistNodeApi.wishRequest.Connection;
import mistNodeApi.Peer;
import mistNodeApi.Errors;
import mistNodeApi.RequestInterface;

/**
 * Created by jeppe on 9/28/16.
 */
class IdentityFriendRequest {
    static void request(byte[] luid, BsonDocument contact, BsonDocument document, Identity.FriendRequestCb callback) {
        final String op = "identity.friendRequest";

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();
        writer.writeStartArray("args");

        writer.writeBinaryData(new BsonBinary(luid));

        try {
            if (contact.containsKey("data") && contact.containsKey("meta")) {
                writer.writeStartDocument();
                writer.writeBinaryData("data", contact.getBinary("data"));
                writer.writeBinaryData("meta", contact.getBinary("meta"));
                writer.writeEndDocument();
            } else {
                callback.err(757, "invalide contact");
                return;
            }
        } catch (BSONException e) {
            callback.err(757, e.getMessage());
            return;
        }

        if (document != null) {
            writer.pipe(new BsonDocumentReader(document));
        }

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        RequestInterface.getInstance().wishRequest(op, buffer.toByteArray(), new RequestInterface.Callback() {
            private Identity.FriendRequestCb callback;

            @Override
            public void ack(byte[] dataBson) {
                response(dataBson);
                callback.end();
            }

            @Override
            public void sig(byte[] dataBson) {
                response(dataBson);
            }

            private void response(byte[] dataBson) {
                try {
                    BsonDocument bson = new RawBsonDocument(dataBson);
                    boolean state = bson.get("data").asBoolean().getValue();
                    callback.cb(state);
                } catch (BSONException e) {
                    Errors.wishError(op, 333, e.getMessage(), dataBson);
                    callback.err(333, "bson error: " + e.getMessage());
                }
            }

            @Override
            public void err(int code, String msg) {
                Log.d(op, "RPC error: " + msg + " code: " + code);
                callback.err(code, msg);
            }

            private RequestInterface.Callback init(Identity.FriendRequestCb callback) {
                this.callback = callback;
                return this;
            }
        }.init(callback));
    }


    static void requestDocument(final byte[] luid, final BsonDocument contact, final Peer peer, Identity.FriendRequestCb callback) {
        Identity.export(peer.getRemoteId(), new Identity.ExportCb() {

            private Identity.FriendRequestCb callback;

            @Override
            public void cb(byte[] data, byte[] raw) {

                final BasicOutputBuffer dataBuffer = new BasicOutputBuffer();
                BsonWriter dataWriter = new BsonBinaryWriter(dataBuffer);
                dataWriter.writeStartDocument();
                dataWriter.writeString("alias", new RawBsonDocument(data).getString("alias").getValue());
                dataWriter.writeBinaryData("uid", new BsonBinary(peer.getRemoteId()));
                dataWriter.writeBinaryData("hid", new BsonBinary(peer.getRemoteHostId()));
                dataWriter.writeBinaryData("sid", new BsonBinary(peer.getRemoteServiceId()));
                dataWriter.writeBinaryData("pubkey", new RawBsonDocument(data).getBinary("pubkey"));
                dataWriter.writeEndDocument();
                dataWriter.flush();

                BasicOutputBuffer buffer = new BasicOutputBuffer();
                BsonWriter writer = new BsonBinaryWriter(buffer);
                writer.writeStartDocument();
                writer.writeBinaryData("data", new BsonBinary(dataBuffer.toByteArray()));
                writer.writeBinaryData("meta", new RawBsonDocument(raw).getDocument("data").getBinary("meta"));
                writer.writeEndDocument();
                writer.flush();

                byte[] cert = buffer.toByteArray();

                Connection.list(new Connection.ListCb() {
                    private Identity.FriendRequestCb callback;
                    private byte[] cert;

                    @Override
                    public void cb(ArrayList<mistNodeApi.Connection> connections) {
                        for (mistNodeApi.Connection connection : connections) {
                            if (Arrays.equals(connection.getLuid(), peer.getLocalId()) && Arrays.equals(connection.getRhid(), peer.getRemoteHostId()) && Arrays.equals(connection.getRuid(), peer.getRemoteId())) {

                                Identity.sign(connection, peer.getRemoteId(), new RawBsonDocument(cert), new Identity.SignCb() {

                                    private Identity.FriendRequestCb callback;

                                    @Override
                                    public void cb(byte[] data) {
                                        Cert cert = new Cert();
                                        cert.setCert(new RawBsonDocument(data));
                                        request(luid, contact, new RawBsonDocument(data), callback);
                                    }

                                    @Override
                                    public void err(int code, String msg) {
                                        callback.err(code, msg);
                                    }

                                    @Override
                                    public void end() {}

                                    private Identity.SignCb init(Identity.FriendRequestCb callback) {
                                        this.callback = callback;
                                        return this;
                                    }
                                }.init(callback));

                            }
                        }
                    }

                    @Override
                    public void err(int code, String msg) {
                        callback.err(code, msg);
                    }

                    @Override
                    public void end() {}

                    private Connection.ListCb init(Identity.FriendRequestCb callback, byte[] cert) {
                        this.callback = callback;
                        this.cert = cert;
                        return this;
                    }
                }.init(callback, cert));


            }

            @Override
            public void err(int code, String msg) {
                callback.err(code, msg);
            }

            @Override
            public void end() {}

            private Identity.ExportCb init(Identity.FriendRequestCb callback) {
                this.callback = callback;
                return this;
            }

        }.init(callback));
    }

}
