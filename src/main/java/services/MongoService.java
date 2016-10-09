package services;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * Created by Solovyev on 08/10/2016.
 */
public interface MongoService {
    MongoClient getClient();
    MongoDatabase getDb();
}
