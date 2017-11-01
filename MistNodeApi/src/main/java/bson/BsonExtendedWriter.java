package bson;

import org.bson.BsonArray;
import org.bson.BsonWriter;

/**
 * Created by jeppe on 10/27/17.
 */

public interface BsonExtendedWriter extends BsonWriter {

    public void pipeArray(BsonArray array);

}