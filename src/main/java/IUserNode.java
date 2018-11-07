import java.net.URL;

public interface IUserNode {
    /** Stores a key value pair in the network */
    void setValue(String key, String value, int k);

    /** Returns the value for a key */
    String getValue(String key, int k);
}
