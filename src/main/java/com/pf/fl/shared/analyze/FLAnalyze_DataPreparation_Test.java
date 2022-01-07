package com.pf.fl.shared.analyze;

import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.utils.D_Utils;
import com.pf.fl.shared.utils.MM;

import java.util.ArrayList;
import java.util.List;

public class FLAnalyze_DataPreparation_Test {

    //------------------------------------------------------------------------
    public static void main(String[] args) {
        try {
            test_addNullMonths();
            test_fillVoids();

//            String DIR = "/Users/magnushyttsten/Desktop/Vanguard";
//            String DB_FILENAME = "fundinfo-db-master.bin";
//            byte[] fileDBDataBA = MM.fileReadFrom(DIR + File.separator + DB_FILENAME);
//            DB_FundInfo.initialize(fileDBDataBA, true);
//
//            FLAnalyze_Analyze fla = new FLAnalyze_Analyze(D_FundInfo.TYPE_SEB, 2);
//            IndentWriter iw = new IndentWriter();
//            fla.analyze(iw);
        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }

    //------------------------------------------------------------------------
    public static void test_addNullMonths() {
        {
            // Start and Ending Fridays exist, but missing some in middle
            System.out.println("Test 1");
            List<D_FundDPDay> dpds = new ArrayList<>();
            D_FundDPDay dpd = null;
            String fridayStart = D_Utils.getLastExtractedFriday();
            String curr = fridayStart;
            test_addDPD(dpds, curr, null, null);
            curr =  MM.tgif_getLastFridayTodayExcl(curr);
            test_addDPD(dpds, curr, null, null);
            curr =  MM.tgif_getLastFridayTodayExcl(curr);
            curr =  MM.tgif_getLastFridayTodayExcl(curr);
            curr =  MM.tgif_getLastFridayTodayExcl(curr);
            test_addDPD(dpds, curr, null, null);
            printDPDs("Before", dpds);
            FLAnalyze_DataPreparation.insertNullMonths(dpds, fridayStart, curr);
            printDPDs("After", dpds);
        }

        {
            // Start and Ending Fridays do not exist, and missing some in middle
            System.out.println("Test 2");
            List<D_FundDPDay> dpds = new ArrayList<>();
            D_FundDPDay dpd = null;
            String fridayStart = D_Utils.getLastExtractedFriday();
            String curr = fridayStart;
            curr =  MM.tgif_getLastFridayTodayExcl(curr);
            curr =  MM.tgif_getLastFridayTodayExcl(curr);
            test_addDPD(dpds, curr, null, null);
            curr =  MM.tgif_getLastFridayTodayExcl(curr);
            curr =  MM.tgif_getLastFridayTodayExcl(curr);
            test_addDPD(dpds, curr, null, null);
            curr =  MM.tgif_getLastFridayTodayExcl(curr);
            curr =  MM.tgif_getLastFridayTodayExcl(curr);
            curr =  MM.tgif_getLastFridayTodayExcl(curr);
            System.out.println("Last week: " + curr);
            printDPDs("Before", dpds);
            FLAnalyze_DataPreparation.insertNullMonths(dpds, fridayStart, curr);
            printDPDs("After", dpds);
        }
    }
    private static void test_addDPD(List<D_FundDPDay> dpds, String friday, Float r1wF, Float r1mF) {
        float r1w = D_FundDPDay.FLOAT_NULL;
        if (r1wF != null) {
            r1w = r1wF;
        }
        float r1m = D_FundDPDay.FLOAT_NULL;
        if (r1mF != null) {
            r1m = r1mF;
        }
        D_FundDPDay dpd = new D_FundDPDay();
        dpd._dateYYMMDD = friday;
        dpd._r1w = r1w;
        dpd._r1m = r1m;
        dpds.add(dpd);
    }

    //------------------------------------------------------------------------
    public static void printDPDs(String header, List<D_FundDPDay> dpds) {
        System.out.println(header);
        for (int i=0; i < dpds.size(); i++) {
            D_FundDPDay dpd = dpds.get(i);
            System.out.println("   " + dpd.toString_Date_R1W_R1M());
        }
    }


    //------------------------------------------------------------------------
    public static void test_fillVoids() {

        // Week: 0,    1,     2,     3,     4,     5,     6,     7,     8
        // r1m:  1,    1,     1,     n,     1,     n,     n,     n,     1
        // r1w:  n,    n,     n,     0.25,  0,25,  n,     0.25,  0.25,  n

        // 1: 5 r1w should become 0.25
        // 2: Based on #1, 3 r1m should become 1
        // 3: Based on #1, 2 r1w should become 0.25
        // 4: Based on #3, 1 r1w should become 0.25
        // 5: Based on #4, 0 r1w should become 0.25

        System.out.println("Test fillVoids");

        List<D_FundDPDay> dpds = new ArrayList<>();
        String fridayStart = D_Utils.getLastExtractedFriday();
        String curr = fridayStart;
        test_addDPD(dpds, curr, null, 1.0F);
        curr =  MM.tgif_getLastFridayTodayExcl(curr);
        test_addDPD(dpds, curr, null, 1.0F);
        curr =  MM.tgif_getLastFridayTodayExcl(curr);
        test_addDPD(dpds, curr, null, 1.0F);
        curr =  MM.tgif_getLastFridayTodayExcl(curr);
        test_addDPD(dpds, curr, 0.25F, null);
        curr =  MM.tgif_getLastFridayTodayExcl(curr);
        test_addDPD(dpds, curr, 0.25F, 1.0F);
        curr =  MM.tgif_getLastFridayTodayExcl(curr);
        test_addDPD(dpds, curr, null, null);
        curr =  MM.tgif_getLastFridayTodayExcl(curr);
        test_addDPD(dpds, curr, 0.25F, null);
        curr =  MM.tgif_getLastFridayTodayExcl(curr);
        test_addDPD(dpds, curr, 0.25F, null);
        curr =  MM.tgif_getLastFridayTodayExcl(curr);
        test_addDPD(dpds, curr, null, 1.0F);
        FLAnalyze_DataPreparation.insertMissingDPs(null, dpds);
        printDPDs("Results", dpds);
    }



}
