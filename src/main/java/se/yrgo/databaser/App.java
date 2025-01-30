package se.yrgo.databaser;

import static org.bson.codecs.configuration.CodecRegistries.*;

import java.util.*;
import java.util.logging.*;

import org.bson.codecs.configuration.*;
import org.bson.codecs.pojo.*;
import org.bson.types.*;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;

import se.yrgo.databaser.documents.*;

public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {

        // Just logging something to show how it's done
        logger.info("Starting application");

        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        String host = System.getenv("DB_HOST");

        String uri = String.format("mongodb://%s:%s@%s:27017/shop", username, password, host);

        ConnectionString connectionString = new ConnectionString(uri);

        // Create a codec registry given the package of our records
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().register("se.yrgo.databaser.documents").build());

        // Combine our codecs with the default codes so we can use both
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        // Create settings object for our client, we could change lots of
        // settings here if we wanted to.
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();

        // Create a client that is connected to our database server
        try (MongoClient mongoClient = MongoClients.create(clientSettings)) {

            // Get hold of the database we want to use
            MongoDatabase db = mongoClient.getDatabase("shop");
            
            // Generate some data if the database is empty
            DataFaker.generateData(db);


            // Some simple examples of what can be done

            // Get typed collections
            MongoCollection<Product> products = db.getCollection("products", Product.class);
            MongoCollection<InventoryLocation> inventoryLocations = db.getCollection("inventoryLocations", InventoryLocation.class);
            
            System.out.println("Antal produkter: " + products.countDocuments());
            System.out.println("Antal lageplatser: " + inventoryLocations.countDocuments());

            // Find a thing with "Bronze" in its name.
            Product bronzeThing = products.find(Filters.regex("name", ".*Bronze.*")).first();
            System.out.println(bronzeThing);

            // Find a inventory location of that bronze thing.
            InventoryLocation location = inventoryLocations.find(Filters.eq("productId", new ObjectId(bronzeThing.productId()))).first();
            System.out.println(location);

            // We can also get untyped collections if we need to
            var rawProducts = db.getCollection("products");

            // The result of the aggregate isn't a Product or an InventoryLocation and we either
            // need a third record, or just get a Document as we do here

            // find the document with the given id
            // joins it with its inventory locations
            // selects which fields should be present in the output

            var res = rawProducts.aggregate(
                List.of(
                    Aggregates.match(Filters.eq("_id", new ObjectId("679a3d4600c2902fe1794c4b"))),
                    Aggregates.lookup("inventoryLocations", "_id", "productId", "locations"),
                    Aggregates.project(Projections.fields(
                        Projections.include("name", "price", "locations.location", "locations.quantity")
                    ))
                )
            );

            // prints "all" results, which should be just one document due to the "match"
            for (var doc : res) {
                System.out.println(doc);
            }
        }
    }

}
