import org.javatuples.Pair;

import java.util.function.Supplier;

public class Timer {
    public static <T> Pair<Long, T> measure(Supplier<T> function) {
        int times = 1;
        T result = null;
        while (true) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < times; i++) {
                result = function.get();
            }
            long period = System.currentTimeMillis() - start;
            if (period > 1000) {
                long time = period * 1000 / times;
                return new Pair<>(time, result);
            }
            times *= 2;
        }
    }
}
