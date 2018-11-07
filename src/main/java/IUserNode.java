import java.net.URL;

public interface IUserNode {
    /** Instructs the node to store the KeyValuePair somewhere in the network*/
    void setValue(String key, String value, int k);

    /** Returns the k closest known nodes to the nodeId */
    String getValue(String key, int k, int maxIterations);
}
