public class BruteForceSolver implements IReconfigurationSolver {
    final double timeout;

    public BruteForceSolver(double timeout) {
        this.timeout = timeout;
    }

    @Override
    public IntArray3D solve(IntArray2D logicalTopology, IntArray3D physicalTopology) {
        return ReconfigurationSolver.bruteForce(logicalTopology, physicalTopology, timeout);
    }
}
