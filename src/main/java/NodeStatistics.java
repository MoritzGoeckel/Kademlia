import java.util.HashMap;

public class NodeStatistics {

    private HashMap<String, Integer> stats = new HashMap<>();

    public void recordEvent(String name){
        if(!stats.containsKey(name))
            stats.put(name, 0);

        stats.put(name, stats.get(name) + 1);
    }

    @Override
    public String toString() {
        return stats.toString();
    }
}
