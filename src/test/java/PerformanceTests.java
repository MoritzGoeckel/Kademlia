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

        Node firstNode = new Node(PORT, ADDRESS, 100);
        nodes.add(firstNode);

        for(int i = 0; i < 10_000; i++)
            nodes.add(new Node(new LocalNode(nodes.get(R.nextInt(nodes.size())), PORT, ADDRESS), PORT, ADDRESS, 100));

        Supplier<Node> randomNode = () -> nodes.get(R.nextInt(nodes.size() - 1));
        nodes.forEach(Node::performPing);

        Node.resetStatistics();

        for(int i = 0; i < 10_000; i++)
            randomNode.get().setValue("Hello" + i, "world", 1);

        System.out.println("Storing procedure (10_000): " + Node.getStatistics());
        Node.resetStatistics();

        int fails = 0;
        for(int i = 0; i < 10_000; i++)
            if(randomNode.get().getValue("Hello" + i, 50) == null)
                fails++;

        System.out.println("Lookup procedure (10_000): " + Node.getStatistics());
        System.out.println("Failed lookups: " + fails);
    }
}
