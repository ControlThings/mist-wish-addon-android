package bson;

import android.util.Log;

import org.bson.BsonArray;
import org.bson.BsonBinaryWriter;
import org.bson.BsonBinaryWriterSettings;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriterSettings;
import org.bson.FieldNameValidator;
import org.bson.io.BsonOutput;

/**
 * Created by jeppe on 10/27/17.
 */

public class BsonExtendedBinaryWriter extends BsonBinaryWriter implements BsonExtendedWriter{


    public BsonExtendedBinaryWriter(BsonOutput bsonOutput) {
        super(bsonOutput);
    }

    public BsonExtendedBinaryWriter(BsonOutput bsonOutput, FieldNameValidator validator) {
        super(bsonOutput, validator);
    }

    public BsonExtendedBinaryWriter(BsonWriterSettings settings, BsonBinaryWriterSettings binaryWriterSettings, BsonOutput bsonOutput) {
        super(settings, binaryWriterSettings, bsonOutput);
    }

    public BsonExtendedBinaryWriter(BsonWriterSettings settings, BsonBinaryWriterSettings binaryWriterSettings, BsonOutput bsonOutput, FieldNameValidator validator) {
        super(settings, binaryWriterSettings, bsonOutput, validator);
    }

    @Override
    public void pipeArray(BsonArray array) {
        BsonDocument bsonDocument = new BsonDocument();
        bsonDocument.append("array", array);

        BsonReader reader = new BsonDocumentReader(bsonDocument);

        reader.readStartDocument();
        readArray(reader);
        reader.readEndDocument();
    }


    private void readArray(BsonReader reader) {
        reader.readStartArray();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            switch (reader.getCurrentBsonType()) {
                case BOOLEAN:
                    this.writeBoolean(reader.readBoolean());
                    break;
                case INT32:
                    this.writeInt32(reader.readInt32());
                    break;
                case INT64:
                    this.writeInt64(reader.readInt64());
                    break;
                case DOUBLE:
                    this.writeDouble(reader.readDouble());
                    break;
                case STRING:;
                    this.writeString(reader.readString());
                    break;
                case BINARY:
                    this.writeBinaryData(reader.readBinaryData());
                    break;
                case DOCUMENT:
                    this.pipe(reader);
                    break;
                case ARRAY:
                    this.writeStartArray();
                    readArray(reader);
                    this.writeEndArray();
                    break;

            }
        }
        reader.readEndArray();
    }

}
