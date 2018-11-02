public class RemoteNodesOrKeyValuePair {
    private RemoteNode remoteNode;
    private RemoteNode[] remoteNodes;

    public RemoteNodesOrKeyValuePair(RemoteNode remoteNode){
        this.remoteNode = remoteNode;
    }

    public RemoteNodesOrKeyValuePair(RemoteNode[] remoteNodes){
        this.remoteNodes = remoteNodes;
    }

    public RemoteNode getRemoteNode() {
        return remoteNode;
    }

    public RemoteNode[] getRemoteNodes() {
        return remoteNodes;
    }
}
