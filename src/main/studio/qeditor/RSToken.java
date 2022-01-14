package studio.qeditor;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import studio.kdb.Config;

import java.awt.*;
import java.util.Arrays;

public enum RSToken {

    NULL(TokenTypes.NULL, Config.COLOR_DEFAULT),
    SYMBOL(TokenTypes.DEFAULT_NUM_TOKEN_TYPES, Config.COLOR_SYMBOL),
    STRING(TokenTypes.LITERAL_CHAR, Config.COLOR_CHARVECTOR),
    ML_STRING(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 1, Config.COLOR_CHARVECTOR),
    ERROR_STRING(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 2, Font.BOLD, Config.COLOR_ERROR),
    IDENTIFIER(TokenTypes.IDENTIFIER, Config.COLOR_IDENTIFIER),
    OPERATOR(TokenTypes.OPERATOR, Config.COLOR_OPERATOR),
    BRACKET(TokenTypes.SEPARATOR, Config.COLOR_BRACKET),
    EOL_COMMENT(TokenTypes.COMMENT_EOL, Font.ITALIC, Config.COLOR_EOLCOMMENT),
    ML_COMMENT(TokenTypes.COMMENT_MULTILINE, Font.ITALIC, Config.COLOR_EOLCOMMENT),
    KEYWORD(TokenTypes.RESERVED_WORD, Font.BOLD, Config.COLOR_KEYWORD),
    WHITESPACE(TokenTypes.WHITESPACE, Config.COLOR_WHITESPACE),
    UNKNOWN(TokenTypes.ERROR_NUMBER_FORMAT, Font.BOLD, Config.COLOR_ERROR),
    INTEGER(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 3, Config.COLOR_INTEGER),
    MINUTE(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 4, Config.COLOR_MINUTE),
    SECOND(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 5, Config.COLOR_SECOND),
    TIME(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 6, Config.COLOR_TIME),
    DATE(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 7, Config.COLOR_DATE),
    MONTH(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 8, Config.COLOR_MONTH),
    FLOAT(TokenTypes.LITERAL_NUMBER_FLOAT, Config.COLOR_FLOAT),
    LONG(TokenTypes.LITERAL_NUMBER_DECIMAL_INT, Config.COLOR_LONG),
    SHORT(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 9, Config.COLOR_SHORT),
    REAL(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 10, Config.COLOR_REAL),
    BYTE(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 11, Config.COLOR_BYTE),
    BOOLEAN(TokenTypes.LITERAL_BOOLEAN, Config.COLOR_BOOLEAN),
    DATETIME(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 12, Config.COLOR_DATETIME),
    TIMESTAMP(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 13, Config.COLOR_TIMESTAMP),
    TIMESPAN(TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 14, Config.COLOR_TIMESPAN),
    SYSTEM(TokenTypes.PREPROCESSOR, Config.COLOR_SYSTEM),
    COMMAND(TokenTypes.VARIABLE, Config.COLOR_COMMAND);

    public final static int NUM_TOKEN_TYPES = TokenTypes.DEFAULT_NUM_TOKEN_TYPES + 15;

    private static RSToken[] tokenTypesToQToken = new RSToken[NUM_TOKEN_TYPES];
    static {
        Arrays.fill(tokenTypesToQToken, null);
        for (RSToken token: values()) {
            tokenTypesToQToken[token.getTokenType()] = token;
        }
    }

    public static RSToken fromTokenType(int tokenType) {
        RSToken result = tokenTypesToQToken[tokenType];
        if (result == null) throw new IllegalArgumentException(String.format("Token type %d is not defined", tokenType));
        return result;
    }

    public int getTokenType() {
        return tokenType;
    }

    private int tokenType;
    private Style style;
    public Style getStyle() {
        return style;
    }

    RSToken(int tokenType, int fontStyle, String colorTokenName) {
        this.tokenType = tokenType;
        Font font = RSyntaxTextArea.getDefaultFont();
        if (fontStyle != Font.PLAIN) font = font.deriveFont(fontStyle);
        Color color = Config.getInstance().getColor(colorTokenName);
        style = new Style(color, null, font);
    }

    RSToken(int tokenType, String colorTokenName) {
        this(tokenType, Font.PLAIN, colorTokenName);
    }

    RSToken(int tokenType) {
        this.tokenType = tokenType;
    }

    public static SyntaxScheme getDefaulSyntaxScheme() {
        SyntaxScheme scheme = new SyntaxScheme(false);
        Style[] defaultStyles = scheme.getStyles();
        Style[] styles = new Style[NUM_TOKEN_TYPES];
        System.arraycopy(defaultStyles, 0, styles, 0, defaultStyles.length);
        for (RSToken token: RSToken.values()) {
            styles[token.getTokenType()] = token.getStyle();
        }
        scheme.setStyles(styles);
        return scheme;
    }

    
}
