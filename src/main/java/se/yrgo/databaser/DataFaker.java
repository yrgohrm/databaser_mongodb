package se.yrgo.databaser;

import java.math.*;
import java.util.*;
import java.util.logging.*;

import org.bson.*;
import org.bson.types.*;

import com.mongodb.client.*;

import net.datafaker.*;
import se.yrgo.databaser.documents.*;

public final class DataFaker {
    private static final Logger logger = Logger.getLogger(DataFaker.class.getName());

    private DataFaker() {
    }

    public static void generateData(MongoDatabase db) {
        // same "random" things every time
        Faker faker = new Faker(new Random(4321));

        MongoCollection<Product> products = db.getCollection("products", Product.class);
        MongoCollection<InventoryLocation> inventoryLocations = db.getCollection("inventoryLocations",
                InventoryLocation.class);

        if (products.countDocuments() == 0 && inventoryLocations.countDocuments() == 0) {
            logger.info("Generating data into database.");

            List<ObjectId> productIds = generateProducts(products, faker);

            generateInventoryLocations(inventoryLocations, productIds, faker);
        }
        else {
            logger.info("No data generation into existing collections.");
        }

    }

    private static void generateInventoryLocations(MongoCollection<InventoryLocation> inventoryLocations,
            List<ObjectId> productIds, Faker faker) {
        
        var productIdsCopy = new ArrayList<>(productIds);
        Collections.shuffle(productIdsCopy);
        var productIterator = productIdsCopy.iterator();

        List<InventoryLocation> locationsToAdd = new ArrayList<>(productIds.size() + 100);

        // Give every product a place in the warehouse
        for (var isle : List.of("A", "B", "C", "D", "E")) {
            for (int i = 1; i <= productIds.size() / 5 && productIterator.hasNext(); ++i) {
                var productId = productIterator.next();
                InventoryLocation location = new InventoryLocation(isle + i, productId.toHexString(), faker.number().numberBetween(10, 100));
                locationsToAdd.add(location);
            }
        }

        // Then give some of them more places
        for (int i = 1; i <= 100; i++) {
            var productId = productIds.get(faker.number().numberBetween(0, productIds.size()));
            InventoryLocation location = new InventoryLocation("F" + i, productId.toHexString(), faker.number().numberBetween(10, 100));
            locationsToAdd.add(location);
        }

        var res = inventoryLocations.insertMany(locationsToAdd);
        if (!res.wasAcknowledged()) {
            throw new RuntimeException("Unable to save generated inventory locations to database");
        }

        logger.info(() -> "Generated " + res.getInsertedIds().size() + " inventory locations.");
    }

    private static List<ObjectId> generateProducts(MongoCollection<Product> products, Faker faker) {
        final int count = 1000;
        final List<Product> productsToAdd = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String name = faker.commerce().productName();
            String description = name + " of the most excellent quality by " + faker.commerce().vendor() + ".";
            BigDecimal price = BigDecimal.valueOf(faker.number().numberBetween(10, 100));

            Product product = new Product(name, description, price);
            productsToAdd.add(product);
        }

        var res = products.insertMany(productsToAdd);
        if (!res.wasAcknowledged()) {
            throw new RuntimeException("Unable to save generated products to database");
        }

        logger.info(() -> "Generated " + res.getInsertedIds().size() + " products.");

        return res.getInsertedIds().values().stream().map(BsonValue::asObjectId).map(BsonObjectId::getValue).toList();
    }
}
