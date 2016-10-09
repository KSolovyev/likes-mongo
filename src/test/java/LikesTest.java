import com.mongodb.client.MongoCollection;
import impl.LikeServiceImpl;
import impl.MongoServiceImpl;
import impl.ParamsImpl;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.junit.*;
import org.junit.runner.RunWith;
import services.LikeService;
import services.MongoService;
import services.Params;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by Solovyev on 08/10/2016.
 */
@RunWith(RepeatRunner.class)
public class LikesTest {

    private Params params;
    private MongoService mongoService;
    private LikeService likeService;


    @Before
    public void setup() throws IOException {
        params = new ParamsImpl("mongo.test");
        mongoService = new MongoServiceImpl(params);
        likeService = new LikeServiceImpl(mongoService, params);

        clearUsers();
        final String[] usersRaw = {
                "{\"_id\":\"1\",\"name\":\"Vasya\", \"likes\":[]}",
                "{\"_id\":\"2\",\"name\":\"Petya\", \"likes\":[]}",
                "{\"_id\":\"3\",\"name\":\"Kolya\", \"likes\":[]}",
                "{\"_id\":\"4\",\"name\":\"Dima\", \"likes\":[]}",
                "{\"_id\":\"5\",\"name\":\"Antuan\", \"likes\":[]}",
                "{\"_id\":\"6\",\"name\":\"Sisilia\", \"likes\":[]}",
                "{\"_id\":\"7\",\"name\":\"Sophia\", \"likes\":[]}",
                "{\"_id\":\"8\",\"name\":\"Anastatia\", \"likes\":[]}",
                "{\"_id\":\"9\",\"name\":\"Adam\", \"likes\":[]}",
                "{\"_id\":\"10\",\"name\":\"Eva\", \"likes\":[]}",
        };

        final List<Document> users = Stream.of(usersRaw).map(Document::parse).collect(Collectors.toList());
        getUsers().insertMany(users);
    }

    @Test
    public void simpleLikeTest() {
        likeService.like("1", "2");
        Assert.assertEquals(1, likeService.getLikes("2"));
    }

    @Test
    public void selfLikeTest() {
        likeService.like("2", "2");
        Assert.assertEquals(0, likeService.getLikes("2"));
    }

    @Test
    public void doubleLikeTest() {
        likeService.like("1", "2");
        likeService.like("1", "2");
        Assert.assertEquals(1, likeService.getLikes("2"));
    }

    @Test
    public void severalLikeTest() {
        likeService.like("1", "2");
        likeService.like("3", "2");
        likeService.like("4", "2");
        likeService.like("5", "2");
        Assert.assertEquals(4, likeService.getLikes("2"));
    }

    @Test
    public void severalDoubleLikeTest() {
        likeService.like("1", "2");
        likeService.like("1", "2");
        likeService.like("3", "2");
        likeService.like("3", "2");
        likeService.like("4", "2");
        likeService.like("4", "2");
        likeService.like("5", "2");
        Assert.assertEquals(4, likeService.getLikes("2"));
    }

    @Test
    public void noLikeTest() {
        Assert.assertEquals(0, likeService.getLikes("2"));
    }

    @Test
    public void noUserTest() {
        Assert.assertEquals(0, likeService.getLikes("20"));
    }

    @Test
    public void wrongUserTest() {
        likeService.like("5", "25");
        likeService.like("25", "5");
        Assert.assertEquals(0, likeService.getLikes("25"));
        Assert.assertEquals(1, likeService.getLikes("5")); //Лайк получен от несуществующего пользователя. Это нормально, пока мы нигде не получаем id напрямую от пользователя
    }

    //Для того чтобы быть уверенным что с многопоточностью все более-менее в порядке нужно прогнать тест пару тысяч раз
    @Test
    @Repeat(1)
    public void concurrentLikeTest() throws InterruptedException {
        ExecutorService executorService = null;
        try {
            final List<Runnable> tasks = new ArrayList<>();
            final CountDownLatch startLiking = new CountDownLatch(1);
            tasks.add(new LikeJob("1", "2", startLiking));
            tasks.add(new LikeJob("3", "2", startLiking));
            tasks.add(new LikeJob("4", "2", startLiking));
            tasks.add(new LikeJob("4", "2", startLiking));
            tasks.add(new LikeJob("4", "2", startLiking));
            tasks.add(new LikeJob("5", "2", startLiking));
            tasks.add(new LikeJob("6", "2", startLiking));
            tasks.add(new LikeJob("7", "2", startLiking));
            tasks.add(new LikeJob("8", "2", startLiking));
            tasks.add(new LikeJob("9", "2", startLiking));
            tasks.add(new LikeJob("10", "2", startLiking));

            executorService = Executors.newFixedThreadPool(tasks.size());

            final ArrayList<Future<?>> likeFutures = new ArrayList<>();
            for (Runnable task : tasks) {
                final Future<?> likeFuture = executorService.submit(task);
                likeFutures.add(likeFuture);
            }
            startLiking.countDown();
            waitForAll(likeFutures);
            Assert.assertEquals(9, likeService.getLikes("2"));
        } finally {
            if (executorService != null)
                executorService.shutdown();
        }
    }


    @After
    public void tearDown() {
        clearUsers();
        mongoService.getClient().close();
    }

    private MongoCollection<Document> getUsers() {
        return mongoService.getDb().getCollection(params.getUserCollectionName());
    }

    private void clearUsers() {
        //noinspection unchecked
        mongoService.getDb().getCollection("user").deleteMany(new Document(Collections.EMPTY_MAP));
    }

    private void waitForAll(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException("Waiting for threads failed", e);
            }
        }
    }

    private final class LikeJob implements Runnable {
        @NotNull
        private String initiator;
        @NotNull
        private String target;
        @NotNull
        private CountDownLatch sync;

        private LikeJob(@NotNull String initiator, @NotNull String target, @NotNull CountDownLatch sync) {
            this.initiator = initiator;
            this.target = target;
            this.sync = sync;
        }

        @Override
        public void run() {
            try {
                sync.await();
                likeService.like(initiator, target);
            } catch (InterruptedException ignore) {
            }
        }
    }
}
