package com.pf.fl.shared.fund_db_update;

import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.DB_FundInfo;
import com.pf.fl.shared.datamodel.D_FundDPYear;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.datamodel.D_FundInfo_Validator;
import com.pf.fl.shared.extract.ExtractFromHTML_Helper;
import com.pf.fl.shared.extract.ExtractFromHTML_Morningstar;
import com.pf.fl.shared.extract.ExtractFromHTML_Vanguard;
import com.pf.fl.shared.utils.HtmlRetriever;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.naming.ldap.ExtendedRequest;

public class Main20331Converter {

    //------------------------------------------------------------------------
    public static void main(String[] args) {
        try {
            mainImpl(args);
        } catch(Exception exc) {
            exc.printStackTrace();
            System.out.println("Exception: " + exc);
        }

    }
    public static void mainImpl(String[] args) throws Exception {

//        String url = "https://www.morningstar.se/se/funds/snapshot/snapshot.aspx?id=F0GBR04M5F";
//        String urlHistory = url + "&tab=1";
//        IndentWriter iwd = new IndentWriter();
//        byte[] rm = null;
//        byte[] rh = null;

//        if (true) {
//            rm = HtmlRetriever.htmlGet(iwd, url, 5000, 4);
//            System.out.println("Main bytes fetched: " + (rm == null ? "NULL" : rm.length));
//            MM.fileWrite("/tmp/fl/fl_main", rm);
//            rh = HtmlRetriever.htmlGet(iwd, urlHistory, 5000, 4);
//            System.out.println("History bytes fetched: " + (rh == null ? "NULL" : rh.length));
//            MM.fileWrite("/tmp/fl/fl_history", rh);
//        }
//        rm = MM.fileReadFrom("/tmp/fl/fl_main");
//        System.out.println("Main bytes read: " + (rm == null ? "NULL" : rm.length));
//        rh = MM.fileReadFrom("/tmp/fl/fl_history");
//        System.out.println("History bytes read: " + (rh == null ? "NULL" : rh.length));

//        String str = MM.newString(rm, Constants.ENCODING_FILE_READ);
//        String dataMain = MM.stripHTMLComments(str);
//        str =  MM.newString(rh, Constants.ENCODING_FILE_READ);
//        String dataHistory = MM.stripHTMLComments(str);

        byte[] fileDBDataBA = MM.fileReadFrom("/Users/magnushyttsten/flupdate/fundinfo-db-master.bin");
        if (fileDBDataBA == null || fileDBDataBA.length <= 0) {
            System.out.println("Could not find fund DB");
            return;
        }
        DB_FundInfo.initialize(fileDBDataBA, true);
        D_FundInfo_Validator fiv = new D_FundInfo_Validator(DB_FundInfo.getAllFundInfos());
        fiv.process();
        if (fiv._error) {
            System.out.println("*** ERROR ****: Fund database in invalid, will not initiate extraction\n" + fiv._iwErrors.getString());
            return;
        }
        System.out.println("Initialized from a valid fund DB file, number of bytes: " + fileDBDataBA.length);
        System.out.println("Total funds (before updates from files): " + DB_FundInfo.getAllFundInfos().size());

        List<D_FundInfo> l = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_PPM);
//        List<D_FundInfo> l = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_VANGUARD);
        Collections.sort(l, new Comparator<D_FundInfo>() {
            @Override
            public int compare(D_FundInfo f1, D_FundInfo f2) {
                return f1.getTypeAndName().compareTo(f2.getTypeAndName());
            }
        });

        String dateNow_YYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        String dateLastFriday_YYMMDD   = MM.tgif_getLastFridayTodayExcl(dateNow_YYMMDD);
        for (int i = 0; i < l.size(); i++) {

            D_FundInfo fi = l.get(i);

//            if (i < 570) { continue; }
//            if (i > 5) { break; }
            if (!fi.getNameMS().startsWith("AMF Aktiefond Asien Stilla havet")) { continue; }

            System.out.println("\n[" + i + "]: " + fi.getTypeAndName() + ": " + fi._url);
            boolean debug = true;
//            boolean debug = false;

            IndentWriter iwb = new IndentWriter();
            if (debug) {
                iwb.println("****************");
                iwb.println("Before");
                fi.dumpInfo(iwb);
                iwb.println("****************");
                iwb.println("Retrieve debug information");
            }

            ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
            int rc = eh.extractFundDetails(fi, iwb);
//        int rc = ExtractFromHTML_Morningstar.extractFundDetails(iwb, l.get(0));
//        int rc = ExtractFromHTML_Vanguard.extractFundDetails(iwb, l.get(0));

            if (debug) {
                iwb.println("****************");
                iwb.println("After");
                fi.dumpInfo(iwb);
                System.out.println(iwb.toString());
                System.out.println("RC was: " + rc);
            }

            if (rc == ExtractFromHTML_Helper.RC_SUCCESS) {
                System.out.print("...SUCCESS: " + fi._dpDays.get(0).toString());
                if (fi._dpDays.size() == 0 || !fi._dpDays.get(0)._dateYYMMDD.equals(dateLastFriday_YYMMDD)) {
                    System.out.println("...*** ERROR ***: Last DPDay not last Friday");
                }
            } else if (rc == ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND) {
                System.out.println("...NO_DPDAY: " + fi._url + "&tab=1");
            } else if (rc == ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED) {
                System.out.println("...DATA UPDATED: " + fi._url);
            } else if (rc == ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND) {
                System.out.println("...*** ERROR ***: " + fi._url);
            }

            if (fi._dpYears.size() == 0) {
                System.out.println("   NO DP Years");
            }
            for (D_FundDPYear fy: fi._dpYears) {
                System.out.println("   " + fy.toString());
            }

            Random r = new Random();
            int min = 1;
            int max = 5;
            int sleepTime = r.nextInt((max - min) + 1) + min;
            MM.sleepInMS(sleepTime*1000);
        }
    }
}
