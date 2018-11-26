package com.moritzgoeckel.kademlia;

import java.io.Serializable;

/** Either holds a key value pair or a list of nodes closer to its key */
class RemoteNodesOrKeyValuePair implements Serializable {
    private INode[] remoteNodes;
    private KeyValuePair pair;

    RemoteNodesOrKeyValuePair(INode[] remoteNodes){
        this.remoteNodes = remoteNodes;
    }
    RemoteNodesOrKeyValuePair(KeyValuePair pair) { this.pair = pair; }

    INode[] getRemoteNodes() {
        return remoteNodes;
    }

    KeyValuePair getPair() {
        return pair;
    }
}
