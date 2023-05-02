import org.javatuples.Pair;

public class Benchmarker {
    public static BenchmarkResult[] benchmark(IntArray2D[] traffic, IReconfigurationSolver reconfigurationSolver,
                                              int numOcses, int numUplinks) {
        BenchmarkResult[] results = new BenchmarkResult[traffic.length - 1];
        IntArray2D logicalTopology = TopologyGenerator.getLogicalTopology(traffic[0], numUplinks);
        if (!Verifier.isValidLogicalTopology(logicalTopology, numUplinks)) {
            throw new RuntimeException();
        }
        IntArray3D physicalTopology = TopologyGenerator.getPhysicalTopology(logicalTopology, numOcses);
        if (!Verifier.isValidPhysicalTopology(physicalTopology, logicalTopology)) {
            throw new RuntimeException();
        }
        for (int i = 0; i < results.length; i++) {
            logicalTopology = TopologyGenerator.getLogicalTopology(traffic[i + 1], numUplinks);
            if (!Verifier.isValidLogicalTopology(logicalTopology, numUplinks)) {
                throw new RuntimeException();
            }
            IntArray2D finalLogicalTopology = logicalTopology;
            IntArray3D finalPhysicalTopology = physicalTopology;
            Pair<Long, IntArray3D> result = Timer.measure(() ->
                    reconfigurationSolver.solve(finalLogicalTopology, finalPhysicalTopology));
            long time = result.getValue0();
            IntArray3D newPhysicalTopology = result.getValue1();
            if (newPhysicalTopology == null) {
                for (int j = i; j < results.length; j++) {
                    results[j] = new BenchmarkResult(-1, -1);
                }
                break;
            }
            if (!Verifier.isValidPhysicalTopology(newPhysicalTopology, logicalTopology)) {
                throw new RuntimeException();
            }
            int cost = ReconfigurationSolver.getReconfigurationCost(physicalTopology, newPhysicalTopology);
            results[i] = new BenchmarkResult(time, cost);
            physicalTopology = newPhysicalTopology;
        }
        return results;
    }
}
