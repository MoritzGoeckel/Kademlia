import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

public class FirstTests {

    @Test
    public void hashKeyTest(){
        Assert.assertThat("a and be are different", !HashKey.fromRandom().equals(HashKey.fromRandom()), is(true));

        Assert.assertThat("these are the same",
                HashKey.fromString("abc").equals(HashKey.fromString("abc")),
                is(true));

        Assert.assertThat("these are the different",
                !HashKey.fromString("abc").equals(HashKey.fromString("abcd")),
                is(true));

        HashKey a = HashKey.fromString("abc");
        HashKey b = HashKey.fromString("abcd");
        HashKey c = HashKey.fromString("abcd");

        Assert.assertThat("distance is the same both ways",
                a.getDistance(b) == b.getDistance(a),
                is(true));

        Assert.assertThat("distance is bigger than 0",
                a.getDistance(b) > 0,
                is(true));

        Assert.assertThat("triangle inequality",
                a.getDistance(b) + b.getDistance(c) >= a.getDistance(c),
                is(true));
    }


}
