import java.util.HashMap;
import java.util.Map;

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

    public void print(int divider){
        System.out.println("---------Stats---------");
        for(Map.Entry<String, Integer> e : stats.entrySet())
            System.out.println(e.getKey() + "\t" + (e.getValue() / (double)divider));
        System.out.println("-----------------------");
    }
}
