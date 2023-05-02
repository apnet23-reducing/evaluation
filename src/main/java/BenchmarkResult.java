public class BenchmarkResult {
    private final long time;
    private final int cost;

    BenchmarkResult(long time, int cost) {
        this.time = time;
        this.cost = cost;
    }

    public long getTime() {
        return time;
    }

    public int getCost() {
        return cost;
    }
}
