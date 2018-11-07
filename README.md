# Kademlia

This is an implementation of the distributed hashtable Kademlia

## Getting started

``` java
Node firstNode = new Node(PORT, ADDRESS, 5);
Node secondNode = new Node(new LocalNode(firstNode, PORT, ADDRESS), PORT, ADDRESS, 5);

firstNode.performPing();
secondNode.performPing();

secondNode.setValue("Hello", "world", 1);

Assert.assertThat("Should be able to retrieve the set value",
        firstNode.getValue("Hello", 1),
        is("world"));

Assert.assertThat("Should be able to retrieve the set value",
        secondNode.getValue("Hello", 1),
        is("world"));
```

## API

``` java
/** Stores a key value pair in the network */
void setValue(String key, String value, int k);

/** Returns the value for a key */
String getValue(String key, int k);
```
