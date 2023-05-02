public class McfTwoRackSolver implements ITwoRackSolver {
    McfTwoRackSolver() {
    }

    public IntArray3D solve(IntArray2D logicalTopology, IntArray3D physicalTopology) {
        return ReconfigurationSolver.minCostFlowTwoRacks(logicalTopology, physicalTopology);
    }
}
