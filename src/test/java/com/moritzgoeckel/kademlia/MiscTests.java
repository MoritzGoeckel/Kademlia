package com.moritzgoeckel.kademlia;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

public class MiscTests {

    @Test
    public void hashKeyTest(){
        Assert.assertThat("a and be are different", !HashKey.fromRandom().equals(HashKey.fromRandom()), is(true));

        Assert.assertThat("these are the same",
                HashKey.fromString("abc").equals(HashKey.fromString("abc")),
                is(true));

        Assert.assertThat("these are the different",
                !HashKey.fromString("abc").equals(HashKey.fromString("abcd")),
                is(true));

        HashKey a = HashKey.fromString("abc");
        HashKey b = HashKey.fromString("abcd");
        HashKey c = HashKey.fromString("abcd");

        Assert.assertThat("distance is the same both ways",
                a.getDistance(b).equals(b.getDistance(a)),
                is(true));

        Assert.assertThat("distance is bigger than 0",
                a.getDistance(b).compareTo(new BigInteger("0")) > 0,
                is(true));

        Assert.assertThat("triangle inequality",
                a.getDistance(b).add(b.getDistance(c)).compareTo(a.getDistance(c)) >= 0,
                is(true));

        Assert.assertThat("com.moritzgoeckel.kademlia.HashKey equals should work", HashKey.fromString("Hello").equals(HashKey.fromString("Hello")), is(true));
    }

    @Test
    public void keyValuePairTests(){
        KeyValuePair p = new KeyValuePair(HashKey.fromString("A"), "B");
        Assert.assertThat("Checking key", p.getKey(), is(HashKey.fromString("A")));
        Assert.assertThat("Checking value", p.getValue(), is("B"));
        Assert.assertThat("Checking string", p.toString(), is(HashKey.fromString("A") + " -> B"));
        Assert.assertThat("Checking hashCode", p.hashCode() != new KeyValuePair(HashKey.fromString("A"), "C").hashCode(), is(true));

    }

    @Test
    public void remoteNodesOrKeyValuePairTests() {
        RemoteNodesOrKeyValuePair a = new RemoteNodesOrKeyValuePair(new KeyValuePair(HashKey.fromString("A"), "B"));
        Assert.assertThat("Checking pair", a.getPair().getValue(), is("B"));
        Assert.assertThat("Checking nodes", a.getRemoteNodes(), nullValue());


        RemoteNodesOrKeyValuePair b = new RemoteNodesOrKeyValuePair(new INode[]{
                new Node(0, "http://localhost", 10, false, false),
                new Node(0, "http://localhost", 10, false, false)
        });
        Assert.assertThat("Checking pair", b.getPair(), nullValue());
        Assert.assertThat("Checking nodes", b.getRemoteNodes().length, is(2));
    }

    @Test
    public void utilNodesTest() {
        RMINodeConnection r = new RMINodeConnection("localhost", 3000);
        assert(r.getAddress().equals("localhost"));
        assert(r.getPort() == 3000);
    }
}
