import com.moritzgoeckel.kademlia.Node;
import com.moritzgoeckel.kademlia.RemoteNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;

public class RemoteTests {

    private static Random R = new Random();
    private static String ADDRESS = "localhost";

    @Test
    public void twoNodesRemoteTest() {
        Node firstNode = new Node(3000, ADDRESS, 10);
        Node secondNode = new Node(new RemoteNode("localhost", 3000, firstNode.getNodeId()), 3001, ADDRESS, 10);

        firstNode.setValue("Hello", "world", 1);

        Assert.assertThat("Should be able to retrieve the set value",
                secondNode.getValue("Hello", 1),
                is("world"));

        Assert.assertThat("Should be able to retrieve the set value",
                firstNode.getValue("Hello", 1),
                is("world"));
    }

    @Test
    public void manyNodesRemoteTest() {
        System.out.println("Performing manyNodesRemoteTest... this could take a while");

        AtomicInteger port = new AtomicInteger(3000);

        LinkedList<Node> nodes = new LinkedList<>();

        Node firstNode = new Node(port.getAndIncrement(), ADDRESS, 10);
        nodes.add(firstNode);

        Consumer<Integer> addNodes = num -> {
            for(int i = 0; i < num; i++) {
                Node otherNode = nodes.get(R.nextInt(nodes.size()));
                nodes.add(new Node(new RemoteNode(otherNode.getAddress(), otherNode.getPort(), otherNode.getNodeId()), port.getAndIncrement(), ADDRESS, 10));
            }
        };

        Consumer<Integer> removeNodes = num -> {
            for(int i = 0; i < num; i++) {
                int index = R.nextInt(nodes.size() - 1);
                nodes.get(index).shutdown();
                nodes.remove(index);
            }
        };

        Supplier<Node> randomNode = () -> nodes.get(R.nextInt(nodes.size() - 1));

        addNodes.accept(20);
        nodes.forEach(Node::performPing);

        for(int i = 0; i < 100; i++) {
            Node from = randomNode.get();

            from.setValue("Hello" + i, "world", 6);

            removeNodes.accept(5);
            Node to = randomNode.get();

            Assert.assertThat("Should be able to retrieve the set value ("+i+")",
                    to.getValue("Hello" + i, 5),
                    is("world"));

            addNodes.accept(5);
            nodes.forEach(Node::performPing);

            System.out.println(i + " / 100");
        }
    }
}
