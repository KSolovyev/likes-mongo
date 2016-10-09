package impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.jetbrains.annotations.NotNull;
import services.MongoService;
import services.Params;

/**
 * Created by Solovyev on 08/10/2016.
 */
public class MongoServiceImpl implements MongoService {

    private final MongoClient mongoClient;
    private final Params params;


    public MongoServiceImpl(@NotNull Params params) {
        mongoClient = new MongoClient(params.getHost(), params.getPort());
        this.params = params;
    }

    @Override
    @NotNull
    public MongoDatabase getDb() {
        return mongoClient.getDatabase(params.getDb());
    }

    @Override
    @NotNull
    public MongoClient getClient() {
        return mongoClient;
    }
}
