import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Supplier;

public class PerformanceTests {

    private static Random R = new Random();

    private static int PORT = -1;
    private static URL ADDRESS;
    static {
        try {
            ADDRESS = new URL("http://localhost");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }

    @Test
    public void getNSetStatistics(){
        LinkedList<Node> nodes = new LinkedList<>();

        Node firstNode = new Node(PORT, ADDRESS, 5);
        nodes.add(firstNode);

        for(int i = 0; i < 1000; i++)
            nodes.add(new Node(new LocalNode(nodes.get(R.nextInt(nodes.size())), PORT, ADDRESS), PORT, ADDRESS, 5));

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

    //Todo: Churn statistics
}
