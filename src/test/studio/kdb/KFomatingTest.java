package studio.kdb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KFomatingTest {

    private KFormatContext comma;

    @BeforeEach
    public void init() {
        comma = new KFormatContext(false, true);
    }

    @Test
    public void commaInNumbers() {
        assertEquals("1,234,567,890", new K.KLong(1234567890).toString(comma));
        assertEquals("-1,234,567,890", new K.KLong(-1234567890).toString(comma));
        assertEquals("123", new K.KLong(123).toString(comma));
        assertEquals("-123", new K.KLong(-123).toString(comma));
        assertEquals("0", new K.KLong(0).toString(comma));

        assertEquals("1,234,567", new K.KInteger(1234567).toString(comma));
        assertEquals("1,234", new K.KShort((short)1234).toString(comma));

        assertEquals("1,234,567.1234", new K.KDouble(1234567.1234).toString(comma));
        assertEquals("-1,234,567.1234", new K.KDouble(-1234567.1234).toString(comma));
        assertEquals("1,234,567", new K.KDouble(1234567).toString(comma));
        assertEquals("0.1234", new K.KDouble(0.1234).toString(comma));

        assertEquals("1,234.0625", new K.KFloat(1234.0625f).toString(comma));
    }

    @Test
    public void commaInList() {
        K.KLongVector longVector = new K.KLongVector(1, 1234, Long.MAX_VALUE, Long.MIN_VALUE);
        assertEquals("1 1,234 0W 0N", longVector.toString(comma));

        K.KList list = new K.KList(new K.KLong(12345), new K.KSymbol("12345"),
                new K.KCharacterVector("12345"), new K.KDouble(12345.67));
        assertEquals("(12,345;12345;12345;12,345.67)", list.toString(comma));
    }

    @Test
    public void commaInOthers() {
        assertEquals("0x7b", new K.KByte((byte)123).toString(comma));
        assertEquals("123456789", new K.KCharacterVector("123456789").toString(comma));
        assertEquals("123456789", new K.KSymbol("123456789").toString(comma));
        assertEquals("2020.12.09", new K.KDate(7648).toString(comma));
    }
}
