package studio.kdb;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class K {
    private final static DecimalFormat nsFormatter = new DecimalFormat("000000000");
    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd");
    private final static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS");
    private final static SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyy.MM.dd'D'HH:mm:ss.");


    static {
        TimeZone gmtTimeZone = java.util.TimeZone.getTimeZone("GMT");
        Stream.of(dateFormatter, dateTimeFormatter, timestampFormatter)
                .forEach(f -> f.setTimeZone(gmtTimeZone));
    }

    private static final String enlist = "enlist ";
    private static final String flip = "flip ";

    public static void write(OutputStream o, byte b) throws IOException {
        o.write(b);
    }

    public static void write(OutputStream o, short h) throws IOException {
        write(o, (byte) (h >> 8));
        write(o, (byte) h);
    }

    public static void write(OutputStream o, int i) throws IOException {
        write(o, (short) (i >> 16));
        write(o, (short) i);
    }

    public static void write(OutputStream o, long j) throws IOException {
        write(o, (int) (j >> 32));
        write(o, (int) j);
    }

    public abstract static class KBase implements Comparable<KBase> {
        public abstract String getDataType();

        private final int type;

        protected KBase(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void serialise(OutputStream o) throws IOException {
            write(o, (byte) type);
            serialiseData(o);
        }

        protected void serialiseData(OutputStream o) throws IOException {
            throw new IllegalStateException("The method is not implemented");
        }

        public boolean isNull() {
            return false;
        }

        public final String toString() {
            return toString(KFormatContext.DEFAULT);
        }

        public final String toString(KFormatContext context) {
            return format(null, context).toString();
        }

        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            if (builder == null) builder = new StringBuilder();
            return builder;
        }

        public int count() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass().equals(this.getClass());
        }

        @Override
        public int compareTo(KBase o) {
            return toString(KFormatContext.NO_TYPE).compareTo(o.toString(KFormatContext.NO_TYPE));
        }
    }

    private abstract static class KByteBase extends KBase implements ToDouble {
        protected byte value;

        KByteBase(int type, byte value) {
            super(type);
            this.value = value;
        }
        public double toDouble() {
            return value;
        }

        @Override
        protected void serialiseData(OutputStream o) throws IOException {
            write(o, value);
        }

        @Override
        public int hashCode() {
            return Byte.hashCode(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) return false;
            return value == ((KByteBase)obj).value;
        }

        @Override
        public String getDataType() {
            return null;
        }

        @Override
        public int compareTo(KBase o) {
            if (o instanceof KByteBase)  {
                return Byte.compare(value, ((KByteBase)o).value);
            }
            return super.compareTo(o);
        }
    }

    private abstract static class KIntBase extends KBase implements ToDouble {
        protected int value;

        KIntBase(int type, int value) {
            super(type);
            this.value = value;
        }
        public boolean isNull() {
            return value == Integer.MIN_VALUE;
        }

        public double toDouble() {
            return value;
        }

        public int toInt() {
            return value;
        }

        @Override
        public void serialiseData(OutputStream o) throws IOException {
            write(o, value);
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) return false;
            return value == ((KIntBase)obj).value;
        }

        @Override
        public int compareTo(KBase o) {
            if (o instanceof KIntBase) {
                return Integer.compare(value, ((KIntBase)o).value);
            }
            return super.compareTo(o);
        }
    }

    private abstract static class KLongBase extends KBase implements ToDouble {
        protected long value;

        KLongBase(int type, long value) {
            super(type);
            this.value = value;
        }
        public boolean isNull() {
            return value == Long.MIN_VALUE;
        }

        public double toDouble() {
            return value;
        }

        public long toLong() {
            return value;
        }
        @Override
        public void serialiseData(OutputStream o) throws IOException {
            write(o, value);
        }
        @Override
        public int hashCode() {
            return Long.hashCode(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (! obj.getClass().equals(this.getClass())) return false;
            return value == ((KLongBase)obj).value;
        }

        @Override
        public int compareTo(KBase o) {
            if (o instanceof KLongBase) {
                return Long.compare(value, ((KLongBase)o).value);
            }
            return super.compareTo(o);
        }
    }

    private abstract static class KDoubleBase extends KBase implements ToDouble {
        protected double value;

        KDoubleBase(int type, double value) {
            super(type);
            this.value = value;
        }

        public boolean isNull() {
            return Double.isNaN(value);
        }

        public double toDouble() {
            return value;
        }

        @Override
        public void serialiseData(OutputStream o) throws IOException {
            long j = Double.doubleToLongBits(value);
            write(o, j);
        }

        @Override
        public int hashCode() {
            return Double.hashCode(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) return false;
            return Double.doubleToLongBits(value) == Double.doubleToLongBits(((KDoubleBase)obj).value);
        }

        @Override
        public int compareTo(KBase o) {
            if (o instanceof KDoubleBase) {
                return Double.compare(value, ((KDoubleBase)o).value);
            }
            return super.compareTo(o);
        }
    }

    private abstract static class KArrayBase extends KBase {
        protected KBase[] array;

        KArrayBase(int type, KBase[] array) {
            super(type);
            this.array = array;
        }


        @Override
        public void serialiseData(OutputStream o) throws IOException {
            write(o, array.length);
            for (KBase obj: array) {
                obj.serialise(o);
            }
        }

        @Override
        public int hashCode() {
            return array.length;
        }

        @Override
        public boolean equals(Object obj) {
            if (! super.equals(obj)) return false;
            return Objects.deepEquals(array, ((KArrayBase)obj).array);
        }
    }

    public abstract static class Adverb extends KBase {
        public String getDataType() {
            return "Adverb";
        }

        protected K.KBase obj;

        public Adverb(int type, K.KBase o) {
            super(type);
            this.obj = o;
        }

        public K.KBase getObject() {
            return obj;
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            return super.format(builder, context).append(obj.toString(context));
        }

        @Override
        protected void serialiseData(OutputStream o) throws IOException {
            obj.serialise(o);
        }

        @Override
        public int hashCode() {
            return obj.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) return false;
            return this.obj.equals(((Adverb)obj).obj);
        }
    }

    public static class BinaryPrimitive extends Primitive {
        private final static String[] ops = {":", "+", "-", "*", "%", "&", "|", "^", "=", "<", ">", "$", ",", "#", "_", "~", "!", "?", "@", ".", "0:", "1:", "2:", "in", "within", "like", "bin", "ss", "insert", "wsum", "wavg", "div", "xexp", "setenv", "binr", "cov", "cor"};

        public String getDataType() {
            return "Binary Primitive";
        }

        public BinaryPrimitive(int i) {
            super(102, ops, i);
        }

    }

    public static class FComposition extends KArrayBase {
        public String getDataType() {
            return "Function Composition";
        }

        public FComposition(KBase... array) {
            super(105, array);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            for (KBase arg: array) {
                arg.format(builder, context);
            }
            return builder;
        }
    }

    public static class FEachLeft extends Adverb {
        public FEachLeft(K.KBase o) {
            super(111, o);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            return super.format(builder, context).append("\\:");
        }
    }

    public static class FEachRight extends Adverb {
        public FEachRight(K.KBase o) {
            super(110, o);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            return super.format(builder, context).append("/:");
        }
    }

    public static class FPrior extends Adverb {
        public FPrior(K.KBase o) {
            super(109, o);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            return super.format(builder, context).append("':");
        }
    }

    public static class Feach extends Adverb {
        public Feach(K.KBase o) {
            super(106, o);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            return super.format(builder, context).append("'");
        }
    }

    public static class Fover extends Adverb {
        public Fover(K.KBase o) {
            super(107, o);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            return super.format(builder, context).append("/");
        }
    }

    public static class Fscan extends Adverb {
        public Fscan(KBase o) {
            super(108, o);
            this.obj = o;
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            return super.format(builder, context).append("\\");
        }
    }

    public static class Function extends KBase {
        public String getDataType() {
            return "Function";
        }

        private final String body;

        public Function(String body) {
            super(100);
            this.body = body;
        }

        public Function(KCharacterVector body) {
            this(body.getString());
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            return super.format(builder, context).append(body);
        }

        @Override
        protected void serialiseData(OutputStream o) throws IOException {
            write(o, (byte) 0);
            new KCharacterVector(body).serialise(o);
        }

        @Override
        public int hashCode() {
            return body.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof Function)) return false;
            return body.equals(((Function)obj).body);
        }
    }

    public abstract static class Primitive extends KByteBase {
        public String getDataType() {
            return "Primitive";
        }

        private String s = " ";

        public Primitive(int type, String[] ops, int value) {
            super(type, (byte) value);
            if (value >= 0 && value < ops.length)
                s = ops[value];
        }

        public String getPrimitive() {
            return s;
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (value != -1) builder.append(getPrimitive());
            return builder;
        }

    }

    public static class Projection extends KArrayBase {
        public String getDataType() {
            return "Projection";
        }

        public Projection(KBase... array) {
            super(104, array);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (array.length == 0) return builder; // not sure if such is possible
            array[0].format(builder, context);
            builder.append("[");
            for (int i = 1; i < array.length; i++) {
                if (i > 1) builder.append(";");
                array[i].format(builder, context);
            }
            builder.append("]");
            return builder;
        }
    }

    public static class TernaryOperator extends Primitive {
        private final static String[] ops = {"'", "/", "\\"};

        public String getDataType() {
            return "Ternary Operator";
        }

        public TernaryOperator(int i) {
            super(103, ops, i);
        }
    }

    public static class UnaryPrimitive extends Primitive {
        private static final String[] ops = {"::", "+:", "-:", "*:", "%:", "&:", "|:", "^:", "=:", "<:", ">:", "$:", ",:", "#:", "_:", "~:", "!:", "?:", "@:", ".:", "0::", "1::", "2::", "avg", "last", "sum", "prd", "min", "max", "exit", "getenv", "abs", "sqrt", "log", "exp", "sin", "asin", "cos", "acos", "tan", "atan", "enlist", "var", "dev", "hopen"};

        public UnaryPrimitive(int i) {
            super(101, ops, i);
        }

        public boolean isIdentity() {
            return value == 0;
        }
    }

    public static class KBoolean extends KBase implements ToDouble {
        public String getDataType() {
            return "Boolean";
        }

        public boolean b;

        public KBoolean(boolean b) {
            super(-1);
            this.b = b;
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            return super.format(builder, context)
                        .append(b ? "1" : "0")
                        .append(context.showType() ? "b" : "");
        }

        public double toDouble() {
            return b ? 1.0 : 0.0;
        }

        public boolean toBoolean() {
            return b;
        }

        @Override
        protected void serialiseData(OutputStream o) throws IOException {
            write(o,(byte) (b ? 1:0));
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(b);
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof KBoolean)) return false;
            return b == ((KBoolean)obj).b;
        }

        @Override
        public int compareTo(KBase o) {
            if (o instanceof KBoolean) {
                return Boolean.compare(b, ((KBoolean)o).b);
            }
            return super.compareTo(o);
        }
    }

    public static class KByte extends KByteBase{
        public String getDataType() {
            return "Byte";
        }

        public KByte(byte b) {
            super(-4, b);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            return super.format(builder, context)
                        .append("0x")
                        .append(Integer.toHexString((value >> 4) & 0xf))
                        .append(Integer.toHexString(value & 0xf));
        }

    }

    public static class KShort extends KBase implements ToDouble {
        public String getDataType() {
            return "Short";
        }

        public short s;

        public double toDouble() {
            return s;
        }

        public KShort(short s) {
            super(-5);
            this.s = s;
        }

        public boolean isNull() {
            return s == Short.MIN_VALUE;
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0N");
            else if (s == Short.MAX_VALUE) builder.append("0W");
            else if (s == -Short.MAX_VALUE) builder.append("-0W");
            else builder.append(context.getNumberFormat().format(s));
            if (context.showType()) builder.append("h");
            return builder;
        }

        @Override
        protected void serialiseData(OutputStream o) throws IOException {
            write(o, s);
        }

        @Override
        public int hashCode() {
            return Short.hashCode(s);
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof KShort)) return false;
            return s == ((KShort)obj).s;
        }

        @Override
        public int compareTo(KBase o) {
            if (o instanceof KShort) {
                return Short.compare(s, ((KShort)o).s);
            }
            return super.compareTo(o);
        }
    }

    public static class KInteger extends KIntBase {
        public String getDataType() {
            return "Integer";
        }

        public KInteger(int i) {
            super(-6, i);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0N");
            else if (value == Integer.MAX_VALUE) builder.append("0W");
            else if (value == -Integer.MAX_VALUE) builder.append("-0W");
            else builder.append(context.getNumberFormat().format(value));
            if (context.showType()) builder.append("i");
            return builder;
        }
    }

    public static class KSymbol extends KBase {
        public String getDataType() {
            return "Symbol";
        }

        public String s;

        public KSymbol(String s) {
            super(-11);
            this.s = s;
        }

        public boolean isNull() {
            return s.length() == 0;
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (context.showType()) builder.append("`");
            return builder.append(s);
        }

        @Override
        public void serialiseData(OutputStream o) throws IOException {
            o.write(s.getBytes(Config.getInstance().getEncoding()));
            write(o, (byte) 0);
        }

        @Override
        public int hashCode() {
            return s.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof KSymbol)) return false;
            return s.equals(((KSymbol)obj).s);
        }
    }

    public static class KLong extends KLongBase {
        public String getDataType() {
            return "Long";
        }

        public KLong(long j) {
            super(-7, j);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0N");
            else if (value == Long.MAX_VALUE) builder.append("0W");
            else if (value == -Long.MAX_VALUE) builder.append("-0W");
            else builder.append(context.getNumberFormat().format(value));
            return builder;
        }
    }

    public static class KCharacter extends KBase {
        public String getDataType() {
            return "Character";
        }

        public char c;

        public KCharacter(char c) {
            super(-10);
            this.c = c;
        }

        public boolean isNull() {
            return c == ' ';
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (context.showType()) builder.append("\"").append(c).append("\"");
            else builder.append(c);
            return builder;
        }

        @Override
        public void serialiseData(OutputStream o) throws IOException {
            write(o, (byte) c);
        }

        @Override
        public int hashCode() {
            return Character.hashCode(c);
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof KCharacter)) return false;
            return c == ((KCharacter)obj).c;
        }
    }

    public static class KFloat extends KBase implements ToDouble {
        public String getDataType() {
            return "Float";
        }

        public float f;

        public double toDouble() {
            return f;
        }

        public KFloat(float f) {
            super(-8);
            this.f = f;
        }

        public boolean isNull() {
            return Float.isNaN(f);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0N");
            else if (f == Float.POSITIVE_INFINITY) builder.append("0w");
            else if (f == Float.NEGATIVE_INFINITY) builder.append("-0w");
            else builder.append(context.getNumberFormat().format(f));
            if (context.showType()) builder.append("e");
            return builder;
        }

        @Override
        public void serialiseData(OutputStream o) throws IOException {
            int i = Float.floatToIntBits(f);
            write(o, i);
        }

        @Override
        public int hashCode() {
            return Float.hashCode(f);
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof KFloat)) return false;
            return Float.floatToIntBits(f) == Float.floatToIntBits(((KFloat)obj).f);
        }

        @Override
        public int compareTo(KBase o) {
            if (o instanceof KFloat) {
                return Float.compare(f, ((KFloat)o).f);
            }
            return super.compareTo(o);
        }
    }

    public static class KDouble extends KDoubleBase {
        public String getDataType() {
            return "Double";
        }

        public KDouble(double d) {
            super(-9, d);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0n");
            else if (value == Double.POSITIVE_INFINITY) builder.append("0w");
            else if (value == Double.NEGATIVE_INFINITY) builder.append("-0w");
            else builder.append(context.getNumberFormat().format(value));
            if (context.showType()) builder.append("f");
            return builder;
        }

        @Override
        public void serialiseData(OutputStream o) throws IOException {
            long j = Double.doubleToLongBits(value);
            write(o, j);
        }
    }

    public static class KDate extends KIntBase {
        public String getDataType() {
            return "Date";
        }

        public KDate(int date) {
            super(-14, date);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0Nd");
            else if (value == Integer.MAX_VALUE) builder.append("0Wd");
            else if (value == -Integer.MAX_VALUE) builder.append("-0Wd");
            else builder.append(dateFormatter.format(toDate()));
            return builder;
        }

        public Date toDate() {
            return new Date(86400000L * (value + 10957));
        }
    }

    public static class KGuid extends KBase {
        static UUID nuuid = new UUID(0, 0);

        public String getDataType() {
            return "Guid";
        }

        UUID uuid;

        public KGuid(UUID uuid) {
            super(-2);
            this.uuid = uuid;
        }

        public boolean isNull() {
            return uuid == nuuid;
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            return super.format(builder, context).append(uuid);
        }

        @Override
        protected void serialiseData(OutputStream o) throws IOException {
            write(o, uuid.getMostSignificantBits());
            write(o, uuid.getLeastSignificantBits());

        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof KGuid)) return false;
            return uuid.equals(((KGuid)obj).uuid);
        }
    }

    public static class KTime extends KIntBase {
        public String getDataType() {
            return "Time";
        }

        public KTime(int time) {
            super(-19, time);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0Nt");
            else if (value == Integer.MAX_VALUE) builder.append("0Wt");
            else if (value == -Integer.MAX_VALUE) builder.append("-0Wt");
            else {
                int v = value;
                if (v<0) {
                    builder.append("-");
                    v = -v;
                }
                int ms = v % 1000;
                int s = v / 1000 % 60;
                int m = v / 60000 % 60;
                int h = v / 3600000;
                builder.append(i2(h)).append(":").append(i2(m)).append(":").append(i2(s))
                        .append(".").append(i3(ms));
            }
            return builder;
        }

        public Time toTime() {
            return new Time(value);
        }
    }

    public static class KDatetime extends KDoubleBase {
        public String getDataType() {
            return "Datetime";
        }

        public KDatetime(double time) {
            super(-15, time);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0Nz");
            else if (value == Double.POSITIVE_INFINITY) builder.append("0wz");
            else if (value == Double.NEGATIVE_INFINITY) builder.append("-0wz");
            else builder.append(dateTimeFormatter.format(toTimestamp()));
            return builder;
        }

        public Timestamp toTimestamp() {
            return new Timestamp(((long) (.5 + 8.64e7 * (value + 10957))));
        }
    }


    public static class KTimestamp extends KLongBase {
        public String getDataType() {
            return "Timestamp";
        }

        public KTimestamp(long time) {
            super(-12, time);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0Np");
            else if (value == Long.MAX_VALUE) builder.append("0Wp");
            else if (value == -Long.MAX_VALUE) builder.append("-0Wp");
            else {
                Timestamp ts = toTimestamp();
                builder.append(timestampFormatter.format(ts))
                                .append(nsFormatter.format(ts.getNanos()));
            }
            return builder;
        }

        public Timestamp toTimestamp() {
            long k = 86400000L * 10957;
            long n = 1000000000L;
            long d = value < 0 ? (value + 1) / n - 1 : value / n;
            long ltime = value == Long.MIN_VALUE ? value : (k + 1000 * d);
            int nanos = (int) (value - n * d);
            Timestamp ts = new Timestamp(ltime);
            ts.setNanos(nanos);
            return ts;
        }
    }

    public static class Dict extends KBase {
        public String getDataType() {
            return "Dictionary";
        }

        private byte attr = 0;

        public K.KBase x;
        public K.KBase y;

        public Dict(K.KBase X, K.KBase Y) {
            super(99);
            x = X;
            y = Y;
        }

        //@TODO: change to somethign like setSortAttr
        public void setAttr(byte attr) {
            this.attr = attr;
        }

        @Override
        public int count() {
            return x.count();
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            boolean useBrackets = attr != 0 || x instanceof Flip || (x.count() == 1);
            if (useBrackets) builder.append("(");
            x.format(builder, context);
            if (useBrackets) builder.append(")");
            builder.append("!");
            y.format(builder, context);
            return builder;
        }

        @Override
        public void serialise(OutputStream o) throws IOException {
            write(o, (byte) (attr == 1 ? 127 : 99));
            x.serialise(o);
            y.serialise(o);
        }

        @Override
        public int hashCode() {
            return x.hashCode() +  137 * y.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof Dict)) return false;
            Dict dict = (Dict) obj;
            return x.equals(dict.x) && y.equals(dict.y) && attr == dict.attr;
        }
    }

    abstract private static class FlipBase extends KBase {
        public String getDataType() {
            return "Flip";
        }

        public FlipBase() {
            super(98);
        }

        abstract public K.KBase getX();
        abstract public K.KBase getY();

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            builder.append(flip);
            return new Dict(getX(), getY()).format(builder, context);
        }

        @Override
        protected void serialiseData(OutputStream o) throws IOException {
            write(o, (byte)0);
            new Dict(getX(),getY()).serialise(o);
        }

        @Override
        public int hashCode() {
            return getX().hashCode() + 137 * getY().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof FlipBase)) return false;
            FlipBase flip = (FlipBase) obj;
            return getX().equals(flip.getX()) && getY().equals(flip.getY());
        }
    }

    public static class Flip extends FlipBase {
        public K.KSymbolVector x;
        public K.KBaseVector<? extends KBase> y;

        public Flip(K.KSymbolVector names, K.KBaseVector<? extends KBase> cols) {
            super();
            x = names;
            y = cols;
        }

        @Override
        public KBase getX() {
            return x;
        }

        @Override
        public KBase getY() {
            return y;
        }

        @Override
        public int count() {
            return y.at(0).count();
        }
    }

    public static class MappedTable extends FlipBase {
        private K.Dict dict;

        public MappedTable(K.Dict dict) {
            super();
            this.dict = dict;
        }

        @Override
        public KBase getX() {
            return dict.x;
        }

        @Override
        public KBase getY() {
            return dict.y;
        }
    }

    //@TODO: rename to KMonth
    public static class Month extends KIntBase {
        public String getDataType() {
            return "Month";
        }

        public Month(int x) {
            super(-13, x);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0N");
            else if (value == Integer.MAX_VALUE) builder.append("0W");
            else if (value == -Integer.MAX_VALUE) builder.append("-0W");
            else {
                int m = value + 24000, y = m / 12;

                builder.append(i2(y / 100)).append(i2(y % 100))
                        .append(".").append(i2(1 + m % 12));
            }
            if (context.showType()) builder.append("m");
            return builder;
        }

        public Date toDate() {
            int m = value + 24000, y = m / 12;
            m %= 12;
            Calendar cal = Calendar.getInstance();
            cal.set(y, m, 1);
            return cal.getTime();
        }
    }

    //@TODO: rename to Minute
    public static class Minute extends KIntBase {
        public String getDataType() {
            return "Minute";
        }

        public Minute(int x) {
            super(-17, x);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0Nu");
            else if (value == Integer.MAX_VALUE) builder.append("0Wu");
            else if (value == -Integer.MAX_VALUE) builder.append("-0Wu");
            else builder.append(i2(value / 60)).append(":").append(i2(value % 60));
            return builder;
        }
        public Date toDate() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, value / 60);
            cal.set(Calendar.MINUTE, value % 60);
            return cal.getTime();
        }
    }

    //@TODO: rename to KSecond
    public static class Second extends KIntBase {
        public String getDataType() {
            return "Second";
        }

        public Second(int x) {
            super(-18, x);
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0Nv");
            else if (value == Integer.MAX_VALUE) builder.append("0Wv");
            else if (value == -Integer.MAX_VALUE) builder.append("-0Wv");
            else {
                int s = value % 60;
                int m = value / 60 % 60;
                int h = value / 3600;
                builder.append(i2(h)).append(":").append(i2(m)).append(":").append(i2(s));
            }
            return builder;
        }

        public Date toDate() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, value / (60 * 60));
            cal.set(Calendar.MINUTE,  (value % (60 * 60)) / 60);
            cal.set(Calendar.SECOND, value % 60);
            return cal.getTime();
        }
    }

    public static class KTimespan extends KLongBase {
        public KTimespan(long x) {
            super(-16, x);
        }

        public String getDataType() {
            return "Timespan";
        }

        @Override
        public StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (isNull()) builder.append("0Nn");
            else if (value == Long.MAX_VALUE) builder.append("0Wn");
            else if (value == -Long.MAX_VALUE) builder.append("-0Wn");
            else {
                long jj = value;
                if (jj < 0) {
                    jj = -jj;
                    builder.append("-");
                }
                int d = ((int) (jj / 86400000000000L));
                if (d != 0) builder.append(d).append("D");
                builder.append(i2((int) ((jj % 86400000000000L) / 3600000000000L)))
                        .append(":").append(i2((int) ((jj % 3600000000000L) / 60000000000L)))
                        .append(":").append(i2((int) ((jj % 60000000000L) / 1000000000L)))
                        .append(".").append(nsFormatter.format((int) (jj % 1000000000L)));
            }
            return builder;
        }

        public Time toTime() {
            return new Time((value / 1000000));
        }
    }

    private static java.text.DecimalFormat i2Formatter = new java.text.DecimalFormat("00");
    private static java.text.DecimalFormat i3Formatter = new java.text.DecimalFormat("000");

    private static String i2(int i) {
        return i2Formatter.format(i);
    }

    private static String i3(int i) {
        return i3Formatter.format(i);
    }

    public static abstract class KBaseVector<E extends KBase> extends KBase {
        protected Object array;
        private int length;
        private byte attr = 0;
        private final String typeName;
        private final String typeChar;

        protected KBaseVector(Object array, int type, String typeName, String typeChar) {
            super(type);
            this.array = array;
            this.length = Array.getLength(array);
            this.typeName = typeName;
            this.typeChar = typeChar;
        }

        public abstract E at(int i);

        public byte getAttr() {
            return attr;
        }

        public void setAttr(byte attr) {
            this.attr = attr;
        }

        @Override
        public int count() {
            return getLength();
        }

        //@TODO: replace with count()
        public int getLength() {
            return length;
        }

        public Object getArray() {
            return array;
        }

        private final static String[] sAttr = new String[]{"", "`s#", "`u#", "`p#", "`g#"};

        //default implementation
        protected StringBuilder formatVector(StringBuilder builder, KFormatContext context) {
            if (getLength() == 0) builder.append("`").append(typeName).append("$()");
            else {
                if (getLength() == 1) builder.append(enlist);
                KFormatContext childContext = context.showType() ? new KFormatContext(context).setShowType(false) : context;
                for (int i = 0; i < getLength(); i++) {
                    if (i > 0) builder.append(" ");
                    at(i).format(builder, childContext);
                }
                if (context.showType()) builder.append(typeChar);
            }
            return builder;
        }

        @Override
        public final StringBuilder format(StringBuilder builder, KFormatContext context) {
            builder = super.format(builder, context);
            if (context.showType() && attr <= sAttr.length) builder.append(sAttr[attr]);
            return formatVector(builder, context);
        }

        @Override
        public void serialiseData(OutputStream o) throws IOException {
            write(o, attr);
            write(o, length);
            for (int index=0; index<length; index++) {
                at(index).serialiseData(o);
            }
        }

        @Override
        public int hashCode() {
            return length*getType();
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) return false;
            KBaseVector<? extends KBase> vector = (KBaseVector<? extends KBase>)obj;
            return Objects.deepEquals(array, vector.array) && attr == vector.attr;
        }
    }

    public static class KShortVector extends KBaseVector<KShort> {
        public String getDataType() {
            return "Short Vector";
        }

        public KShortVector(short... array) {
            super(array, 5, "short", "h");
        }

        public KShort at(int i) {
            return new KShort(Array.getShort(array, i));
        }
    }

    public static class KIntVector extends KBaseVector<KInteger> {
        public String getDataType() {
            return "Int Vector";
        }

        public KIntVector(int... array) {
            super(array, 6, "int", "i");
        }

        public KInteger at(int i) {
            return new KInteger(Array.getInt(array, i));
        }
    }

    public static class KList extends KBaseVector<KBase> {
        public String getDataType() {
            return "List";
        }

        public KList(KBase... array) {
            super(array, 0, "", "");
        }

        public KBase at(int i) {
            return (KBase) Array.get(array, i);
        }

        @Override
        protected StringBuilder formatVector(StringBuilder builder, KFormatContext context) {
            if (getLength() == 1) builder.append(enlist);
            else builder.append("(");
            for (int i = 0; i < getLength(); i++) {
                if (i > 0) builder.append(";");
                at(i).format(builder, context);
            }
            if (getLength() != 1) builder.append(")");
            return builder;
        }

        @Override
        public void serialiseData(OutputStream o) throws IOException {
            int length = getLength();
            write(o, (byte) 0);
            write(o, length);
            for (int index=0; index<length; index++) {
                at(index).serialise(o);
            }
        }
    }

    public static class KDoubleVector extends KBaseVector<KDouble> {
        public String getDataType() {
            return "Double Vector";
        }

        public KDoubleVector(double... array) {
            super(array, 9, "float", "f");
        }

        public KDouble at(int i) {
            return new KDouble(Array.getDouble(array, i));
        }
    }

    public static class KFloatVector extends KBaseVector<KFloat> {
        public String getDataType() {
            return "Float Vector";
        }

        public KFloatVector(float... array) {
            super(array, 8, "real", "e");
        }

        public KFloat at(int i) {
            return new KFloat(Array.getFloat(array, i));
    }

    }

    public static class KLongVector extends KBaseVector<KLong> {
        public String getDataType() {
            return "Long Vector";
        }

        public KLongVector(long... array) {
            super(array, 7, "long", "");
        }

        public KLong at(int i) {
            return new KLong(Array.getLong(array, i));
        }
    }

    public static class KMonthVector extends KBaseVector<Month> {
        public String getDataType() {
            return "Month Vector";
        }

        public KMonthVector(int... array) {
            super(array, 13, "month", "m");
        }

        public Month at(int i) {
            return new Month(Array.getInt(array, i));
        }
    }

    public static class KDateVector extends KBaseVector<KDate> {
        public String getDataType() {
            return "Date Vector";
        }

        public KDateVector(int... array) {
            super(array, 14, "date", "");
        }

        public KDate at(int i) {
            return new KDate(Array.getInt(array, i));
        }
    }

    public static class KGuidVector extends KBaseVector<KGuid> {
        public String getDataType() {
            return "Guid Vector";
        }

        public KGuidVector(UUID... array) {
            super(array, 2, "guid", "");
        }

        public KGuid at(int i) {
            return new KGuid((UUID) Array.get(array, i));
        }
    }

    public static class KMinuteVector extends KBaseVector<Minute> {
        public String getDataType() {
            return "Minute Vector";
        }

        public KMinuteVector(int... array) {
            super(array, 17, "minute", "");
        }

        public Minute at(int i) {
            return new Minute(Array.getInt(array, i));
        }
    }

    public static class KDatetimeVector extends KBaseVector<KDatetime> {
        public String getDataType() {
            return "Datetime Vector";
        }

        public KDatetimeVector(double... array) {
            super(array, 15, "datetime", "");
        }

        public KDatetime at(int i) {
            return new KDatetime(Array.getDouble(array, i));
        }
    }

    public static class KTimestampVector extends KBaseVector<KTimestamp> {
        public String getDataType() {
            return "Timestamp Vector";
        }

        public KTimestampVector(long... array) {
            super(array, 12, "timestamp", "");
        }

        public KTimestamp at(int i) {
            return new KTimestamp(Array.getLong(array, i));
        }
    }

    public static class KTimespanVector extends KBaseVector<KTimespan> {
        public String getDataType() {
            return "Timespan Vector";
        }

        public KTimespanVector(long... array) {
            super(array, 16, "timespan", "");
        }

        public KTimespan at(int i) {
            return new KTimespan(Array.getLong(array, i));
        }
    }

    public static class KSecondVector extends KBaseVector<Second> {
        public String getDataType() {
            return "Second Vector";
        }

        public KSecondVector(int... array) {
            super(array, 18, "second", "");
        }

        public Second at(int i) {
            return new Second(Array.getInt(array, i));
        }
    }

    public static class KTimeVector extends KBaseVector<KTime> {
        public String getDataType() {
            return "Time Vector";
        }

        public KTimeVector(int... array) {
            super(array, 19, "time", "");
        }

        public KTime at(int i) {
            return new KTime(Array.getInt(array, i));
        }
    }

    public static class KBooleanVector extends KBaseVector<KBoolean> {
        public String getDataType() {
            return "Boolean Vector";
        }

        public KBooleanVector(boolean... array) {
            super(array, 1, "boolean", "b");
        }

        public KBoolean at(int i) {
            return new KBoolean(Array.getBoolean(array, i));
        }

        @Override
        protected StringBuilder formatVector(StringBuilder builder, KFormatContext context) {
            if (getLength() == 0) builder.append("`boolean$()");
            else {
                if (getLength() == 1) builder.append(enlist);
                for (int i = 0; i < getLength(); i++)
                    builder.append(Array.getBoolean(array, i) ? "1" : "0");
                builder.append("b");
            }
            return builder;
        }
    }

    public static class KByteVector extends KBaseVector<KByte> {
        public String getDataType() {
            return "Byte Vector";
        }

        public KByteVector(byte... array) {
            super(array, 4, "byte", "x");
        }

        public KByte at(int i) {
            return new KByte(Array.getByte(array, i));
        }

        @Override
        protected StringBuilder formatVector(StringBuilder builder, KFormatContext context) {
            if (getLength() == 0) builder.append("`byte$()");
            else {
                if (getLength() == 1) builder.append(enlist);
                builder.append("0x");
                for (int i = 0; i < getLength(); i++) {
                    byte b = Array.getByte(array, i);
                    builder.append(Integer.toHexString((b >> 4) & 0xf))
                            .append(Integer.toHexString(b & 0xf));
                }
            }
            return builder;
        }
    }

    public static class KSymbolVector extends KBaseVector<KSymbol> {
        public String getDataType() {
            return "Symbol Vector";
        }

        public KSymbolVector(String... array) {
            super(array, 11, "symbol", "s");
        }

        public KSymbol at(int i) {
            return new KSymbol((String) Array.get(array, i));
        }

        @Override
        protected StringBuilder formatVector(StringBuilder builder, KFormatContext context) {
            if (getLength() == 0) builder.append("`symbol$()");
            else {
                if (getLength() == 1) builder.append(enlist);
                for (int i = 0; i < getLength(); i++)
                    builder.append("`").append(Array.get(array, i));
            }
            return builder;
        }
    }

    public static class KCharacterVector extends KBaseVector<KCharacter> {
        public String getDataType() {
            return "Character Vector";
        }

        public KCharacterVector(String value) {
            super(value.toCharArray(), 10, "char", "c");
        }

        public KCharacter at(int i) {
            return new KCharacter(Array.getChar(array, i));
        }

        public String getString() {
            return new String((char[]) array);
        }

        @Override
        protected StringBuilder formatVector(StringBuilder builder, KFormatContext context) {
            if (getLength() == 1) {
                char ch = Array.getChar(array, 0);
                if (ch<=255) {
                    builder.append(enlist);
                }
            }

            if (context.showType()) builder.append("\"");
            builder.append(getString());
            if (context.showType()) builder.append("\"");
            return builder;
        }

        @Override
        public void serialiseData(OutputStream o) throws IOException {
            write(o, getAttr());
            byte[] b = getString().getBytes(Config.getInstance().getEncoding());
            write(o, b.length);
            o.write(b);
        }
    }
}
