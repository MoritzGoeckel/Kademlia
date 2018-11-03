import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Random;

public class HashKey {

    public static final int LENGTH = 160;

    private static Random random = new Random();

    public static HashKey fromString(String s){
        return new HashKey(Hashing.sha1().hashString(s, Charsets.UTF_8).asBytes());
    }

    public static HashKey fromRandom(){
        return fromString(String.valueOf(random.nextInt()));
    }

    private BitSet bits;

    private HashKey(byte[] bytes){
        this.bits = BitSet.valueOf(bytes);

        assert (bytes.length == LENGTH / 8); //Should be 160 bits
        assert (bits.length() == LENGTH);
    }

    public int getDistance(HashKey other){
        assert (other.bits.length() == this.bits.length());

        BitSet distanceBits = (BitSet) this.bits.clone();
        distanceBits.xor(other.bits);

        return ByteBuffer.wrap(distanceBits.toByteArray()).getInt();
    }

    public boolean matchesPrefix(BitSet prefix){
        assert (prefix.length() <= this.bits.length());

        return prefix.equals(this.bits.get(0, prefix.length()));
    }

    public boolean getBit(int index){
        return this.bits.get(index);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof HashKey
                && this.bits.equals(((HashKey)other).bits);
    }
}
