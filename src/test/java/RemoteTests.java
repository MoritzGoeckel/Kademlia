import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

public class RemoteTests {

    private static Random R = new Random();

    private static String ADDRESS = "localhost";

    @Test
    public void twoNodesRemoteTest() {
        Node firstNode = new Node(3000, ADDRESS, 10, true);
        Node secondNode = new Node(new RemoteNode("localhost", 3000, firstNode.getNodeId()), 3001, ADDRESS, 10, true);

        firstNode.performPing();
        secondNode.performPing();

        firstNode.setValue("Hello", "world", 1);

        Assert.assertThat("Should be able to retrieve the set value",
                secondNode.getValue("Hello", 1),
                is("world"));

        Assert.assertThat("Should be able to retrieve the set value",
                firstNode.getValue("Hello", 1),
                is("world"));
    }

    @Test
    public void utilNodesTest() {
        HashKey k = HashKey.fromRandom();
        RemoteNode r = new RemoteNode("localhost", 3000, k);
        assert(r.getAddress().equals("localhost"));
        assert(r.getPort() == 3000);
        assert(r.getNodeId().equals(k));
    }

    //TODO: Many nodes remote test
    //TODO: Shutdown tests
    //TODO: Ping test
}
