package com.pf.fl.appengine.jsphelper;

import com.pf.fl.appengine.extract.FLOps1_Ext1_Extract_New;
import com.pf.fl.appengine.extract.GCSWrapper;
import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.DB_FundInfo;
import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.datamodel.D_FundInfo_Serializer;
import com.pf.fl.shared.extract.ExtractStatistics;
import com.pf.fl.shared.utils.FundList_Validator;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;
import com.pf.fl.shared.utils.OTuple2G;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JSP_Helper {
    private static final Logger log = Logger.getLogger(JSP_Helper.class.getName());
    private static final String TAG = MM.getClassName(JSP_Helper.class.getName());

    //-----------------------------------------------------------------------
    public static boolean _isInitialized;
    public static void initialize() throws IOException {
        log.info("JSP_Helper.initialize");
        if (_isInitialized) {
            log.info("...we are already initialized, returning");
            return;
        }
        byte[] data = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        log.info("...reading data: " + data.length + " bytes");
        DB_FundInfo._isInitialized = false;
        DB_FundInfo.initialize(data, false);
        _isInitialized = true;
    }

    //-----------------------------------------------------------------------
    public static final String OP_NAME = "op_name";
    public static final String OP_DO_THING = "dothing";
    public static final String OP_REWIND_ATTEMPTED = "rewindattempted";
    public static String operation(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        initialize();
        String p = req.getParameter(OP_NAME);
        if (p.trim().toLowerCase().equals(OP_DO_THING)) {
            return op_do_thing(req, resp);
        }
        if (p.trim().toLowerCase().equals(OP_REWIND_ATTEMPTED)) {
            return op_rewindAttemptedDate(req, resp);
        }
        return "No such operation: " + p;
    }

    private static String op_do_thing(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();
        int count = 0;
        StringBuffer strb = new StringBuffer();
        for (D_FundInfo fi : fis) {
//                strb.append("ms: " + fi.getNameMS() + ", orig: " + fi.getNameMS() + "<br>");
//            count++;
        }

//        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
//        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
        return strb.toString();
//        return "Number of funds: " + fis.size();
    }

    private static String op_rewindAttemptedDate(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();

        // Rewind attempted date to 2 days before today
        String date = MM.getNowAs_YYMMDD(null);
        date = MM.tgif_adjustByDayCount(-2, date);
        for (D_FundInfo fi : fis) {
            fi._dateYYMMDD_Update_Attempted = date;
        }
        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
        return "Rewinded attempted date to: " + date;
    }

    // **********************************************************************

    //-----------------------------------------------------------------------
    public static String jsp_displayFundDB(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String s = jsp_displayFundDBImpl(req, resp);
        return s = s.replace("&", "&amp;");
    }
    private static String jsp_displayFundDBImpl(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        IndentWriter iwd = new IndentWriter();
        iwd.setNewline("<br>");
        iwd.setIndentChar('.');
        iwd.println("Fund information from DB");

        String fundTypeAndName = req.getParameter(JSP_Constants.PARAM2_TYPEDOTNAME_);
        if (fundTypeAndName != null) {
            fundTypeAndName = URLDecoder.decode(fundTypeAndName, Constants.ENCODING_FILE_READ);
        }

        initialize();

        List<D_FundInfo> fis = new ArrayList<>();
        if (fundTypeAndName != null && fundTypeAndName.trim().length() > 0) {
            D_FundInfo fi = DB_FundInfo.getFundInfosByTypeDotName(fundTypeAndName);
            if (fi != null) {
                fis.add(fi);
            }
        } else {
            fis = DB_FundInfo.getAllFundInfos();
        }
        int count = 0;
        for (D_FundInfo fi: fis) {
            count++;
            if ((count % 50) == 0) {
                log.info("Have printed: " + count + " funds");
            }
            fi.dumpInfo(iwd);
            iwd.println("\n");
        }

        return iwd.getString();
    }

    //-----------------------------------------------------------------------
    public static String jsp_extractDebugger(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String s = jsp_extractDebuggerImpl(req, resp);
        return s = s.replace("&", "&amp;");
    }
    private static String jsp_extractDebuggerImpl(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        IndentWriter iwd = new IndentWriter();
        iwd.setNewline("<br>");
        iwd.setIndentChar('.');
        iwd.println("Starting extraction debugger");

        String fundTypeAndName = req.getParameter(JSP_Constants.PARAM2_TYPEDOTNAME_);
        if (fundTypeAndName == null || fundTypeAndName.trim().length() == 0) {
            iwd.println("Error, missing parameter: " + JSP_Constants.PARAM2_TYPEDOTNAME_);
            return iwd.getString();
        }

        fundTypeAndName = URLDecoder.decode(fundTypeAndName, Constants.ENCODING_FILE_READ);
        iwd.println("Fund to extract: " + fundTypeAndName);

        iwd.println("Now reading DB");
        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        DB_FundInfo.initialize(fundInfoBA, false);
        D_FundInfo fi = DB_FundInfo.getFundInfosByTypeDotName(fundTypeAndName);
        if(fi == null) {
            iwd.println("Could not find the fund in DB: " + fundTypeAndName);
            return iwd.getString();
        }
        iwd.println("Funds read from DB, printing it");
        iwd.push();
        fi.dumpInfo(iwd);
        iwd.pop();

        List<D_FundInfo> fiToExtract = new ArrayList<>();
        fiToExtract.add(fi);
        iwd.println("Now initiating extraction");
        FLOps1_Ext1_Extract_New extract = new FLOps1_Ext1_Extract_New(
                fiToExtract,
                true,
                false,
                45,
                iwd);
        extract.doIt();
        return iwd.getString();
    }

    //------------------------------------------------------------------------
    public static String jsp_Report_LastFriday(HttpServletResponse resp) throws Exception {
//        OutputStream out = resp.getOutputStream();
        String now = MM.getNowAs_YYMMDD(null);
        String fridayLast = MM.tgif_getLastFridayTodayExcl(now);
        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();
        IndentWriter iw = new IndentWriter();
        iw.generateHTML();
        String debug = ExtractStatistics.getExtractSummary(
                iw,
                -1,
                -1,
                fridayLast,
                fis);
        String s = iw.getString().replace("&", "&amp;");
        return "*** Report for friday: " + fridayLast + "<br>"
                + s
                + "<br>*** DEBUG" + debug;
    }

    public static String validateFunds(String type) throws IOException {
        initialize();
        List<D_FundInfo> fis = null;

        log.info("Now in validateFunds");
        FundList_Validator v = null;
        if (type.equals(D_FundInfo.TYPE_SEB)) {
            log.info("...it's SEB");
            byte[] data = GCSWrapper.gcsReadFile(Constants.FUNDLIST_SEB);
            String fundListFile = new String(data, Constants.ENCODING_FILE_READ);
            v = new FundList_Validator(fundListFile, DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_SEB));
            v.process_SEB();
        }
        else if (type.equals(D_FundInfo.TYPE_PPM)) {
            log.info("...it's PPM");
            byte[] data = GCSWrapper.gcsReadFile(Constants.FUNDLIST_PPM);
            String fundListFile = new String(data, Constants.ENCODING_FILE_READ);
            v = new FundList_Validator(fundListFile, DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_PPM));
            v.process_PPM();
        }
        else if (type.equals(D_FundInfo.TYPE_VANGUARD)) {
            throw new IOException("Validating: " + type + ", is not supported yet");
        }
        else if (type.equals(D_FundInfo.TYPE_SPP)) {
            throw new IOException("Validating: " + type + ", is not supported yet");
        }
        else {
            throw new IOException("Unknown fund type: " + type);
        }

        IndentWriter iw = new IndentWriter();

        iw.println("<br>");
        iw.println("In List but not in DB<br>");
        for (int i=0; i < v._fiInListButNotDB.size(); i++) {
            OTuple2G<String, String> e = v._fiInListButNotDB.get(i);
            iw.println("\"" + e._o1 + "\",\"" + e._o2 + "\",<br>");
        }
        iw.println("...Total of: " + v._fiInListButNotDB.size()+ "<br>");

        iw.println("<br>");
        iw.println("In DB but not in List<br>");
        for (int i=0; i < v._fiInDBButNotList.size(); i++) {
            iw.println("\"" + v._fiInDBButNotList.get(i).getNameOrig()
                    + "\",\"" + v._fiInDBButNotList.get(i)._url
                    + "\",<br>");
        }
        iw.println("...Total of: " + v._fiInDBButNotList.size() + "<br>");

        iw.println("<br>");
        iw.println("*** Error: Name match, URL mismatch<br>");
        for (int i=0; i < v._fiNameMatchURLMismatch.size(); i++) {
            OTuple2G<String, String> e = v._fiNameMatchURLMismatch.get(i);
            iw.println("[" + i + "] List name: " + e._o1 + ", url: " + e._o2 + "<br>");
            D_FundInfo fi = DB_FundInfo.getFundInfosByTypeAndName(type, e._o1, false);
            iw.println("...DB name: " + fi.getNameOrig() + ", url: " + fi._url + "<br>");
        }

        iw.println("<br>");
        iw.println("*** Error: URL match, Name mismatch<br>");
        for (int i=0; i < v._fiURLMatchNameMismatch.size(); i++) {
            OTuple2G<String, String> e = v._fiURLMatchNameMismatch.get(i);
            iw.println("[" + i + "] List name: " + e._o1 + ", url: " + e._o2 + "<br>");
            D_FundInfo fi = DB_FundInfo.getFundInfosByTypeAndURL(type, e._o2);
            iw.println("...DB name: " + fi.getNameOrig() + ", url: " + fi._url + "<br>");
        }

        iw.println("<br>");
        iw.println("All Lists<br>");
        for (int i=0; i < v._nameAndURLList.size(); i++) {
            OTuple2G<String, String> e = v._nameAndURLList.get(i);
            iw.println("[" + i + "] List name: " + e._o1 + ", url: " + e._o2 + "<br>");
        }

        iw.println("<br>");
        iw.println("All FIS<br>");
        for (int i=0; i < v._fis.size(); i++) {
            iw.println("[" + i + "] DB name: "
                    + v._fis.get(i).getNameOrig()
                    + ", ms: " + v._fis.get(i).getNameMS()
                    + ", url: " + v._fis.get(i)._url
                    + "<br>");
        }

        return iw.getString();
    }

    public static void writeFundInfo(byte[] data) throws IOException {
        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
    }

    public static String fundsDisplayAll() {
        List<D_FundInfo> fisSEB = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_SEB);
        List<D_FundInfo> fisSPP = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_SPP);
        List<D_FundInfo> fisVGD = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_VANGUARD);
        List<D_FundInfo> fisPPM = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_PPM);

        StringBuffer sb = new StringBuffer();
        sb.append("<h2>Total SEB: " + fisSEB.size() + "</h2><br>");
        sb.append("<h2>Total SPP: " + fisSPP.size() + "</h2><br>");
        sb.append("<h2>Total VGD: " + fisVGD.size() + "</h2><br>");
        sb.append("<h2>Total PPM: " + fisPPM.size() + "</h2><br>");

        sb.append("<h2>Fund list for SEB</h2><br>");
        printFundType(sb, fisSEB);
        sb.append("<h2>Fund list for SPP</h2><br>");
        printFundType(sb, fisSPP);
        sb.append("<h2>Fund list for VGD</h2><br>");
        printFundType(sb, fisVGD);
        sb.append("<h2>Fund list for PPM</h2><br>");
        printFundType(sb, fisPPM);
        return sb.toString();
    }
    private static void printFundType(StringBuffer sb, List<D_FundInfo> l) {
        for (D_FundInfo fi: l) {
            String dpdStr = "-";
            if (fi._dpDays.size() > 0) {
                D_FundDPDay dpd = fi._dpDays.get(0);
                dpdStr = dpd.toString();
            }
            sb.append(fi.getNameMS() + ", " + fi.getNameOrig() + ", " + fi._url + ", " + dpdStr + "<br>");
            sb.append("..." + fi.getTypeAndNameURLEncoded() + "<br>");
        }
    }

    private static List<D_FundDPDay> getDPWeeks(
            String fundName,
            List<String> dates,
            List<D_FundDPDay> dpws) throws Exception {
        
        List<D_FundDPDay> r = new ArrayList<>();
        for (int i=0; i < dates.size(); i++) {
            String date = dates.get(i);
            boolean found = false;
            for (int j=0; j < dpws.size(); j++) {
                D_FundDPDay fdpw = dpws.get(j);
                if (!MM.tgif_isFriday(fdpw._dateYYMMDD)) {
                    log.severe("For fund: " + fundName + ", dpw: " + fdpw._dateYYMMDD + ", is not a friday");
                } else if (date.equals(fdpw._dateYYMMDD)) {
                    r.add(fdpw);
                    found = true;
                }
            }
            if (!found) {
                r.add(null);
            }
        }

        return r;
    }

    private static List<String> generateDPWeekDates(int count, List<D_FundInfo> l) throws Exception {
        List<String> dates = new ArrayList<>();
        String fridayBefore = MM.tgif_getLastFridayTodayIncl(MM.getNowAs_YYMMDD(null));
        dates.add(fridayBefore);
        for (int i=1; i < count; i++) {
            fridayBefore = MM.tgif_getLastFridayTodayExcl(fridayBefore);
            dates.add(fridayBefore);
        }
        return dates;
    }

    public static final String WT_FILTER_TYPE = "TYPE";
    public static final String WT_FILTER_INDEX = "INDEX";
    public static final String WT_FILTER_FUND = "FUND";
    public static String fundReport_WeeklyTable(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String paramType = request.getParameter(JSP_Constants.PARAM_TYPE);
        String paramId = request.getParameter(JSP_Constants.PARAM_ID);
        if (paramType == null || paramType.trim().length() == 0) {
            throw new Exception(JSP_Constants.PARAM_TYPE + " not set");
        }
        paramType = paramType.trim();
        if (paramId == null || paramId.trim().length() == 0) {
            throw new Exception(JSP_Constants.PARAM_ID + " not set");
        }
        paramId = paramId.trim();

        List<D_FundInfo> list = new ArrayList<>();
        String[] column1 = null;
        List<String> dates = null;
        if (paramType.equals(WT_FILTER_TYPE)) {
            log.info("Getting funds of type: " + paramId);
            list = DB_FundInfo.getFundInfosByType(paramId);
            log.info("Number of funds: " + list.size());
            dates = generateDPWeekDates(JSP_Constants.CACHED_DPS, list);
        } else if (paramType.equals(WT_FILTER_INDEX)) {
            list = DB_FundInfo.getFundInfosByIndex(paramId);
            dates = generateDPWeekDates(JSP_Constants.CACHED_DPS, list);
        } else if (paramType.equals(WT_FILTER_FUND)) {
            throw new IOException("Not Converted to new code");
//            list = FLA_Cache.cacheFundInfoById(paramId);
//            dates = generateDPWeekDates(FLA_Cache_FundInfo.CACHED_DPS, list);
        }

        StringBuilder s = new StringBuilder();
        s.append("Filter type: " + paramType);
        s.append("&nbsp;");
        s.append("Filter id: " + paramId);

        s.append("<div class=\"selectList3\">");
        s.append("<table>"); // width=\"1200\">");

        fundReport_WeeklyTable_Headers(s, dates);

        log.info("Date count: " + dates.size());
//        List<OTuple2G<D_FundInfo, List<Double>>> rfull =
//                JSP_Helper_DataSorter.getCachedFundInfoWithinDates(
//                        dates,
//                        list);
//        log.info("Full data set: " + rfull.size());
//        rarg = JSP_Helper_DataSorter.convertToTableRows(rfull);

        fundReport_WeeklyTable_Rows(s, "All funds",null, dates, list);

        s.append("</table>");
        s.append("</div>");
        return s.toString();
    }

    /**
     *
     */
    public static int fundReport_WeeklyTable_Headers(
            StringBuilder s,
            List<String> dates) throws Exception {
        int countColumns = 1;
        s.append("<tr>");
        s.append("<th align=\"left\" width=\"400\">Name</th>");
        for (int i = 0; i < dates.size(); i++) {
            s.append("<th align=\"right\">" + dates.get(i).substring(2) + "</th>");
            countColumns++;
        }
        s.append("</tr>");
        return countColumns;
    }

    /**
     *
     */
    public static void fundReport_WeeklyTable_Rows(
            StringBuilder s,
            String titleRow,
            String[] column1,
            List<String> dates,
            List<D_FundInfo> cfis) throws Exception {
        if (titleRow != null) {
            s.append("<tr>");
            s.append("<td align=\"left\" colspan=\"2\">");
            s.append("<b>" + titleRow + "</b>");
            s.append("</td>");
            s.append("</tr>");
        }

        log.info("fundReport_WeeklyTable_Rows, dates: " + dates.size() + ", fis: " + cfis.size());
        for (int i = 0; i < cfis.size(); i++) {
            s.append("<tr>");
            D_FundInfo cfi = cfis.get(i);

            // Type, name and index
            String nameMSURL = "JSP_DisplayFundDB.jsp?" + JSP_Constants.PARAM2_TYPEDOTNAME_ + "=" + URLEncoder.encode(cfi.getTypeAndName(), "UTF-8");
            s.append("<td align=\"left\">");
//            s.append("<a href=\"" + cfi._url + "\" target=\"_blank\">" + cfi._type + ":</a>"
//                    + "&nbsp;"
//                    + "<a href=\"" + nameMSURL + "\" target=\"_blank\">" + cfi._nameMS + "</a>");
            s.append("<a href=\"" + nameMSURL + "\" target=\"_blank\">"
                    + cfi._type
                    + ":&nbsp;"
                    + cfi.getNameMS() + "</a>");
            if (cfi._ppmNumber != null && cfi._ppmNumber.trim().length() > 0) {
                s.append("&nbsp;(" + String.valueOf(cfi._ppmNumber) + ")");
            }
            String indexName = "-";
            if (cfi.getIndexName() == null) {
                log.info(cfi._type + "." + cfi.getNameMS() + ": Had null as index");
                s.append("&nbsp;(-)");
            } else {
                indexName = cfi.getIndexName();
                if (indexName == null || indexName.trim().equals("-")) {
                    s.append("&nbsp;(-)");
                } else {
                    String fundIndexEnc = URLEncoder.encode(indexName, "UTF-8");
                    String url = "JSP_Report02_Weekly_Display.jsp"
                            + "?" + JSP_Constants.PARAM_TYPE + "=" + WT_FILTER_INDEX
                            + "&" + JSP_Constants.PARAM_ID + "=" + fundIndexEnc;
                    s.append("&nbsp;(" + "<a href=\"" + url + "\" target=\"_blank\">index</a>)");
                }
            }
            s.append("</td>");

            // Get the weeks
            List<D_FundDPDay> dpws = null;
            if (cfi != null) {
                dpws = cfi._dpDays;
                dpws = getDPWeeks(cfi._type + "." + cfi.getNameMS(), dates, dpws);
            }

            // Monthlys
            if (dpws != null) {
                int index = 0;
                while (index + 4 <= dpws.size()) {
                    String r1m = "-";
                    if (dpws.get(index) != null && dpws.get(index + 1) != null && dpws.get(index + 2) != null && dpws.get(index + 3) != null) {
                        Double[] ds = {null, null, null, null};
                        if (dpws.get(index)._r1w != D_FundDPDay.FLOAT_NULL) {
                            ds[0] = new Double(dpws.get(index)._r1w);
                        }
                        if (dpws.get(index+1)._r1w != D_FundDPDay.FLOAT_NULL) {
                            ds[1] = new Double(dpws.get(index+1)._r1w);
                        }
                        if (dpws.get(index+2)._r1w != D_FundDPDay.FLOAT_NULL) {
                            ds[2] = new Double(dpws.get(index+2)._r1w);
                        }
                        if (dpws.get(index+3)._r1w != D_FundDPDay.FLOAT_NULL) {
                            ds[3] = new Double(dpws.get(index+3)._r1w);
                        }
                        r1m = getR1M(ds);
                    }
                    index += 4;
                    s.append("<td align=\"right\">" + r1m + "</td>");
                    s.append("<td align=\"right\">" + "</td>");
                    s.append("<td align=\"right\">" + "</td>");
                    s.append("<td align=\"right\">" + "</td>");
                }
                s.append("</tr>");
            }

            // Weeklys
            s.append("<tr>");


            // Then the weekly columns
            s.append("<td align=\"right\"></td>");  // The magic 1st column which is not used anymore
            if (dpws != null) {
                for (int j = 0; j < dpws.size(); j++) {
                    D_FundDPDay cfw = dpws.get(j);
                    if (cfw != null && cfw._r1w != D_FundDPDay.FLOAT_NULL) {
                        s.append("<td align=\"right\">" + cfw._r1w + "</td>");
                    } else {
                        s.append("<td align=\"right\">-</td>");
                    }
                }
            }
            s.append("</tr>");

            // Create an empty row between each fund for readability
            s.append("<tr><td>&nbsp;</td></tr>");
        }
    }

    public static String getR1M(Double[] ds) {
        if (ds[0] == null || ds[1] == null || ds[2] == null || ds[3] == null) {
            return "-";
        }

        double r = 1.0D;
        for (int i=ds.length-1; i >= 0; i--) {
            Double d = ds[i];
            if (d.doubleValue() > 0.0D) {
                r *= (1.0D + d.doubleValue() / 100.0D);
            } else if (d.doubleValue() < 0.0D) {
                r *= (1.0D + d.doubleValue() / 100.0D);
            }
        }

        double delta = r - 1.0D;
        double newr = delta / 1.0D;
        newr *= 100;
        return String.format("%.1f", newr);
    }

}

