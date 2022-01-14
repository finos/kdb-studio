package studio.kdb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class SorterTest {

    @Test
    public void testSort() {
        K.KIntVector array = new K.KIntVector(2, 1, 1, 2, 3);

        assertArrayEquals(new int[] {1, 2, 0, 3, 4},
                Sorter.sort(array, new int[]{0, 1, 2, 3 ,4}) );

        assertArrayEquals(new int[] {2, 1, 0, 3, 4},
                Sorter.sort(array, new int[]{0, 2, 1, 3 ,4}) );

    }

    @Test
    public void testReverse() {
        K.KIntVector array = new K.KIntVector(2, 1, 1, 2, 3);
        assertArrayEquals(new int[] {4, 0, 3, 1, 2},
                Sorter.reverse(array, new int[]{1, 2, 0, 3, 4}) );

        assertArrayEquals(new int[] {2, 1, 0, 3, 4},
                Sorter.reverse(array, new int[]{4, 0, 3, 2, 1}) );
    }
}
