package studio.qeditor.syntax;

public enum QToken {
    SYMBOL,
    CHAR_VECTOR,
    IDENTIFIER,
    OPERATOR,
    BRACKET ,
    EOL_COMMENT,
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
    COMMAND, //q command like \p
}
