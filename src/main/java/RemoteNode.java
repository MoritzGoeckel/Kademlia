import jdk.jshell.spi.ExecutionControl;

import java.net.URL;

public class RemoteNode implements INode{
    private final HashKey nodeId;
    private final int port;
    private final URL ipAddress;

    public RemoteNode(HashKey nodeId, int port, URL ipAddress){
        this.nodeId = nodeId;
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public HashKey getNodeId() {
        return nodeId;
    }

    public int getPort() {
        return port;
    }

    public URL getIpAddress() {
        return ipAddress;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RemoteNode
                && getNodeId().equals(((RemoteNode)obj).getNodeId());
    }

    @Override
    public boolean ping(RemoteNode sender) {
        //TODO: Implement remote calls
        throw new RuntimeException("Remote calls not implemented yet");
    }

    @Override
    public void store(KeyValuePair pair, RemoteNode sender) {
        //TODO: Implement remote calls
        throw new RuntimeException("Remote calls not implemented yet");
    }

    @Override
    public RemoteNode[] findNode(HashKey nodeID, int k, RemoteNode sender) {
        //TODO: Implement remote calls
        throw new RuntimeException("Remote calls not implemented yet");
    }

    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey valueID, int k, RemoteNode sender) {
        //TODO: Implement remote calls
        throw new RuntimeException("Remote calls not implemented yet");
    }
}
