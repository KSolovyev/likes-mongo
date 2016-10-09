import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Тест демонстрирует, что с помощью повторений действительно можно отловить проблемы с многопоточностью
 */
@RunWith(RepeatRunner.class)
public class ConcurrentModificationTest {
    @Test
    @Repeat(10000)
    @Ignore
    public void concurrentTest() throws InterruptedException {
        ExecutorService executorService = null;
        try {
            final List<Runnable> tasks = new ArrayList<>();
            final Map<Integer, Integer> map = new HashMap<>();
            tasks.add(() -> {
                        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                            final Integer val = entry.getValue();
                        }
                    }
            );
            tasks.add(() -> map.put(1, 1));
            tasks.add(() -> map.put(2, 1));
            tasks.add(() -> map.put(3, 1));
            tasks.add(() -> map.put(4, 1));
            tasks.add(() -> map.put(5, 1));
            tasks.add(() -> map.put(1, 1));
            tasks.add(() -> map.put(2, 1));
            tasks.add(() -> map.put(3, 1));
            tasks.add(() -> map.put(4, 1));
            tasks.add(() -> map.put(5, 1));
            tasks.add(() -> map.put(6, 1));
            tasks.add(() -> map.put(7, 1));

            executorService = Executors.newFixedThreadPool(tasks.size());

            final ArrayList<Future<?>> likeFutures = new ArrayList<>();
            for (Runnable task : tasks) {
                final Future<?> likeFuture = executorService.submit(task);
                likeFutures.add(likeFuture);
            }
            waitForAll(likeFutures);
        } finally {
            if (executorService != null)
                executorService.shutdown();
        }
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

}
