package studio.qeditor;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerBase;
import studio.qeditor.syntax.QSyntaxParser;
import studio.qeditor.syntax.QToken;

import javax.swing.text.Segment;
import java.util.stream.Stream;


public class RSTokenMaker extends TokenMakerBase {

    public static final String CONTENT_TYPE = "text/q";

    private QSyntaxParser parser = new QSyntaxParser();

    // Make sure the same list and ordering in QToken, RSToken, RSTokenMaker.tokens
    private static final RSToken[] tokens = Stream.of(RSToken.values()).skip(1).toArray(RSToken[]::new);

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        resetTokenList();

        RSToken initToken = RSToken.fromTokenType(initialTokenType);

        int offset = text.offset;
        int delta = startOffset - text.offset;

        if (text.count == 0) {
            if (initToken == RSToken.NULL) {
                addNullToken();
            } else {
                addToken(text, offset, offset-1, initialTokenType, delta + offset);
            }
            return firstToken;
        }

        int initState;
        if (initToken == RSToken.NULL) {
            initState = QSyntaxParser.InitState;
        } else {
            QToken qtoken = QToken.values()[initToken.ordinal() - 1];
            initState = QSyntaxParser.getInitState(qtoken);
        }

        parser.init(initState, text.array, offset, text.offset + text.count);

        QToken lastToken = null;
        for (;;) {
            QToken token = parser.next();
            if (token == null) break;
            lastToken = token;
            addToken(text, offset, parser.getOffset() - 1, tokens[token.ordinal()].getTokenType(),delta + offset);
            offset = parser.getOffset();
        }
        if (lastToken == null || ! lastToken.isMultiLine()) {
            addNullToken();
        }

        return firstToken;
    }

    @Override
    public boolean getCurlyBracesDenoteCodeBlocks(int languageIndex) {
        return true;
    }

    @Override
    public boolean getShouldIndentNextLineAfter(Token t) {
        if (t!=null && t.length()==1) {
            char ch = t.charAt(0);
            return ch=='{' || ch=='(';
        }
        return false;
    }

}
