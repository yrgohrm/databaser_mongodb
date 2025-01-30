package se.yrgo.databaser.documents;

import java.math.*;

import org.bson.*;
import org.bson.codecs.pojo.annotations.*;
import org.bson.types.*;

public record Product(
        @BsonId 
        @BsonRepresentation(BsonType.OBJECT_ID) 
        String productId,
        String name,
        String description,
        BigDecimal price) {

    public Product(String name,
            String description,
            BigDecimal price) {
        this(new ObjectId().toHexString(), name, description, price);
    }
}
