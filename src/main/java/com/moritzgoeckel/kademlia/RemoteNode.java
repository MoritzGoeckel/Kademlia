package com.moritzgoeckel.kademlia;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/** A proxy for a node on another computer.
 * The class contains information on where to find the node
 * and deals with invoking that functions remotely
 * This class also deals with error handling in regards of RMI exceptions
 * */
public class RemoteNode implements INode, Remote, Serializable {

    /** The RMI connection to the remote node if already established */
    private IRemoteNode remote = null;

    //TODO: Close connection or use UDP all together.
    // (UDP is used in the original Kademlia but RMI does not support UDP)

    private final String address;
    private final int port;
    private HashKey id;

    public RemoteNode(String address, int port){
        this.address = address;
        this.port = port;
        this.id = HashKey.fromString(address +"/"+ port);
    }

    /** Opens an actual connection to the remote node with the given url and port */
    private void establishConnection() throws RemoteException, NotBoundException {
        if(remote == null){
            Registry registry = LocateRegistry.getRegistry(String.valueOf(address), port);
            remote = (IRemoteNode) registry.lookup("kademliaNode");
        }
    }

    /** Proxy method for ping
     * If the node is not reachable it will return false */
    @Override
    public boolean ping(INode sender) {
        try {
            establishConnection();
            return remote.ping(sender);
        } catch (RemoteException | NotBoundException e) {
            return false; //com.moritzgoeckel.kademlia.Node is not reachable
        }
    }

    /** Proxy method for store
     * If the node is not reachable it will return false*/
    @Override
    public boolean store(KeyValuePair pair, INode sender) {
        try {
            establishConnection();
            return remote.store(pair, sender);
        } catch (RemoteException | NotBoundException e) {
            return false;
        }
    }

    /** Proxy method for findNodes
     * Be aware! Returns null if the node is not reachable*/
    @Override
    public INode[] findNodes(HashKey targetID, int k, INode sender) {
        try {
            establishConnection();
            return remote.findNodes(targetID, k, sender);
        } catch (RemoteException | NotBoundException e) {
            return null;
        }
    }

    /** Proxy method for findValue
     * Returns an empty array of "closer nodes" if the node is not reachable*/
    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey targetValueID, int k, INode sender) {
        try {
            establishConnection();
            return remote.findValue(targetValueID, k, sender);
        } catch (RemoteException | NotBoundException e) {
            //Return empty result
            return new RemoteNodesOrKeyValuePair(new INode[]{});
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof INode && ((INode) obj).getNodeId().equals(this.getNodeId()));
    }

    @Override
    public HashKey getNodeId() {
        return id;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getAddress() {
        return address;
    }
}
