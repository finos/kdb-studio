package studio.qeditor;

import org.fife.ui.rsyntaxtextarea.Token;
import org.junit.jupiter.api.Test;
import studio.qeditor.syntax.QToken;

import javax.swing.text.Segment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QSyntaxTest {

    @Test
    public void testInternalLogicAssumptions() {
        String rsToken = Stream.of(RSToken.values()).skip(1).map(Enum::toString).collect(Collectors.joining(","));
        String qToken = Stream.of(QToken.values()).map(Enum::toString).collect(Collectors.joining(","));
        assertEquals(rsToken, qToken, "RSToken and QToken should have the same list and order");

        long count = Stream.of(RSToken.values()).map(RSToken::getTokenType).distinct().count();
        assertEquals(RSToken.values().length, count, "All RSToken types should be different");

        long maxType = Stream.of(RSToken.values()).map(RSToken::getTokenType).max(Comparator.naturalOrder()).orElse(0);
        assertEquals(RSToken.NUM_TOKEN_TYPES, maxType+1, "RSToken.NUM_TOKEN_TYPES should be max token type + 1");

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

    private void assertSyntax(String text, String expectedLine) {
        StringBuilder actualLine = new StringBuilder();
        RSTokenMaker tokenMaker = new RSTokenMaker();
        Segment segment = new Segment(text.toCharArray(), 0, 0);
        int offset = 0;
        int tokenType = 0;
        do {
            if (offset > 0) actualLine.append(';');

            int lineEnd = text.indexOf('\n', offset);
            if (lineEnd == -1) lineEnd = text.length();
            segment.offset = offset;
            segment.count = lineEnd - offset;
            Token token = tokenMaker.getTokenList(segment, tokenType, offset);

            boolean firstToken = true;
            while (token != null) {
                tokenType = token.getType();
                RSToken rsToken = RSToken.fromTokenType(tokenType);
                if (rsToken != RSToken.NULL) {
                    if (firstToken) {
                        firstToken = false;
                    } else {
                        actualLine.append(',');
                    }
                    actualLine.append(rsToken.toString().toLowerCase());
                }
                token = token.getNextToken();
            }

            offset = lineEnd + 1;
        } while (offset < text.length());

        assertEquals(expectedLine, actualLine.toString(), "Failure for text: " + encode(text));
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

            assertSyntax(decode(text), expectedLine);
        }
        reader.close();
    }

    @Test
    public void test() {
        assertSyntax("\"a\\1\nb\"", "ml_string,error_string;string");
    }
}
