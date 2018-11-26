package com.moritzgoeckel.kademlia;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.Random;

/** HashKey implemented with a BitSet
 *  to be used as node ID and as key for the values stored in Kademlia  */
public class HashKey implements Serializable {

    public static final int LENGTH = 160;
    private static Random random = new Random();

    private BitSet bits;

    /** Creates a HashKey from a String */
    public static HashKey fromString(String s){
        return new HashKey(Hashing.sha1().hashString(s, Charsets.UTF_8).asBytes());
    }

    /** Creates a random HashKey */
    public static HashKey fromRandom(){
        return fromString(String.valueOf(random.nextInt()));
    }

    /** Converts a BitSet to a Number */
    private static BigInteger bitsetToNum(BitSet bits) {
        if(bits.length() == 0)
            return new BigInteger("0");

        return new BigInteger(bits.toByteArray());
    }

    /** Creats a HashKey from an array of bytes */
    private HashKey(byte[] bytes){
        this.bits = BitSet.valueOf(bytes);

        assert (bytes.length == LENGTH / 8); //Should be 160 bits
        assert (bits.length() <= LENGTH);
    }

    /** Determines the distance between two a HashKeys using XOR */
    public BigInteger getDistance(HashKey other){
        //assert (other.bits.length() == this.bits.length()); //This does not hold with Bitset.
        // Todo: Maybe change underlying data structure from bitset to bit array

        BitSet distanceBits = (BitSet) this.bits.clone();
        distanceBits.xor(other.bits);

        return bitsetToNum(distanceBits);
    }

    /** Determines whether or not the HashKey starts with a certain prefix */
    public boolean matchesPrefix(BitSet prefix){
        assert (prefix.length() <= this.bits.length());

        return prefix.equals(this.bits.get(0, prefix.length()));
    }

    /** Returns the bit on a given index in the HashKey */
    public boolean getBit(int index){
        return this.bits.get(index);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof HashKey
                && this.bits.equals(((HashKey)other).bits);
    }

    @Override
    public String toString() {
        return bitsetToNum(this.bits) + "B";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
