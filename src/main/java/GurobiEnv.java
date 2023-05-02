import gurobi.GRBEnv;
import gurobi.GRBException;

public class GurobiEnv {
    private GurobiEnv() {

    }

    private static class SingletonInstance {
        private static final GRBEnv instance;

        static {
            try {
                instance = new GRBEnv();
            } catch (GRBException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static GRBEnv getGurobiEnv() {
        return SingletonInstance.instance;
    }
}