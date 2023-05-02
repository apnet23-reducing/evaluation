import org.javatuples.Pair;

public class TopologyGenerator {
    public TopologyGenerator() {
    }

    public static IntArray2D getLogicalTopology(IntArray2D traffic, int numUplinks) {
        if (traffic.getDim1() != traffic.getDim2()) {
            throw new RuntimeException();
        }
        int numRacks = traffic.getDim1();
        int[] supplies = new int[numRacks];
        int[] demands = new int[numRacks];
        for (int i = 0; i < numRacks; i++) {
            supplies[i] = demands[i] = numUplinks;
        }
        IntArray2D costs = new IntArray2D(numRacks, numRacks);
        IntArray2D capacities = new IntArray2D(numRacks, numRacks);
        for (int i = 0; i < numRacks; i++) {
            for (int j = 0; j < numRacks; j++) {
                costs.set(i, j, -traffic.at(i, j));
                capacities.set(i, j, 1);
            }
        }

        var transportationProblemSolver = new TransportationProblemSolver();
        return transportationProblemSolver.solve(supplies, demands, costs, capacities);
    }

    public static IntArray3D getPhysicalTopology(IntArray2D logical, int ocses) {
        logical = logical.clone();
        int racks = logical.getDim1();
        IntArray3D physical = new IntArray3D(racks, racks, ocses);
        IntArray3D phy = new IntArray3D(racks, racks, 2);
        for (int i = 0; i < ocses - 1; i++) {
            IntArray3D r = ReconfigurationSolver.minCostFlowTwoRacks(logical, phy, new Pair<>(1, ocses - 1 - i));
            for (int j = 0; j < racks; j++) {
                for (int k = 0; k < racks; k++) {
                    physical.put(j, k, i, r.at(j, k, 0));
                    logical.set(j, k, logical.at(j, k) - r.at(j, k, 0));
                }
            }
        }
        for (int j = 0; j < racks; j++) {
            for (int k = 0; k < racks; k++) {
                physical.put(j, k, ocses - 1, logical.at(j, k));
            }
        }
        return physical;
    }

}
