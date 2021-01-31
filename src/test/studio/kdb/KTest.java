package studio.kdb;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KTest {

    @Test
    public void testPrimitiveToString() {
        assertEquals("+", new K.BinaryPrimitive(1).toString());
    }

    @Test
    public void testKLongVector() {
        K.KLongVector vector = new K.KLongVector(5);
        long[] array = (long[]) vector.getArray();
        for (int i = 0; i < 5; i++) {
            array[i] = i;
        }

        assertEquals("0 1 2 3 4j", vector.toString());
        assertEquals("0 1 2 3 4", vector.toString(false));
    }

    @Test
    public void testKIntVector() {
        K.KIntVector vector = new K.KIntVector(5);
        int[] array = (int[]) vector.getArray();
        for (int i = 0; i < 5; i++) {
            array[i] = i;
        }

        assertEquals("0 1 2 3 4i", vector.toString());
        assertEquals("0 1 2 3 4", vector.toString(false));
    }

    @Test
    public void testKDoubleVector() {
        K.KDoubleVector vector = new K.KDoubleVector(5);
        double[] array = (double[]) vector.getArray();
        for (int i = 0; i < 5; i++) {
            array[i] = i;
        }

        // KDoubleVector like KFloat vector always print "f" if 1 double is an "integer" at 1e-9
        // and never prints it all of them are not
        assertEquals("0 1 2 3 4f", vector.toString());
        assertEquals("0 1 2 3 4f", vector.toString(false));

        for (int i = 2; i < 7; i++) {
            array[i - 2] = 1 / (i * 1.0);
        }
        assertEquals("0.5 0.3333333 0.25 0.2 0.1666667", vector.toString());
        assertEquals("0.5 0.3333333 0.25 0.2 0.1666667", vector.toString(false));
    }

    @Test
    public void testKFloatVector() {
        K.KFloatVector vector = new K.KFloatVector(5);
        float[] array = (float[]) vector.getArray();
        for (int i = 0; i < 5; i++) {
            array[i] = i;
        }

        assertEquals("0 1 2 3 4e", vector.toString());
        // KFloatVector like KDoubleVector always print e at the end
        assertEquals("0 1 2 3 4e", vector.toString(false));
    }

    @Test
    public void testKShortVector() {
        K.KShortVector vector = new K.KShortVector(5);
        short[] array = (short[]) vector.getArray();
        for (short i = 0; i < 5; i++) {
            array[i] = i;
        }

        assertEquals("0 1 2 3 4h", vector.toString());
        assertEquals("0 1 2 3 4", vector.toString(false));
    }

    @Test
    public void testKList() {
        K.KList list = new K.KList(2);
        K.KBase[] array = (K.KBase[]) list.getArray();
        array[0] = new K.KCharacterVector(3);
        System.arraycopy(new char[] {'A','B','C'}, 0, ((K.KBaseVector) array[0]).getArray(), 0, 3);
        array[1] = new K.KCharacterVector(3);
        System.arraycopy(new char[] {'D','E','F'}, 0, ((K.KBaseVector) array[1]).getArray(), 0, 3);

        assertEquals("(\"ABC\";\"DEF\")", list.toString());
        assertEquals("(ABC;DEF)", list.toString(false));
    }

    @Test
    public void testSymbolVector() {
        K.KSymbolVector vector = new K.KSymbolVector(2);
        String[] array = (String[]) vector.getArray();
        array[0] = "ABC";
        array[1] = "DEF";

        assertEquals("`ABC`DEF", vector.toString());
        assertEquals("`ABC`DEF", vector.toString(false));
    }

}