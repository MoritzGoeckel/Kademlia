import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class HashKey {

    private static Random random = new Random();

    public static HashKey fromString(String s){
        return new HashKey(Hashing.sha1().hashString(s, Charsets.UTF_8).asBytes());
    }

    public static HashKey fromRandom(){
        return fromString(String.valueOf(random.nextInt()));
    }

    private byte[] bytes;
    private HashKey(byte[] bytes){
        assert (bytes.length == 160 / 8); //Should be 160 bits
        this.bytes = bytes;
    }

    public int getDistance(HashKey other){
        assert (other.bytes.length == this.bytes.length);

        byte[] out = new byte[this.bytes.length];
        for(int i = 0; i < this.bytes.length; i++)
            out[i] = (byte) (this.bytes[i] ^ other.bytes[i]);

        return ByteBuffer.wrap(out).getInt();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HashKey
                && Arrays.equals(((HashKey) obj).bytes, this.bytes);
    }
}
