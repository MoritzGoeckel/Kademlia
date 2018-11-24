import java.io.Serializable;

public class RemoteNodesOrKeyValuePair implements Serializable {
    private INode[] remoteNodes;
    private KeyValuePair pair;

    public RemoteNodesOrKeyValuePair(INode[] remoteNodes){
        this.remoteNodes = remoteNodes;
    }
    public RemoteNodesOrKeyValuePair(KeyValuePair pair) { this.pair = pair; }

    public INode[] getRemoteNodes() {
        return remoteNodes;
    }

    public KeyValuePair getPair() {
        return pair;
    }
}
