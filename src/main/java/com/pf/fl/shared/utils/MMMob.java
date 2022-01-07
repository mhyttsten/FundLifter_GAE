package com.pf.fl.shared.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MMMob {
    private static final Logger log = Logger.getLogger(MM.class.getName());
    private static final String TAG = "MM";

    //------------------------------------------------------------------------
    public static void main(String[] args) {
        try {
//            String yymmdd = "140103";
//            System.out.println(tgif_getLastFridayTodayIncl(yymmdd));
//            System.out.println(MM.tgif_getNextWeekday("140704", Calendar.MONDAY));

            String[] s1 = { "1", "2", "3", "4" };
            String[] s2 = { "1", "2", "3", "4", "5" };
            String[] s3 = { "1", "2", "3" };
            String[] s4 = { "1", "2" };
            String[] s5 = { "1" };

        } catch (Exception exc) {
            System.out.println("Exception: " + exc);
            exc.printStackTrace();
        }
    }

    //------------------------------------------------------------------------
    public static void printArray(Object[] a) {
        for (int i=0; i < a.length; i++) {
            System.out.print(a[i]);
            if (i+1 < a.length) System.out.print(", ");
        }
        System.out.println("");
    }
    public static void reverse(Object[] a) {
        int istart = 0;
        int iend = a.length-1;
        while (iend - istart > 0) {
            Object tmp = a[iend];
            a[iend] = a[istart];
            a[istart] = tmp;
            iend--;
            istart++;
        }
    }

    //------------------------------------------------------------------------
    public static void hmValuesToList(HashMap hm, List v) {
        Collection c = hm.values();
        Iterator i = c.iterator();
        while (i.hasNext()) {
            v.add(i.next());
        }
    }

    /**
     *
     */
    public static String tgif_getDayOfWeekStr(int dayOfWeek) throws Exception {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            case Calendar.SUNDAY:
                return "Sunday";
        }
        throw new Exception("Unknown day of week: " + dayOfWeek);
    }


    /**
     *
     */
    public static int tgif_getDayOfWeek(String dateYYMMDD) throws Exception {
        java.util.Date d = getDateFrom_YYMMDD(null, dateYYMMDD);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        int dow = gc.get(Calendar.DAY_OF_WEEK);
        return dow;
    }

    /**
     *
     */
    public static boolean tgif_isThursday(String dateYYMMDD) throws Exception {
        java.util.Date d = getDateFrom_YYMMDD(null, dateYYMMDD);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        int dow = gc.get(Calendar.DAY_OF_WEEK);
        if (dow == Calendar.THURSDAY) {
            return true;
        }
        return false;
    }

    /**
     *
     */
    public static boolean tgif_isFriday(String dateYYMMDD) {
        java.util.Date d = getDateFrom_YYMMDD(null, dateYYMMDD);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        int dow = gc.get(Calendar.DAY_OF_WEEK);
        if (dow == Calendar.FRIDAY) {
            return true;
        }
        return false;
    }

    /**
     *
     */
    public static String tgif_getLastFridayTodayIncl(String dateYYMMDD) throws Exception {
        java.util.Date d = getDateFrom_YYMMDD(null, dateYYMMDD);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);

        while (true) {
            int dow = gc.get(Calendar.DAY_OF_WEEK);
            if (dow == Calendar.FRIDAY) {
                d = gc.getTime();
                SimpleDateFormat f = new SimpleDateFormat("yyMMdd");
                return f.format(d);
            }
            gc.add(Calendar.DAY_OF_MONTH, -1);
        }
    }

    /**
     *
     */
    public static String tgif_getNextWeekday(String dateYYMMDD, int weekday) throws Exception {
        java.util.Date d = getDateFrom_YYMMDD(null, dateYYMMDD);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        int dayOfWeekNow = -1;
        do {
            gc.add(Calendar.DAY_OF_MONTH, 1);
            dayOfWeekNow = gc.get(Calendar.DAY_OF_WEEK);
        } while (dayOfWeekNow != weekday);

        java.util.Date date = gc.getTime();
        SimpleDateFormat f = new SimpleDateFormat("yyMMdd");
        return f.format(date);
    }

    /**
     *
     */
    public static String tgif_getLastFridayTodayExcl(String dateYYMMDD) {
        java.util.Date d = getDateFrom_YYMMDD(null, dateYYMMDD);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);

        gc.add(Calendar.DAY_OF_MONTH, -1);
        while (true) {
            int dow = gc.get(Calendar.DAY_OF_WEEK);
            if (dow == Calendar.FRIDAY) {
                d = gc.getTime();
                SimpleDateFormat f = new SimpleDateFormat("yyMMdd");
                return f.format(d);
            }
            gc.add(Calendar.DAY_OF_MONTH, -1);
        }
    }


    /**
     *
     */
    public static String tgif_getNextFridayTodayExcl(String dateYYMMDD) throws Exception {
        java.util.Date d = getDateFrom_YYMMDD(null, dateYYMMDD);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);

        gc.add(Calendar.DAY_OF_MONTH, 1);
        while (true) {
            int dow = gc.get(Calendar.DAY_OF_WEEK);
            if (dow == Calendar.FRIDAY) {
                d = gc.getTime();
                SimpleDateFormat f = new SimpleDateFormat("yyMMdd");
                return f.format(d);
            }
            gc.add(Calendar.DAY_OF_MONTH, 1);
        }
    }


    /**
     *
     */
    public static final int V_TD_OK = 0;
    public static final int V_TD_NULL = 1;
    public static final int V_TD_DASH = 2;
    public static final int V_TD_NAN = 3;

    public static int testDouble(String period, String value, IndentWriter iw) {
        if (value == null || value.trim().length() == 0) {
            return V_TD_NULL;
        }
        if (value.trim().equals("-")) {
            return V_TD_DASH;
        }
        try {
            double d = Double.parseDouble(value);
        } catch (NumberFormatException exc) {
            iw.println(period + ", NAN: " + value);
            return V_TD_NAN;
        }

        return V_TD_OK;
    }

    public static void timeStart() {
        mTimeStart = System.currentTimeMillis();
    }

    public static long timeEnd() {
        long timeEnd = System.currentTimeMillis();
        return timeEnd - mTimeStart;
    }

    private static long mTimeStart = 0;


    //------------------------------------------------------------------------
    public static String getString(String str, int size) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= size) {
            return str;
        }
        return str.substring(0, size);
    }

    //------------------------------------------------------------------------
    public static byte[] gzipWriteString(String s, String charset) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gzout = new GZIPOutputStream(bout);
        gzout.write(s.getBytes(charset));
        gzout.flush();
        gzout.close();
        return bout.toByteArray();
    }

    //------------------------------------------------------------------------
    public static String gzipReadString(byte[] data, String charset) throws IOException {
        if (data == null || data.length == 0) {
            return null;
        }

        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        GZIPInputStream gzin = new GZIPInputStream(bin);
        byte[] result = readData(gzin);
        String s = new String(result, charset);
        return s;
    }

    public static byte[] readData(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int readBytes = -1;
        byte[] data = new byte[500000];
        do

        {
            readBytes = is.read(data);
            if (readBytes != -1) {
                bout.write(data, 0, readBytes);
            }
        }

        while(readBytes!=-1);

        byte[] result = bout.toByteArray();
        return result;
    }

    //------------------------------------------------------------------------
    public static byte[] serialize_WriteGZIP(Serializable s) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gzout = new GZIPOutputStream(bout);
        ObjectOutputStream oos = new ObjectOutputStream(gzout);
        oos.writeObject(s);
        oos.flush();
        oos.close();
        return bout.toByteArray();
    }

    //------------------------------------------------------------------------
    public static Object serialize_ReadGZIP(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return null;
        }

        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        GZIPInputStream gzin = new GZIPInputStream(bin);
        ObjectInputStream ois = new ObjectInputStream(gzin);
        Object o = null;
        try {
            o = ois.readObject();
        } catch(Exception exc) {
            throw new IOException(exc);
        }
        return o;
    }


    //-------------------------------------------------------------------------------------
    public static Double stringToDoubleOrNull(String arg, String nullValue) {
        if (arg == null) {
            return null;
        }
        if (arg.equals(nullValue)) {
            return null;
        }
        return Double.parseDouble(arg);
    }

    //-------------------------------------------------------------------------------------
    public static String getClassName(String className) {
        int io = className.lastIndexOf('.');
        if (io == -1) {
            return className;
        }
        return className.substring(io+1);
    }

    //-------------------------------------------------------------------------------------
    public static void throwIOException(String message, Throwable exc) throws IOException {
        if (message == null) {
            message = "";
        }
        String stackTrace = MM.getStackTraceString(exc);
        throw new IOException(message + ". " + exc.toString() + "\nStack trace: " + stackTrace);
    }

    //------------------------------------------------------------------------
    public static void iwPrintln(IndentWriter iw, String str) {
        if (iw == null) {
            return;
        }
        iw.println(str);
    }
    public static void iwPush(IndentWriter iw) {
        if (iw == null) {
            return;
        }
        iw.push();
    }
    public static void iwPop(IndentWriter iw) {
        if (iw == null) {
            return;
        }
        iw.pop();
    }

    //-------------------------------------------------------------------------------------
    public static boolean equals(Object a1, Object a2, String fieldName, IndentWriter iw) {
        if(fieldName == null) {
            fieldName = "";
        }
        if(iw == null) {
            iw = new IndentWriter();
        }

        if(a1 == null && a2 == null) {
            return true;
        }
        if(a1 == null && a2 != null) {
            iw.println(fieldName + ", arg1 was null but arg2 was non-null");
            return false;
        }
        if(a1 != null && a2 == null) {
            iw.println(fieldName + ", arg1 was non-null but arg2 was null");
            return false;
        }
        boolean result = a1.equals(a2);
        if(!result) {
            iw.println(fieldName + ", arg1 and arg2 were non-null but they were not equals");
            iw.push();
            iw.println("arg1: " + a1.toString());
            iw.println("arg2: " + a2.toString());
            iw.pop();
        }
        return result;
    }


    //-------------------------------------------------------------------------------------
    public static String getGCString(GregorianCalendar gc) throws Exception {

        String year = String.valueOf(gc.get(Calendar.YEAR));
        int monthInt = gc.get(Calendar.MONTH);
        monthInt++;
        String month = String.valueOf(monthInt);
        if(month.length() == 1) {
            month = "0" + month;
        }
        String dayOfMonth = String.valueOf(gc.get(Calendar.DAY_OF_MONTH));
        if(dayOfMonth.length() == 1) {
            dayOfMonth = "0" + dayOfMonth;
        }

        String dayOfW_eek = "unknown";
        switch(gc.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                dayOfW_eek = "Monday";
                break;
            case Calendar.TUESDAY:
                dayOfW_eek = "Tuesday";
                break;
            case Calendar.WEDNESDAY:
                dayOfW_eek = "Wednesday";
                break;
            case Calendar.THURSDAY:
                dayOfW_eek = "Thursday";
                break;
            case Calendar.FRIDAY:
                dayOfW_eek = "Friday";
                break;
            case Calendar.SATURDAY:
                dayOfW_eek = "Saturday";
                break;
            case Calendar.SUNDAY:
                dayOfW_eek = "Sunday";
                break;
        }

        return year + "-" + month + "-" + dayOfMonth + ": " + dayOfW_eek;
    }

    //-------------------------------------------------------------------------------------
    public static int fl_compareToStrings(String o1, String o2) {
        if(o1 == null && o2 == null) {
            return 0;
        } else if(o1 == null && o2 != null) {
            return -1;
        } else if(o1 != null && o2 == null) {
            return 1;
        }
        return o1.compareTo(o2);
    }

    //-------------------------------------------------------------------------------------
    public static int fl_compareTo(GregorianCalendar a1, GregorianCalendar a2) throws Exception {
        int result = a1.getTime().compareTo(a2.getTime());

        System.out.println("MM.fl_compareTo:");
        System.out.println("   a1: " +  MM.getGCString(a1));
        System.out.println("   a2: " +  MM.getGCString(a2));
        System.out.println("   result: " +  result);
        return result;
    }

    //-------------------------------------------------------------------------------------
    public static boolean fl_isEqualYYMMDD(GregorianCalendar a1, GregorianCalendar a2) throws Exception {
        return
                a1.get(Calendar.YEAR) == a2.get(Calendar.YEAR) &&
                        a1.get(Calendar.MONTH) == a2.get(Calendar.MONTH) &&
                        a1.get(Calendar.DAY_OF_MONTH) == a2.get(Calendar.DAY_OF_MONTH);
    }

    //-------------------------------------------------------------------------------------
    public static ArrayList<GregorianCalendar> fl_getTradingDayGapNI(
            GregorianCalendar gcLast,
            GregorianCalendar gcNext) throws Exception {
        if(gcLast.getTime().compareTo(gcNext.getTime()) >=0 ) {
            throw new Exception("Last is not smaller than next. Last: " + gcLast.getTime().toString() + ", next: " + gcNext.getTime().toString());
        }
        if(fl_isTradingDay(gcLast)) {
            throw new Exception("GCLast is not a trading day: " + gcLast.getTime().toString());
        }
        if(fl_isTradingDay(gcNext)) {
            throw new Exception("GCNextt is not a trading day: " + gcNext.getTime().toString());
        }

        ArrayList<GregorianCalendar> result = new ArrayList<GregorianCalendar>();
        GregorianCalendar gcWork = gcLast;
        while(true) {
            gcWork.add(Calendar.DAY_OF_MONTH, 1);
            if(fl_isEqualYYMMDD(gcWork, gcNext)) {
                break;
            }
            if(fl_isTradingDay(gcWork)) {
                result.add(gcWork);
            }
        }
        return result;
    }

    //-------------------------------------------------------------------------------------
    public static GregorianCalendar fl_getNextTradingDay(GregorianCalendar arg) throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(arg.getTime());
        while(true) {
            gc.add(Calendar.DAY_OF_MONTH, 1);
            if(MM.fl_isTradingDay(gc)) {
                break;
            }
        }
        return gc;
    }

    //-------------------------------------------------------------------------------------
    public static GregorianCalendar fl_addDaysToNext(GregorianCalendar arg, int dayFromCalendarClass) throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(arg.getTime());
        while(true) {
            gc.add(Calendar.DAY_OF_MONTH, 1);
            if(gc.get(Calendar.DAY_OF_WEEK) == dayFromCalendarClass) {
                break;
            }
        }
        return gc;
    }

    //-------------------------------------------------------------------------------------
    public static boolean fl_isTradingDay(GregorianCalendar gc) throws Exception {
        int dayOfW_eek = gc.get(GregorianCalendar.DAY_OF_WEEK);
        //System.out.println("   " + gnow.getTime().toString());

        switch(dayOfW_eek) {
            case Calendar.SATURDAY:
            case Calendar.SUNDAY:
                return false;
        }
        return true;
    }
    public static boolean fl_isTradingDay(String yymmdd) throws Exception {
        String yyStr = yymmdd.substring(0,2);
        String mmStr = yymmdd.substring(2, 4);
        String ddStr = yymmdd.substring(4);
        int yy = Integer.parseInt(yyStr) + 2000;
        int mm = Integer.parseInt(mmStr) - 1;
        int dd = Integer.parseInt(ddStr);

        GregorianCalendar gnow = new GregorianCalendar();
        gnow.set(GregorianCalendar.YEAR, yy);
        gnow.set(GregorianCalendar.MONTH, mm);
        gnow.set(GregorianCalendar.DAY_OF_MONTH, dd);
        gnow.set(GregorianCalendar.SECOND, 1);
        gnow.set(GregorianCalendar.HOUR_OF_DAY, 0);
        gnow.set(GregorianCalendar.MINUTE, 0);

        return fl_isTradingDay(gnow);
    }

    //-------------------------------------------------------------------------------------
    public static String getDayFromGC(int gcDay) throws Exception {
        switch (gcDay) {
            case GregorianCalendar.MONDAY:
                return "Monday";
            case GregorianCalendar.TUESDAY:
                return "Tuesday";
            case GregorianCalendar.WEDNESDAY:
                return "Wednesday";
            case GregorianCalendar.THURSDAY:
                return "Thursday";
            case GregorianCalendar.FRIDAY:
                return "Friday";
            case GregorianCalendar.SATURDAY:
                return "Saturday";
            case GregorianCalendar.SUNDAY:
                return "Sunday";
            default:
                return "<ERROR>";
        }
    }


    //------------------------------------------------------------------------
    public static String getRegExp(
            IndentWriter iw,
            String content,
            String findTagLoc,
            String findAfter,
            String findTo,
            boolean trim) {

        if (iw != null) {
            iw.println("MM.getRegExp, findTagLoc: " + findTagLoc + ", findAfter: " + findAfter + ", findTo: " + findTo);
            iw.push();
            iw.println("Content length: " + content.length());
        }

        Pattern p = Pattern.compile(findTagLoc);
        Matcher m = p.matcher(content);
        boolean found = m.find();
        if(!found) {
            if (iw != null) iw.println("findTagLoc returned null");
            return null;
        }
        content = content.substring(m.end());

        if (iw != null) {
            String dcontent = content;
            if (dcontent.length() > 100) {
                dcontent = dcontent.substring(0, 100) + "...";
            }
            iw.println("Content is: " + dcontent);
        }

        p = Pattern.compile(findAfter);
        m = p.matcher(content);
        found = m.find();
        if(!found) {
            if (iw != null) iw.println("findAfter returned null");
            return null;
        }
        content = content.substring(m.end());

        if (iw != null) {
            String dcontent = content;
            if (dcontent.length() > 100) {
                dcontent = dcontent.substring(0, 100) + "...";
            }
            iw.println("Content is: " + dcontent);
        }

        p = Pattern.compile(findTo);
        m = p.matcher(content);
        found = m.find();
        if(!found) {
            if (iw != null) iw.println("findAfter returned null");
            return null;
        }
        content = content.substring(0, m.end()-findTo.length());

        if(trim) {
            content = content.trim();
        }
        if (iw != null) iw.println("Successfully extracted content: " + content);
        return content;
    }

    //-------------------------------------------------------------------------------------
    public static String replaceArgTo(String arg, String search, String replace) throws Exception {
        if(arg == null || search == null || replace == null) {
            return null;
        }

        int io = arg.indexOf(search);
        if(io == -1) {
            return arg;
        }

        String newSearch = arg.substring(0, io);
        if(io >= arg.length() + search.length() - replace.length()) {
            return newSearch;
        }

        newSearch = newSearch + replace + arg.substring(io+search.length());
        String result = replaceArgTo(newSearch, search, replace);
        return result;
    }

    //-------------------------------------------------------------------------------------
    public static void fileWrite(String fileName, byte[] data) throws Exception {
        FileOutputStream fout = new FileOutputStream(fileName);
        fout.write(data);
        fout.close();
    }

    //-------------------------------------------------------------------------------------
    public static ArrayList<String[]> fileReadToLin_eelements(String fileName, String separator, String encoding) throws Exception {
        return fileReadToLin_eelements(new File(fileName), separator, encoding);
    }
    public static ArrayList<String[]> fileReadToLin_eelements(File file, String separator, String encoding) throws Exception {
        byte[] fileContent = fileReadFrom(file);
        String fileContentStr = null;
        if(encoding != null && encoding.trim().length() > 0) {
            fileContentStr = new String(fileContent,encoding);
        }
        else {
            fileContentStr = new String(fileContent);
        }
        BufferedReader d = new BufferedReader(new StringReader(fileContentStr));
        ArrayList<String[]> al = new ArrayList<String[]>();
        String line = null;
        do {
            line = d.readLine();
            if(line != null) {
                String[] elems = line.split(separator);
                al.add(elems);
            }
        } while(line != null);
        return al;
    }

    //-------------------------------------------------------------------------------------
    public static ArrayList<String> fileReadToLines(File file, String encoding) throws Exception {
        byte[] fileContent = fileReadFrom(file);
        String fileContentStr = null;
        if(encoding != null && encoding.trim().length() > 0) {
            fileContentStr = new String(fileContent,encoding);
        }
        else {
            fileContentStr = new String(fileContent);
        }
        BufferedReader d = new BufferedReader(new StringReader(fileContentStr));
        ArrayList<String> al = new ArrayList<String>();
        String line = null;
        do {
            line = d.readLine();
            if(line != null) {
                line = line.trim();
                if(line.length() > 0) {
                    al.add(line);
                }
            }
        } while(line != null);
        return al;
    }

    //-------------------------------------------------------------------------------------
    public static byte[] getURLContentBA(String url) throws Exception {

        URL yahoo = new URL(url);
        URLConnection yc = yahoo.openConnection();
        yc.setConnectTimeout(50000);
        yc.setReadTimeout(50000);

        InputStream is = yc.getInputStream();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int readBytes = -1;
        byte[] data = new byte[500000];
        do {
            readBytes = is.read(data);
            if(readBytes != -1) {
                bout.write(data, 0, readBytes);
            }
        } while(readBytes != -1);

        byte[] result = bout.toByteArray();
        return result;
    }
    public static String getURLContent(String url, String charSet) throws Exception {

        URL yahoo = new URL(url);
        URLConnection yc = yahoo.openConnection();
        BufferedReader in = null;

        if(charSet == null) {
            in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
        }
        else {
            in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream(), charSet));
        }
        StringBuffer strb = new StringBuffer();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            strb.append(inputLine);
            // System.out.println(inputLine);
        }
        in.close();
        return strb.toString();
    }

    //--------------------------------------------------------------------------
    public static byte[] fileReadFrom(String fileName) throws Exception {
        return fileReadFrom(new File(fileName));
    }
    public static byte[] fileReadFrom(File f) throws Exception {
        FileInputStream fin = new FileInputStream(f);
        return fileReadFrom(fin);
    }
    public static byte[] fileReadFrom(InputStream fin) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int data = -1;
        do {
            data = fin.read();
            if(data != -1) {
                bout.write(data & 0xFF);
            }
        }
        while(data != -1);
        fin.close();
        return bout.toByteArray();
    }

    // ***********************************************************************

    //------------------------------------------------------------------------
    public static String htmlReplaceHTMLCodes(String str) throws Exception {
        String result = str;

        String[][] replacements = {
                { "&Aring;",  "������" },
                { "&#197;",   "������" },
                { "&Auml;",   "������" },
                { "&#196;",   "������" },
                { "&Ouml;",   "������" },
                { "&#214;",   "������" },
                { "&aring;",  "������" },
                { "&#229;",   "������" },
                { "&auml;",   "������" },
                { "&#228;",   "������" },
                { "&ouml;",   "������" },
                { "&#246;",   "������" },
                { "&nbsp;",   " " },
                { "&amp"  ,   "&" }
        };
        boolean found = true;
        while(found) {
            found = false;
            for(int i=0; i < replacements.length; i++) {
                String[] repEntry = replacements[i];
                int io = result.indexOf(repEntry[0]);
                if(io == -1) {
                    continue;
                }
                found = true;
                String newString = result.substring(0, io) + repEntry[1];
                if(result.length() > io+repEntry[0].length()) {
                    newString = newString + result.substring(io+repEntry[0].length());
                    result = newString;
                }
                break;
            }
        }
        return result;
    }

    //------------------------------------------------------------------------
    //    hdata:           HTML block as input
    //    extractStartTag: HTML tag that you want to extract, in the form of e.g "<div"
    //    absoluteTag:     Something unique within the payload where to start
    public static String getHTMLBlock(
            String hdata,
            String extractStartTag,
            String absoluteTag,
            boolean backwards) throws Exception {

        String str = hdata;
        int io = -1;

        io = str.indexOf(absoluteTag);
        if(io == -1)
            return null;

        // Find the block starting with start tag and all to the end
        int hextractIndexLast = -1;
        String hextractIndexLastStr = null;
        int startIndex = 0;
        int hextractIndex = -1;
        do {
            hextractIndex = str.indexOf(extractStartTag, startIndex);
            // If backwards, we take the last match before passing io
            if(backwards) {
                if(hextractIndex == -1 ||
                        hextractIndex >= io) {
                    break;
                }
            }
            hextractIndexLast = hextractIndex;
            hextractIndexLastStr = str.substring(hextractIndex);
            startIndex = hextractIndex + extractStartTag.length();

            // If forwards, we take the last match after passing io
            if(!backwards) {
                if(hextractIndex == -1 ||
                        hextractIndex >= io) {
                    break;
                }
            }
        } while(true);

        // If we could not find before or after the absoluteTag
        if(hextractIndexLastStr == null)
            return hextractIndexLastStr;

        // Take away everything after the close tag (manage recursion)
        String extractEndTag = extractStartTag.substring(0, 1) + "/" + extractStartTag.substring(1);
        String result = getHtmlBlock(hextractIndexLastStr, extractStartTag, extractEndTag);
        return result;
    }
    private static String getHtmlBlock(String hdata, String tagStart, String tagEnd) throws Exception {
        if(!hdata.startsWith(tagStart))
            throw new Exception("Expected to start with tag: " + tagStart + "\ndata: " + hdata);

        int recursionLvl = 0;
        int io = tagStart.length();
        int endTagIndex = hdata.indexOf(tagEnd);
        // Find the d_eepest start nesting tag
        while(true) {
            int newIO = hdata.indexOf(tagStart, io);
            if(newIO == -1 || endTagIndex < newIO)
                break;
            recursionLvl++;
            io = newIO + tagStart.length();
        }
        // Find the shallowest end tag
        while(true) {
            int newIO = hdata.indexOf(tagEnd, io);
            if(newIO == -1)
                break;
            io = newIO + tagEnd.length();
            // Found last
            if(recursionLvl == 0) {
                return hdata.substring(0, io+tagEnd.length());
            }
            else {
                recursionLvl--;
            }
        }
        throw new Exception("Error, could not find endTag: " + tagEnd + ", from position:\n" + hdata.substring(io));
    }


    //------------------------------------------------------------------------
    public static String convertTo7bitAscii(String fileNameFundDetails) throws Exception {
        byte[] ba = fileNameFundDetails.getBytes();
        for(int i=0; i < ba.length; i++) {
            int a = (((int)ba[i]) & 0xFF);
            if(!(
                    (a >= 0x30 && a <= 0x39) ||
                            (a >= 0x41 && a <= 0x5A) ||
                            (a >= 0x61 && a <= 0x7A))) {
                ba[i] = (byte)(0x5F & 0xFF);
            }
        }
        return new String(ba);
    }

    //------------------------------------------------------------------------
    public static String newString(byte[] arg, String enc) throws Exception {
        if(enc != null && enc.trim().length() > 0) {
            return new String(arg, enc);
        }
        return new String(arg);
    }

    //------------------------------------------------------------------------
    public static ArrayList<String> getTagValues(String content, String tag) throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        OTuple2G<String, String> ot = new OTuple2G<String, String>(null, content);
        while(assignAndReturnNextTagValue(ot, tag) != null) {
            result.add((String)ot._o1);
        }
        return result;
    }

    //  o1: The result value
    //  o2: The string starting after the closing tag (but also ">" may be the starting character)
    //  Returns null if it cannot find any more values
    public static String assignAndReturnNextTagValue(
            OTuple2G<String, String> t,
            String tagStart) {

        String TAG_START = tagStart;
        String TAG_START_END = ">";
        String TAG_END_START = tagStart.substring(0, 1) + "/" + tagStart.substring(1);
        int io = -1;

        t._o1 = null;

        // <tr id="2214>Hello</tr><tr>HERE

        // Part 1 - get Hello out
        String str = (String)t._o2;
        io = str.indexOf(TAG_START);
        if(io == -1) {
            return null;
        }
        str = str.substring(io+TAG_START.length());
        io = str.indexOf(TAG_START_END);
        str = str.substring(io+TAG_START_END.length());
        io = str.indexOf(TAG_END_START);
        String o1 = str.substring(0, io).trim();

        // Part 2 - Move to <tr>HERE
        String o2 = null;
        if(str.length() > io+TAG_END_START.length()) {
            o2 = str.substring(io+TAG_END_START.length());
        }

        t._o1 = o1;
        t._o2 = o2;
        return o1;
    }

    //------------------------------------------------------------------------
    //  o1: The result value
    //  o2: The string starting after the closing tag (but also ">" may be the starting character)
    //  Returns null if it cannot find any more values
    public static String assignAndReturnNextTag(
            OTuple2G<String, String> t,
            String tagStart) {

        String TAG_START = tagStart;
        String TAG_END_START = tagStart.substring(0, 1) + "/" + tagStart.substring(1) + ">";

        int io = -1;
        String data = (String)t._o2;

        io = data.indexOf(TAG_START);
        if(io == -1) {
            t._o1 = null;
            return null;
        }
        data = data.substring(io);
        io = data.indexOf(TAG_END_START);
        if(io == -1) {
            t._o1 = null;
            return null;
        }
        io += TAG_END_START.length();
        String result = data.substring(0, io);
        t._o2 = data.substring(io);
        t._o1 = result;
        return result;
    }

    //------------------------------------------------------------------------
    public static boolean isDigit(char c) {
        if(c == '0' ||
                c == '1' ||
                c == '2' ||
                c == '3' ||
                c == '4' ||
                c == '5' ||
                c == '6' ||
                c == '7' ||
                c == '8' ||
                c == '9') {
            return true;
        }
        return false;
    }

    //------------------------------------------------------------------------
    public static String getStackTraceString(Throwable e) {
        StackTraceElement[] st = e.getStackTrace();
        String str = "";
        if(st == null)
            return "";
        for(int i=0; i < st.length; i++)
            str = str + st[i].toString() + "\n";
        return str;
    }

    //------------------------------------------------------------------------
    public static int getByteMatchesStartIndex(
            byte[] src,
            byte[] find,
            int startIndex) {
        for (int i=startIndex; i < src.length; i++) {
            if (src[i] == find[0]) {
                if (src.length >= i+find.length) {
                    int index1 = i;
                    int index2 = 0;
                    boolean match = true;
                    while (match && index2 < find.length) {
                        if (src[index1] != find[index2]) {
                            match = false;
                        }
                        index1++;
                        index2++;
                    }
                    if (match) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    //------------------------------------------------------------------------
    public static String bytesToHexDumpString(byte[] data, String newline) {
        byte[] ascii_data = new byte[16];
        StringBuffer strb = new StringBuffer();
        if(data == null)
            return "null";
        else
            strb.append("Data length: " + data.length + newline);

        int ascii_data_index = 0;
        int i=0;
        for(i=0; i < data.length; i++) {
            if(i == 0) {
                strb.append("0x" + bytesToString_to2HexDigits(i) + ": ");
            }
            int itmp = data[i] & 0xFF;
            ascii_data[ascii_data_index++] = (byte)itmp;
            String strtmp = bytesToString_to2HexDigits(itmp);
            strb.append(strtmp + " ");
            if(((i+1) % 16) == 0) {
                strb.append("   ");
                for(int j=0; j < 16; j++) {
                    if(ascii_data[j] < 32 || ascii_data[j] > 126)
                        ascii_data[j] = 46;
                }

                ascii_data_index = 0;
                strb.append(new String(ascii_data));
                strb.append(newline);
                strb.append("0x" + bytesToString_to2HexDigits(i+1) + ": ");
            }
        }
        while((i % 16) != 0) {
            strb.append("   ");
            ascii_data[ascii_data_index++] = 46;
            i++;
        }
        strb.append("   ");
        strb.append(new String(ascii_data));
        strb.append(newline);

        return strb.toString();
    }
    private static String bytesToString_to2HexDigits(int value) {
        String result = "";
        if(value < 16)
            result = "0";
        result = result + Integer.toHexString(value);
        return result;
    }

    //------------------------------------------------------------------------
    public static String[] getSeparatedElems(String line, char character) {
        int io = -1;

        ArrayList<String> result = new ArrayList<String>();
        while(line != null) {
            io = line.indexOf(character);
            if(io == -1) {
                result.add(line);
                line = null;
                break;
            }
            String elem = line.substring(0, io);
            result.add(elem);

            if(elem.length()+1 == line.length())
                line = "";
            else
                line = line.substring(io+1);
        }

        String[] resultStr = new String[result.size()];
        for(int i=0; i < result.size(); i++)
            resultStr[i] = result.get(i);
        return resultStr;
    }

    //------------------------------------------------------------------------
    public static String getSecondsAs_HH_MM_SS(int seconds) {
        int hourCount = seconds / 3600;
        seconds = seconds % 3600;
        int minuteCount = seconds / 60;
        seconds = seconds % 60;

        String hours = String.valueOf(hourCount);
        if(hours.length() < 2)
            hours = "0" + hours;
        String minutes = String.valueOf(minuteCount);
        if(minutes.length() < 2)
            minutes = "0" + minutes;
        String secondsStr = String.valueOf(seconds);
        if(secondsStr.length() < 2)
            secondsStr = "0" + secondsStr;
        return hours + ":" + minutes + ":" + secondsStr;
    }

    //------------------------------------------------------------------------
    public static void sleepInMS(long ms) {
        long timeNow = System.currentTimeMillis();
        long timeItShouldBe = timeNow + ms;
        do {
            try {
                Thread.currentThread().sleep(ms);
            }
            catch(Exception exc) {
            }
            timeNow = System.currentTimeMillis();
            if(timeNow < timeItShouldBe) {
                ms = timeItShouldBe - timeNow;
            }
            else {
                ms = 0;
            }
        } while(ms > 0);
    }

    //------------------------------------------------------------------------
    public static String stripCComments(String str) {
        StringBuffer result = new StringBuffer();
        int nesting_level = 0;
        for(int i=0; i < str.length(); i++) {
            char c = str.charAt(i);

            if(c == '/' && str.charAt(i+1) == '*') {
                nesting_level++;
            }
            else if(c == '*') {
                if(i+1 < str.length() && str.charAt(i+1) == '/') {
                    nesting_level--;
                    i+=2;
                    if(i >= str.length())
                        return result.toString();
                    c = str.charAt(i);
                }
            }

            if(nesting_level == 0) {
                result.append(c);
            }
        }
        return result.toString();
    }

/*
    //------------------------------------------------------------------------
    public static String getAs_YYMMDD_HHMMSS(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("yyMMdd_HHmmss");
        return f.format(date);
    }

    //------------------------------------------------------------------------
    public static String getAs_YYMMDDHHMMSS(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("yyMMddHHmmss");
        return f.format(date);
    }

    //------------------------------------------------------------------------
    public static String getAs_YYMMDD(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("yyMMdd");
        return f.format(date);
    }

    //------------------------------------------------------------------------
    public static String getAs_HHMMSS(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("HHmmss");
        return f.format(date);
    }
*/

    public static Double getDouble(String value) throws IOException {
        Double d = null;
        if (value == null || value.trim().length() == 0) {
            return null;
        }

        value = value.trim();
        if (value.equals("-")) {
            return null;
        }

        try {
            d = Double.parseDouble(value);
        } catch(NumberFormatException exc) {
            throw new IOException(exc);
        }
        return d;
    }

    //------------------------------------------------------------------------
    public static java.util.Date getDateFrom_YYMMDD(IndentWriter iw, String yymmdd) {
        if (iw == null) {
            iw = new IndentWriter();
        }
        if (yymmdd == null || yymmdd.length() != 6) {
            return null;
        }

        java.util.Date d = null;

        // Try validity of Date
        try {
            DateFormat df = new SimpleDateFormat("yyMMdd");
            df.setLenient(false);
            d = df.parse(yymmdd);
        } catch (ParseException e) {
            return null;
        }

        return d;
    }

    //------------------------------------------------------------------------
    public static String getNowAs_YYMMDD(String timeZone) {
        Calendar calendar = Calendar.getInstance();
        if (timeZone != null) {
            calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bout);
        ps.format("%02d%02d%02d",
                calendar.get(Calendar.YEAR)-2000,
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
        String s = new String(bout.toByteArray());
        return s;
    }

    //------------------------------------------------------------------------
    public static String getNowAs_HHMMSS(String timeZone) {
        Calendar calendar = Calendar.getInstance();
        if (timeZone != null) {
            calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bout);
        ps.format("%02d%02d%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
        String s = new String(bout.toByteArray());
        return s;
    }

    //------------------------------------------------------------------------
    public static String getNowAs_YYMMDD_HHMMSS(String timeZone) {
        Calendar calendar = Calendar.getInstance();
        if (timeZone != null) {
            calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bout);
        ps.format("%02d%02d%02d_%02d%02d%02d",
                calendar.get(Calendar.YEAR)-2000,
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
        String s = new String(bout.toByteArray());
        return s;
    }

    //------------------------------------------------------------------------
    public static String getNowAs_YYMMDDHHMMSS(String timeZone) {
        Calendar calendar = Calendar.getInstance();
        if (timeZone != null) {
            calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bout);
        ps.format("%02d%02d%02d%02d%02d%02d",
                calendar.get(Calendar.YEAR)-2000,
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
        String s = new String(bout.toByteArray());
        return s;
    }

    //--------------------------------------------------------------------------
    public static String dateConvert_GC_To_YYMMDD(GregorianCalendar gc) throws Exception {
        GregorianCalendar gnow = new GregorianCalendar();
        String yy = String.valueOf(gnow.get(GregorianCalendar.YEAR)-2000);
        String mm = String.valueOf(gnow.get(GregorianCalendar.MONTH)+1);
        String dd = String.valueOf(gnow.get(GregorianCalendar.DAY_OF_MONTH));

        if(yy.length() == 1) {
            yy = "0" + yy;
        }
        if(mm.length() == 1) {
            mm = "0" + mm;
        }
        if(dd.length() == 1) {
            dd = "0" + dd;
        }
        return yy + mm + dd;
    }

    //--------------------------------------------------------------------------
    public static GregorianCalendar dateConvert_YYMMDD_To_GC(String yymmdd) throws Exception {
        String yyStr = yymmdd.substring(0,2);
        String mmStr = yymmdd.substring(2, 4);
        String ddStr = yymmdd.substring(4);
        int yy = Integer.parseInt(yyStr) + 2000;
        int mm = Integer.parseInt(mmStr) - 1;
        int dd = Integer.parseInt(ddStr);

        GregorianCalendar gnow = new GregorianCalendar();
        gnow.set(GregorianCalendar.YEAR, yy);
        gnow.set(GregorianCalendar.MONTH, mm);
        gnow.set(GregorianCalendar.DAY_OF_MONTH, dd);
        gnow.set(GregorianCalendar.SECOND, 1);
        gnow.set(GregorianCalendar.HOUR_OF_DAY, 0);
        gnow.set(GregorianCalendar.MINUTE, 0);

        return gnow;
    }

    //--------------------------------------------------------------------------
    public static String dateConvert_DDSMM_To_YYMMDD(String ddsmm) throws Exception {
        GregorianCalendar gnow = new GregorianCalendar();
        gnow.setTime(new java.util.Date());

        int nowYY = gnow.get(GregorianCalendar.YEAR);
        int nowMM = gnow.get(GregorianCalendar.MONTH);
        nowMM++;
        int nowDD = gnow.get(GregorianCalendar.DAY_OF_MONTH);

        int io = -1;
        io = ddsmm.indexOf("/");
        String thenDDStr = ddsmm.substring(0, io);
        String thenMMStr = ddsmm.substring(io+1);
        int thenDD = Integer.parseInt(thenDDStr);
        int thenMM = Integer.parseInt(thenMMStr);
        if(thenMM > nowMM)
            nowYY--;
        else if(thenMM == nowMM && thenDD > nowDD)
            nowYY--;

        nowYY -= 2000;
        String yys = String.valueOf(nowYY);
        String mms = String.valueOf(thenMM);
        String dds = String.valueOf(thenDD);
        if(yys.length() < 2)
            yys = "0" + yys;
        if(mms.length() < 2)
            mms = "0" + mms;
        if(dds.length() < 2)
            dds = "0" + dds;
        return yys + mms + dds;
    }

    //--------------------------------------------------------------------------
    public static String dateConvert_YYYYDMMDDD_To_YYMMDD(String str) throws Exception {
        str = str.trim();
        int io = -1;

        try {
            io = str.indexOf("-");
            String yy = str.substring(0,io);
            yy = yy.substring(2);
            str = str.substring(io+1);

            io = str.indexOf("-");
            String mm = str.substring(0, io);
            str = str.substring(io+1);

            String dd = str;
            return yy + mm + dd;
        } catch(Exception exc) {
            throw new Exception("Exception caught, incoming string was: " + str + "\n" +
                    exc.getMessage() + "\n" +
                    MM.getStackTraceString(exc));
        }
    }

    //--------------------------------------------------------------------------
    public static void fileMove(String fileSrc, String fileDest) throws Exception {
        File fsrc = new File(fileSrc);
        File fdest = new File(fileDest);
        fsrc.renameTo(fdest);
    }

    //------------------------------------------------------------------------
    public static Float getFloatFromSWEStr(String sweStr) throws Exception {
        String javaStr = sweStr.replace(',', '.');
        Float f = Float.valueOf(javaStr);
        return f;
    }

    //------------------------------------------------------------------------
    public static String getUntilNext(String next, StringBuffer strb) {
        if(strb == null || strb.length() == 0) {
            // System.out.println("MM.gun, returning null");
            return null;
        }

        String s = strb.toString();

        int io = -1;
        io = s.indexOf(next);
        if(io == -1) {
            strb.delete(0, strb.length());
            // System.out.println("MM.gun, returning the remains: " + s);
            return s;
        }

        String result = "";
        if(io > 0) {
            result = s.substring(0, io);
        }
        strb.delete(0, io+next.length());
        // System.out.println("MM.gun, returning result: " + result);
        return result;
    }

    public static int getIntFromStrOrMinus1(String str) {
        int r = -1;
        if (str == null || str.trim().length() == 0) {
            return -1;
        }

        try {
            r = Integer.valueOf(str);
        } catch (Exception exc) {
            return -1;
        }
        return r;
    }
}
