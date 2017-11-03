package wishApp.request;

import org.bson.BsonDocument;

import java.util.ArrayList;

import wishApp.Cert;
import wishApp.Connection;
import mistNode.Peer;
import mistNode.request.Callback;
import wishApp.Friend;
import wishApp.MistIdentity;

/**
 * Created by jeppe on 11/30/16.
 */

public class Identity {

    public static void create(String alias, CreateCb callback) {
        IdentityCreate.request(alias, callback);
    }

    public static void export(byte[] id, ExportCb callback) {
        IdentityExport.request(null, id, callback);
    }

    public static void export(Connection connection, byte[] id, ExportCb callback) {
        IdentityExport.request(connection, id, callback);
    }

    public static void _import(byte[] identity, byte[] localUid, ImportCb callback) {
        IdentityImport.request(identity, localUid, callback);
    }

    public static void remove(byte[] uid, RemoveCb callback) {
        IdentityRemove.request(null, uid, callback);
    }

    public static void remove(Connection connection, byte[] uid, RemoveCb callback) {
        IdentityRemove.request(connection, uid, callback);
    }

    public static void list(ListCb callback) {
        IdentityList.request(null, callback);
    }

    public static void list(Connection connection, ListCb callback) {
        IdentityList.request(connection, callback);
    }

    public static void get(byte[] id, GetCb callback) {
        IdentityGet.request(id, callback);
    }

    public static void friendRequest(byte[] luid, BsonDocument contact, FriendRequestCb callback) {
        IdentityFriendRequest.request(luid, contact, null, callback);
    }

    public static void friendRequest(byte[] luid, BsonDocument contact, Peer peer, FriendRequestCb callback) {
        IdentityFriendRequest.requestDocument(luid, contact, peer, callback);
    }

    public static void friendRequestList(FriendRequestListCb callback) {
        IdentityFriendRequestList.request(null, callback);
    }

    public static void friendRequestList(Connection connection, FriendRequestListCb callback) {
        IdentityFriendRequestList.request(connection, callback);
    }

    public static void friendRequestAccept(byte[] luid, byte[] ruid, FriendRequestAcceptCb callback) {
        IdentityFriendRequestAccept.request(null, luid, ruid, callback);
    }

    public static void friendRequestAccept(Connection connection, byte[] luid, byte[] ruid, FriendRequestAcceptCb callback) {
        IdentityFriendRequestAccept.request(connection, luid, ruid, callback);
    }

    public static void friendRequestDecline(byte[] luid, byte[] ruid, FriendRequestDeclineCb callback) {
        IdentityFriendRequestDecline.request(null, luid, ruid, callback);
    }

    public static void friendRequestDecline(Connection connection, byte[] luid, byte[] ruid, FriendRequestDeclineCb callback) {
        IdentityFriendRequestDecline.request(connection, luid, ruid, callback);
    }

    public static void sign(byte[] uid, BsonDocument cert, SignCb callback) {
        IdentitySign.request(null, uid, cert, callback);
    }

    public static void sign(Connection connection, byte[] uid, BsonDocument cert, SignCb callback) {
        IdentitySign.request(connection, uid, cert, callback);
    }

    public static void verify(Cert cert, VerifyCb callback) {
        IdentityVerify.request(cert, callback);
    }

    public interface CreateCb extends Callback {
        public void cb(MistIdentity identity);
    }

    public interface ExportCb extends Callback {
        public void cb(byte[] data, byte[] raw);
    }

    public interface ImportCb extends Callback {
        public void cb(String name, byte[] uid);
    }

    public interface ListCb extends Callback {
        public void cb(ArrayList<MistIdentity> identityList);
    }

    public interface GetCb extends Callback {
        public void cb(MistIdentity identity);
    }

    public interface RemoveCb extends Callback {
        public void cb(boolean state);
    }

    public interface FriendRequestCb extends Callback {
        public void cb(boolean state);
    }

    public interface FriendRequestListCb extends Callback {
        public void cb(ArrayList<Friend> identity);
    }

    public interface FriendRequestAcceptCb extends Callback {
        public void cb(boolean state);
    }

    public interface FriendRequestDeclineCb extends Callback {
        public void cb(boolean state);
    }

    public interface SignCb extends Callback {
        public void cb(byte[] data);
    }

    public interface VerifyCb extends Callback {
        public void cb(boolean data);
    }
}
