import gurobi.GRB;

public class BruteForceTwoRackSolver implements ITwoRackSolver {
    BruteForceTwoRackSolver() {
    }

    public IntArray3D solve(IntArray2D logicalTopology, IntArray3D physicalTopology) {
        return ReconfigurationSolver.bruteForce(logicalTopology, physicalTopology, GRB.INFINITY);
    }
}
