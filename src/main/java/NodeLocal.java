import java.net.MalformedURLException;
import java.net.URL;

public class NodeLocal extends Node {

    private static int MOCK_PORT = 42;
    private static URL MOCK_URL;

    static {
        try {
            MOCK_URL = new URL("http://localhost");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("MalformedURLException: " + e.toString());
        }
    }

    public NodeLocal(RemoteNodeLocal knownNode) throws MalformedURLException {
        super(knownNode, MOCK_PORT, MOCK_URL);
        me = new RemoteNodeLocal(MOCK_PORT, MOCK_URL, this);
    }

    public NodeLocal() {
        super(MOCK_PORT, MOCK_URL);
        me = new RemoteNodeLocal(MOCK_PORT, MOCK_URL, this);
    }
}
