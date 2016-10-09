package impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import services.LikeService;
import services.MongoService;
import services.Params;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Solovyev on 05/10/16.
 */
public class LikeServiceImpl implements LikeService {
    @NotNull
    private final MongoService mongoService;
    @NotNull
    private final Params params;

    public LikeServiceImpl(@NotNull MongoService mongoService, @NotNull Params params) {
        this.mongoService = mongoService;
        this.params = params;
    }

    @Override
    public void like(@NotNull String initiator, @NotNull String target) {
        if(initiator.equals(target)) {
            return;
        }
        final Document selector = new Document("_id",  target);
        final Document updater = new Document("$addToSet", new Document("likes", initiator));
        mongoService.getDb().getCollection(params.getUserCollectionName())
                .updateOne(selector, updater);
    }

    @Override
    public long getLikes(@NotNull String playerId) {
        final FindIterable<Document> userLikes = mongoService.getDb().getCollection(params.getUserCollectionName())
                .find(eq("_id", playerId));
        final MongoCursor<Document> iterator = userLikes.projection(Projections.include("likes")).iterator();
        if(!iterator.hasNext()) {
            return 0;
        }
        final Document user = iterator.next();
        return user.get("likes", ArrayList.class).size();
    }
}
