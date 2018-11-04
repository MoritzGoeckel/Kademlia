public class KeyValuePair {
    private final HashKey key;
    private final String value;

    public KeyValuePair(HashKey key, String value){
        this.key = key;
        this.value = value;
    }

    public HashKey getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
