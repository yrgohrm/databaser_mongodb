package se.yrgo.databaser.documents;

import org.bson.*;
import org.bson.codecs.pojo.annotations.*;
import org.bson.types.*;

public record InventoryLocation(
    @BsonId 
    @BsonRepresentation(BsonType.OBJECT_ID) 
    String inventoryLocationId,
    String location,
    @BsonRepresentation(BsonType.OBJECT_ID) 
    String productId,
    long quantity
) {
    public InventoryLocation(
            String location,
            String productId,
            long quantity) {
        this(new ObjectId().toHexString(), location, productId, quantity);
    }
}
