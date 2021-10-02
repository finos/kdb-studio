package studio.kdb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class KCompareTest {

    private void testLess(K.KBase k0, K.KBase k1) {
        assertTrue(k0.compareTo(k1) < 0);
    }

    @Test
    public void testBoolean() {
        K.KBase kTrue = new K.KBoolean(true);
        K.KBase kFalse = new K.KBoolean (false);
        testLess(kFalse, kTrue);
    }

    @ParameterizedTest
    @ValueSource(chars = { ' ', 'a', '\n'})
    public void testChar(char value) {
        K.KBase k0 = new K.KCharacter(value);
        K.KBase kk = new K.KCharacter((char) (value +1));

        testLess(k0, kk);
    }

    @ParameterizedTest
    @ValueSource(bytes = { 0, Byte.MIN_VALUE, 10, -1})
    public void testByte(byte value) {
        K.KBase k0 = new K.KByte(value);
        K.KBase kk = new K.KByte( (byte) (value +1) );

        testLess(k0, kk);
    }

    @ParameterizedTest
    @ValueSource(shorts = { 0, Short.MIN_VALUE, 10, -1})
    public void testShort(short value) {
        K.KBase k0 = new K.KShort(value);
        K.KBase kk = new K.KShort((short) (value +1) );

        testLess(k0, kk);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, Integer.MIN_VALUE, 10, -1})
    public void testInt(int value) throws Exception {
        Class[] classes = new Class[] {K.KInteger.class, K.KTime.class, K.Month.class, K.Minute.class, K.Second.class};
        for (Class clazz:classes) {
            K.KBase k0 = (K.KBase)clazz.getConstructor(int.class).newInstance(value);
            K.KBase kk = (K.KBase)clazz.getConstructor(int.class).newInstance(value+1);

            testLess(k0, kk);
        }
    }


    @ParameterizedTest
    @ValueSource(ints = { 0, 3, 1000, -1000, Integer.MIN_VALUE})
    public void testPrimitive(int value) throws Exception {
        Class[] classes = new Class[] {K.UnaryPrimitive.class, K.BinaryPrimitive.class, K.TernaryOperator.class};
        for (Class clazz:classes) {
            K.KBase k0 = (K.KBase)clazz.getConstructor(int.class).newInstance(value);
            K.KBase kk = (K.KBase)clazz.getConstructor(int.class).newInstance(value+1);

            testLess(k0, kk);
        }
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, Long.MIN_VALUE, 10, -1})
    public void testLong(long value) throws Exception {
        Class[] classes = new Class[] {K.KLong.class, K.KTimespan.class, K.KTimestamp.class};
        for (Class clazz:classes) {
            K.KBase k0 = (K.KBase)clazz.getConstructor(long.class).newInstance(value);
            K.KBase kk = (K.KBase)clazz.getConstructor(long.class).newInstance(value+1);

            testLess(k0, kk);
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 10, -1.1, 1337})
    public void testDouble(double value) throws Exception {
        Class[] classes = new Class[] {K.KDouble.class, K.KDatetime.class};
        for (Class clazz:classes) {
            K.KBase k0 = (K.KBase)clazz.getConstructor(double.class).newInstance(value);
            K.KBase kk = (K.KBase)clazz.getConstructor(double.class).newInstance(value + 0.1);

            testLess(k0, kk);
        }
    }

    @ParameterizedTest
    @ValueSource(floats = {0, 10, -1.1f, 1337f})
    public void testDouble(float value) {
        K.KBase k0 = new K.KFloat(value);
        K.KBase kk = new K.KFloat(value + 0.1f);

        testLess(k0, kk);
    }

    @Test
    public void testMix() {
        testLess(new K.KDouble(10.2), new K.KFloat(2));
    }

    @Test
    public void testString() {
        testLess(new K.KCharacterVector("aab"), new K.KCharacterVector("abb"));
        testLess(new K.KCharacterVector("aab"), new K.KCharacterVector("ab"));
    }

    @Test
    public void testSymbol() {
        testLess(new K.KSymbol("aab"), new K.KSymbol("abb"));
        testLess(new K.KSymbol("aab"), new K.KSymbol("ab"));

        testLess(new K.KCharacterVector("aab"), new K.KSymbol("abb"));
        testLess(new K.KSymbol("aab"), new K.KCharacterVector("abb"));

    }

    //@TODO: need tests for Flip and Dict
}
