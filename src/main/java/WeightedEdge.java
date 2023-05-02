import org.javatuples.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;

public class WeightedEdge extends DefaultWeightedEdge {
    public Pair<Integer, Integer> getSrcDst() {
        String s = toString();
        int i = s.indexOf(":");
        int a = Integer.parseInt(s.substring(1, i-1));
        int b = Integer.parseInt(s.substring(i+2, s.length()-1));
        return new Pair<>(a, b);
    }
}
