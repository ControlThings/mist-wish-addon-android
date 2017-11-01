package mistNodeApi.wishRequest;

import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonWriter;
import org.bson.io.BasicOutputBuffer;

import mistNodeApi.Connection;

/**
 * Created by jeppe on 10/18/16.
 */
class ConnectionRequest {

    static final private String op = "connections.request";

    static BasicOutputBuffer getBuffer(Connection connection, String typeOp, GetRequestArgs args) {

        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonWriter writer = new BsonBinaryWriter(buffer);
        writer.writeStartDocument();

        writer.writeStartArray("args");

        writer.writeStartDocument();
        writer.writeBinaryData("luid", new BsonBinary(connection.getLuid()));
        writer.writeBinaryData("ruid", new BsonBinary(connection.getRuid()));
        writer.writeBinaryData("rhid", new BsonBinary(connection.getRhid()));
        writer.writeEndDocument();

        writer.writeString(typeOp);

        writer.writeStartArray();
        args.args(writer);
        writer.writeEndArray();

        writer.writeEndArray();
        writer.writeEndDocument();
        writer.flush();

        return buffer;
    }

    static String getOp() {
        return op;
    }

    interface GetRequestArgs {
        void args(BsonWriter writer);
    }

}
