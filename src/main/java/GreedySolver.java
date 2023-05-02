public class GreedySolver implements IReconfigurationSolver {
    @Override
    public IntArray3D solve(IntArray2D logicalTopology, IntArray3D physicalTopology) {
        return ReconfigurationSolver.greedy(logicalTopology, physicalTopology);
    }
}
