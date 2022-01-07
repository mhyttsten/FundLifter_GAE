package com.pf.fl.shared.fund_db_update;

import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.DB_FundInfo;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.datamodel.D_FundInfo_Validator;
import com.pf.fl.shared.extract.ExtractFromHTML_Helper;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Get_1_VBO_Funds {

    // Manual
    // - Create /tmp2/
    // - Download latest fundinfo-db-master.bin into this directory
    // - Use below URLs and do "Save As" on the HTML content in this directory
    //   Each file must be prefiexed VGD_
    //   Vanguard funds must contain Vanguard in filename
    // - Execute this program

    // List of Vanguard Mutual Funds (first entry: 500 Index Admiral Shares)
    //   https://investor.vanguard.com/mutual-funds/list#/mutual-funds/name/month-end-returns
    // List of Vanguard ETF (first entry: Communication Services ETF)
    //   https://investor.vanguard.com/mutual-funds/list#/etf/name/month-end-returns
    // List of non-Vanguard funds
    //
    // Aberdeen
    // BlackRock
    // Fidelity
    // Goldman Sachs
    // HSBC
    // MotelyFool
    // Morgan Stanley
    // Pimco
    // Primecap
    // T. Rowe

    /*
        // Vanguard
           https://investor.vanguard.com/mutual-funds/list#/etf/name/month-end-returns
           https://investor.vanguard.com/mutual-funds/list#/mutual-funds/name/month-end-returns
        // Aberdeen
           https://personal.vanguard.com/us/funds/other/bytype?FundFamilyId=31698

    */
/*
    //------------------------------------------------------------------------
    public static void main(String[] args) {
        try {
            mainImpl(args);
        } catch(Exception exc) {
            System.out.println(exc);
            exc.printStackTrace();
        }
    }

    //------------------------------------------------------------------------
    public static final String DIR = "/Users/magnushyttsten/Desktop/Vanguard";
    public static final String FILE_PREFIX = "VGD_";
    public static final String DB_FILENAME = "fundinfo-db-master.bin";

    //------------------------------------------------------------------------
    public static void mainImpl(String[] args) throws Exception {

        // Get all the files to analyze
        List<File> tmpList = new ArrayList<>();
        String error = MM.fileList(tmpList, DIR, false);
        if (error != null) {
            System.out.println("Error: " + error);
            return;
        }
        ArrayList<File> fileList = new ArrayList<>();
        for (File f: tmpList) {
            if (f.getName().startsWith(FILE_PREFIX)) {
                fileList.add(f);
                System.out.println("Adding: " + f.getName() + ", for analysis");
            }
        }

        // Get fund database
        byte[] fileDBDataBA = MM.fileReadFrom(DIR + File.separator + DB_FILENAME);
        DB_FundInfo.initialize(fileDBDataBA, true);

        // Go through each files HTML content
        List<D_FundInfo> fisFiles = new ArrayList<>();
        List<D_FundInfo> fisFilesTestExtraction = new ArrayList<>();
        for (File f: fileList) {
            System.out.println("Now processing HTML from: " + f.getName());
            byte[] fileDataBA = MM.fileReadFrom(f);
            String fileDataStr = new String(fileDataBA , "Windows-1252");
            boolean isVanguard = false;
            if (f.getName().contains("Vanguard")) {
                isVanguard = true;
            }
            List<D_FundInfo> fis = doItImpl(null, fileDataStr, isVanguard);
            if (fis == null) {
                System.out.println("...error, null was returned");
                return;
            } else {
                System.out.println("...success, found: " + fis.size() + " number of funds");
            }
            if (fis.size() > 0) {
                fisFilesTestExtraction.add(fis.get(0));
            }
            fisFiles.addAll(fis);
        }
        System.out.println("Processed all " + fileList.size() + " files");

        // Try to successfully extract
        System.out.println("Testing extraction for 1 fund in every file");
        for (D_FundInfo fi: fisFilesTestExtraction) {
            System.out.println("...Extracting fund: " + fi.getTypeAndName());
            IndentWriter iwd = new IndentWriter();
            ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
            int errorCode = eh.extractFundDetails(fi, iwd);
            if (errorCode != ExtractFromHTML_Helper.RC_SUCCESS) {
                System.out.println("*** Error extracting fund: " + errorCode + ", " + fi.getTypeAndName());
                System.out.println(iwd.getString());
                return;
            }
            System.out.println("...successfully extracted: " + fi.getOneLiner());
        }

        // Checking which in Files were new/already existed in DB
        System.out.println("Now checking for deviation against Fund DB");
        List<D_FundInfo> fisToAdd = new ArrayList<>();
        List<D_FundInfo> fisDB = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_VANGUARD);
        int countNameMismatch = 0;
        int countNewFunds = 0;
        for (D_FundInfo fi: fisFiles) {
            D_FundInfo name = DB_FundInfo.getFundInfosByTypeAndName(fi._type, fi._nameOrig, true);
            D_FundInfo url =  DB_FundInfo.getFundInfosByTypeAndURL(fi._type, fi._url);
            if (name != null || url != null) {
                if (name == null) {
                    url._nameOrig = fi._nameOrig;
                    url._nameMS = fi._nameMS;
                    countNameMismatch++;
//                    System.out.println("Found URL duplicate: " + fi.getTypeAndName());
//                    System.out.println("...DB:   [" + url._nameOrig + ", " + url._nameMS + "]");
//                    System.out.println("...File: [" + fi._nameOrig + ", " + fi._nameMS + "]");
                } else if (url == null) {
                    System.out.println("Dup on NAME: " + fi.getTypeAndName());
                } else {
                    System.out.println("Dup on NAME&URL: " + fi.getTypeAndName());
                }
            } else {
                countNewFunds++;
                System.out.println("New in files: " + fi.getTypeAndName());
                fisToAdd.add(fi);
            }
        }
        DB_FundInfo.addFundInfo(fisToAdd);
        System.out.println("Total in DB: " + fisDB.size());
        System.out.println("Total in files: " + fisFiles.size());
        System.out.println("...Name updated in DB (URL match): " + countNameMismatch);
        System.out.println("...New funds found in files: " + countNewFunds);

        // Checking which in DB did not have an entry in file
        System.out.println("Check which are in DB but not in files");
        int countUniqueInDB = 0;
        for (D_FundInfo fiDB: fisDB) {
            boolean found = false;
            for (D_FundInfo fiFi: fisFiles) {
                if (fiDB._url.equals(fiFi._url)) {
                    found = true;
                }
            }
            if (!found) {
                countUniqueInDB++;
                System.out.println("...unique in DB: " + fiDB.getTypeAndName() + ": " + MM.getString(fiDB.getDPDOneLiner(), 40));
            }
        }
        System.out.println("Count unique in DB: " + countUniqueInDB);

        // Writing updated DB
        D_FundInfo_Validator fiv = new D_FundInfo_Validator(DB_FundInfo.getAllFundInfos());
        fiv.process();
        if (!fiv._error) {
            System.out.println("Updated fund data is valid");
        } else {
            System.out.println("*** ERROR ****: Fund database in invalid, will not initiate extraction\n" + fiv._iwErrors.getString());
            return;
        }
        System.out.println("Now writing updated DB");
        byte[] dataToWrite = DB_FundInfo.crunch();
        MM.fileWrite(DIR + File.separator + DB_FILENAME + ".updated", dataToWrite);
    }

    //------------------------------------------------------------------------
    private static List<D_FundInfo> doItImpl(IndentWriter iw, String html, boolean isVanguard) throws Exception {
        if (iw == null) iw = new IndentWriter();

        List<D_FundInfo> l = new ArrayList<>();

        int count = 0;
        boolean first = true;
        while (true) {
            int io1 = -1;
            int io2 = -1;
            String stag = null;
            String etag = null;

            // Start of fund entry
            stag = "<a href=\"https://personal.vanguard.com/us/funds/snapshot?";
                 // <a href=\"https://personal.vanguard.com/us/funds/snapshot?FundId=Y914&amp;FundIntExt=EXT">

            io1 = html.indexOf(stag);
            if (io1 == -1) {
                System.out.println("No more <a href, total count: " + count);
                return l;
            }
            html = html.substring(io1);

            if (first) {
                first = false;
                stag = "</table>";
                io1 = html.indexOf(stag);
                if (io1 == -1) {
                    System.out.println("Could not find </table>");
                    return null;
                }
                html = html.substring(0, io1);
            }

            // Name
            stag = ">";
            etag = "</a>";
            io1 = html.indexOf(stag);
            io2 = html.indexOf(etag);
            if (io1 == -1 || io2 == -1) {
                System.out.println("Could not find start of name: " + io1 + ", or end tag: " + io2);
                return null;
            }
            String name = html.substring(io1 + stag.length(), io2);
            name = name.replace("<span>", "");
            name = name.replace("</span>", "");
            html = html.substring(io2+etag.length());

            // Ticker
            if (isVanguard) {
                stag = "class=\"ng-binding\">";
                io1 = html.indexOf(stag);
                html = html.substring(io1+stag.length());
            } else {
                io1 = html.indexOf("<td");
                html = html.substring(io1);
                io1 = html.indexOf(">");
                html = html.substring(io1+1);
            }
            if (io1 == -1) {
                System.out.println("Could not find start ticker name");
                return null;
            }
            stag = "</td>";
            io1 = html.indexOf(stag);
            if (io1 == -1) {
                System.out.println("Could not find end of ticker");
                return null;
            }
            String ticker = html.substring(0, io1);
            if (ticker.length() > 5) {
                System.out.println("Ticker did not have length 5: " + ticker);
                return null;
            }

            String url = url_getVanguard(name, ticker);
            D_FundInfo fi = new D_FundInfo();
            fi._type = D_FundInfo.TYPE_VANGUARD;
            fi._nameOrig = name + " (" + ticker + ")";
            fi._nameMS = fi._nameOrig;
            fi._url = url;
            fi._msRating = -1;
            fi._ppmNumber = "";

            String today = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
            String lfriday = MM.tgif_getLastFridayTodayExcl(today);

            fi._dateYYMMDD_Updated = lfriday;
            fi._dateYYMMDD_Update_Attempted = lfriday;
            l.add(fi);

            // System.out.println("Found: " + fi.getTypeAndName() + ", url: " + fi._url);
            count++;
        }
    }

    //--------------------------------------------------------------
    public static final String url_getVanguard(String name, String ticker) {
//        int tEnd = name.lastIndexOf(")");
//        int tStart = name.lastIndexOf("(");
//        if (tEnd == -1 || tStart == -1) {
//            return null;
//        }
//        String ticker = name.substring(tStart+1, tEnd);

        String vgURL = null;
        if (name.toLowerCase().contains("etf")) {
            vgURL = "https://performance.morningstar.com/perform/Performance/etf/trailing-total-returns.action?"
                    + "&t=" + ticker
                    + "&region=usa&culture=en-US&cur=&ops=clear&s=0P00001MJB&ndec=2&ep=true&align=d&annlz=true&comparisonRemove=false&loccat=&taxadj=&benchmarkSecId=&benchmarktype=";
        } else {
            vgURL = "https://performance.morningstar.com/perform/Performance/fund/trailing-total-returns.action?&t=XNAS:"
                    + ticker
                    + "&region=usa&culture=en-US&cur=&ops=clear&s=0P00001MJB&ndec=2&ep=true&align=d&annlz=true&comparisonRemove=false&loccat=&taxadj=&benchmarkSecId=&benchmarktype=";
        }
        return vgURL;
    }
*/
}
/*
ETF
https://performance.morningstar.com/perform/Performance/etf/trailing-total-returns.action?&t=TICKER&region=usa&culture=en-US&cur=&ops=clear&s=0P00001MJB&ndec=2&ep=true&align=d&annlz=true&comparisonRemove=false&loccat=&taxadj=&benchmarkSecId=&benchmarktype=

Mutual fund
https://performance.morningstar.com/perform/Performance/fund/trailing-total-returns.action?&t=XNAS:TICKER&region=usa&culture=en-US&cur=&ops=clear&s=0P00001MJB&ndec=2&ep=true&align=d&annlz=true&comparisonRemove=false&loccat=&taxadj=&benchmarkSecId=&benchmarktype=
*/
