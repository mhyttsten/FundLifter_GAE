package com.pf.fl.shared.extract;

import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.utils.HtmlRetriever;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;
import com.pf.fl.shared.utils.OTuple2G;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Logger;

public class ExtractFromHTML_Helper {
    private static final Logger log = Logger.getLogger(ExtractFromHTML_Helper.class.getName());
    private static final String TAG = "";

    public static final int RC_SUCCESS = 0;
    public static final int RC_SUCCESS_BUT_DATA_WAS_UPDATED = 1;
    public static final int RC_ERROR_INVALID_FUND = 3;
    public static final int RC_WARNING_NO_DPDAY_FOUND = 5;

    private IndentWriter _iwd = new IndentWriter();

    public String _dateNow_YYMMDD;
    public String _dateLastFriday_YYMMDD;

    public int extractFundDetails(
            D_FundInfo fi,
            IndentWriter iwd) throws IOException {

        if (iwd != null) {
            _iwd = iwd;
        }

        int rv = RC_SUCCESS;
        try {
            rv = extractFundDetailsImpl(fi);
        } catch(Exception exc) {
            StringBuffer strb = new StringBuffer();
            strb.append(fi.getTypeAndName() + "\n");
            strb.append("*** EXCEPTION CAUGHT WHEN extractFund\n");
            strb.append("...exception: " + exc.getMessage() + "\n");
            strb.append(MM.getStackTraceString(exc) + "\n");
            _iwd.println(strb.toString());
            return RC_ERROR_INVALID_FUND;
        }

        if (rv != RC_SUCCESS) {
            String s = "Error for: " + fi.getTypeAndName() + ", ec: " + rv + " (" + fi.error2str() + ")";
            _iwd.println(s);
            return rv;
        }

        return RC_SUCCESS;
    }

    private int extractFundDetailsImpl(D_FundInfo fi) throws Exception {
        _dateNow_YYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        _dateLastFriday_YYMMDD = MM.tgif_getLastFridayTodayExcl(_dateNow_YYMMDD);
        fi._dateYYMMDD_Update_Attempted = _dateNow_YYMMDD;

        // Check the DPDay we might already have
        D_FundDPDay dpd1 = null;
        if (fi._dpDays != null && fi._dpDays.size() > 0) {
            dpd1 = fi._dpDays.get(0);
            if (dpd1._dateYYMMDD.equals(_dateLastFriday_YYMMDD)) {
                _iwd.println("Found dpd matching last friday: " + dpd1);
                // Are we already done?
                if (dpd1._r1w != D_FundDPDay.FLOAT_NULL) {
                    _iwd.println("...it was not null, so extraction has already been done");
                    return ExtractFromHTML_Helper.RC_SUCCESS;
                } else {
                    _iwd.println("...it was null, so removing it to retry extraction");
                    dpd1 = fi._dpDays.remove(0);
                }
            } else {
                dpd1 = null;
            }
        } else {
            _iwd.println("_dpDays was null or had no entries");
        }

        // Contract for extractFundDetails
        // - RC_SUCCESS is returned only if all of parsing went well
        // - In this case fi._dpDays.get(0) has been added and it's _dateYYMMDD_Actual has been set
        int errorCode = ExtractFromHTML_Helper.RC_SUCCESS;
        if (fi._type.equals(D_FundInfo.TYPE_VANGUARD)) {
            _iwd.println("Extracting Vanguard");
            errorCode = ExtractFromHTML_Vanguard.extractFundDetails(_iwd, fi);
        } else {
            _iwd.println("Extracting Morningstar");
            errorCode = ExtractFromHTML_Morningstar.extractFundDetails(_iwd, fi);
        }
        if (errorCode != ExtractFromHTML_Helper.RC_SUCCESS) {
            _iwd.println("...Encountered error: " + errorCode);
            fi._lastExtractInfo = "Now: " + MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM)
                    + ", trying to extract for: " + _dateLastFriday_YYMMDD
                    + ". ExtractFromHTML_Helper error: " + errorCode + ". FundInfo error: " + fi._errorCode;
            return errorCode;
        } else {
            _iwd.println("...Successfully extracted fund");
        }

        // We've seen cases where a DPD table is completely empty
        if (fi._dpDays.size() == 0) {
            _iwd.println("_dpDays.size() == 0, this is weird but ok. It means we have no DPDAY");
            fi._errorCode = D_FundInfo.IC_NO_RECENT_DPDAY;
            fi._lastExtractInfo = "Extraction completed by _dpDays.size() == 0";
            return RC_WARNING_NO_DPDAY_FOUND;
        }

        D_FundDPDay dpd2 = fi._dpDays.remove(0);  // Lets look at the things just added
        _iwd.println("Back in ExtractFromHTML_Helper after successful extract: " + dpd2._dateYYMMDD_Actual);

        // If we arrive here, then dpd1 may be a valid dateFriday, but r1w == NULL
        // See if we should keep dpd1?
        if (dpd1 != null) {
            // If there was any problem finding an actual date for the new, then keep old
            if (dpd2._dateYYMMDD_Actual == null || dpd2._dateYYMMDD_Actual.length() != 6) {
                _iwd.println("Keeping old DPD, new DPD had weird _dateYYMMDD_Actual: " + dpd2._dateYYMMDD_Actual);
                fi._dpDays.add(0, dpd1);
                return RC_SUCCESS;
            }
            if (dpd1._r1m != D_FundDPDay.FLOAT_NULL && dpd2._r1w == D_FundDPDay.FLOAT_NULL) {
                _iwd.println("Keeping old DPD, new DPD had r1w == null");
                fi._dpDays.add(0, dpd1);
                return RC_SUCCESS;
            }
        }

        // A current DP was found
        if (dpd2._dateYYMMDD_Actual == null) {
            _iwd.println("Lastest DPDay had null for _dateYYMMDD_Actual");
            fi._errorCode = D_FundInfo.IC_HTML_MS_DPDAY_NULLDATE;
            fi._lastExtractInfo = "Extract: " + dpd2._dateYYMMDD + ", actual: NULL";
            return RC_WARNING_NO_DPDAY_FOUND;
        }

        String dateNow_YYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        String dateLastFriday_YYMMDD   = MM.tgif_getLastFridayTodayExcl(dateNow_YYMMDD);
        String dateLastSaturday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.SATURDAY);
        String dateLastSunday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.SUNDAY);
        String dateLastMonday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.MONDAY);
        String dateLastTuesday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.TUESDAY);
        String dateLastWednesday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.WEDNESDAY);
        String dateLastThursday_YYMMDD = MM.tgif_getPrevWeekday(dateLastFriday_YYMMDD, Calendar.THURSDAY);

        // We have a valid extraction if actual date is last Friday or Thursday, or Mon/Tue week after
        if (dpd2._dateYYMMDD_Actual.equals(dateLastFriday_YYMMDD)
                || dpd2._dateYYMMDD_Actual.equals(dateLastThursday_YYMMDD)
                || dpd2._dateYYMMDD_Actual.equals(dateLastSaturday_YYMMDD)
                || dpd2._dateYYMMDD_Actual.equals(dateLastSunday_YYMMDD)
                || dpd2._dateYYMMDD_Actual.equals(dateLastMonday_YYMMDD)
                || dpd2._dateYYMMDD_Actual.equals(dateLastTuesday_YYMMDD)
        ) {
            _iwd.println("Success, lastest DPDay was valid: " + dpd2);
            dpd2._dateYYMMDD = dateLastFriday_YYMMDD;
            fi._dpDays.add(0, dpd2);
            fi._dateYYMMDD_Updated = _dateLastFriday_YYMMDD;
            fi._lastExtractInfo = "Extract: " + dpd2._dateYYMMDD + ", actual: " + dpd2._dateYYMMDD_Actual
                    + "Success, r1w: " + dpd2._r1w;
            return ExtractFromHTML_Helper.RC_SUCCESS;
        }

        // We don't have a valid DPDay, wait until next data point
        _iwd.println("Wanted to extract for friday: " + dateLastFriday_YYMMDD);
        _iwd.println("But found DP: " + dpd2._dateYYMMDD_Actual);
        _iwd.println("Which is not in expected range: " + dateLastThursday_YYMMDD + "-" + dateLastTuesday_YYMMDD);

        int daydiff = MM.tgif_dayCountDiff(dateLastFriday_YYMMDD, dpd2._dateYYMMDD_Actual);
        _iwd.println("Day diff is: " + daydiff);

        fi._lastExtractInfo = "Extract: " + dpd2._dateYYMMDD + ", actual: " + dpd2._dateYYMMDD_Actual
                + "Error, actual date (date of dpday) is older than last friday";

        if (daydiff > 62) {
            _iwd.println("Setting fund invalid, number of days since last DPDay: " + daydiff);
            fi._errorCode = D_FundInfo.IC_NO_RECENT_DPDAY;
            fi._isValid = false;
            log.warning("Big daydiff for fund: " + fi.getTypeAndName() + ", setting to invalid, dayiff: " + daydiff);
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        }

        return ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND;
    }

    public static OTuple2G<Boolean, Float> validFloat(String s) {
        if (s == null || s.trim().length() == 0) {
            return new OTuple2G<>(true, D_FundDPDay.FLOAT_NULL);
        }
        s = s.trim();
        if (s.equals("-")) {
            return new OTuple2G<>(true, D_FundDPDay.FLOAT_NULL);
        }
        if (s.equals("&mdash;")) {
            return new OTuple2G<>(true, D_FundDPDay.FLOAT_NULL);
        }
        s = s.replace(',', '.');
        try {
            float d = Float.parseFloat(s);
            return new OTuple2G<>(true, new Float(d));
        } catch(Exception exc) {
            return new OTuple2G<>(false, null);
        }
    }

    // Returns D_FundInfo.IC_SUCCESS (or some error) and string to assign to lastExtractAttempt
    public static OTuple2G<Integer, String> htmlGet(StringBuffer resultData, IndentWriter iwd, String url) throws Exception {
        // Get the raw HTML data
        iwd.println("Now performing HTTP GET of: " + url);
        iwd.push();
        byte[] htmlDataRaw = HtmlRetriever.htmlGet(
                iwd,
                url,
                5000,
                4);
        iwd.println("Done");
        iwd.pop();

        boolean error = false;
        int errorCode = D_FundInfo.IC_NO_ERROR;
        if (htmlDataRaw == null || htmlDataRaw.length == 0) {
            String s = "HTTP GET call returned null or 0 bytes";
            iwd.println(s);
            errorCode = D_FundInfo.IC_COM_NO_URL_DATA;
            error = true;
        }
        iwd.println("Now decoding HTTP GET data");
        String htmlDataString = MM.newString(htmlDataRaw, Constants.ENCODING_FILE_READ);
        if (htmlDataString == null || htmlDataString.length() == 0) {
            String s = "Data from HTTP GET could not be decoded with: " + Constants.ENCODING_FILE_READ;
            iwd.println(s);
            errorCode = D_FundInfo.IC_COM_NO_DECODABLE_DATA;
            error = true;
        }
        if (error) {
            String s = "Exiting extraction with error";
            iwd.println(s);
            String lei = "Now: " + MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM)
                    + ". HTMLGet error. FundInfo error: " + errorCode;
            return new OTuple2G<>(errorCode, lei);
        }
        iwd.println("Now stripping HTML comments");
        String result = MM.stripHTMLComments(htmlDataString);
        resultData.append(result);
        iwd.println("...Done");
        return new OTuple2G<>(D_FundInfo.IC_NO_ERROR, "");
    }
}
