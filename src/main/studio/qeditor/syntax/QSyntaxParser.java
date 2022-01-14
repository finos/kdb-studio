package studio.qeditor.syntax;

import java.util.*;

import static studio.qeditor.syntax.QToken.*;
import static studio.qeditor.syntax.SyntaxStateMachine.Action.*;
import static studio.qeditor.syntax.SyntaxStateMachine.Action.LooksLike;

public class QSyntaxParser  {

    private enum State {Init, // start of a line (or file)
        AfterAtom, // - will means operator - after atom, identifier, brackets
        AfterOperator, // - could mean start of atom like -10
        AfterWhitespace,
        Dot,
        Long, Int, Short, Float, Real,
        Date, Time, Datetime, Timestamp, Timespan, Month, Minute, Second,
        Boolean, Byte, UnknownAtom,
        NullLong, NullFloat, InfLong, InfFloat,
        Zero, One, Two, ZeroOnes, Digits, DigitsDot, DigitsDotDigits, FractionE, FractionESign, Scientific,
        DigitsDotDigitsDot, DateLike,
        DigitsColon,MinuteLike,MinuteColon,SecondLike,SecondDot,TimeLikeD1,TimeLikeD2, TimeLikeD3, TimespanLike, //the order on this line is critical
        DateD1,DateD2,DateD3,DateD4,DateD5,DateD6,DateD7,DateD8,
        DateT1,DateT2,DateT3,DateT4,DateT5,DateT6,DateT7,DateT8,
        DigitsD1,DigitsD2,DigitsD3,DigitsD4,DigitsD5,DigitsD6,DigitsD7,DigitsD8,
        MinusZero, MinusForAtom,
        String, StringEscapeBefore, StringEscape, StringEscapeD, StringEscapeDD, Symbol, SymbolColon,
        Identifier,IdentifierDot,
        CommentEOL,
        CommentMLFirstLine, CommentMLInit, CommentML, CommentMLLastLine,
        InitBaskSlash, QCommandStart, QCommand, OSCommand
    }

    public static final int InitState = State.Init.ordinal();

    public static int getInitState(QToken token) {
        switch (token) {
            case ML_COMMENT: return State.CommentMLInit.ordinal();
            case ML_STRING:
            case ERROR_STRING: return State.String.ordinal();
            default: return State.Init.ordinal();
        }
    }

    private static final State firstAtomState = State.Long;
    private static final State lastAtomState = State.MinusZero;

    private static final Set<String> keywords = new HashSet<>(Arrays.asList(
            "bin","asin","atan","exit","prd","prior","setenv","tan","wj","wj1","wsum","ej","aj0",
            "insert","acos","exp","wavg","avgs","log","sin","cos","sqrt","exec","abs","neg","not","null",
            "floor","string","reciprocal","ceiling","signum","div","mod","xbar","xlog","and","or","each",
            "mmu","lsq","inv","md5","ltime","gtime","count","first","var","dev","med","cov","cor","all",
            "any","rand","sums","prds","mins","maxs","fills","deltas","ratios","differ","prev","next",
            "rank","reverse","iasc","idesc","asc","desc","msum","mcount","mavg","mdev","xrank","mmin",
            "mmax","xprev","rotate","list","distinct","group","where","flip","type","key","til","get",
            "value","attr","cut","set","upsert","raze","union","inter","except","cross","ss","sv","vs",
            "sublist","read0","read1","hopen","hclose","hdel","hsym","hcount","peach","system","ltrim",
            "rtrim","trim","lower","upper","ssr","view","tables","views","cols","xcols","keys","xkey","xcol",
            "xasc","xdesc","fkeys","meta","uj","ij","lj","pj","aj","asof","fby","ungroup","xgroup","plist",
            "enlist","txf","save","load","rsave","rload","show","csv","parse","eval","over","scan","select",
            "from","where","within","update","in","delete","sum","avg","min","max","like","last","by","do",
            "while","if","getenv","xexp") );

    private final static SyntaxStateMachine stateMachine = new SyntaxStateMachine();

    private char[] buffer;
    private int offset;
    private int stopOffset;
    private State state;

    public void init(int state, char[] buffer, int offset, int stopOffset) {
        this.buffer = buffer;
        this.offset = offset;
        this.stopOffset = stopOffset;
        this.state = State.values()[state];
    }

    public int getOffset() {
        return offset;
    }

    public int getState() {
        return state.ordinal();
    }


    public QToken next() {
        int start = offset;
        QToken token = getNext();
        if (token == IDENTIFIER) {
            String word = new String(buffer, start, offset-start);
            if (keywords.contains(word)) {
                token = KEYWORD;
            }
        }
        return token;
    }

    private QToken getNext() {
        QToken token = null;
        while (offset<stopOffset) {
            SyntaxStateMachine.Next next = stateMachine.getNext(state, buffer[offset++]);
            if (next == null) {
                state = State.AfterAtom;
                if (token != null) {
                    offset--;
                    return token;
                }
                return UNKNOWN;
            } else {
                state = (State) next.nextState;
                if (next.action == Match) return next.token;
                if (next.action == MatchPrev) {
                    offset--;
                    return next.token;
                }
                token = next.token;
            }
        }

        return token;
    }

    private static void add(State fromState, String chars, State nextState, QToken token, SyntaxStateMachine.Action action) {
        stateMachine.add(fromState, chars, nextState, token, action);
    }
    private static void add(State[] fromStates, String chars, State nextState, QToken token, SyntaxStateMachine.Action action) {
        for(State fromState:fromStates) {
            add(fromState, chars, nextState, token, action);
        }
    }
    private static State[] s(State... states) {
        return states;
    }

    private static final String digits = "0123456789";
    private static final String a2z = "abcdefghijklmnopqrstuvwxyz";
    private static final String A2Z = a2z.toUpperCase();
    private static final String alpha = a2z + A2Z;
    private static final String alphaNumeric = alpha + digits;
    private static final String atomChars = alphaNumeric + ":.";
    private static final String brackets = "[](){}";
    private static final String operators = "|/&^:!+-*%$=~#;@\\.><,?_'";
    private static final String whitespace = " \t\r";

    private static String except(String src, String exclude) {
        for (int i=0; i<exclude.length(); i++) {
            int index = src.indexOf(exclude.charAt(i));
            if (index == -1) continue;
            src = src.substring(0,index) + src.substring(index+1);
        }
        return src;
    }

    private final static State[] typeStates = new State[] {
            State.Long, State.Int, State.Short, State.Float, State.Real,
            State.Date, State.Time, State.Datetime, State.Timestamp, State.Timespan,
            State.Month, State.Minute, State.Second, State.Boolean };
    private final static QToken[] atomTokens = new QToken[] {
            LONG, INTEGER, SHORT, FLOAT, REAL, DATE, TIME, DATETIME, TIMESTAMP, TIMESPAN,
            MONTH, MINUTE, SECOND, BOOLEAN };
    private final static String atomTypes = "jihfedtzpnmuvb";

    private static void initUnknownAtom(State firstAtomState, State lastAtomState) {
        add(State.UnknownAtom, "", State.AfterAtom, UNKNOWN, MatchPrev);
        for (int i=0; i<typeStates.length; i++) {
            add(typeStates[i], "", State.AfterAtom, atomTokens[i], MatchPrev);
        }

        int firstIndex = firstAtomState.ordinal();
        int lastIndex = lastAtomState.ordinal();
        for(int index = firstIndex; index<=lastIndex; index++) {
            State state = State.values()[index];
            String wrongChars = except(atomChars, stateMachine.getChars(state));
            add(state, wrongChars, State.UnknownAtom, UNKNOWN, LooksLike);
        }
    }

    /* Various ending can be found with the following code. We don't code all possible combinations. Only natural ones
     test:{[s]
        s:"",s;
        types:"jihfepmdznuvt";
        types: types where @'[{parse x; 1b};;0b] s,/:types;
        values: parse each s,/:types;
        defTypes: @[{[types;values;s]types where values~\:parse s}[types;values;];s;" "];
        (defTypes; types except defTypes)
    }
    test "100"
     */
    private static void addAtomTypeEnding(String endingTypes, State... fromStates) {
        for (State fromState: fromStates) {
            addAtomTypeEnding(fromState, endingTypes);
        }
    }
    private static void addAtomTypeEnding(State fromState, String endingTypes) {
        for (int i=0; i<endingTypes.length(); i++) {
            int index = atomTypes.indexOf(endingTypes.charAt(i));
            if (index==-1) throw new IllegalStateException("Unknown type");

            add(fromState, ""+ atomTypes.charAt(index), typeStates[index], atomTokens[index], LooksLike);
        }
    }

    private static void addTemporal(State[] fromStates, State firstState, QToken[] tokens, String types, String... charsArray) {
        String chars = charsArray[0];
        for(State fromState: fromStates) {
            add(fromState, chars, firstState, tokens[0], LooksLike);
        }

        State state = firstState;
        for(int index = 1; index<charsArray.length; index++) {
            chars = charsArray[index];
            State nextState = State.values()[state.ordinal()+1];
            add(state, chars, nextState, tokens[index], LooksLike);
            if (chars.equals(digits)) {
                add(nextState, digits, nextState, tokens[index], LooksLike);
            }
            addAtomTypeEnding(state, types);
            state = nextState;
        }
        addAtomTypeEnding(state, types);
    }

    private static void addTemporal(State[] fromStates, State firstState, QToken token, String types, String... charsArray) {
        QToken[] tokens = new QToken[charsArray.length];
        Arrays.fill(tokens, token);
        addTemporal(fromStates, firstState, tokens, types, charsArray);
    }

    private static void addTemporal(State fromState, State firstState, QToken token, String types, String... charsArray) {
        addTemporal(new State[] {fromState}, firstState, token, types, charsArray);
    }

    /*
    rules for -
    INIT,whitespace -> sign
    operator ->sign

    bracket -> operator
    atom -> operator
    identifier -> operator

     */

    static {
        add(s(State.Init, State.AfterOperator, State.AfterAtom, State.AfterWhitespace), "0", State.Zero, LONG, LooksLike);
        add(s(State.Init, State.AfterOperator, State.AfterAtom, State.AfterWhitespace), "1", State.One, LONG, LooksLike);
        add(s(State.Init, State.AfterOperator, State.AfterAtom, State.AfterWhitespace), "2", State.Two, LONG, LooksLike);
        add(s(State.Init, State.AfterOperator, State.AfterAtom, State.AfterWhitespace), except(digits, "012"), State.Digits, LONG, LooksLike);
        add(s(State.Init, State.AfterOperator, State.AfterAtom, State.AfterWhitespace), ".", State.Dot, OPERATOR, LooksLike);
        add(s(State.Init, State.AfterOperator, State.AfterAtom, State.AfterWhitespace), brackets, State.AfterAtom, BRACKET, Match);
        add(s(State.Init, State.AfterOperator, State.AfterAtom, State.AfterWhitespace), except(operators, ".-\\/"), State.AfterOperator, OPERATOR, Match);
        add(State.Init, whitespace, State.Init, WHITESPACE, Match);
        add(s(State.AfterOperator, State.AfterAtom, State.AfterWhitespace), whitespace, State.AfterWhitespace, WHITESPACE, Match);
        add(s(State.Init, State.AfterOperator, State.AfterAtom, State.AfterWhitespace), "\n", State.Init, WHITESPACE, Match);
        add(s(State.Init, State.AfterOperator, State.AfterAtom, State.AfterWhitespace), "\"", State.String, ML_STRING, LooksLike);
        add(s(State.Init, State.AfterOperator, State.AfterAtom, State.AfterWhitespace), "`", State.Symbol, SYMBOL, LooksLike);
        add(s(State.Init, State.AfterOperator, State.AfterAtom, State.AfterWhitespace), alpha, State.Identifier, IDENTIFIER, LooksLike);

        add(s(State.Init, State.AfterOperator, State.AfterWhitespace), "-", State.MinusForAtom, OPERATOR, LooksLike);
        add(State.AfterAtom, "-", State.AfterOperator, OPERATOR, Match);

        add(State.Init, "/", State.CommentMLFirstLine, ML_COMMENT, LooksLike);
        add(State.AfterWhitespace, "/", State.CommentEOL, EOL_COMMENT, LooksLike);
        add(State.Init, "\\", State.InitBaskSlash, OPERATOR, LooksLike);
        add(State.AfterWhitespace, "\\", State.AfterOperator, OPERATOR, Match);
        add(s(State.AfterAtom, State.AfterOperator), "\\/", State.AfterOperator, OPERATOR, Match);

        add(State.CommentEOL, "\n", State.Init, EOL_COMMENT, Match);
        add(State.CommentEOL, "", State.CommentEOL, EOL_COMMENT, LooksLike);

        add(State.CommentMLFirstLine, whitespace, State.CommentMLFirstLine, ML_COMMENT, LooksLike);
        add(State.CommentMLFirstLine, "\n", State.CommentMLInit, ML_COMMENT, Match);
        add(State.CommentMLFirstLine, "", State.CommentEOL, EOL_COMMENT, LooksLike);

        add(State.CommentMLInit, whitespace, State.CommentMLInit, ML_COMMENT, LooksLike);
        add(State.CommentMLInit, "\\", State.CommentMLLastLine, EOL_COMMENT, LooksLike); // should start with non comment in the next line. That's needed for RSyntaxTextArea
        add(State.CommentMLInit, "\n", State.CommentMLInit, ML_COMMENT, Match);
        add(State.CommentMLInit, "", State.CommentML, ML_COMMENT, LooksLike);

        add(State.CommentML, "\n", State.CommentMLInit, ML_COMMENT, Match);
        add(State.CommentML, "", State.CommentML, ML_COMMENT, LooksLike);

        add(State.CommentMLLastLine, whitespace, State.CommentMLLastLine, EOL_COMMENT, LooksLike); // should start with non comment in the next line. That's needed for RSyntaxTextArea
        add(State.CommentMLLastLine, "\n", State.Init, EOL_COMMENT, Match); // should start with non comment in the next line. That's needed for RSyntaxTextArea
        add(State.CommentMLLastLine, "", State.CommentML, ML_COMMENT, LooksLike);

        add(State.InitBaskSlash, alpha, State.QCommandStart, COMMAND, LooksLike);
        add(State.InitBaskSlash, "", State.AfterOperator, OPERATOR, MatchPrev);

        add(State.QCommandStart, whitespace, State.QCommand, COMMAND, LooksLike);
        add(State.QCommandStart, "\n", State.Init, COMMAND, Match);
        add(State.QCommandStart, "", State.OSCommand, SYSTEM, LooksLike);

        add(State.QCommand, "\n", State.Init, COMMAND, Match);
        add(State.QCommand, "", State.QCommand, COMMAND, LooksLike);

        add(State.OSCommand, "\n", State.Init, SYSTEM, Match);
        add(State.OSCommand, "", State.OSCommand, SYSTEM, LooksLike);

        add(State.MinusForAtom, except(digits,"0"), State.Digits, LONG, LooksLike);
        add(State.MinusForAtom, "0", State.MinusZero, LONG, LooksLike);
        add(State.MinusForAtom, ".", State.Dot, OPERATOR, LooksLike);
        add(State.MinusForAtom, "", State.AfterOperator, OPERATOR, MatchPrev);

        add(s(State.Zero, State.One), "01", State.ZeroOnes, LONG, LooksLike);
        add(s(State.Zero, State.One), except(digits,"01"), State.Digits, LONG, LooksLike);
        add(s(State.Zero, State.One, State.Two, State.MinusZero, State.ZeroOnes, State.Digits), ".", State.DigitsDot, FLOAT, LooksLike);
        add(s(State.Zero, State.One, State.Two, State.MinusZero,
                        State.ZeroOnes, State.Digits, State.DigitsDot, State.DigitsDotDigits),
                "e", State.FractionE, REAL, LooksLike);
        add(State.Zero, "x", State.Byte, BYTE, LooksLike);
        add(State.Zero, "n", State.NullFloat, FLOAT, LooksLike);
        add(State.Zero, "N", State.NullLong, LONG, LooksLike);
        add(s(State.Zero, State.MinusZero), "w", State.InfFloat, FLOAT, LooksLike);
        add(s(State.Zero, State.MinusZero), "W", State.InfLong, LONG, LooksLike);
        addAtomTypeEnding("jihfpuvtb", State.Zero, State.One, State.Two, State.MinusZero);
        addAtomTypeEnding("jihfepmdznuvt", State.NullFloat, State.NullLong, State.InfFloat, State.InfLong);
        add(s(State.NullFloat, State.NullLong), "g", State.AfterAtom, KEYWORD, Match);
        add(s(State.NullFloat, State.NullLong), "c", State.AfterAtom, STRING, Match);

        add(s(State.MinusZero, State.Digits), digits, State.Digits, LONG, LooksLike);

        add(State.Two, digits, State.Digits, LONG, LooksLike);
        add(s(State.Zero, State.One, State.Two),":", State.AfterOperator, OPERATOR, Match);

        add(State.ZeroOnes, "01", State.ZeroOnes, LONG, LooksLike);
        add(State.ZeroOnes, except(digits, "01"), State.Digits, LONG, LooksLike);
        addAtomTypeEnding(State.ZeroOnes, "jihfpnuvtb");

        addAtomTypeEnding(State.Digits, "jihfpnuvt");

        add(State.DigitsDot, ".", State.UnknownAtom, UNKNOWN, LooksLike);
        add(State.DigitsDot, digits, State.DigitsDotDigits, FLOAT, LooksLike);
        addAtomTypeEnding(State.DigitsDot,"fpnt");

        add(State.DigitsDotDigits, digits, State.DigitsDotDigits, FLOAT, LooksLike);
        add(State.DigitsDotDigits, ".", State.DigitsDotDigitsDot, UNKNOWN, LooksLike);
        addAtomTypeEnding(State.DigitsDotDigits, "fpmnt");

        add(State.Dot, digits, State.DigitsDotDigits, FLOAT, LooksLike);
        add(State.Dot, "", State.AfterOperator, OPERATOR, MatchPrev);

        add(State.FractionE, "+-", State.FractionESign, UNKNOWN, LooksLike);
        add(State.FractionE, digits, State.Scientific, FLOAT, LooksLike);

        add(State.FractionESign, digits, State.Scientific, FLOAT, LooksLike);

        add(State.Scientific, digits, State.Scientific, FLOAT, LooksLike);
        addAtomTypeEnding(State.Scientific, "fem");

        add(State.DigitsDotDigitsDot, digits, State.DateLike, DATE, LooksLike);
        add(State.DateLike, digits, State.DateLike, DATE, LooksLike);
        addAtomTypeEnding(State.DateLike, "dpmzn");

        addTemporal(State.DateLike, State.DateD1, TIMESTAMP,"pmzn","D",digits,":",digits,":",digits,".",digits);
        addTemporal(State.DateLike, State.DateT1, DATETIME,"pmzn","T",digits,":",digits,":",digits,".",digits);
        addTemporal(s(State.Zero, State.One, State.Two, State.ZeroOnes, State.Digits), State.DigitsD1, TIMESPAN, "pmzn", "D",digits,":",digits,":",digits,".",digits);

        String d1 ="9876543210"; // to not equals to digits
        addTemporal(s(State.ZeroOnes, State.Digits), State.DigitsColon,
                new QToken[] {
                        MINUTE, MINUTE, MINUTE, SECOND, TIME, TIME, TIME, TIME, TIMESPAN
                },"uvtpn", ":",digits,":",digits,".",d1,d1,d1,digits);


        add(State.Byte, digits + "abcdefABCDEF", State.Byte, BYTE, LooksLike);

        initUnknownAtom(firstAtomState, lastAtomState);

        add(State.String, "\\", State.StringEscapeBefore, ML_STRING, MatchPrev);
        add(State.String, "\"", State.AfterAtom, STRING, Match);
        add(State.String, "\n", State.String, ML_STRING, Match); // I think this couldn't happen for RSyntaxTokenMaker
        add(State.String, "", State.String, ML_STRING, LooksLike);
        add(State.StringEscapeBefore, "\\", State.StringEscape, ERROR_STRING, LooksLike);
        add(State.StringEscape, "nrt\\\"", State.String, ML_STRING, Match);
        add(State.StringEscape, "0123", State.StringEscapeD, ERROR_STRING, LooksLike);
        add(State.StringEscape, "", State.String, ERROR_STRING, Match);
        add(State.StringEscapeD, "01234567", State.StringEscapeDD, ERROR_STRING, LooksLike);
        add(State.StringEscapeD, "", State.String, ERROR_STRING, Match);
        add(State.StringEscapeDD, "01234567", State.String, ML_STRING, Match);
        add(State.StringEscapeDD, "", State.String, ERROR_STRING, Match);

        add(State.Symbol, alphaNumeric + "`_.", State.Symbol, SYMBOL, LooksLike);
        add(State.Symbol, ":", State.SymbolColon, SYMBOL, LooksLike);
        add(State.Symbol, "", State.AfterAtom, SYMBOL, MatchPrev);

        add(State.SymbolColon, alphaNumeric + "_.:/", State.SymbolColon, SYMBOL, LooksLike);
        add(State.SymbolColon, "`", State.Symbol, SYMBOL, LooksLike);
        add(State.SymbolColon, "", State.AfterAtom, SYMBOL, MatchPrev);

        add(State.Identifier, alphaNumeric +"_", State.Identifier, IDENTIFIER, LooksLike);
        add(State.Identifier, ".", State.IdentifierDot, IDENTIFIER, LooksLike);
        add(State.IdentifierDot, alpha, State.Identifier, IDENTIFIER, LooksLike);
        add(State.Dot, alpha, State.Identifier, IDENTIFIER, LooksLike);
    }

    static {
        stateMachine.init();
    }

}
