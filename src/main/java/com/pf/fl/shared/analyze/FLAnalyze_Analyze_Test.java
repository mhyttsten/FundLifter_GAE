package com.pf.fl.shared.analyze;

import com.pf.fl.shared.datamodel.DB_FundInfo;
import com.pf.fl.shared.datamodel.D_Analyze_FundRank;
import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.utils.D_Utils;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;
import com.pf.fl.shared.utils.Timer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FLAnalyze_Analyze_Test {
    public static Timer _t = new Timer();

    //------------------------------------------------------------------------
    public static void main(String[] args) {
        try {
            _t.start();
            byte[] raw = MM.fileReadFrom("/Users/magnushyttsten/tmp/fundinfo-db-master.bin");
            DB_FundInfo.initialize(raw, true);
            _t.mark("Done, DB read and initialize");

            List<D_FundInfo> fis = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_PPM);
            fis = FLAnalyze_Analyze.cloneAndFillVoids(fis);
            _t.mark("Done, cloneAndFillVoids");

            Map<String, D_Analyze_FundRank.D_Analyze_FundRankElement[]> m =
                    FLAnalyze_Analyze.setMaxRange(D_FundInfo.TYPE_PPM, fis, 16);
            _t.mark("Done, setMaxRange");

            D_Analyze_FundRank fr = null;
            fr = test_analysis(D_FundInfo.TYPE_PPM,1, m);
            _t.mark("Done, 01w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,2, m);
            _t.mark("Done, 02w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,3, m);
            _t.mark("Done, 03w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,4, m);
            _t.mark("Done, 04w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,5, m);
            _t.mark("Done, 05w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,6, m);
            _t.mark("Done, 06w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,7, m);
            _t.mark("Done, 07w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,8, m);
            _t.mark("Done, 08w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,9, m);
            _t.mark("Done, 09w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,10, m);
            _t.mark("Done, 10w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,11, m);
            _t.mark("Done, 11w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,12, m);
            _t.mark("Done, 12w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,13, m);
            _t.mark("Done, 13w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,14, m);
            _t.mark("Done, 14w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,15, m);
            _t.mark("Done, 15w");
            fr = test_analysis(D_FundInfo.TYPE_PPM,16, m);
            _t.mark("Done, 16w");

//            test_analysis_synthesized();
            System.out.println(_t);

        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }

    //------------------------------------------------------------------------
    public static D_Analyze_FundRank test_analysis(
            String type,
            int weekCount,
            Map<String, D_Analyze_FundRank.D_Analyze_FundRankElement[]> m) {
        D_Analyze_FundRank fr =
                FLAnalyze_Analyze.analyze(D_FundInfo.TYPE_PPM, 4, m);
       return fr;
    }

    //------------------------------------------------------------------------
    public static void test_analysis_synthesized() {
        float NULL = D_FundDPDay.FLOAT_NULL;

        float[] f01 = new float[] { 0.10F, 0.10F, 0.10F, 0.10F, 0.10F, NULL, 0.10F, 0.10F };
        float[] f02 = new float[] { 0.20F, 0.20F, 0.20F, 0.20F, 0.20F, NULL, 0.20F, 0.20F };
        float[] f03 = new float[] { 0.30F, 0.30F, 0.30F, 0.30F, 0.30F, NULL, 0.30F, 0.30F };
        float[] f04 = new float[] { 0.40F, 0.40F, 0.40F, 0.40F, 0.40F, NULL, 0.40F, 0.40F };
        float[] f05 = new float[] { 0.50F, 0.50F, NULL, 0.50F, 0.50F, NULL, 0.50F, NULL  };

        List<D_FundInfo> fis = new ArrayList<>();
        fis.add(create_D_FundInfo(D_FundInfo.TYPE_PPM, "FL01", 8, f01));
        fis.add(create_D_FundInfo(D_FundInfo.TYPE_PPM, "FL02", 8, f02));
        fis.add(create_D_FundInfo(D_FundInfo.TYPE_PPM, "FL03", 8, f03));
        fis.add(create_D_FundInfo(D_FundInfo.TYPE_PPM, "FL04", 8, f04));
        fis.add(create_D_FundInfo(D_FundInfo.TYPE_PPM, "FL05", 8, f05));

        fis = FLAnalyze_Analyze.cloneAndFillVoids(fis);
        Map<String, D_Analyze_FundRank.D_Analyze_FundRankElement[]> m =
                FLAnalyze_Analyze.setMaxRange(D_FundInfo.TYPE_PPM, fis, 4);
        Iterator<String> iter = m.keySet().iterator();
        System.out.println("Length: " + m.get(iter.next()).length);

        D_Analyze_FundRank fr =
                FLAnalyze_Analyze.analyze(D_FundInfo.TYPE_PPM, 4, m);

        System.out.println("Type: " + fr._type);
        System.out.print("Fridays: ");
        for (int i=0; i < fr._fridays.length; i++) {
            System.out.print(fr._fridays[i]);
            if (i+1 < fr._fridays.length) {
                System.out.print(", ");
            }
        }
        System.out.println("");

        System.out.println("Each friday");
        List<D_Analyze_FundRank.D_Analyze_FundRankElement> frel;
        for (int i=0; i < fr._frEachFriday.size(); i++) {
            frel = fr._frEachFriday.get(i);
            System.out.println("..." + fr._fridays[i]);
            for (D_Analyze_FundRank.D_Analyze_FundRankElement fre: frel) {
                System.out.println("......" + fre.toString());
            }
        }

        System.out.println("Summary");
        for (int i=0; i < fr._frSummaryForAllFridays.size(); i++) {
            D_Analyze_FundRank.D_Analyze_FundRankElement fre = fr._frSummaryForAllFridays.get(i);
            System.out.println("..." + fre.toString());
        }
    }

    //------------------------------------------------------------------------
    private static D_FundInfo create_D_FundInfo(String type, String name, int weekCount, float[] r1ws) {
        D_FundInfo fi = new D_FundInfo();
        fi._type = type;
        fi.setNameMS(name);
        fi.setNameOrig(name);

        String[] fridays = D_Utils.getRecentDates(weekCount);
        for (int i=0; i < fridays.length; i++) {
            D_FundDPDay dpd = new D_FundDPDay();
            dpd._dateYYMMDD = fridays[i];
            dpd._r1w = r1ws[i];
            fi._dpDays.add(dpd);
        }
        return fi;
    }


}
