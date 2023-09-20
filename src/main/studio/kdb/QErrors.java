package studio.kdb;

import java.util.HashMap;
import java.util.Map;

public class QErrors {
    private static Map map = new HashMap();

    public static String lookup(String s) {
        return (String) map.get(s);
    }
    

    static {
        map.put("access","attempt to read files above directory or run system commands in -u 1 mode, or failed usr/pwd");
        map.put("assign","attempt to assign a value to a reserved word");
        map.put("badmsg","failure in IPC validation");
        map.put("badtail","incompelte transaction at end of log file, get good (count;length) with -11!(-2;`:file)");
        map.put("cast","value not in enumeration");
        //map.put("close handle.*","handle was closed by the remote while a msg was expected"); //this would require regex matching
        //map.put("conn","too many incoming connections (1022 max)"); //this is a server-side error, shouldn't happen during a client query
        map.put("domain","out of domain");
        map.put("from","badly formed select query");
        //map.put("glim","`g# limit, kdb+ currently limited to 99 concurrent `g#'s "); //obsolete
        map.put("insert","trying to insert a record with an existing key into a keyed table");
        map.put("length","incompatible lengths, e.g. 1 2 3 4 + 1 2 3");
        map.put("limit","trying to generate a list longer than 2^40-1, or serialized object is > 1TB, or too many constants in a function");
        map.put("loop","dependency loop");
        map.put("mismatch","columns that can't be aligned for R,R or K,K ");
        map.put("Mlim","more than 65530 nested columns in splayed tables");
        map.put("noamend","can't change global state inside an amend");
        map.put("nosocket","trying use sockets in a thread other than the main thread");
        map.put("noupdate","trying to update state while blocked with -b cmd line arg or reval, or in a thread other than the main thread");
        map.put("nyi","not yet implemented - suggests the\noperation you are tying to do makes sense\nbut it has not yet been implemented");
        map.put("os","operating system error");
        map.put("par","unsupported operation on a partitioned table");
        map.put("parse","invalid syntax, bad IPC header or bad binary data in file");
        //map.put("pl","peach can't handle parallel lambda's (2.3 only)"); //obsolete
        map.put("Q7","nyi op on file nested array");
        map.put("rank","invalid rank");
        map.put("splay","nyi op on splayed table");
        map.put("stack","ran out of stack space");
        map.put("stop","user interrupt (ctrl-c) or time limit (-T)");
        map.put("stype","invalid type used to signal");
        map.put("type","wrong type, e.g `a+1, or trying to serialize a nested object which has > 2 billion elements");
        map.put("value","no value");
        map.put("vd1","attempted multithread update");
        //map.put("wsfull","malloc failed. ran out of swap (or addressability on 32bit). or hit -w limit."); //this is an untrappable error

        map.put("branch","a branch(if;do;while;$[.;.;.]) too many byte codes away"); //the exact number is useless info to an end user
        map.put("char","invalid character");
        //map.put("constants","too many constants (max 96)"); //obsolete in 3.6
        map.put("globals","too many global variables (max 110)");
        map.put("locals","too many local variables (max 110)");
        map.put("params","too many parameters (max 8)");
        map.put("u-fail","cannot apply `u# to data (not unique values), e.g `u#1 1");
        map.put("s-fail","cannot apply `s# to data (not ascending values) , e.g `s#2 1");
        //map.put("elim","more than 57 distinct enumerations"); //obsolete in 3.6
    }
}
