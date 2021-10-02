package studio.utils;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.kdb.K;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TableConnExtractorTest {

    private TableModel table = new DefaultTableModel(
            getTable(
                new String[][] {
                        {"server.com","1234","$something","$something"},
                        {"1.2.3.4","1234","$something","$something"},
                        {"1.2.3.4","1234","`:abc:1111","xyz"},
                        {"1.2.3.4","1234","abc","xyz"},
                        {"server.com","port","$something","$something"},
                        {"server.com","aHost:1050","$something","$something"},
                }
            ), new String[] {"aHost","aPort","Connection","Handle"});

    private static TableConnExtractor extractor1;
    private static TableConnExtractor extractor2;

    private Object[][] getTable(String[][] values) {
        Object[][] res = new Object[values.length][];
        for (int row=0; row<values.length; row++) {
            res[row] = new K.KCharacterVector[values[row].length];
            for (int col=0; col<values[row].length; col++) {
                res[row][col] = new K.KCharacterVector(values[row][col]);
            }
        }
        return res;
    }

    @BeforeEach
    public void init() {
        extractor1 = new TableConnExtractor();
        extractor1.setConnWords(new String[] {"conn", "handle"});
        extractor1.setHostWords(new String[] {"host", "conn", "handle"});
        extractor1.setPortWords(new String[] {"port"});

        extractor2 = new TableConnExtractor();
        extractor2.setConnWords(new String[] {"conn", "handle"});
        extractor2.setHostWords(new String[] {"host"});
        extractor2.setPortWords(new String[] {"port"});
    }

    @Test
    public void test() {
        assertArrayEquals(new String[] {"server.com:1234"}, extractor1.getConnections(table, 0,0));
        assertArrayEquals(new String[] {"1.2.3.4:1234"}, extractor1.getConnections(table, 1, 0));
        assertArrayEquals(new String[] {"`:abc:1111", "1.2.3.4:1234", "xyz:1234"}, extractor1.getConnections(table, 2,0));
        assertArrayEquals(new String[] {"1.2.3.4:1234", "abc:1234", "xyz:1234"}, extractor1.getConnections(table, 3, 0));

        assertArrayEquals(new String[] {"server.com:1234"}, extractor2.getConnections(table, 0, 0));
        assertArrayEquals(new String[] {"1.2.3.4:1234"}, extractor2.getConnections(table, 1, 0));
        assertArrayEquals(new String[] {"`:abc:1111", "1.2.3.4:1234"}, extractor2.getConnections(table, 2, 0));
        assertArrayEquals(new String[] {"1.2.3.4:1234"}, extractor2.getConnections(table, 3, 0));
    }

    @Test
    public void testMaxConn() {
        extractor1.setMaxConn(2);
        assertArrayEquals(new String[] {"`:abc:1111", "1.2.3.4:1234"}, extractor1.getConnections(table, 2, 0));
    }

    @Test
    public void testEmpty() {
        assertArrayEquals(new String[0], extractor1.getConnections(table, 4, 0));
        assertArrayEquals(new String[0], extractor2.getConnections(table, 4, 0));
    }

    @Test
    public void testValue() {
        assertArrayEquals(new String[]{"aHost:1050"}, extractor1.getConnections(table, 5, 1));
        assertArrayEquals(new String[]{"aHost:1050"}, extractor2.getConnections(table, 5, 1));

        assertArrayEquals(new String[0], extractor1.getConnections(table, 5, 0));
        assertArrayEquals(new String[0], extractor2.getConnections(table, 5, 0));

    }

}
