package com.pf.fl.shared.extract;

import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;
import com.pf.fl.shared.utils.OTuple2G;

import java.util.logging.Logger;

public class ExtractFromHTML_Vanguard {
	private static final Logger log = Logger.getLogger(ExtractFromHTML_Vanguard.class.getName());
	private static final String TAG = "FL_MSExtractDetails";

	private static final String URL_MUTUAL_FUND = "https://performance.morningstar.com/perform/Performance/fund/trailing-total-returns.action";
    private static final String URL_MUTUAL_FUND2 = "http://performance.morningstar.com/perform/Performance/fund/trailing-total-returns.action";
	private static final String URL_ETF = "https://performance.morningstar.com/perform/Performance/etf/trailing-total-returns.action";
    private static final String URL_ETF2 = "http://performance.morningstar.com/perform/Performance/etf/trailing-total-returns.action";

    public static int extractFundDetails(IndentWriter iwd, D_FundInfo fi) throws Exception {

        // Extract HTML information
        StringBuffer pageContentSB = new StringBuffer();
        OTuple2G<Integer, String> ec = ExtractFromHTML_Helper.htmlGet(pageContentSB, iwd, fi._url);
        if (ec._o1 != D_FundInfo.IC_NO_ERROR) {
            fi._errorCode = ec._o1;
            fi._lastExtractInfo = ec._o2;
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        }
        String pageContent = pageContentSB.toString();

        boolean isMutualFund = false;
        boolean isETF = false;
        String searchFund = null;
        String searchDateString = null;
        if (fi._url.startsWith(URL_MUTUAL_FUND) || fi._url.startsWith(URL_MUTUAL_FUND2)) {
            iwd.println("Mutual fund");
            isMutualFund = true;
            searchFund = getVanguardTickerFromFundName(fi.getNameMS(), iwd);
            searchDateString = searchFund + "&nbsp;return as of &nbsp;";
        } else if (fi._url.startsWith(URL_ETF) || fi._url.startsWith(URL_ETF2)) {
            iwd.println("ETF fund");
            isETF = true;
            searchFund = getVanguardTickerFromFundName(fi.getNameMS(), iwd) + " (Price)";
            searchDateString = searchFund + " return as of ";
        } else {
            fi._errorCode = D_FundInfo.IC_VG_NOT_MF_NOR_ETF;
            iwd.println("Error, Vanguard fund was neither Mutual or ETF returning");
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        }

        D_FundDPDay dpd = new D_FundDPDay();

        // First, make sure we have a good date
        iwd.println("Searching for: " + searchDateString);
        int io = pageContent.indexOf(searchDateString);
        if (io == -1) {
            fi._errorCode = D_FundInfo.IC_HTML_VG_DATE_NOT_FOUND;
            iwd.println("Error, Could not find date for Vanguard MF: " + isMutualFund + ", or ETF: " + isETF);
            return ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND;
        } else {
            int index = io + searchDateString.length();
            String dateMMSDDSYYYY = pageContent.substring(index, index + 10);
            String[] dateSplit = dateMMSDDSYYYY.split("/");
            String dateYYMMDD = dateSplit[2].substring(2) + dateSplit[0] + dateSplit[1];
            iwd.println("Found date: " + dateYYMMDD);
            try {
                java.util.Date d = MM.getDateFrom_YYMMDD(null, dateYYMMDD);
            } catch (Exception exc) {
                fi._errorCode = D_FundInfo.IC_HTML_VG_DATE_NOT_FOUND;
                iwd.println("Error, Date conversion threw exception: " + dateYYMMDD + " for Vanguard " + isMutualFund + ", or ETF: " + isETF);
                return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
            }
            iwd.println("Date valid, setting _dateYYMMDD_Actual to: " + dateYYMMDD);
            dpd._dateYYMMDD_Actual = dateYYMMDD;
        }

        iwd.println("Setting currency to USD");
        fi._currencyName = "USD";

        OTuple2G<String, String> ot = null;
        int io1 = -1;
        String findTagLoc = null;

        ot = new OTuple2G<String, String>(null, pageContent);

        // <tr>Total Return %
        findTagLoc = "Total Return";
        iwd.println("Now finding: " + findTagLoc);
        io1 = ot._o2.indexOf(findTagLoc);
        if (io1 == -1) {
            fi._errorCode = D_FundInfo.IC_HTML_VG_TOTAL_RETURNS_NOT_FOUND;
            iwd.println("...Error, Could not find Total Returns for Vanguard fund");
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        }
        ot._o2 = ot._o2.substring(io1 + findTagLoc.length());

        int rc = ExtractFromHTML_Helper.RC_SUCCESS;

        // DPDay for our fund
        // <tr>"Fund ticker"
        iwd.println("Now finding the returns");
        String nextTR = MM.assignAndReturnNextTagValue(ot, "<tr");
        rc = parseTR(iwd, fi, nextTR);
        if (rc != ExtractFromHTML_Helper.RC_SUCCESS) {
            iwd.println("Error was encountered while parsingTFs, returning: " + rc);
            return rc;
        }
        if (!tr_ticker.equals(searchFund)) {
            iwd.println("Error, Vanguard ticker mismatch"
                    + ", ticker from HTML: " + tr_ticker
                    + ", Ticker from name: " + searchFund
                    + ", Fund name: " + fi.getNameMS());
            fi._errorCode = D_FundInfo.IC_HTML_VG_DATA_TICKER_MISMATCH;
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        }
        iwd.println("Our DPD, actual: " + dpd._dateYYMMDD_Actual);
        iwd.println("...data points: " + tr_dps[0] + ", " + tr_dps[1] + ", " + tr_dps[2] + ", " + tr_dps[3] + ", " + tr_dps[4] + ", " + tr_dps[5] + ", " + tr_dps[6] + ", " + tr_dps[7] + ", " + tr_dps[8]);
        dpd._r1d = tr_dps[0];
        dpd._r1w = tr_dps[1];
        dpd._r1m = tr_dps[2];
        dpd._r3m = tr_dps[3];
        dpd._rYTDFund = tr_dps[4];
        dpd._r1y = tr_dps[5];
        dpd._r3y = tr_dps[6];
        dpd._r5y = tr_dps[7];
        dpd._r10y = tr_dps[8];

        // Rest of rows in table
        // MTF:
        //    <Index>
        //    Category (<>)
        //    2 +/- rows
        //    Rank in Category
        // ETF:
        //    <Ticker> (NAV)
        //    <Index> (Price)
        //    <Category> (NAV)
        //    Rank in Category (NAV)
        String indexName = "";
        String categoryName = null;
        boolean error = false;
        iwd.println("Now finding the index");
        int i = 0;

        // Mutual fund: 0=Index, 1=Category
        // ETF:         0=NAV, 1=Index, 2=Category
        while ((i<=1 && isMutualFund) || (i<=2 && isETF)) {
            iwd.println("In parseTR loop with index: " + i);
            error = false;
            nextTR = MM.assignAndReturnNextTagValue(ot, "<tr");
            if (nextTR == null) {
                iwd.println("nextTR was null at index: " + i);
                error = true;
            } else {
                rc = parseTR(iwd, fi, nextTR);
                iwd.println("parsed nextTR, rc: " + rc);
            }
            if (rc != ExtractFromHTML_Helper.RC_SUCCESS || error) {
                iwd.println("Error, Parsing DP Rows Table, rc: " + rc + ", error: " + error);
                fi._errorCode = D_FundInfo.IC_HTML_VG_DP_ROWS_INCONSISTENCY;
                if (!error) return rc;
                else return rc;
            }

            // Pick up indexName
            if (isETF && i == 1) {
                indexName = tr_ticker;
                if (!indexName.endsWith(" (Price)")) {
                    iwd.println("Error, Vanguard Index name for ETF did not end with (Price)");
                    fi._errorCode = D_FundInfo.IC_HTML_VG_DP_ROWS_INCONSISTENCY;
                    return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
                }
                indexName = indexName.substring(0, indexName.length()-8);
                iwd.println("ETF indexName assigned to: " + indexName);
            }
            else if(isMutualFund && i == 0) {
                indexName = tr_ticker;
                iwd.println("Mutual fund indexName assigned to: " + indexName);
            }

            // Pick up categoryName
            if (isETF && i == 2) {
                categoryName = tr_ticker;
                if (!categoryName.endsWith(" (NAV)")) {
                    iwd.println("Error, Vanguard Category name for ETF did not end with (NAV)");
                    fi._errorCode = D_FundInfo.IC_HTML_VG_DP_ROWS_INCONSISTENCY;
                    return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
                }
                categoryName = categoryName.substring(0, categoryName.length()-6);
                iwd.println("ETF categoryName assigned to: [" + categoryName + "]");
            }
            else if(isMutualFund && i == 1) {
                categoryName = tr_ticker;
                int cnio1 = tr_ticker.indexOf("(");
                int cnio2 = tr_ticker.indexOf(")");
                if (cnio1 == -1 || cnio2 == -1 || (cnio2-cnio1) <= 1) {
                    iwd.println("Error, Vanguard Category name for MTF did not have format: Category (...): [" + categoryName + "]");
                    fi._errorCode = D_FundInfo.IC_HTML_VG_DP_ROWS_INCONSISTENCY;
                    return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
                }
                categoryName = categoryName.substring(cnio1+1, cnio2);
                iwd.println("MTF categoryName assigned to: " + categoryName);
            }
            i++;
        }
        if (categoryName == null) {
            iwd.println("***** Weird, no categoryName found");
        } else {
            indexName += " (" + categoryName + ")";
        }
        iwd.println("Resulting indexName is: " + indexName);

        iwd.println("Will set resulting DPD:");
        iwd.println(dpd.toString());
        fi.setIndexName(indexName);
        fi.setCategoryName(indexName);
        fi._dpDays.add(0, dpd);
        iwd.println("Returning successfully");
        return ExtractFromHTML_Helper.RC_SUCCESS;
    }

    private static String tr_ticker;
    private static float[] tr_dps;
    private static int parseTR(IndentWriter iwd, D_FundInfo fi, String nextTR) {
        iwd.println("parseTR");
        iwd.push();
        int rc = parseTRImpl(iwd, fi, nextTR);
        iwd.pop();
        return rc;
    }
    private static int parseTRImpl(IndentWriter iwd, D_FundInfo fi, String nextTR) {
        tr_ticker = null;
        tr_dps = new float[9];

		OTuple2G<String, String> ot = new OTuple2G<String, String>(null, nextTR);
		tr_ticker = MM.assignAndReturnNextTagValue(ot, "<th");
        iwd.println("TR Ticker: " + tr_ticker);

        OTuple2G<Boolean, Float> rv = null;
    	rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
		if (!rv._o1) {
            iwd.println("Vanguard, r1d ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        } else {
		    float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
		    tr_dps[0] = r;
		    iwd.println("Float 1: " + String.valueOf(r));
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iwd.println("Vanguard, r1w ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[1] = r;
            iwd.println("Float 2: " + String.valueOf(r));
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iwd.println("Vanguard, r1m ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[2] = r;
            iwd.println("Float 3: " + String.valueOf(r));
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iwd.println("Vanguard, r3m ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[3] = r;
            iwd.println("Float 4: " + String.valueOf(r));
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iwd.println("Vanguard, YTD ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[4] = r;
            iwd.println("Float 5: " + String.valueOf(r));
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iwd.println("Vanguard, r1y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[5] = r;
            iwd.println("Float 5: " + String.valueOf(r));
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iwd.println("Vanguard, r3y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[6] = r;
            iwd.println("Float 6: " + String.valueOf(r));
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iwd.println("Vanguard, r5y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[7] = r;
            iwd.println("Float 7: " + String.valueOf(r));
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iwd.println("Vanguard, r10y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[8] = r;
            iwd.println("Float 8: " + String.valueOf(r));
        }
        iwd.println("parseTR successful");
        return ExtractFromHTML_Helper.RC_SUCCESS;
	}

    public static final String getVanguardTickerFromFundName(
            String name,
            IndentWriter iwd) {
        int io1 = name.lastIndexOf("(");
        int io2 = name.lastIndexOf(")");
        if (io1 == -1 || io2 == -1) {
            iwd.println("Could not find ( or )");
            return null;
        }
        String s = name.substring(io1+1, io2);
        iwd.println("Ticker name is: " + s);
        return s;
    }
}
