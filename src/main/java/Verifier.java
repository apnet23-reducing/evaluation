public class Verifier {
    public static boolean isValidLogicalTopology(IntArray2D topo, int numUplinks) {
        if (topo.getDim1() != topo.getDim2()) {
            return false;
        }
        int numRacks = topo.getDim1();
        int[] t = new int[numRacks];
        for (int i = 0; i < numRacks; i++) {
            int s = 0;
            for (int j = 0; j < numRacks; j++) {
                int k = topo.at(i, j);
                if (k < 0) {
                    return false;
                }
                s += k;
                t[j] += k;
            }
            if (s != numUplinks) {
                return false;
            }
        }
        for (int i : t) {
            if (i != numUplinks) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidPhysicalTopology(IntArray3D physicalTopology, IntArray2D logicalTopology) {
        for (int i = 0; i < physicalTopology.getDim1(); i++) {
            for (int j = 0; j < physicalTopology.getDim2(); j++) {
                for (int k = 0; k < physicalTopology.getDim3(); k++) {
                    if (physicalTopology.at(i, j, k) < 0) {
                        return false;
                    }
                }
            }
        }
        return physicalTopology.sum(2).equals(logicalTopology);
    }
}
