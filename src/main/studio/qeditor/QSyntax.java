package studio.qeditor;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import studio.qeditor.syntax.QSyntaxParser;
import studio.qeditor.syntax.QToken;

import static studio.qeditor.QTokenContext.*;

public class QSyntax extends Syntax {

    private final QSyntaxParser parser = new QSyntaxParser();

    // Order should match QToken enum
    private final static TokenID[] nbTokens = new TokenID[] {
                SYMBOL, CHAR_VECTOR, IDENTIFIER, OPERATOR, BRACKET, EOL_COMMENT, KEYWORD, WHITESPACE,
                UNKNOWN, INTEGER, MINUTE, SECOND, TIME, DATE, MONTH, FLOAT, LONG, SHORT, REAL, BYTE,
                BOOLEAN, DATETIME, TIMESTAMP, TIMESPAN, SYSTEM, COMMAND};

    public QSyntax() {
        tokenContextPath = QTokenContext.contextPath;
    }

    @Override
    public void load(StateInfo stateInfo, char[] buffer, int offset, int len, boolean lastBuffer, int stopPosition) {
        super.load(stateInfo, buffer, offset, len, lastBuffer, stopPosition);
        parser.init(state - INIT, buffer, offset, stopOffset);
    }

    @Override
    public void relocate(char[] buffer, int offset, int len, boolean lastBuffer, int stopPosition) {
        super.relocate(buffer, offset, len, lastBuffer, stopPosition);
        parser.init(state - INIT, buffer, offset, stopOffset);
    }

    public TokenID parseToken() {
        QToken token = parser.next();
        offset = parser.getOffset();
        state = parser.getState() + INIT;
        if (token == null) return null;
        return nbTokens[token.ordinal()];
    }

}
