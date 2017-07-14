import org.junit.Assert;
import org.junit.Test;
import test.Foo;

/**
 * Created by yoichimatsuyama on 7/7/17.
 */
public class FooTest {
    @Test
    public void testToString() {
        String expected = "Test";
        Foo foo = new Foo(expected);
        Assert.assertEquals(expected, foo.toString());
    }
}
