import org.javatuples.Pair;

import java.util.Arrays;

public class IntArray2D implements Cloneable {
    private int[] data;
    private final int dim1;
    private final int dim2;

    public IntArray2D(int dim1, int dim2) {
        this.dim1 = dim1;
        this.dim2 = dim2;
        data = new int[dim1 * dim2];
    }

    public IntArray2D(int[][] arr) {
        dim1 = arr.length;
        dim2 = arr[0].length;
        data = new int[dim1 * dim2];
        for (int i = 0; i < dim1; ++i) {
            if (arr[i].length != dim2) {
                throw new RuntimeException();
            }
            System.arraycopy(arr[i], 0, data, i * dim2, dim2);
        }
    }

    public int at(int i1, int i2) {
        check(i1, i2);
        return data[i1 * dim2 + i2];
    }

    public void set(int i1, int i2, int val) {
        check(i1, i2);
        data[i1 * dim2 + i2] = val;
    }

    public void add(int i1, int i2, int val) {
        check(i1, i2);
        data[i1 * dim2 + i2] += val;
    }

    private void check(int i1, int i2) {
        if (i1 >= dim1 || i2 >= dim2 || i1 < 0 || i2 < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public IntArray2D clone() {
        try {
            IntArray2D clone = (IntArray2D) super.clone();
            clone.data = data.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public int getDim1() {
        return dim1;
    }

    public int getDim2() {
        return dim2;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        String sep = "";
        for (int i = 0; i < dim1; i++) {
            s.append(sep);
            s.append("[");
            String sep2 = "";
            for (int j = 0; j < dim2; j++) {
                s.append(sep2);
                s.append(at(i, j));
                sep2 = ", ";
            }
            s.append("]");
            sep = ", ";
        }
        s.append("]");
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntArray2D that = (IntArray2D) o;

        if (dim1 != that.dim1) return false;
        if (dim2 != that.dim2) return false;
        return Arrays.equals(data, that.data);
    }

}
