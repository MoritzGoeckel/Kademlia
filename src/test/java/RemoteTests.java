import com.moritzgoeckel.kademlia.IKademliaNode;
import com.moritzgoeckel.kademlia.Node;
import com.moritzgoeckel.kademlia.RemoteNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

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

    //TODO: Many nodes remote test
    //TODO: Shutdown tests
    //TODO: Ping test
}
