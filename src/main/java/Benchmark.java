import com.google.gson.Gson;
import org.javatuples.Pair;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Benchmark {
    public static void main(String[] args) throws IOException {
        final int numUplinks = 32;

        List<Pair<IReconfigurationSolver, String>> solvers = new ArrayList<>();
        solvers.add(new Pair<>(new BruteForceSolver(60), "bruteForce"));
        solvers.add(new Pair<>(new GreedySolver(), "greedy"));
        solvers.add(new Pair<>(new BipartitionSolver(new BruteForceTwoRackSolver()), "google"));
        solvers.add(new Pair<>(new BipartitionSolver(new McfTwoRackSolver()), "ours"));

        String inputPath = args[0];
        String outputPath = args[1];

        TrafficMatrix jsonTrafficMatrix;
        try (FileReader fileReader = new FileReader(inputPath)) {
            Gson gson = new Gson();
            jsonTrafficMatrix = gson.fromJson(fileReader, TrafficMatrix.class);
        }
        IntArray2D[] traffic = new IntArray2D[jsonTrafficMatrix.getData().length];
        for (int i = 0; i < traffic.length; i++) {
            traffic[i] = new IntArray2D(jsonTrafficMatrix.getData()[i]);
        }

        Map<String, BenchmarkResult[]> benchmarkResults = new LinkedHashMap<>();
        for (Pair<IReconfigurationSolver, String> i : solvers) {
            IReconfigurationSolver solver = i.getValue0();
            String solverName = i.getValue1();
            for (int numOcses : new int[]{4, 8, 16}) {
                BenchmarkResult[] results = Benchmarker.benchmark(traffic, solver, numOcses, numUplinks);
                benchmarkResults.put(solverName + "_" + numOcses, results);
            }
        }

        try (FileWriter fileWriter = new FileWriter(outputPath)) {
            Gson gson = new Gson();
            gson.toJson(benchmarkResults, fileWriter);
        }
    }
}
