import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RemoteNode implements INode, Remote, Serializable {

    private IRemoteNode remote = null;

    private final String address;
    private final int port;
    private HashKey id;

    public RemoteNode(String address, int port, HashKey id){
        this.address = address;
        this.port = port;
        this.id = id;
    }

    private void establishConnection() throws RemoteException, NotBoundException {
        if(remote == null){
            Registry registry = LocateRegistry.getRegistry(String.valueOf(address), port);
            remote = (IRemoteNode) registry.lookup("kademliaNode");
        }
    }

    @Override
    public boolean ping(INode sender) {
        try {
            establishConnection();
            return remote.ping(sender);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            throw new RuntimeException("REMOTE EXCEPTION!");
        }
    }

    @Override
    public void store(KeyValuePair pair, INode sender) {
        try {
            establishConnection();
            remote.store(pair, sender);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            throw new RuntimeException("REMOTE EXCEPTION!");
        }
    }

    @Override
    public INode[] findNodes(HashKey targetID, int k, INode sender) {
        try {
            establishConnection();
            return remote.findNodes(targetID, k, sender);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            throw new RuntimeException("REMOTE EXCEPTION!");
        }
    }

    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey targetValueID, int k, INode sender) {
        try {
            establishConnection();
            return remote.findValue(targetValueID, k, sender);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            throw new RuntimeException("REMOTE EXCEPTION!");
        }
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
