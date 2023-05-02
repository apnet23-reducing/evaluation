import org.javatuples.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import gurobi.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.lang.Math.*;

public class ReconfigurationSolver {

    public static IntArray3D minCostFlowTwoRacks(IntArray2D logical, IntArray3D physical, Pair<Integer, Integer> ocsRatio) {
        if (physical.getDim3() != 2) {
            throw new RuntimeException();
        }
        int racks = logical.getDim1();
        int uplinks = 0;
        for (int i = 0; i < racks; i++) {
            uplinks += logical.at(0, i);
        }
        int a = uplinks * ocsRatio.getValue0() / (ocsRatio.getValue0() + ocsRatio.getValue1());
        Graph<Integer, WeightedEdge> g = new DirectedWeightedMultigraph<>(WeightedEdge.class);
        for (int i = 0; i < 2 * racks; i++) {
            g.addVertex(i); //[0, racks): supply; [racks, 2*racks): demand
        }
        Function<Integer, Integer> supply = (i) -> (i < racks) ? a : -a;
        Map<WeightedEdge, Integer> capacityMap = new HashMap<>();
        for (int i = 0; i < racks; i++) {
            for (int j = 0; j < racks; j++) {
                int t1 = physical.at(i, j, 0);
                int t2 = logical.at(i, j) - physical.at(i, j, 1);
                int x1 = min(t1, t2);
                int x2 = max(t1, t2);
                x1 = max(x1, 0);
                x2 = min(x2, logical.at(i, j));
                int capacity = x1;
                if (capacity != 0) {
                    WeightedEdge e1 = g.addEdge(i, j + racks);
                    g.setEdgeWeight(e1, -1);
                    capacityMap.put(e1, capacity);
                }
                capacity = x2 - x1;
                if (capacity != 0) {
                    WeightedEdge e2 = g.addEdge(i, j + racks);
                    g.setEdgeWeight(e2, 0);
                    capacityMap.put(e2, capacity);
                }
                capacity = logical.at(i, j) - x2;
                if (capacity != 0) {
                    WeightedEdge e3 = g.addEdge(i, j + racks);
                    g.setEdgeWeight(e3, 1);
                    capacityMap.put(e3, logical.at(i, j) - x2);
                }
            }
        }
        Function<WeightedEdge, Integer> capacity = capacityMap::get;
        MinimumCostFlowProblem<Integer, WeightedEdge> problem =
                new MinimumCostFlowProblem.MinimumCostFlowProblemImpl<>(g, supply, capacity);
        MinimumCostFlowAlgorithm<Integer, WeightedEdge> solver = new CapacityScalingMinimumCostFlow<>();
        Map<WeightedEdge, Double> flow = solver.getMinimumCostFlow(problem).getFlowMap();
        IntArray3D result = new IntArray3D(racks, racks, 2);
        for (WeightedEdge e : flow.keySet()) {
            Pair<Integer, Integer> srcDst = e.getSrcDst();
            result.add(srcDst.getValue0(), srcDst.getValue1() - racks, 0, (int) (round(flow.get(e))));
        }
        for (int i = 0; i < racks; i++) {
            for (int j = 0; j < racks; j++) {
                result.put(i, j, 1, logical.at(i, j) - result.at(i, j, 0));
            }
        }
        return result;
    }

    public static IntArray3D minCostFlowTwoRacks(IntArray2D logical, IntArray3D physical) {
        return minCostFlowTwoRacks(logical, physical, new Pair<>(1, 1));
    }

    public static IntArray3D bruteForce(IntArray2D logical, IntArray3D physical, double timeout) {
        int racks = physical.getDim1();
        int ocses = physical.getDim3();
        int uplinks = 0;
        for (int i = 0; i < racks; i++) {
            uplinks += logical.at(0, i);
        }
        int numRackOcsLinks = uplinks / ocses;

        try {
            GRBModel model = new GRBModel(GurobiEnv.getGurobiEnv());
            model.set(GRB.DoubleParam.TimeLimit, timeout);
            model.set(GRB.IntParam.OutputFlag, 0);
            GRBVar[][][] varsX = new GRBVar[racks][racks][ocses];
            for (int i = 0; i < racks; i++) {
                for (int j = 0; j < racks; j++) {
                    varsX[i][j] = model.addVars(ocses, GRB.INTEGER);
                }
            }
            GRBVar[][][] varsY = new GRBVar[racks][racks][ocses];
            for (int i = 0; i < racks; i++) {
                for (int j = 0; j < racks; j++) {
                    varsY[i][j] = model.addVars(ocses, GRB.INTEGER);
                }
            }
            model.update();
            GRBLinExpr expr = new GRBLinExpr();
            for (int i = 0; i < racks; i++) {
                for (int j = 0; j < racks; j++) {
                    for (int k = 0; k < ocses; k++) {
                        expr.addTerm(1, varsY[i][j][k]);
                    }
                }
            }
            model.setObjective(expr, GRB.MINIMIZE);
            for (int i = 0; i < racks; i++) {
                for (int k = 0; k < ocses; k++) {
                    GRBLinExpr exp = new GRBLinExpr();
                    for (int j = 0; j < racks; j++) {
                        exp.addTerm(1, varsX[i][j][k]);
                    }
                    model.addConstr(exp, GRB.EQUAL, numRackOcsLinks, "constr1");
                    GRBLinExpr exp2 = new GRBLinExpr();
                    for (int j = 0; j < racks; j++) {
                        exp2.addTerm(1, varsX[j][i][k]);
                    }
                    model.addConstr(exp2, GRB.EQUAL, numRackOcsLinks, "constr2");
                }
            }
            for (int i = 0; i < racks; i++) {
                for (int j = 0; j < racks; j++) {
                    GRBLinExpr exp = new GRBLinExpr();
                    for (int k = 0; k < ocses; k++) {
                        exp.addTerm(1, varsX[i][j][k]);
                    }
                    model.addConstr(exp, GRB.EQUAL, logical.at(i, j), "constr3");
                }
            }
            for (int i = 0; i < racks; i++) {
                for (int j = 0; j < racks; j++) {
                    for (int k = 0; k < ocses; k++) {
                        GRBLinExpr exp = new GRBLinExpr();
                        exp.addTerm(1, varsY[i][j][k]);
                        exp.addTerm(1, varsX[i][j][k]);
                        model.addConstr(exp, GRB.GREATER_EQUAL, physical.at(i, j, k), "constr4");

                        exp = new GRBLinExpr();
                        exp.addTerm(1, varsX[i][j][k]);
                        model.addConstr(exp, GRB.GREATER_EQUAL, 0, "constr5");

                        exp = new GRBLinExpr();
                        exp.addTerm(1, varsY[i][j][k]);
                        model.addConstr(exp, GRB.GREATER_EQUAL, 0, "constr6");
                    }
                }
            }
            model.optimize();
            IntArray3D result = new IntArray3D(racks, racks, ocses);
            for (int i = 0; i < racks; i++) {
                for (int j = 0; j < racks; j++) {
                    for (int k = 0; k < ocses; k++) {
                        result.put(i, j, k, (int) (round(varsX[i][j][k].get(GRB.DoubleAttr.X))));
                    }
                }
            }
            model.dispose();
            return result;
        } catch (GRBException e) {
            return null;
        }
    }

    public static IntArray3D bipartition(IntArray2D logical, IntArray3D physical, ITwoRackSolver twoRackSolver) {
        int racks = physical.getDim1();
        int ocses = physical.getDim3();
        if (ocses == 2) {
            return twoRackSolver.solve(logical, physical);
        }
        IntArray3D bipartiteSum = new IntArray3D(racks, racks, 2);
        IntArray3D a = new IntArray3D(racks, racks, ocses / 2);
        IntArray3D b = new IntArray3D(racks, racks, ocses - ocses / 2);
        for (int k = 0; k < ocses / 2; k++) {
            for (int j = 0; j < racks; j++) {
                for (int i = 0; i < racks; i++) {
                    bipartiteSum.add(i, j, 0, physical.at(i, j, k));
                    a.put(i, j, k, physical.at(i, j, k));
                }
            }
        }
        for (int k = ocses / 2; k < ocses; k++) {
            for (int j = 0; j < racks; j++) {
                for (int i = 0; i < racks; i++) {
                    bipartiteSum.add(i, j, 1, physical.at(i, j, k));
                    b.put(i, j, k - ocses / 2, physical.at(i, j, k));
                }
            }
        }
        IntArray3D c = twoRackSolver.solve(logical, bipartiteSum);
        IntArray2D c0 = new IntArray2D(racks, racks);
        IntArray2D c1 = new IntArray2D(racks, racks);
        for (int i = 0; i < racks; i++) {
            for (int j = 0; j < racks; j++) {
                c0.set(i, j, c.at(i, j, 0));
                c1.set(i, j, c.at(i, j, 1));
            }
        }
        IntArray3D d = ReconfigurationSolver.bipartition(c0, a, twoRackSolver);
        IntArray3D e = ReconfigurationSolver.bipartition(c1, b, twoRackSolver);
        IntArray3D res = new IntArray3D(racks, racks, d.getDim3() + e.getDim3());
        for (int k = 0; k < d.getDim3(); k++) {
            for (int j = 0; j < racks; j++) {
                for (int i = 0; i < racks; i++) {
                    res.put(i, j, k, d.at(i, j, k));
                }
            }
        }
        for (int k = 0; k < e.getDim3(); k++) {
            for (int j = 0; j < racks; j++) {
                for (int i = 0; i < racks; i++) {
                    res.put(i, j, k + d.getDim3(), e.at(i, j, k));
                }
            }
        }
        return res;
    }

    public static IntArray3D greedy(IntArray2D logicalTopology, IntArray3D physicalTopology) {
        int numRacks = logicalTopology.getDim1();
        int numOcses = physicalTopology.getDim3();
        IntArray3D result = new IntArray3D(numRacks, numRacks, numOcses);
        logicalTopology = logicalTopology.clone();
        for (int i = 0; i < numOcses; i++) {
            IntArray2D phy = new IntArray2D(numRacks, numRacks);
            IntArray2D min = new IntArray2D(numRacks, numRacks);
            IntArray2D max = new IntArray2D(numRacks, numRacks);
            for (int j = 0; j < numRacks; j++) {
                for (int k = 0; k < numRacks; k++) {
                    phy.set(j, k, physicalTopology.at(j, k, i));
                    double f = (double) logicalTopology.at(j, k) / (numOcses - i);
                    min.set(j, k, (int) floor(f));
                    max.set(j, k, (int) ceil(f));
                }
            }

            IntArray2D r = mcfOneRack(min, max, phy);
            for (int j = 0; j < numRacks; j++) {
                for (int k = 0; k < numRacks; k++) {
                    result.put(j, k, i, r.at(j, k));
                    logicalTopology.add(j, k, -r.at(j, k));
                }
            }
        }
        return result;
    }

    public static IntArray2D mcfOneRack(IntArray2D min, IntArray2D max, IntArray2D physicalTopology) {
        int numRacks = physicalTopology.getDim1();
        int numRackOcsLinks = 0;
        for (int i = 0; i < physicalTopology.getDim2(); i++) {
            numRackOcsLinks += physicalTopology.at(0, i);
        }
        int[] supplies = new int[numRacks];
        int[] demands = new int[numRacks];
        for (int i = 0; i < numRacks; i++) {
            supplies[i] = demands[i] = numRackOcsLinks;
        }
        for (int i = 0; i < numRacks; i++) {
            for (int j = 0; j < numRacks; j++) {
                supplies[i] -= min.at(i, j);
                demands[j] -= min.at(i, j);
            }
        }
        IntArray2D costs = new IntArray2D(numRacks, numRacks);
        IntArray2D capacities = new IntArray2D(numRacks, numRacks);
        for (int i = 0; i < numRacks; i++) {
            for (int j = 0; j < numRacks; j++) {
                capacities.set(i, j, max.at(i, j) - min.at(i, j));
                costs.set(i, j, min.at(i, j) < physicalTopology.at(i, j) ? -1 : 0);
            }
        }
        var transportationProblemSolver = new TransportationProblemSolver();
        IntArray2D sol = transportationProblemSolver.solve(supplies, demands, costs, capacities);
        for (int i = 0; i < numRacks; i++) {
            for (int j = 0; j < numRacks; j++) {
                sol.add(i, j, min.at(i, j));
            }
        }
        return sol;
    }

    public static int getReconfigurationCost(IntArray3D oldTopology, IntArray3D newTopology) {
        int s = 0;
        for (int i = 0; i < oldTopology.getDim1(); i++) {
            for (int j = 0; j < oldTopology.getDim2(); j++) {
                for (int k = 0; k < oldTopology.getDim3(); k++) {
                    s += max(oldTopology.at(i, j, k) - newTopology.at(i, j, k), 0);
                }
            }
        }
        return s;
    }
}
