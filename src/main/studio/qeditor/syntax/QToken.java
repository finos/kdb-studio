package studio.qeditor.syntax;

public enum QToken {
    SYMBOL,
    STRING,
    ML_STRING(true),
    ERROR_STRING(true),
    IDENTIFIER,
    OPERATOR,
    BRACKET ,
    EOL_COMMENT,
    ML_COMMENT(true),
    KEYWORD,
    WHITESPACE,
    UNKNOWN,
    INTEGER,
    MINUTE,
    SECOND,
    TIME,
    DATE,
    MONTH,
    FLOAT,
    LONG,
    SHORT,
    REAL,
    BYTE,
    BOOLEAN,
    DATETIME,
    TIMESTAMP,
    TIMESPAN,
    SYSTEM, //OS command like \ls
    COMMAND; //q command like \p

    private boolean multiLine;

    QToken() {
        this(false);
    }

    QToken(boolean multiLine) {
        this.multiLine = multiLine;
    }

    public boolean isMultiLine() {
        return multiLine;
    }

}
