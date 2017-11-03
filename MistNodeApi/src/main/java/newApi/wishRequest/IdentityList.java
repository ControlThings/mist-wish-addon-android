package newApi.wishRequest;

import org.bson.BSONException;
import org.bson.BsonArray;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

import java.util.ArrayList;
import java.util.List;

import node.MistNode;

class IdentityList {
    static int request(Identity.ListCb callback) {
        String op = "identity.list";


        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeString("op", op);

        writer.writeStartArray("args");
        writer.writeEndArray();

        writer.writeInt32("id", 0);

        writer.writeEndDocument();
        writer.flush();

        return MistNode.getInstance().wishRequest(buffer.toByteArray(), new MistNode.RequestCb() {
            Identity.ListCb cb;

            @Override
            public void response(byte[] data) {
                try {
                    BsonDocument bson = new RawBsonDocument(data);
                    BsonArray bsonList = bson.getArray("data");
                    List<newApi.Identity> list = new ArrayList<newApi.Identity>();
                    for (BsonValue bsonIdentity : bsonList) {
                        list.add(newApi.Identity.fromBson(bsonIdentity.asDocument()));
                    }
                    cb.cb(list);
                } catch (BSONException e) {
                    cb.err(Callback.BSON_ERROR_CODE, Callback.BSON_ERROR_STRING);
                }
            }

            @Override
            public void end() {
                cb.end();
            }

            @Override
            public void err(int code, String msg) {
                cb.err(code, msg);
            }

            private MistNode.RequestCb init(Identity.ListCb callback) {
                this.cb = callback;
                return this;
            }

        }.init(callback));
    }
}
