import org.junit.Test;

import java.util.LinkedList;
import java.util.Random;
import java.util.function.Supplier;

public class PerformanceTests {

    private static Random R = new Random();

    private static int PORT = -1;
    private static String ADDRESS = "localhost";

    @Test
    public void getNSetStatistics(){
        LinkedList<Node> nodes = new LinkedList<>();

        Node firstNode = new Node(PORT, ADDRESS, 5, false);
        nodes.add(firstNode);

        for(int i = 0; i < 1000; i++)
            nodes.add(new Node(nodes.get(R.nextInt(nodes.size())), PORT, ADDRESS, 5, false));

        Supplier<Node> randomNode = () -> nodes.get(R.nextInt(nodes.size() - 1));
        nodes.forEach(Node::performPing);

        Node.resetStatistics();

        for(int i = 0; i < 200; i++)
            randomNode.get().setValue("Hello" + i, "world", 10);

        System.out.println("Store");
        Node.getStatistics().print(100);
        Node.resetStatistics();

        int fails = 0;
        for(int i = 0; i < 200; i++)
            if(randomNode.get().getValue("Hello" + i, 50) == null)
                fails++;

        System.out.println("Lookup");
        Node.getStatistics().print(100);
        System.out.println("Failed lookups: " + fails);
    }

    //Todo: Get not found value test (performance?)
}
