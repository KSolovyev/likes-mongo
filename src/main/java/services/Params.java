package services;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Solovyev on 08/10/2016.
 */
public interface Params {
    @NotNull
    String getHost();
    int getPort();
    @NotNull
    String getDb();
    @NotNull
    String getUserCollectionName();
}
