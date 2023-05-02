import org.javatuples.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.Map;
import java.util.function.Function;

import static java.lang.Math.round;

public class TransportationProblemSolver {
    public TransportationProblemSolver() {
    }

    public IntArray2D solve(int[] supplies, int[] demands, IntArray2D costs, IntArray2D capacities) {

        int numSupplies = supplies.length;
        int numDemands = demands.length;
        Graph<Integer, WeightedEdge> g = new DefaultDirectedWeightedGraph<>(WeightedEdge.class);
        for (int i = 0; i < numSupplies + numDemands; ++i) {
            g.addVertex(i);
        }
        for (int i = 0; i < numSupplies; ++i) {
            for (int j = 0; j < numDemands; ++j) {
                WeightedEdge e = g.addEdge(i, j + numSupplies);
                g.setEdgeWeight(e, costs.at(i, j));
            }
        }
        Function<Integer, Integer> supply = (i) -> {
            if (i < numSupplies) {
                return supplies[i];
            } else {
                return -demands[i - numSupplies];
            }
        };
        Function<WeightedEdge, Integer> capacity = (i) -> {
            Pair<Integer, Integer> srcDst = i.getSrcDst();
            return capacities.at(srcDst.getValue0(), srcDst.getValue1() - numSupplies);
        };
        MinimumCostFlowProblem<Integer, WeightedEdge> mcfProblem =
                new MinimumCostFlowProblem.MinimumCostFlowProblemImpl<>(g, supply, capacity);
        MinimumCostFlowAlgorithm<Integer, WeightedEdge> solver = new CapacityScalingMinimumCostFlow<>();
        Map<WeightedEdge, Double> flows = solver.getMinimumCostFlow(mcfProblem).getFlowMap();
        IntArray2D result = new IntArray2D(numSupplies, numDemands);
        for (WeightedEdge e : flows.keySet()) {
            Pair<Integer, Integer> srcDst = e.getSrcDst();
            result.set(srcDst.getValue0(), srcDst.getValue1() - numSupplies, (int) (round(flows.get(e))));
        }
        return result;
    }
}
