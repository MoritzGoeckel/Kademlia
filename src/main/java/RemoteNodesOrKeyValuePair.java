public class RemoteNodesOrKeyValuePair {
    private RemoteNode[] remoteNodes;
    private KeyValuePair pair;

    public RemoteNodesOrKeyValuePair(RemoteNode[] remoteNodes){
        this.remoteNodes = remoteNodes;
    }
    public RemoteNodesOrKeyValuePair(KeyValuePair pair) { this.pair = pair; }

    public RemoteNode[] getRemoteNodes() {
        return remoteNodes;
    }

    public KeyValuePair getPair() {
        return pair;
    }
}
