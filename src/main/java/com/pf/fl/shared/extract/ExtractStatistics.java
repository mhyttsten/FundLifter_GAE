package com.pf.fl.shared.extract;

import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExtractStatistics {
    private static final Logger log = Logger.getLogger(ExtractStatistics.class.getName());

    public List<D_FundInfo> _fiAllFunds = new ArrayList<>();
    public int _fiExtractable = 0;

    public List<D_FundInfo> _fiToExtract = new ArrayList<>();
    public List<D_FundInfo> _fiExtractAttemptedToday = new ArrayList<>();

    // Mutually exclusive
    public List<D_FundInfo> _fiInvalids = new ArrayList<>();
    public List<D_FundInfo> _fiErrors = new ArrayList<>();

    public List<D_FundInfo> _fiExtracted = new ArrayList<>();
    public List<D_FundInfo> _fiExtracted_Thu = new ArrayList<>();
    public List<D_FundInfo> _fiExtracted_Fri = new ArrayList<>();
    public List<D_FundInfo> _fiExtracted_Mon = new ArrayList<>();
    public List<D_FundInfo> _fiExtracted_Tue = new ArrayList<>();
    // These are the non-extracted stats for all valid and non-errored funds
    public List<D_FundInfo> _fiMissedExtractions = new ArrayList<>();
    public List<D_FundInfo> _fiWeeksWithoutExtract_1F = new ArrayList<>();
    public List<D_FundInfo> _fiWeeksWithoutExtract_2F = new ArrayList<>();
    public List<D_FundInfo> _fiWeeksWithoutExtract_3F = new ArrayList<>();
    public List<D_FundInfo> _fiWeeksWithoutExtract_GT3F = new ArrayList<>();
    public Map<String, Integer> _fiWeeksWithoutExtract_1F_Type = new HashMap<>();
    public Map<String, Integer> _fiMissedExtractions_Type = new HashMap<>();

    public String _nowYYMMDD;
    public String _nowYYMMDD_HHMMSS;
    public String _fridayLastYYMMDD;
    public String _fridayLast2YYMMDD;
    public String _fridayLast3YYMMDD;
    public String _fridayLast4YYMMDD;

    //----------------------------------------------------------------------
    public ExtractStatistics(List<D_FundInfo> fiAllFunds) {
        _nowYYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        _nowYYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM);
        String nowYYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM);
        _fridayLastYYMMDD = MM.tgif_getLastFridayTodayExcl(_nowYYMMDD);
        _fridayLast2YYMMDD = MM.tgif_getLastFridayTodayExcl(_fridayLastYYMMDD);
        _fridayLast3YYMMDD = MM.tgif_getLastFridayTodayExcl(_fridayLast2YYMMDD);
        _fridayLast4YYMMDD = MM.tgif_getLastFridayTodayExcl(_fridayLast3YYMMDD);
        _fiAllFunds = fiAllFunds;
    }

    //----------------------------------------------------------------------
    public String extractStats() {
        reset();

        IndentWriter iw = new IndentWriter();
        iw.println("Entered ExtractStatistics.extractStats<br>");

        iw.println("Fund count: " + _fiAllFunds.size() + "<br>");
        for (int i=0; i < _fiAllFunds.size(); i++) {
            D_FundInfo fi = _fiAllFunds.get(i);

            if (i == 0) iw.println("Doing fund: " + fi.getTypeAndName());

            // Filter out Funds for which we already have a valid R1W
            D_FundDPDay dpdAlreadyThere = null;
            if (fi._dpDays.size() > 0) {
                D_FundDPDay dpd = fi._dpDays.get(0);
                if (dpd._dateYYMMDD.equals(_fridayLastYYMMDD) && dpd._r1w != D_FundDPDay.FLOAT_NULL) {
                        dpdAlreadyThere = dpd;
                    if (i == 0) iw.println("Found dpd: " + dpd.toString());
                }
            }

            // We always try to extract everything.
            _fiExtractable++;

            // Error fund
            if (fi._isValid && fi._errorCode != D_FundInfo.IC_NO_ERROR) {
                if (i == 0) iw.println("Errors");
                _fiErrors.add(fi);
            }

            // Invalid fund
            if (!fi._isValid) {
                if (i == 0) iw.println("Invalid");
                _fiInvalids.add(fi);
            }

            // We still try to extract error or invalid funds

            // If we've already extracted this fund with a valid R1W
            // Assign stats to which day it was extracted
            if (dpdAlreadyThere != null) {
                _fiExtracted.add(fi);
                if (i == 0) iw.println("Extracted");
                String thu = MM.tgif_getPrevWeekday(_fridayLastYYMMDD, Calendar.THURSDAY);

                if (dpdAlreadyThere._dateYYMMDD_Actual.equals(thu)) {
                    _fiExtracted_Thu.add(fi);
                }
                if (dpdAlreadyThere._dateYYMMDD_Actual.equals(_fridayLastYYMMDD)) {
                    _fiExtracted_Fri.add(fi);
                }
                String mon = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.MONDAY);
                if (dpdAlreadyThere._dateYYMMDD_Actual.equals(mon)) {
                    _fiExtracted_Mon.add(fi);
                }
                String tue = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.TUESDAY);
                if (dpdAlreadyThere._dateYYMMDD_Actual.equals(tue)) {
                    _fiExtracted_Tue.add(fi);
                }
            }

            // We have not attempted to extract fund today
            // So let's try to extract it
            else if (fi._dateYYMMDD_Update_Attempted == null
                    || fi._dateYYMMDD_Update_Attempted.compareTo(_nowYYMMDD) < 0) {
                if (i == 0) iw.println("To Extract");
                _fiToExtract.add(fi);
                _fiMissedExtractions.add(fi);
                incrementCount(_fiMissedExtractions_Type, fi);
            }

            // We've already tried to extract fund today
            // So let's not try again
            else if (fi._dateYYMMDD_Update_Attempted.compareTo(_nowYYMMDD) == 0) {
                if (i == 0) iw.println("Already Attempted Today");
                _fiExtractAttemptedToday.add(fi);
                _fiMissedExtractions.add(fi);
                incrementCount(_fiMissedExtractions_Type, fi);
            }

            // Error state
            else {
                throw new AssertionError("Weird state for fund, "
                        + "\nnow: " + _nowYYMMDD
                        + "\nfridayLast: " + _fridayLastYYMMDD
                        + "\nfi.updated: " + fi._dateYYMMDD_Updated
                        + "\nfi.updateAttempted: " + fi._dateYYMMDD_Update_Attempted
                        + "\n" + fi.toString());
            }
        }

        // Now analyze how many weeks missing
        for (D_FundInfo fi: _fiMissedExtractions) {

            // Let's interpret this as we've never really tried to extract the fund
            if (fi._dpDays.size() == 0) {
                _fiWeeksWithoutExtract_1F.add(fi);
            }

            else {

                // Sanity check, we should not have anything for 1F
                if (hasDPFor(_fridayLastYYMMDD, fi)) {
                    throw new AssertionError("We should not have last friday in this list");
                }

                if (hasDPFor(_fridayLast2YYMMDD, fi)) {
                    _fiWeeksWithoutExtract_1F.add(fi);
                    incrementCount(_fiWeeksWithoutExtract_1F_Type, fi);
                }
                else if (hasDPFor(_fridayLast3YYMMDD, fi)) {
                    _fiWeeksWithoutExtract_2F.add(fi);
                }
                else if (hasDPFor(_fridayLast4YYMMDD, fi)) {
                    _fiWeeksWithoutExtract_3F.add(fi);
                }
                else {
                    _fiWeeksWithoutExtract_GT3F.add(fi);
                }
            }
        }
        iw.println("Exit ExtractStatistics.extractStats<br>");
        return iw.getString();
    }

    //--------------------------------------------------------------------------------------------
    private static boolean hasDPFor(String yymmdd, D_FundInfo fi) {
        D_FundDPDay dpd = fi._dpDays.get(0);
       return dpd._dateYYMMDD.equals(yymmdd) && dpd._r1w != D_FundDPDay.FLOAT_NULL;
    }

    //--------------------------------------------------------------------------------------------
    private static void incrementCount(Map<String, Integer> counter, D_FundInfo fi) {
        int count = 1;
        if (counter.containsKey(fi._type)) {
            count = counter.get(fi._type) + 1;
        }
        counter.put(fi._type, count);
    }

    //--------------------------------------------------------------------------------------------
    public static String getExtractSummary(
            IndentWriter iw,
            int limitThursday,  // put -1 for all
            int limitNotExtracted,
            String extractStart_YYMMDD_HHMMSS,
            List<D_FundInfo> fiAll) {

        ExtractStatistics es_now = new ExtractStatistics(fiAll);
        es_now.extractStats();

        if (extractStart_YYMMDD_HHMMSS == null) {
            extractStart_YYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM);
        }

        iw.println("Report time: " + extractStart_YYMMDD_HHMMSS + " (Stokholm)");
        iw.println("Now: " + MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM) + " (Stockholm)");        
        iw.println("Extract Friday: " + es_now._fridayLastYYMMDD);
        iw.println("Funds in DB");
        iw.println("...Total: " + es_now._fiAllFunds.size());
        iw.println("...Invalid: " + es_now._fiInvalids.size());
        iw.println("...Error: " + es_now._fiErrors.size() + (es_now._fiErrors.size() > 0 ? ", ***UNEXPECTED***": ""));
        iw.println("Extracted: " + es_now._fiExtracted.size() + ", out of: " + es_now._fiExtractable);
        iw.println("...Thursday: " + es_now._fiExtracted_Thu.size());
        iw.println("...Friday:   " + es_now._fiExtracted_Fri.size());
        iw.println("...Monday:   " + es_now._fiExtracted_Mon.size());
        iw.println("...Tuesday:  " + es_now._fiExtracted_Tue.size());

        iw.println("Missed extractions");
        iw.println("...Only last Friday: " + es_now._fiWeeksWithoutExtract_1F.size());
        for (String type: D_FundInfo.TYPES) {
            int counter = 0;
            if (es_now._fiWeeksWithoutExtract_1F_Type.containsKey(type)) {
                counter = es_now._fiWeeksWithoutExtract_1F_Type.get(type);
            }
            iw.println("......" + type + ": " + counter);
        }
        iw.println("...Last 2 Fridays:   " + es_now._fiWeeksWithoutExtract_2F.size());
        iw.println("...Last 3 Fridays:   " + es_now._fiWeeksWithoutExtract_3F.size());
        iw.println("...>3 Fridays:       " + es_now._fiWeeksWithoutExtract_GT3F.size());
        iw.println("...Total affected:   " + es_now._fiMissedExtractions.size());
        for (String type: D_FundInfo.TYPES) {
            int counter = 0;
            if (es_now._fiMissedExtractions_Type.containsKey(type)) {
                counter = es_now._fiMissedExtractions_Type.get(type);
            }
            iw.println("......" + type + ": " + counter);
        }
        iw.println("...Attempted today:  " + es_now._fiExtractAttemptedToday.size());

        iw.println();
        printListStr(iw, "Invalids", es_now._fiInvalids, 10000);

        iw.println();
        if (es_now._fiErrors.size() > 0) {
            iw.println("*** ERRORS EXISTED UNEXPECTEDLY ***");
        }
        printListStr(iw, "Errors", es_now._fiErrors, 10000);

//        if (limitThursday == -1) {
//            limitThursday = 10000;
//        }
        if (limitNotExtracted == -1) {
            limitNotExtracted = 10000;
        }

        iw.println();
        iw.println("Thursday: " + es_now._fiExtracted_Thu.size());
        iw.println("...Will not list those in detail, see ExtractStatistics to enable this code again");
//        printListStr(iw, "Thursday", es_now._fiExtracted_Thu, limitThursday);
        iw.println();
        printListStr(iw, "Missed last Friday", es_now._fiWeeksWithoutExtract_1F, limitNotExtracted);
        iw.println();
        printListStr(iw, "Missed last 2 Fridays", es_now._fiWeeksWithoutExtract_2F, limitNotExtracted);
        iw.println();
        printListStr(iw, "Missed last 3 Fridays", es_now._fiWeeksWithoutExtract_3F, limitNotExtracted);
        iw.println();
        printListStr(iw, "Missed >3 Fridays", es_now._fiWeeksWithoutExtract_GT3F, limitNotExtracted);

        iw.println();
        return "";
    }


    // ***********************************************************************

    //----------------------------------------------------------------------
    private static String printListStr(IndentWriter iw, String header, List<D_FundInfo> l, int limit) {
        if (iw == null) {
            iw = new IndentWriter();
        }
        iw.setIndentChar('.');
        iw.println(header + ". Count: " + l.size() + ".");
        for (int i = 0; i < l.size(); i++) {
            if (i >= limit) {
                break;
            }
            D_FundInfo fi = l.get(i);
            iw.print("[" + i + "]: "
                    + fi._type + "." + fi.getNameMS()
                    + ", updated: " + fi._dateYYMMDD_Updated
                    + ", attempted: " + fi._dateYYMMDD_Update_Attempted);
            if (fi._errorCode != D_FundInfo.IC_NO_ERROR) {
                iw.println(", ec: " + fi._errorCode + " (" + fi.error2str() + ")");
            } else {
                iw.println(", ec: SUCCESS");
            }

            iw.push();
            iw.print("Latest DPD. ");
            if (fi._dpDays.size() ==0) {
                iw.println("None");
            } else {
                D_FundDPDay dpd = fi._dpDays.get(0);
                iw.print("Updated: " + dpd._dateYYMMDD + ", attempted: " + dpd._dateYYMMDD_Actual);
                if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
                    iw.println(", r1w: NULL");
                } else {
                    iw.println(", r1w: " + String.format("%.2f", dpd._r1w));
                }
            }

            iw.println(fi.getTypeAndNameURLEncoded());
            if (iw._generateHTML) {
                iw.println     ("<a href=\"" + fi._url + "\" target=\"_blank\">" + fi._url + "</a>");
            } else {
                iw.println(fi._url);
            }
            iw.pop();
        }
        return iw.getString();
    }

    //----------------------------------------------------------------------
    private void reset() {
        _fiExtractable = 0;

        _fiToExtract = new ArrayList<>();
        _fiExtractAttemptedToday = new ArrayList<>();

        // Mutually exclusive
        _fiInvalids = new ArrayList<>();
        _fiErrors = new ArrayList<>();

        _fiExtracted = new ArrayList<>();
        _fiExtracted_Thu = new ArrayList<>();
        _fiExtracted_Fri = new ArrayList<>();
        _fiExtracted_Mon = new ArrayList<>();
        _fiExtracted_Tue = new ArrayList<>();
        // These are the non-extracted stats for all valid and non-errored funds
        _fiMissedExtractions = new ArrayList<>();
        _fiWeeksWithoutExtract_1F = new ArrayList<>();
        _fiWeeksWithoutExtract_2F = new ArrayList<>();
        _fiWeeksWithoutExtract_3F = new ArrayList<>();
        _fiWeeksWithoutExtract_GT3F = new ArrayList<>();
    }
}

