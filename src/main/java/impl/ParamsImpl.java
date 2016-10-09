package impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import services.Params;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Solovyev on 08/10/2016.
 */
public class ParamsImpl implements Params{
    private static final String DB_NAME = "db";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String USER_COLLECTION = "user";

    @NotNull
    private final String mongoParamsPrefix;
    @NotNull
    private final Properties properties;

    public ParamsImpl(@NotNull String prefix) throws IOException {
        properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("mongo.properties"));
        final String prefixRaw = prefix.trim();
        this.mongoParamsPrefix = prefixRaw.endsWith(".") ? prefixRaw : prefixRaw + '.';
        checkNotNull(properties.getProperty(mongoParamsPrefix + DB_NAME), "Can't find " + mongoParamsPrefix + DB_NAME +" param ");
        checkNotNull(properties.getProperty(mongoParamsPrefix + HOST), "Can't find " + mongoParamsPrefix + HOST +" param ");
        final String port = properties.getProperty(mongoParamsPrefix + PORT);
        checkNotNull(port, "Can't find " + mongoParamsPrefix + PORT +" param ");
        checkInt(port, "Port should be integer. Port specified: " + port);
    }


    @Override
    @NotNull
    public String getHost() {
        return properties.getProperty(mongoParamsPrefix + HOST);
    }

    @Override
    public int getPort() {
        return Integer.valueOf(properties.getProperty(mongoParamsPrefix + PORT));
    }

    @Override
    @NotNull
    public String getDb() {
        return properties.getProperty(mongoParamsPrefix + DB_NAME);
    }

    @Override
    @NotNull
    public String getUserCollectionName() {
        return USER_COLLECTION;
    }

    private static void checkNotNull(@Nullable Object obj, @NotNull String msg) {
        if(obj == null) {
            throw new IllegalStateException(msg);
        }
    }

    private static void checkInt(@NotNull String str, @NotNull String msg) {
       if(!str.matches("^-?\\d+$"))
           throw new IllegalStateException(msg);
    }
}
