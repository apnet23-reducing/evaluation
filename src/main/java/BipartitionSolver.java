public class BipartitionSolver implements IReconfigurationSolver {
    ITwoRackSolver twoRackSolver;

    BipartitionSolver(ITwoRackSolver twoRackSolver) {
        this.twoRackSolver = twoRackSolver;
    }

    @Override
    public IntArray3D solve(IntArray2D logicalTopology, IntArray3D physicalTopology) {
        return ReconfigurationSolver.bipartition(logicalTopology, physicalTopology, twoRackSolver);
    }
}
