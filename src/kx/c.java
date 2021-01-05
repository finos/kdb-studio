// licensed according to http://code.kx.com/wiki/TermsAndConditions
package kx;

/*
types
20+ userenums
98 table
99 dict
100 lambda
101 unary prim
102 binary prim
103 ternary(operator)
104 projection
105 composition
106 f'
107 f/
108 f\
109 f':
110 f/:
111 f\:
112 dynamic load
 */
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import studio.kdb.K;

public class c {
    DataInputStream inputStream;
    OutputStream outputStream;
    Socket s;
    byte[] b, B;
    int j;
    int J;
    boolean a;
    int rxBufferSize;
    private String encoding = "UTF-8";

    void io(Socket s) throws IOException {
        s.setTcpNoDelay(true);
        inputStream = new DataInputStream(s.getInputStream());
        outputStream = s.getOutputStream();
        rxBufferSize = s.getReceiveBufferSize();
    }

    public void close() {
        // this will force k() to break out i hope
        if (closed) return;

        closed = true;
        if (inputStream != null)
            try {
                inputStream.close();
            } catch (IOException e) {}
        if (outputStream != null)
            try {
                outputStream.close();
            } catch (IOException e) {}
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {}
        }
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public static class K4AccessException extends K4Exception {
        K4AccessException() {
            super("Authentication failed");
        }
    }

    boolean closed = true;

    public boolean isClosed() {
        return closed;
    }

    private void connect(boolean retry) throws IOException, K4AccessException {
        s = new Socket();
        s.setReceiveBufferSize(1024 * 1024);
        s.connect(new InetSocketAddress(host, port));

        if (useTLS) {
            try {
                s = ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(s, host, port, true);
                ((SSLSocket) s).startHandshake();
            } catch (Exception e) {
                s.close();
                throw e;
            }
        }
        io(s);
        java.io.ByteArrayOutputStream baos = new ByteArrayOutputStream();
        java.io.DataOutputStream dos = new DataOutputStream(baos);
        dos.write((up + (retry ? "\3" : "")).getBytes());
        dos.writeByte(0);
        dos.flush();
        outputStream.write(baos.toByteArray());
        byte[] bytes = new byte[2 + up.getBytes().length];
        if (1 != inputStream.read(bytes, 0, 1))
            if (retry)
                connect(false);
            else
                throw new K4AccessException();
        closed = false;
    }

    private String host;
    private int port;
    private String up;
    private boolean useTLS;

    public c(String h, int p, String u, boolean useTLS) {
        host = h;
        port = p;
        up = u;
        this.useTLS = useTLS;
    }

    boolean rb() {
        return 1 == b[j++];
    }

    short rh() {
        int x = b[j++], y = b[j++];
        return (short) (a ? x & 0xff | y << 8 : x << 8 | y & 0xff);
    }

    int ri() {
        int x = rh(), y = rh();
        return a ? x & 0xffff | y << 16 : x << 16 | y & 0xffff;
    }

    long rj() {
        int x = ri(), y = ri();
        return a ? x & 0xffffffffL | (long) y << 32 : (long) x << 32 | y & 0xffffffffL;
    }

    float re() {
        return Float.intBitsToFloat(ri());
    }

    double rf() {
        return Double.longBitsToDouble(rj());
    }

    UUID rg() {
        boolean oa = a;
        a = false;
        UUID g = new UUID(rj(), rj());
        a = oa;
        return g;
    }

    char rc() {
        return (char) (b[j++] & 0xff);
    }

    K.KSymbol rs() throws UnsupportedEncodingException {
        int n = j;
        for (; b[n] != 0; )
            ++n;
        String s = new String(b, j, n - j, encoding);
        j = n;
        ++j;
        return new K.KSymbol(s);
    }

    K.UnaryPrimitive rup() {
        return new K.UnaryPrimitive(b[j++]);
    }

    K.BinaryPrimitive rbp() {
        return new K.BinaryPrimitive(b[j++]);
    }

    K.TernaryOperator rternary() {
        return new K.TernaryOperator(b[j++]);
    }

    K.Function rfn() throws UnsupportedEncodingException {
        K.KSymbol s = rs();
        return new K.Function((K.KCharacterVector) r());
    }

    K.Feach rfeach() throws UnsupportedEncodingException {
        return new K.Feach(r());
    }

    K.Fover rfover() throws UnsupportedEncodingException {
        return new K.Fover(r());
    }

    K.Fscan rfscan() throws UnsupportedEncodingException {
        return new K.Fscan(r());
    }

    K.FComposition rcomposition() throws UnsupportedEncodingException {
        int n = ri();
        K.KBase[] objs = new K.KBase[n];
        for (int i = 0; i < n; i++)
            objs[i] = r();

        return new K.FComposition(objs);
    }

    K.FPrior rfPrior() throws UnsupportedEncodingException {
        return new K.FPrior(r());
    }

    K.FEachRight rfEachRight() throws UnsupportedEncodingException {
        return new K.FEachRight(r());
    }

    K.FEachLeft rfEachLeft() throws UnsupportedEncodingException {
        return new K.FEachLeft(r());
    }

    K.Projection rproj() throws UnsupportedEncodingException {
        int n = ri();
        K.KBase[] array = new K.KBase[n];
        for (int i = 0; i < n; i++)
            array[i] = r();
        return new K.Projection(array);
    }

    K.Minute ru() {
        return new K.Minute(ri());
    }

    K.Month rm() {
        return new K.Month(ri());
    }

    K.Second rv() {
        return new K.Second(ri());
    }

    K.KTimespan rn() {
        return new K.KTimespan(rj());
    }

    K.KTime rt() {
        return new K.KTime(ri());
    }

    K.KDate rd() {
        return new K.KDate(ri());
    }

    K.KDatetime rz() {
        return new K.KDatetime(rf());
    }

    K.KTimestamp rp() {
        return new K.KTimestamp(rj());
    }

    K.KBase r() throws UnsupportedEncodingException {
        int i = 0, n, t = b[j++];
        if (t < 0)
            switch (t) {
                case -1:
                    return new K.KBoolean(rb());
                case -2:
                    return new K.KGuid(rg());
                case -4:
                    return new K.KByte(b[j++]);
                case -5:
                    return new K.KShort(rh());
                case -6:
                    return new K.KInteger(ri());
                case -7:
                    return new K.KLong(rj());
                case -8:
                    return new K.KFloat(re());
                case -9:
                    return new K.KDouble(rf());
                case -10:
                    return new K.KCharacter(rc());
                case -11:
                    return rs();
                case -12:
                    return rp();
                case -13:
                    return rm();
                case -14:
                    return rd();
                case -15:
                    return rz();
                case -16:
                    return rn();
                case -17:
                    return ru();
                case -18:
                    return rv();
                case -19:
                    return rt();
            }

        if (t == 100)
            return rfn(); // fn - lambda
        if (t == 101)
            return rup();  // unary primitive
        if (t == 102)
            return rbp();  // binary primitive
        if (t == 103)
            return rternary();
        if (t == 104)
            return rproj(); // fn projection
        if (t == 105)
            return rcomposition();

        if (t == 106)
            return rfeach(); // f'
        if (t == 107)
            return rfover(); // f/
        if (t == 108)
            return rfscan(); //f\
        if (t == 109)
            return rfPrior(); // f':
        if (t == 110)
            return rfEachRight(); // f/:
        if (t == 111)
            return rfEachLeft(); // f\:
        if (t == 112) {
            // dynamic load
            j++;
            return null;
        }
        if (t == 127) {
            K.Dict d = new K.Dict(r(), r());
            d.setAttr((byte) 1);
            return d;
        }
        if (t > 99) {
            j++;
            return null;
        }
        if (t == 99)
            return new K.Dict(r(), r());
        byte attr = b[j++];
        if (t == 98) {
            K.Dict d = (K.Dict)r();
            if (d.x.type == 11 && d.y.type == 0) {
                return new K.Flip(d);
            } else {
                return d;
            }
        }
        n = ri();
        switch (t) {
            case 0: {
                K.KBase[] array = new K.KBase[n];
                for (; i < n; i++)
                    array[i] = r();
                K.KList L = new K.KList(array);
                L.setAttr(attr);
                return L;
            }
            case 1: {
                boolean[] array = new boolean[n];
                for (; i < n; i++)
                    array[i] = rb();
                K.KBooleanVector B = new K.KBooleanVector(array);
                B.setAttr(attr);
                return B;
            }
            case 2: {
                UUID[] array = new UUID[n];
                for (; i < n; i++)
                    array[i] = rg();
                K.KGuidVector B = new K.KGuidVector(array);
                B.setAttr(attr);
                return B;
            }
            case 4: {
                byte[] array = new byte[n];
                for (; i < n; i++)
                    array[i] = b[j++];
                K.KByteVector G = new K.KByteVector(array);
                G.setAttr(attr);
                return G;
            }
            case 5: {
                short[] array = new short[n];
                for (; i < n; i++)
                    array[i] = rh();
                K.KShortVector H = new K.KShortVector(array);
                H.setAttr(attr);
                return H;
            }
            case 6: {
                int[] array = new int[n];
                for (; i < n; i++)
                    array[i] = ri();
                K.KIntVector I = new K.KIntVector(array);
                I.setAttr(attr);
                return I;
            }
            case 7: {
                long[] array = new long[n];
                for (; i < n; i++)
                    array[i] = rj();
                K.KLongVector J = new K.KLongVector(array);
                J.setAttr(attr);
                return J;
            }
            case 8: {
                float[] array = new float[n];
                for (; i < n; i++)
                    array[i] = re();
                K.KFloatVector E = new K.KFloatVector(array);
                E.setAttr(attr);
                return E;
            }
            case 9: {
                double[] array = new double[n];
                for (; i < n; i++)
                    array[i] = rf();
                K.KDoubleVector F = new K.KDoubleVector(array);
                F.setAttr(attr);
                return F;
            }
            case 10: {
                String value = new String(b, j, n, encoding);
                K.KCharacterVector C = new K.KCharacterVector(value);
                C.setAttr(attr);
                j += n;
                return C;
            }
            case 11: {
                String[] array = new String[n];
                for (; i < n; i++)
                    array[i] = rs().s;
                K.KSymbolVector S = new K.KSymbolVector(array);
                S.setAttr(attr);
                return S;
            }
            case 12: {
                long[] array = new long[n];
                for (; i < n; i++) {
                    array[i] = rj();
                }
                K.KTimestampVector P = new K.KTimestampVector(array);
                P.setAttr(attr);
                return P;
            }
            case 13: {
                int[] array = new int[n];
                for (; i < n; i++)
                    array[i] = ri();
                K.KMonthVector M = new K.KMonthVector(array);
                M.setAttr(attr);
                return M;
            }
            case 14: {
                int[] array = new int[n];
                for (; i < n; i++)
                    array[i] = ri();
                K.KDateVector D = new K.KDateVector(array);
                D.setAttr(attr);
                return D;
            }
            case 15: {
                double[] array = new double[n];
                for (; i < n; i++)
                    array[i] = rf();
                K.KDatetimeVector Z = new K.KDatetimeVector(array);
                Z.setAttr(attr);
                return Z;
            }
            case 16: {
                long[] array = new long[n];
                for (; i < n; i++) {
                    array[i] = rj();
                }
                K.KTimespanVector N = new K.KTimespanVector(array);
                N.setAttr(attr);
                return N;
            }
            case 17: {
                int[] array = new int[n];
                for (; i < n; i++)
                    array[i] = ri();
                K.KMinuteVector U = new K.KMinuteVector(array);
                U.setAttr(attr);
                return U;
            }
            case 18: {
                int[] array = new int[n];
                for (; i < n; i++)
                    array[i] = ri();
                K.KSecondVector V = new K.KSecondVector(array);
                V.setAttr(attr);
                return V;
            }
            case 19: {
                int[] array = new int[n];
                for (; i < n; i++)
                    array[i] = ri();
                K.KTimeVector T = new K.KTimeVector(array);
                T.setAttr(attr);
                return T;
            }
        }
        return null;
    }

    void w(int i, K.KBase x) throws IOException {
        java.io.ByteArrayOutputStream baosBody = new ByteArrayOutputStream();
        java.io.DataOutputStream dosBody = new DataOutputStream(baosBody);
        x.serialise(dosBody);

        java.io.ByteArrayOutputStream baosHeader = new ByteArrayOutputStream();
        java.io.DataOutputStream dosHeader = new DataOutputStream(baosHeader);
        dosHeader.writeByte(0);
        dosHeader.writeByte(i);
        dosHeader.writeByte(0);
        dosHeader.writeByte(0);
        int msgSize = 8 + dosBody.size();
        K.write(dosHeader, msgSize);
        byte[] b = baosHeader.toByteArray();
        outputStream.write(b);
        b = baosBody.toByteArray();
        outputStream.write(b);
    }

    public static class K4Exception extends Exception {
        K4Exception(String s) {
            super(s);
        }
    }


    private K.KBase k(ProgressCallback progress) throws K4Exception, IOException {
        boolean firstMessage = true;
        boolean responseMsg = false;
        boolean c = false;
        while (!responseMsg) { // throw away incoming aync, and error out on incoming sync
            if (firstMessage) {
                firstMessage = false;
            } else {
                inputStream.readFully(b = new byte[8]);
            }
            a = b[0] == 1;
            c = b[2] == 1;
            byte msgType = b[1];
            if (msgType == 1) {
                close();
                throw new IOException("Cannot process sync msg from remote");
            }
            responseMsg = msgType == 2;
            j = 4;

            final int msgLength = ri() - 8;

            if (progress!=null) {
                progress.setCompressed(c);
                progress.setMsgLength(msgLength);
            }

            b = new byte[msgLength];
            int total = 0;
            int packetSize = 1 + msgLength / 100;
            if (packetSize < rxBufferSize)
                packetSize = rxBufferSize;

            while (total < msgLength) {
                int remainder = msgLength - total;
                if (remainder < packetSize)
                    packetSize = remainder;

                int count = inputStream.read(b, total, packetSize);
                if (count < 0) throw new EOFException("Connection is broken");
                total += count;
                if (progress != null) progress.setCurrentProgress(total);
            }
        }
        if (c)
            u();
        else
            j = 0;

        if (b[0] == -128) {
            j = 1;
            throw new K4Exception(rs().toString());
        }
        return r();
    }

    private void u() {
        int n = 0, r = 0, f = 0, s = 8, p = s;
        short i = 0;
        j = 0;
        byte[] dst = new byte[ri()];
        int d = j;
        int[] aa = new int[256];
        while (s < dst.length) {
            if (i == 0) {
                f = 0xff & (int) b[d++];
                i = 1;
            }
            if ((f & i) != 0) {
                r = aa[0xff & (int) b[d++]];
                dst[s++] = dst[r++];
                dst[s++] = dst[r++];
                n = 0xff & (int) b[d++];
                for (int m = 0; m < n; m++) {
                    dst[s + m] = dst[r + m];
                }
            } else {
                dst[s++] = b[d++];
            }
            while (p < s - 1) {
                aa[(0xff & (int) dst[p]) ^ (0xff & (int) dst[p + 1])] = p++;
            }
            if ((f & i) != 0) {
                p = s += n;
            }
            i *= 2;
            if (i == 256) {
                i = 0;
            }
        }
        b = dst;
        j = 8;
    }

    public synchronized K.KBase k(K.KBase x, ProgressCallback progress) throws K4Exception, IOException {
        try {

            if (isClosed()) connect(true);
            try {
                w(1, x);
                inputStream.readFully(b = new byte[8]);
            } catch (IOException e) {
                close();
                // may be the socket was closed on the server side?
                connect(true);
                w(1, x);
                inputStream.readFully(b = new byte[8]);
            }

            return k(progress);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public K.KBase k(K.KBase x) throws K4Exception, IOException {
        return k(x, null);
    }
}
