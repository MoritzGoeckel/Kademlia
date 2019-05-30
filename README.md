# Kademlia

This is an implementation of the distributed hash table Kademlia

## Getting started

``` java
Node firstNode = new Node(3000, ADDRESS, 10);
Node secondNode = new Node(new RemoteNode("localhost", 3000, firstNode.getNodeId()), 3001, ADDRESS, 10);

firstNode.setValue("Hello", "world", 1);

Assert.assertThat("Should be able to retrieve the set value",
        secondNode.getValue("Hello", 1),
        is("world"));

Assert.assertThat("Should be able to retrieve the set value",
        firstNode.getValue("Hello", 1),
        is("world"));
```

## API

``` java
/** Stores a key value pair in the network */
void setValue(String key, String value, int k);

/** Returns the value for a key */
String getValue(String key, int k);

/** Shuts the node down */
void shutdown();

boolean isShutdown();
```

## Report

Download the report and documentation here: [KademliaReport.pdf](https://github.com/MoritzGoeckel/Kademlia/raw/master/KademliaReport.pdf)
