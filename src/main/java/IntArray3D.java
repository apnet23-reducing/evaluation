import org.apache.commons.lang3.NotImplementedException;

public class IntArray3D implements Cloneable {
    private int[] data;
    private final int dim1;
    private final int dim2;
    private final int dim3;
    private final int dim23;

    public IntArray3D(int dim1, int dim2, int dim3) {
        data = new int[dim1 * dim2 * dim3];
        this.dim1 = dim1;
        this.dim2 = dim2;
        this.dim3 = dim3;
        dim23 = this.dim2 * this.dim3;
    }

    public int at(int d1, int d2, int d3) {
        if (d1 >= dim1 || d2 >= dim2 || d3 >= dim3) throw new IllegalArgumentException("Out of bound");
        return data[d1 * dim23 + d2 * dim3 + d3];
    }

    public void put(int d1, int d2, int d3, int newVal) {
        if (d1 >= dim1 || d2 >= dim2 || d3 >= dim3) throw new IllegalArgumentException("Out of bound");
        data[d1 * dim23 + d2 * dim3 + d3] = newVal;
    }

    public int getDim1() {
        return dim1;
    }

    public int getDim2() {
        return dim2;
    }

    public int getDim3() {
        return dim3;
    }

    @Override
    public IntArray3D clone() {
        try {
            IntArray3D clone = (IntArray3D) super.clone();
            clone.data = data.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void add(int d1, int d2, int d3, int val) {
        if (d1 >= dim1 || d2 >= dim2 || d3 >= dim3) throw new IllegalArgumentException("Out of bound");
        data[d1 * dim23 + d2 * dim3 + d3] += val;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int d3 = 0; d3 < dim3; d3++) {
            result.append("Printing layer ");
            result.append(dim3);
            result.append('\n');
            for (int d1 = 0; d1 < dim1; d1++) {
                for (int d2 = 0; d2 < dim2; d2++) {
                    result.append(at(d1, d2, d3));
                    result.append(' ');
                }
                result.append('\n');
            }
        }
        return result.toString();
    }

    public IntArray2D sum(int axis) {
        if (axis != 2) {
            throw new NotImplementedException();
        }
        IntArray2D result = new IntArray2D(dim1, dim2);
        for (int i = 0; i < dim1; i++) {
            for (int j = 0; j < dim2; j++) {
                for (int k = 0; k < dim3; k++) {
                    result.add(i, j, at(i, j, k));
                }
            }
        }
        return result;
    }
}
