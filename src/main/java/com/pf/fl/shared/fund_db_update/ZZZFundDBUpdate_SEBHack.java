package com.pf.fl.shared.fund_db_update;

import com.pf.fl.shared.datamodel.DB_FundInfo;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.datamodel.D_FundInfo_Validator;
import com.pf.fl.shared.extract.ExtractFromHTML_Helper;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;
import com.pf.fl.shared.utils.OTuple2G;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZZZFundDBUpdate_SEBHack {

    /* Manual
     1. Create /tmp/funddbupdate and make sure it is empty

     2. Put your fund TSV, and HTML files here as per below (do one type at a time).

     a. SEB and SPP
        Update their spreadsheets
        Then download as TSV
        Filenames should be: seb_<index>.tsv, spp_<index>.tsv

     b. PPM:
        Go to: https://www.morningstar.se/Funds/Quickrank.aspx?ppm=on&view=overview
        Order by name
        Right click on each page table and do save as to filename: ppm<idx>.[something htm/html]
        Move these files to /tmp/funddbupdate

     c. Vanguard is HTML
        [Important note: Vanguards MS name is never updated while extracting, so name is what gets set in import]
        Go to each table on their HTML page and do 'Save As'
        Use filenames:
           vgd_Vanguard_<index>.html  // For Vanguard funds, important it contains 'Vanguard'
           vgd_Aberdeen_<index>.html  // For non-Vanguard funds
           *** OBS: Important that non-Vanguard funds do not contain 'Vanguard' in filename
       List of Vanguard Mutual Funds (first entry: 500 Index Admiral Shares)
          https://investor.vanguard.com/mutual-funds/list#/mutual-funds/name/month-end-returns
       List of Vanguard ETF (first entry: Communication Services ETF)
          https://investor.vanguard.com/mutual-funds/list#/etf/name/month-end-returns
       List of non-Vanguard funds: See "Select a mutual fund company"
          https://investor.vanguard.com/other-funds/
          Then use "Fund Family" selector at top right of list area
       Aberdeen:      https://personal.vanguard.com/us/funds/other/bytype?FundFamilyId=31698
       BlackRock:     https://personal.vanguard.com/us/funds/other/bytype?FundFamilyId=6371
       Fidelity:      https://personal.vanguard.com/us/funds/other/bytype?FundFamilyId=6082
       Goldman Sachs: https://personal.vanguard.com/us/funds/other/bytype?FundFamilyId=6341
       HSBC:          https://personal.vanguard.com/us/funds/other/bytype?FundFamilyId=6452
       Morgan Stanley https://personal.vanguard.com/us/funds/other/bytype?FundFamilyId=6383
       MotelyFool:    https://personal.vanguard.com/us/funds/other/bytype?FundFamilyId=42698
       Pimco          https://personal.vanguard.com/us/funds/other/bytype?FundFamilyId=31816
       Primecap       https://personal.vanguard.com/us/funds/other/bytype?FundFamilyId=225092
       T. Rowe

     3. Download latest fundinfo-db-master.bin into this directory
        https://console.firebase.google.com/project/ql-magnushyttsten/storage/ql-magnushyttsten.appspot.com/files~2Fbackend~2F
    */

    //------------------------------------------------------------------------
    public static void main(String[] args) {
        try {
            mainImpl(args);
        } catch (Exception exc) {
            System.out.println(exc);
            exc.printStackTrace();
        }
    }

    //------------------------------------------------------------------------
    public static final String DIR = "/Users/magnushyttsten/flupdate";
    public static final String DB_ABSOLUTE_FILENAME = DIR + "/fundinfo-db-master.bin";
    public static final String FILE_TYPE_SEB = "seb_";
    public static final String FILE_TYPE_SPP = "spp_";
    public static final String FILE_TYPE_VGD_VANGUARD = "vgd_vanguard";
    public static final String FILE_TYPE_VGD = "vgd_";
    public static final String FILE_TYPE_PPM = "ppm_";

    //------------------------------------------------------------------------
    public static void mainImpl(String[] args) throws Exception {
//        doit(false,  D_FundInfo.TYPE_SEB, FILE_TYPE_SEB);
//        doit(true, D_FundInfo.TYPE_SPP, FILE_TYPE_SPP);
        doit(false, D_FundInfo.TYPE_PPM, FILE_TYPE_PPM);
//        doit(false, D_FundInfo.TYPE_VANGUARD, FILE_TYPE_VGD);
    }

    //------------------------------------------------------------------------
    public static void doit(boolean testHTMLExtraction,
                            String type,
                            String filePrefix) throws Exception {

        // Get fund database
        byte[] fileDBDataBA = MM.fileReadFrom(DB_ABSOLUTE_FILENAME);
        if (fileDBDataBA == null || fileDBDataBA.length <= 0) {
            throw new Exception("Could not find: " + DB_ABSOLUTE_FILENAME);
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
        System.out.println("...PPM: " + DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_PPM).size());
        System.out.println("...SEB: " + DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_SEB).size());
        System.out.println("...SPP: " + DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_SPP).size());
        System.out.println("...VGD: " + DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_VANGUARD).size());

        // Get all the files to analyze
        List<File> tmpList = new ArrayList<>();
        List<File> fileList = new ArrayList<>();
        String error = MM.fileList(tmpList, DIR, false);
        if (error != null) {
            System.out.println("Error reading files: " + error);
            return;
        }
        for (File f : tmpList) {
            if (!f.getName().toLowerCase().startsWith(filePrefix)) {
                continue;
            }
            fileList.add(f);
        }
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });
        for (File f: fileList) {
            System.out.println("Adding file for process: " + f.getName());
        }
        System.out.println("\n");

        List<OTuple2G<Boolean, D_FundInfo>> fisFiles = new ArrayList<>();

        int countNew = 0;
        int countUpdated = 0;
        int count = 0;
        Map<String, D_FundInfo> hm = new HashMap<>();
        for (D_FundInfo fi: DB_FundInfo.getAllFundInfos()) {
            if (fi._type.equals(D_FundInfo.TYPE_PPM)) {
                hm.put(fi.getNameMS(), fi);
            }
        }

        for (File f: fileList) {
            List<D_FundInfo> tmpFIS = new ArrayList<>();
            byte[] data = MM.fileReadFrom(f);

            // Update PPM - Phase 2
            /*
            String c = null;
            try {
                c = new String(data, "UTF-8");
            } catch(UnsupportedEncodingException exc) {
                throw new AssertionError("UnsupportedCodingException caught");
            }
            List<String> lines = MM.splitIntoLines(c);
            if (lines == null) {
                return;
            }

            while (true) {
                String s = lines.remove(0);
                if (s.startsWith("NEWENTRY")) { break; }
            }
            while (true) {
                System.out.println("\nNow doing fund: " + count);
                count++;
                String name = lines.remove(0);
                String urlNew = lines.remove(0);
                String urlOld = null;
                if (lines.size() > 0) {
                    urlOld = lines.remove(0);
                }

                System.out.println(name);
                System.out.println(urlNew);
                System.out.println(urlOld);

                if (urlOld == null || urlOld.startsWith("NEWENTRY")) {
                    D_FundInfo fiNew = new D_FundInfo();
                    fiNew._type = D_FundInfo.TYPE_PPM;
                    fiNew.setNameMS(name);
                    fiNew._url = urlNew;

                    D_FundInfo fiOld = DB_FundInfo.getFundInfosByTypeAndName(D_FundInfo.TYPE_PPM, fiNew.getNameMS(), false);
                    if (fiOld != null) {
                        System.out.println("*** ERROR, fund already existed: " + fiNew.getNameMS());
                        System.out.println("    It's URL is: " + fiOld._url);
                        System.exit(1);
                    } else {
                        countNew++;
                        System.out.println("...NEW fund");
                        DB_FundInfo.addFundInfo(fiNew);
                    }
                } else {
                    D_FundInfo fiOld = DB_FundInfo.getFundInfosByTypeAndURL(D_FundInfo.TYPE_PPM, urlOld);
                    if (fiOld == null) {
                        System.out.println("Name: " + name);
                        System.out.println("PROBLEM, URL did not exist: " + urlOld);
                        System.exit(1);
                    } else {
                        System.out.println("...OLD fund");
                        fiOld.setNameMS(name);
                        fiOld._url = urlNew;
                    }
                    countUpdated++;
                }

                if (urlOld == null) {
                    break;
                }
                if (!urlOld.startsWith("NEWENTRY")) {
                    String s = null;
                    if (lines.size() == 0) break;
                    do {
                        s = lines.remove(0);
                    } while (!s.startsWith("NEWENTRY") && lines.size() > 0);
                    if (lines.size() == 0)  break;
                }
            }
*/

            // For PPM - Phase 1
            /*
            String c = new String(data, "UTF-8");
            String tag = "target=\"_self\" href=";
            while (true) {
                int io1 = c.indexOf(tag);
                if (io1 == -1) {
                    break;
                }
                c = c.substring(io1+tag.length());
                int io2 = c.indexOf(">");
                String url = c.substring(1, io2-1);
                c = c.substring(io2+1);
                String name = c.substring(0, c.indexOf("</a>")).trim();
                name = MM.htmlEntitiesRemove(name);
                count++;

                System.out.println("NEWENTRY: " + count);
                System.out.println(name);
                System.out.println(url);
                D_FundInfo fi = hm.get(name);;
                if (fi != null) {
                    countUpdated++;
                    System.out.println(fi._url);
                    hm.remove(name);
                } else {
                    countNew++;
                }
                System.out.println("\n");
            }
*/
            // **********************************************************************************
            // For SEB and SPP
            /*
            String c = null;
            try {
                c = new String(data, "UTF-8");
            } catch(UnsupportedEncodingException exc) {
                throw new AssertionError("UnsupportedCodingException caught");
            }
            List<String> lines = MM.splitIntoLines(c);
            if (lines == null) {
                return;
            }


            System.out.println("We have " + lines.size() + " entries to process");
            List<D_FundInfo> lorig = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_SPP);
            List<D_FundInfo> l = new ArrayList<>();
            l.addAll(lorig);
            System.out.println("To process: " + l.size());
            for (String line: lines) {
                if (line.startsWith("\"None") || line.startsWith("None")) {
                    System.out.println("Found a comment line starting with None, skipping");
                    System.out.println("...line: " + line);
                    continue;
                }
                String[] items = line.split("\t");
                String msName = items[0];
                String msURLNew = items[1];
                String msURLOld = items[2];
                for (int i=0; i < l.size(); i++) {
                    D_FundInfo fi = l.get(i);
                    if (fi._url.equals(msURLOld)) {
                        System.out.println(fi.getTypeAndName() + "\nSPP." + msName + "\n" + msURLNew + "\n");
                        fi._url = msURLNew;
                        fi.setNameMS(msName);
                        l.remove(i);
                        break;
                    }
                }
            }
            System.out.println("Left: " + l.size());
            for (D_FundInfo fi: l) {
                System.out.println(fi.getTypeAndName());
            }
             */
        }

        System.out.println("count: " + count);
        System.out.println("countNew: " + countNew);
        System.out.println("countUpdated: " + countUpdated);

//        List<D_FundInfo> vs = new ArrayList<>(hm.values());
//        Collections.sort(vs, new Comparator<D_FundInfo>() {
//                    @Override
//                    public int compare(D_FundInfo d_fundInfo, D_FundInfo t1) {
//                        return d_fundInfo.getNameMS().compareTo(t1.getNameMS());
//                    }
//                });
//        for (D_FundInfo fi: vs) {
//            System.out.println(fi.getNameMS());
//            System.out.println(fi._url);
//            System.out.println();
//        }

//        if (true) System.exit(1);

        // You sure you want to update?
        byte[] readData = new byte[100];
        int readDataCount = 0;
        try {
            System.out.println("Are you sure you want to update DB [yes/no]: ");
            readDataCount = System.in.read(readData);
            byte[] tmp = new byte[readDataCount];
            for (int i=0; i < readDataCount; i++) {
                tmp[i] = readData[i];
            }
            readData = tmp;
        } catch(IOException exc) {
            System.out.println("IOException encountered when trying to read response: " + exc);
            return;
        }
        String readStr = new String(readData).toLowerCase();
        if (!readStr.equals("yes\n")) {
            System.out.println("You opted out of update, exiting!!!");
            return;
        }

        // Write result fund DB
        System.out.println("About to write updated DB");
        fiv = new D_FundInfo_Validator(DB_FundInfo.getAllFundInfos());
        fiv.process();
        if (fiv._error) {
            System.out.println("*** ERROR ****: Fund database in invalid, will not save results\n" + fiv._iwErrors.getString());
            return;
        }
        byte[] dataToWrite = DB_FundInfo.crunch();
        // Store a backupo
        MM.fileWrite(DB_ABSOLUTE_FILENAME + ".backup_" + MM.getNowAs_YYMMDD_HHMMSS(null), fileDBDataBA);
        MM.fileWrite(DB_ABSOLUTE_FILENAME, dataToWrite);
        System.out.println("All done successfully, exiting");
    }

    //------------------------------------------------------------------------
    private static int RC_SUCCESS_MODIFIED = 1;
    private static int RC_SUCCESS_UNMODIFIED = 2;
    private static int RC_FAILURE = 3;
    private static int processFileFIs(boolean testHTMLExtraction,
                                      String type,
                                      List<OTuple2G<Boolean, D_FundInfo>> fisFiles) {

        int rc = RC_SUCCESS_UNMODIFIED;

        IndentWriter iwInfo = new IndentWriter();
        IndentWriter iwUpdate1 = new IndentWriter();
        IndentWriter iwUpdate2 = new IndentWriter();
        IndentWriter iwDelete = new IndentWriter();
        IndentWriter iwInsert = new IndentWriter();

        // Checking which in Files were new/already existed in DB
        System.out.println("\n1. Checking file funds to see if they exist in DB (report at end)");
        List<D_FundInfo> fisToAdd = new ArrayList<>();
        int countURLMatch = 0;
        int countNewFunds = 0;
        for (OTuple2G<Boolean, D_FundInfo> fiFilePair: fisFiles) {
            D_FundInfo fiFile =  fiFilePair._o2;
            D_FundInfo fiDB =  DB_FundInfo.getFundInfosByTypeAndURL(fiFile._type, fiFile._url);
            boolean keepDBMSName = fiFilePair._o1;

            if (fiDB != null) {
                if (!fiDB._isValid) {
                    System.out.println("*** Warning [UPDATE] Found match on URL, but the one in DB is invalid. Will set it to valid");
                    System.out.println("..." + fiDB.getOneLiner());
                    fiDB._isValid = true;
//                    return RC_FAILURE;
                }
                if (!fiFile.getNameOrig().equals(fiDB.getNameOrig())) {
                    iwUpdate2.println("[UPDATE] Original DB name: " + fiDB.getNameOrig() + ", changes to: " + fiFile.getNameOrig());
                    fiDB.setNameOrig(fiFile.getNameOrig());
                    rc = RC_SUCCESS_MODIFIED;
                }
                if (!fiFile.getNameMS().equals(fiDB.getNameMS())) {
                    if (keepDBMSName) {
                        iwInfo.println("[INFO] MS name differed, keeping DB name: " + fiDB.getNameMS() + ", file: " + fiFile.getNameMS());
                        iwInfo.println("       DB URL: " + fiDB._url);
                        countURLMatch++;
                    } else {
                        iwUpdate1.println("[UPDATE] MS DB name: " + fiDB.getNameMS() + ", changes to: " + fiFile.getNameMS());
                        fiDB.setNameMS(fiFile.getNameMS());
                    }
                    rc = RC_SUCCESS_MODIFIED;
                }
            } else {
                boolean didModification = false;
                fiDB = DB_FundInfo.getFundInfosByTypeAndName(fiFile._type, fiFile.getNameOrig(), false);
                if (fiDB != null) {
                    System.out.println("*** Warning [UPDATE] Did not find a match on URL but on _nameOrig");
                    System.out.println("...DB Entry:   " + fiDB.getOneLiner());
                    System.out.println("...File Entry: " + fiFile.getOneLiner());
                    System.out.println("...--> Will change the DB URL to the file one");
                    fiDB._url = fiFile._url;
                    didModification = true;
//                    return RC_FAILURE;
                }
                fiDB = DB_FundInfo.getFundInfosByTypeAndName(fiFile._type, fiFile.getNameMS(), true);
                if (fiDB != null) {
                    System.out.println("*** Warning [UPDATE] Did not find a match on URL but on _nameMS");
                    System.out.println("...DB Entry:   " + fiDB.getOneLiner());
                    System.out.println("...File Entry: " + fiFile.getOneLiner());
                    System.out.println("...--> Will change the DB URL to the file one");
                    fiDB._url = fiFile._url;
                    didModification = true;
//                    return RC_FAILURE;
                }
                if (!didModification) {
                    fisToAdd.add(fiFile);
                }
            }
        }

        int countNoDPDayFound = 0;
        if (testHTMLExtraction) {
            System.out.println("\n2. Testing HTML extraction of added funds from files");
            int index = 0;
            while (index < fisToAdd.size()) {
                D_FundInfo fi = fisToAdd.get(index);
                int rc_html = htmlExtract(testHTMLExtraction, fi);
                switch (rc_html) {
                    case RC_HTML_SUCCESS:
                        index++;
                        break;
                    case RC_HTML_NODPDAY:
                        index++;
                        countNoDPDayFound++;
                        break;
                    case RC_HTML_GENERAL_ERROR_TERMINATE:
                        return RC_FAILURE;
                    case RC_HTML_ERROR:
                        System.out.println("*** Extraction failed, will not add this file fund to DB");
                        fisToAdd.remove(index);
                        countNewFunds--;
                }
            }
        } else {
            System.out.println("\n2. PASS: Will not test HTML extraction");
        }
        if (fisToAdd.size() > 0) {
            rc = RC_SUCCESS_MODIFIED;
            DB_FundInfo.addFundInfo(fisToAdd);
        }
        for (D_FundInfo fi : fisToAdd) {
            countNewFunds++;
            iwInsert.println("[INSERT] Will insert new fund from file: " + fi.getTypeAndName());
        }

        // Checking which in DB did not have an entry in file
        System.out.println("\n3. Checking DB funds to see if they exist in files (report at end)");
        int countUniqueInDB = 0;
        List<D_FundInfo> fisDB = DB_FundInfo.getFundInfosByType(type);
        int countOriginalInDB = fisDB.size();
        for (D_FundInfo fiDB: fisDB) {
            boolean found = false;
            for (OTuple2G<Boolean, D_FundInfo> fiFi: fisFiles) {
                if (fiDB._url.equals(fiFi._o2._url)) {
                    found = true;
                }
            }
            if (!found) {
                rc = RC_SUCCESS_MODIFIED;
                DB_FundInfo.deleteFundInfoByTypeAndURL(type, fiDB._url);
                countUniqueInDB++;
                iwDelete.println("[DELETE] Exist in DB but not in files: " + fiDB.getTypeAndName() + ": " + MM.getString(fiDB.getDPDOneLiner(), 40));
            }
        }

        System.out.println("\nSynchronization Report");
        System.out.println(iwInfo.getString());
        System.out.println(iwUpdate1.getString());
        System.out.println(iwUpdate2.getString());
        System.out.println(iwInsert.getString());
        System.out.println(iwDelete.getString());

        System.out.println("\nTotal in DB: " + fisDB.size());
        System.out.println("Total in files: " + fisFiles.size());
        System.out.println("...Matches between DB/Files: " + countURLMatch);
        System.out.println("...New funds found in files: " + countNewFunds + ", of which had no DP day: " + countNoDPDayFound);
        System.out.println("...Funds to remove from DB: " + countUniqueInDB);

        return rc;
    }

    //------------------------------------------------------------------------
    private final static int RC_HTML_SUCCESS = 0;
    private final static int RC_HTML_NODPDAY = 1;
    private final static int RC_HTML_ERROR = 2;
    private final static int RC_HTML_GENERAL_ERROR_TERMINATE = 3;
    private static int htmlExtract(boolean testHTMLExtraction, D_FundInfo fi) {
        if (testHTMLExtraction) {
            IndentWriter iwd = new IndentWriter();
            ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
            System.out.println("Testing extraction: " + fi.getOneLiner());
            try {
                int errorCode = eh.extractFundDetails(fi, iwd);
                if (errorCode != ExtractFromHTML_Helper.RC_SUCCESS
                        && errorCode != ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND
                        && errorCode != ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED) {
                    System.out.println("*************************************************************");
                    System.out.println("*** Error extracting fund error code: " + errorCode + ", " + fi.getTypeAndName());
                    System.out.println(iwd.getString());
                    return RC_HTML_ERROR;
                }
                if (errorCode == ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND) {
                    System.out.println("...*** WARNING *** No DP day found");
                    System.out.println("...warning fund: " + fi.getOneLiner());
                    return RC_HTML_NODPDAY;
                }
            } catch (IOException exc) {
                System.out.println("IOException caught: " + exc + "\n" + MM.getStackTraceString(exc));
                return RC_HTML_GENERAL_ERROR_TERMINATE;
            }
            System.out.println("...successfully extracted: " + fi.getOneLiner());
        }
        return RC_HTML_SUCCESS;
    }
}