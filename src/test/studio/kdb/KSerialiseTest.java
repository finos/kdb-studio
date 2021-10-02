package studio.kdb;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@EnabledIfEnvironmentVariable(named = "qTestPort", matches = "[0-9]+")
public class KSerialiseTest {

    private static kx.c c;

    @BeforeAll
    public static void connect() throws kx.c.K4Exception, IOException {
        c = new kx.c("localhost", Integer.parseInt(System.getenv("qTestPort")), "", false);
        c.k(new K.KCharacterVector(".z.pg:{$[x~\"reset\";`.z.pg set value;x]}"));
    }

    @AfterAll
    public static void exit() throws kx.c.K4Exception, IOException {
        c.k(new K.KCharacterVector("reset"));
    }

    private void test(K.KBase k) {
        try {
            K.KBase result = c.k(k);
            assertEquals(k, result);
        } catch (kx.c.K4Exception|IOException e) {
            fail(e);
        }
    }

    private final static long[] longs = new long[] { 0, Long.MIN_VALUE, Long.MAX_VALUE, -Long.MAX_VALUE, 134, -1279875634455L};
    private final static int[] ints = new int[] { 0, Integer.MIN_VALUE, Integer.MAX_VALUE, -Integer.MAX_VALUE, 176, -1279875};
    private final static short[] shorts = new short[] { 0, Short.MIN_VALUE, Short.MAX_VALUE, -Short.MAX_VALUE, 176, -127};
    private final static byte[] bytes = new byte[] { 0, -128, 1, 32, 127};
    private final static boolean[] bools = new boolean[] {true, false};
    private final static double[] doubles = new double[] {0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN,
                Double.MAX_VALUE, Double.MIN_VALUE, 134e56, -12.00123};
    private final static float[] floats = new float[] {0, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN,
            Float.MAX_VALUE, Float.MIN_VALUE, 134e5f, -12.00123f};

    private final static String[] strings = { "Test", "Test string", " ", "", "something\nwith new line" };

    private final static UUID[] uuids = {new UUID(0,0), new UUID(1234567890123456L, -9876543210123456L)};

    @Test
    public void testLong() {
        for (long value: longs) {
            test(new K.KLong(value));
        }
        test(new K.KLongVector(longs));
    }

    @Test
    public void testInt() {
        for (int value: ints) {
            test(new K.KInteger(value));
        }
        test(new K.KIntVector(ints));
    }

    @Test
    public void testShort() {
        for (short value: shorts) {
            test(new K.KShort(value));
        }
        test(new K.KShortVector(shorts));
    }

    @Test
    public void testByte() {
        for (byte value: bytes) {
            test(new K.KByte(value));
        }
        test(new K.KByteVector(bytes));
    }

    @Test
    public void testBoolean() {
        for (boolean value: bools) {
            test(new K.KBoolean(value));
        }
        test(new K.KBooleanVector(bools));
    }

    @Test
    public void testDouble() {
        for (double value: doubles) {
            test(new K.KDouble(value));
        }
        test(new K.KDoubleVector(doubles));
    }

    @Test
    public void testFloat() {
        for (float value: floats) {
            test(new K.KDouble(value));
        }
        test(new K.KFloatVector(floats));
    }

    @Test
    public void testString() {
        for (String value: strings) {
            test(new K.KCharacterVector(value));
            for(char c: value.toCharArray()) {
                test(new K.KCharacter(c));
            }
        }
    }

    @Test
    public void testSymbol() {
        for (String value: strings) {
            test(new K.KSymbol(value));
        }
        test(new K.KSymbolVector(strings));
    }

    @Test
    public void testTemporal() {
        test(new K.KTimestamp(12345678));
        test(new K.KTimespan(-12345678));
        test(new K.KDatetime(-123.456));
        test(new K.KTime(12345));
        test(new K.KDate(35241));
        test(new K.Minute(-12345));
        test(new K.Month(54321));
        test(new K.Second(13542));
    }

    @Test
    public void testUUID() {
        for (UUID value: uuids) {
            test(new K.KGuid(value));
        }
        test(new K.KGuidVector(uuids));
    }

    @Test
    public void testList() {
        test(new K.KLongVector());
        test(new K.KCharacterVector(""));
        test(new K.KSymbolVector());

        test(new K.KList());
        test(new K.KList(new K.KLongVector(longs), new K.KSymbolVector(strings), new K.KDoubleVector(doubles),
                new K.KBoolean(true), new K.KList(),
                new K.KList(new K.KFloatVector(floats))));
    }

    @Test
    public void testFunctions() {
        test(new K.Function("{x+y}"));

        for (int i=0; i<30; i++) {
            test(new K.UnaryPrimitive(i));
            test(new K.BinaryPrimitive(i));
        }

        test(new K.TernaryOperator(0));
        test(new K.TernaryOperator(1));
        test(new K.TernaryOperator(2));

        K.Function funcUnary = new K.Function("{1+x}");
        K.Function funcBinary = new K.Function("{x+y}");

        K.FComposition c = new K.FComposition(funcUnary, funcBinary);
        test(c);

        test(new K.Projection(funcBinary, new K.KLong(1), new K.UnaryPrimitive(-1)));
        test(new K.Projection(funcBinary, new K.UnaryPrimitive(-1), new K.KLong(1)));
        test(new K.Projection(new K.BinaryPrimitive(1), new K.KLong(1)));
        test(new K.Projection(new K.TernaryOperator(0), new K.UnaryPrimitive(-1), new K.UnaryPrimitive(-1)));
        test(new K.Projection(new K.FEachRight(new K.BinaryPrimitive(1)), new K.KLong(1), new K.UnaryPrimitive(-1)  ));
        test(new K.Projection(new K.UnaryPrimitive(41), new K.KLong(1), new K.UnaryPrimitive(-1) ));

    }

    @Test
    public void testAdverbs() {
        K.Function func = new K.Function("{x+y}");
        test(new K.FEachLeft(func));
        test(new K.FEachRight(func));
        test(new K.Feach(func));
        test(new K.Fscan(func));
        test(new K.Fover(func));
        test(new K.FPrior(func));
    }


    private static K.Flip getFlip(int count, String... cols) {
        K.KSymbolVector keys = new K.KSymbolVector(cols);

        K.KBase[] array = new K.KBase[cols.length];
        for (int i=0; i<array.length; i++) {
            long[] list = new long[count];
            Arrays.fill(list, i);
            array[i] = new K.KLongVector(list);
        }
        K.KList values = new K.KList(array);

        return new K.Flip(new K.Dict(keys, values));
    }

    @Test
    public void testDict() {
        K.Dict d = new K.Dict(new K.KIntVector(13, -10, 5), new K.KCharacterVector("abc"));
        test(d);

        K.Dict d1 = new K.Dict(new K.KLongVector(11, 111, 1111), new K.KList(new K.KIntVector(17),new K.KSymbol("xyz"), new K.KCharacterVector("test")));
        d1.setAttr((byte)1);
        test(d1);


        K.KIntVector list1 = new K.KIntVector(10, 20, 30);
        K.KIntVector list2 = new K.KIntVector(100, 200, 300);
        K.Flip flip1 = getFlip(3, "a", "b", "c");
        K.Flip flip2 = getFlip(3, "x", "y", "z");

        test(new K.Dict(list1, list2));
        test(new K.Dict(flip1, flip2));
        test(new K.Dict(list1, flip2));
        test(new K.Dict(flip1, list2));

    }

    @Test
    public void testFlip() {
        test(getFlip(4,"a", "bb", "ccc"));
    }
}
