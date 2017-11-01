package mistNodeApi;

import org.bson.BsonDocument;

/**
 * Created by jeppe on 8/15/17.
 */

public class Cert {

    private String alias;
    private BsonDocument cert;


    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public BsonDocument getCert() {
        return cert;
    }

    public void setCert(BsonDocument cert) {
        this.cert = cert;
    }
}
