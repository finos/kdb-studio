package studio.kdb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class KEqualsTest {

    private void testEquals(K.KBase k0, K.KBase k1, String message) {
        assertTrue(k0.equals(k1), message);
        assertEquals(0, k0.compareTo(k1));
        assertEquals(k0.hashCode(), k1.hashCode(), message);
    }

    private void testEquals(K.KBase k0, K.KBase k1) {
        testEquals(k0, k1, null);
    }

    private void testNotEquals(K.KBase k0, K.KBase k1, String message) {
        assertFalse(k0.equals(k1));
    }
    private void testNotEquals(K.KBase k0, K.KBase k1) {
        testNotEquals(k0, k1, null);
    }

    @Test
    public void testBoolean() {
        K.KBase kTrue = new K.KBoolean(true);
        K.KBase kFalse = new K.KBoolean (false);
        testEquals(kTrue, kTrue);
        testEquals(kFalse, kFalse);
        testNotEquals(kTrue, kFalse);
    }

    @ParameterizedTest
    @ValueSource(chars = { ' ', 'a', '\n'})
    public void testChar(char value) {
        K.KBase k0 = new K.KCharacter(value);
        K.KBase k1 = new K.KCharacter(value);
        K.KBase kk = new K.KCharacter((char) (value +1));

        testEquals(k0, k1);
        testNotEquals(k0, kk);
    }

    @ParameterizedTest
    @ValueSource(bytes = { 0, Byte.MAX_VALUE, Byte.MIN_VALUE, -Byte.MAX_VALUE, 10, -1})
    public void testByte(byte value) {
        K.KBase k0 = new K.KByte(value);
        K.KBase k1 = new K.KByte(value);
        K.KBase kk = new K.KByte( (byte) (value +1) );

        testEquals(k0, k1);
        testNotEquals(k0, kk);
    }

    @ParameterizedTest
    @ValueSource(shorts = { 0, Short.MAX_VALUE, Short.MIN_VALUE, -Short.MAX_VALUE, 10, -1})
    public void testShort(short value) {
        K.KBase k0 = new K.KShort(value);
        K.KBase k1 = new K.KShort(value);
        K.KBase kk = new K.KShort((short) (value +1) );

        testEquals(k0, k1);
        testNotEquals(k0, kk);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -Integer.MAX_VALUE, 10, -1})
    public void testInt(int value) throws Exception {
        Class[] classes = new Class[] {K.KInteger.class, K.KTime.class, K.Month.class, K.Minute.class, K.Second.class};
        for (Class clazz:classes) {
            K.KBase k0 = (K.KBase)clazz.getConstructor(int.class).newInstance(value);
            K.KBase k1 = (K.KBase)clazz.getConstructor(int.class).newInstance(value);
            K.KBase kk = (K.KBase)clazz.getConstructor(int.class).newInstance(value+1);

            testEquals(k0, k1, clazz.getName() + " fails with equal test");
            testNotEquals(k0, kk, clazz.getName() + " fails with notEqual test");
        }
    }


    @ParameterizedTest
    @ValueSource(ints = { 0, 3, 1000, -1000, Integer.MIN_VALUE})
    public void testPrimitive(int value) throws Exception {
        Class[] classes = new Class[] {K.UnaryPrimitive.class, K.BinaryPrimitive.class, K.TernaryOperator.class};
        for (Class clazz:classes) {
            K.KBase k0 = (K.KBase)clazz.getConstructor(int.class).newInstance(value);
            K.KBase k1 = (K.KBase)clazz.getConstructor(int.class).newInstance(value);
            K.KBase kk = (K.KBase)clazz.getConstructor(int.class).newInstance(value+1);

            testEquals(k0, k1, clazz.getName() + " fails with equal test");
            testNotEquals(k0, kk, clazz.getName() + " fails with notEqual test");
        }
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, Long.MAX_VALUE, Long.MIN_VALUE, -Long.MAX_VALUE, 10, -1})
    public void testLong(long value) throws Exception {
        Class[] classes = new Class[] {K.KLong.class, K.KTimespan.class, K.KTimestamp.class};
        for (Class clazz:classes) {
            K.KBase k0 = (K.KBase)clazz.getConstructor(long.class).newInstance(value);
            K.KBase k1 = (K.KBase)clazz.getConstructor(long.class).newInstance(value);
            K.KBase kk = (K.KBase)clazz.getConstructor(long.class).newInstance(value+1);

            testEquals(k0, k1, clazz.getName() + " fails with equal test");
            testNotEquals(k0, kk, clazz.getName() + " fails with notEqual test");
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN, 10, -1.1, 13e37})
    public void testDouble(double value) throws Exception {
        Class[] classes = new Class[] {K.KDouble.class, K.KDatetime.class};
        for (Class clazz:classes) {
            K.KBase k0 = (K.KBase)clazz.getConstructor(double.class).newInstance(value);
            K.KBase k1 = (K.KBase)clazz.getConstructor(double.class).newInstance(value);
            double value1 = Double.isNaN(value) ? Double.MAX_VALUE :
                                        Double.longBitsToDouble(1 + Double.doubleToLongBits(value));
            K.KBase kk = (K.KBase)clazz.getConstructor(double.class).newInstance(value1);

            testEquals(k0, k1, clazz.getName() + " fails with equal test");
            testNotEquals(k0, kk, clazz.getName() + " fails with notEqual test");
        }
    }

    @ParameterizedTest
    @ValueSource(floats = {0, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NaN, 10, -1.1f, 13e37f})
    public void testDouble(float value) {
        K.KBase k0 = new K.KFloat(value);
        K.KBase k1 = new K.KFloat(value);
        float value1 = Float.isNaN(value) ? Float.MAX_VALUE :
                Float.floatToIntBits(1 + Float.floatToIntBits(value));
        K.KBase kk = new K.KFloat(value1);

        testEquals(k0, k1);
        testNotEquals(k0, kk);
    }

    @Test
    public void testFunction() {
        String func = "{x+y}";
        String func1 = " {x+y}";
        testEquals(new K.Function(func), new K.Function(func));
        testNotEquals(new K.Function(func), new K.Function(func1));
    }

    @Test
    public void testGuid() {
        K.KGuid g0 = new K.KGuid(new UUID(12345,-987654));
        K.KGuid g1 = new K.KGuid(new UUID(12345,-987654));
        K.KGuid g2 = new K.KGuid(new UUID(0,0));

        testEquals(g0, g1);
        testNotEquals(g0, g2);
    }

    @Test
    public void testAdverb() {
        K.Function funcBinary = new K.Function("{x+y}");
        K.Adverb a1 = new K.FEachLeft(funcBinary);
        K.Adverb a2 = new K.FEachRight(funcBinary);
        K.Adverb a3 = new K.Feach(funcBinary);
        K.Adverb a4 = new K.Fover(funcBinary);
        K.Adverb a5 = new K.Fscan(funcBinary);
        K.Adverb a6 = new K.FPrior(funcBinary);

        K.Function funcBinary1 = new K.Function(new K.KCharacterVector("{x+y}"));
        K.Adverb a11 = new K.FEachLeft(funcBinary1);

        testEquals(a1,a11);
        testNotEquals(a2,a1);
        testNotEquals(a3,a2);
        testNotEquals(a4,a3);
        testNotEquals(a5,a4);
        testNotEquals(a6,a5);
        testNotEquals(a1,a6);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "\"", "`", "some String", "a"})
    public void testString(String value) {
        K.KBase k0 = new K.KCharacterVector(value);
        K.KBase k1 = new K.KCharacterVector(value);
        K.KBase kk = new K.KCharacterVector(value.length() == 0 ? " ": ((char) value.charAt(0)+1) + value.substring(1) );

        testEquals(k0, k1);
        testNotEquals(k0, kk);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "\"", "`", "some String", "a"})
    public void testSymbol(String value) {
        K.KBase k0 = new K.KSymbol(value);
        K.KBase k1 = new K.KSymbol(value);
        K.KBase kk = new K.KSymbol(value.length() == 0 ? " ": ((char) value.charAt(0)+1) + value.substring(1) );

        testEquals(k0, k1);
        testNotEquals(k0, kk);
    }

    @Test
    public void testComposition() {
        K.Function funcUnary = new K.Function("{1+x}");
        K.Function funcBinary = new K.Function("{x+y}");

        K.FComposition c0 = new K.FComposition(funcUnary, funcBinary);
        K.FComposition c1 = new K.FComposition(funcUnary, funcBinary);
        K.FComposition c2 = new K.FComposition(new K.Function("{2+x}"), new K.Function("{x+y}"));

        testEquals(c0, c1);
        testNotEquals(c0, c2);
    }

    @Test
    public void testProjection() {
        K.KList l1 = new K.KList(new K.BinaryPrimitive(1), new K.KLong(1));
        K.Projection p1 = new K.Projection(l1);

        K.KList l2 = new K.KList(new K.BinaryPrimitive(1), new K.KLong(1));
        K.Projection p2 = new K.Projection(l2);

        K.KList l3 = new K.KList(new K.TernaryOperator(0), new K.UnaryPrimitive(-1), new K.UnaryPrimitive(-1));
        K.Projection p3 = new K.Projection(l3);

        testEquals(p1, p2);
        testNotEquals(p1, p3);
    }

    //@TODO: need tests for Flip and Dict
}
