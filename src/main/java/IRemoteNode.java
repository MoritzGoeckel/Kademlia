import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteNode extends Remote {
    /** Returns true if the node is still reachable */
    boolean ping(INode sender) throws RemoteException;

    /** Instructs the node to store the KeyValuePair */
    boolean store(KeyValuePair pair, INode sender) throws RemoteException;

    /** Returns the k closest known nodes to the nodeId */
    INode[] findNodes(HashKey targetID, int k, INode sender) throws RemoteException;

    /** If node has the value it returns it. If not it returns the k closest known nodes to the id */
    RemoteNodesOrKeyValuePair findValue(HashKey targetValueID, int k, INode sender) throws RemoteException;
}
