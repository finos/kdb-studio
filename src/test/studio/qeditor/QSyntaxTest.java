package studio.qeditor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import studio.qeditor.syntax.QSyntaxParser;
import studio.qeditor.syntax.QToken;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class QSyntaxTest {

    private static QSyntaxParser parser;

    @BeforeAll
    private static void init() {
        parser = new QSyntaxParser();
    }

    private String replaceAll(String src, String... pairs) {
        assert(pairs.length % 2 == 0);
        for (int i = 0; i<pairs.length; i+=2) {
            int index;
            while ( (index = src.indexOf(pairs[i])) != -1) {
                src = src.substring(0, index) + pairs[i+1] + src.substring(index + pairs[i].length());
            }
        }
        return src;
    }

    private String decode(String text) {
        return replaceAll(text, "\\\\", "\\", "\\n", "\n", "\\,", ",");
    }

    private String encode(String text) {
        return replaceAll(text, "\n","\\n");
    }

    private void assertSyntax(String text, String... expected) {
        List<QToken> actualTokens = parser.parse(decode(text));
        String[] actual = actualTokens.stream().map(t -> t.toString().toLowerCase()).toArray(String[]::new);
        assertTrue(Arrays.equals(actual, expected), String.format("Actual: %s; expected: %s; for text: %s", Arrays.toString(actual), Arrays.toString(expected), encode(text)));
    }

    @Test
    public void testSyntaxFromFile() throws IOException {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("syntax.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ( (line = reader.readLine()) != null) {
            if (line.trim().length() == 0) continue;
            if (line.charAt(0)=='#') continue;

            int index = 0;
            for(;;) {
                index = line.indexOf(',',index);
                if (index==0 || line.charAt(index-1)!='\\') break;
                index++;
            }
            String text = line.substring(0, index);
            String expectedLine = line.substring(index+1);
            String[] expectedTokens = expectedLine.split(",");

            assertSyntax(text, expectedTokens);
        }
        reader.close();
    }

    @Test
    public void test() {
    }

}
