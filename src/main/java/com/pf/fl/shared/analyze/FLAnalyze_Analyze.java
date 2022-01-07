package com.pf.fl.shared.analyze;

import com.pf.fl.shared.datamodel.DB_FundInfo;
import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.utils.D_Utils;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;
import com.pf.fl.shared.datamodel.D_Analyze_FundRank;
import com.pf.fl.shared.datamodel.D_Analyze_FundRank.D_Analyze_FundRankElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

public class FLAnalyze_Analyze {

    // ***********************************************************************

    // Alternatives for performance optimization:
    // 1. cloneAndFillVoids, setMaxRange then serialize (accept analyze time for each time series)
    // 2. Also do analyze for different friday sequences, then serialize

    //------------------------------------------------------------------------
    // Obs, this method will modify fundsToAnalyze
    public static List<D_FundInfo> cloneAndFillVoids(List<D_FundInfo> fis) {
        List<D_FundInfo> fisClone = D_FundInfo.cloneList(fis);

        // Fill in and extrapolate any missing values
        FLAnalyze_DataPreparation.fillVoids(null, fisClone);
        return fisClone;
    }

    //------------------------------------------------------------------------
    // Obs, this method will modify fundsToAnalyze
    public static Map<String, D_Analyze_FundRankElement[]> setMaxRange(
            String type,
            List<D_FundInfo> fundsToAnalyze,
            int weekCount) {

        if(weekCount <= 0) {
            throw new AssertionError("Week count must be positive");
        }
        String[] fridays = D_Utils.getRecentDates(weekCount);
        return setMaxRange(type, fundsToAnalyze, fridays[0], fridays[fridays.length-1]);
    }

    //------------------------------------------------------------------------
    public static Map<String, D_Analyze_FundRankElement[]> setMaxRange(
            String type,
            List<D_FundInfo> fundsToAnalyze,
            String fridayNewest,
            String fridayOldest) {

        // Make sure all funds are of given type
        for (D_FundInfo fi: fundsToAnalyze) {
            if (!fi._type.equals(type)) {
                throw new AssertionError("Type mismatch, expected: " + type + ", got: " + fi.getTypeAndName());
            }
        }

        // Create the list of fridays to analyze
        List<String> fridayList = new ArrayList<>();
        if(!MM.tgif_isFriday(fridayNewest) || !MM.tgif_isFriday(fridayOldest)) {
            throw new AssertionError("One parameter not a friday: " + fridayNewest + ", " + fridayOldest);
        }
        fridayList.add(fridayNewest);
        String fridayCurrent = fridayNewest;
        while (!fridayCurrent.equals(fridayOldest)) {
            fridayCurrent = MM.tgif_getLastFridayTodayExcl(fridayCurrent);
            fridayList.add(fridayCurrent);
        }

        // Strip out DPDs outside our friday range
        int fcount = 0;
        for (D_FundInfo fi: fundsToAnalyze) {
            List<D_FundDPDay> dpds = fi._dpDays;
            int idx = 0;
            while (idx < dpds.size()) {
                // Remove / keep dpds within our analysis range only
                D_FundDPDay dpd = dpds.get(idx);
                if (dpd._dateYYMMDD.compareTo(fridayNewest) > 0
                        || dpd._dateYYMMDD.compareTo(fridayOldest) < 0) {
                    dpds.remove(idx);
                } else {
                    idx++;
                }
            }
            for (int i=0; i < dpds.size(); i++) {
                String dpdf = dpds.get(i)._dateYYMMDD;
                if (!dpdf.equals(fridayList.get(i))) {
                    IndentWriter iwdbg = new IndentWriter();
                    fi.dumpInfo(iwdbg);
                    if (fcount > 0) {
                        throw new AssertionError(fi.getTypeAndName() + ", DP: " + dpdf + " != " + fridayList.get(i) + ", at index: " + i
                                + "\nWe have processed: " + fcount + " funds successfully prior to this one\n" + iwdbg.getString());
                    }
                }
            }
            fcount++;
        }

        // Create the matrix: typeAndName -> List<FRE> for each friday
        Map<String, D_Analyze_FundRankElement[]> matrix = new HashMap<>();
        for (D_FundInfo fi : fundsToAnalyze) {

            D_Analyze_FundRankElement[] fres = new D_Analyze_FundRankElement[fridayList.size()];

            for (int fridayIndex=0; fridayIndex < fridayList.size(); fridayIndex++) {
                String friday = fridayList.get(fridayIndex);

                // The results for this friday
                D_Analyze_FundRankElement fre = new D_Analyze_FundRankElement();
                fre._fi = fi;
                fre._typeAndName = fi.getTypeAndName();
                fre._countTotal = 1;
                fre._fridays.add(friday);

                // We can index with fridayIndex, because we stripped all DPDs outside our range in setRange
                D_FundDPDay dpd = fre._fi._dpDays.get(fridayIndex);
                if (!dpd._dateYYMMDD.equals(friday)) {
                    throw new AssertionError("Friday mismatch, found: " + dpd._dateYYMMDD + ", expected: " + friday);
                }
                if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
                    fre._countMissing = 1;
                }
                fre._r1w = dpd._r1w;
                fres[fridayIndex] = fre;
            }

            matrix.put(fi.getTypeAndName(), fres);
        }

        return matrix;
    }

    //------------------------------------------------------------------------
    // The entries in matrix may be modified: Rank and R1Ws may change
    public static D_Analyze_FundRank analyze(
            String type,
            int weekCount,
            Map<String, D_Analyze_FundRankElement[]> matrix) {

        // Create a list of fundlists to use for this friday range
        // Filter out funds with not enough values, and assign average r1ws for missing ones
        List<D_Analyze_FundRankElement[]> fundsToUse = new ArrayList<>();
        Iterator<String> iter = matrix.keySet().iterator();
        while(iter.hasNext()) {
            String key = iter.next();
            D_Analyze_FundRankElement[] l = matrix.get(key);

            // We cannot analyze more than is available
            if (l.length < weekCount) {
                throw new AssertionError("Argument weekCount: " + weekCount + ", larger than matrix: " + matrix.size());
            }

            boolean useIt = processElemSequence(weekCount, l);
            if (useIt) {
                fundsToUse.add(l);
           }
        }

        // Assign funds to each individual week
        List<List<D_Analyze_FundRankElement>> r = new ArrayList<>();
        for (int i=0; i < weekCount; i++) {
            List<D_Analyze_FundRankElement> elems = new ArrayList<>();
            for (D_Analyze_FundRankElement[] elem: fundsToUse) {
                elems.add(elem[i]);
            }
            r.add(elems);
        }

        // Rank each week
        D_Analyze_FundRank fr = new D_Analyze_FundRank();
        for (List<D_Analyze_FundRankElement> l: r) {
            Collections.sort(l, R1WComparator);

            // Assign rank according to position
            for (int i=0; i < l.size(); i++) {
                D_Analyze_FundRankElement fre = l.get(i);
                fre._rank = i+1;  // +1 to get average rank correct
            }

            fr._frEachFriday.add(l);
        }

        // Create summary: Iterate over friday sequence of each FRE
        fr._type = type;
        fr._fridays = D_Utils.getRecentDates(weekCount);
        for (D_Analyze_FundRankElement[] elems: fundsToUse) {
            D_Analyze_FundRankElement fres = getSummary(weekCount, elems);
            fr._frSummaryForAllFridays.add(fres);
            Collections.sort(fr._frSummaryForAllFridays, RankComparator);
        }
        return fr;
    }

    //------------------------------------------------------------------------
    public static D_Analyze_FundRankElement getSummary(
            int weekCount,
            D_Analyze_FundRankElement[] l) {

        D_Analyze_FundRankElement r = new D_Analyze_FundRankElement();
        r._r1w = 0.0F;
        r._rank = 0.0F;
        for (int i=0; i < weekCount; i++) {
            D_Analyze_FundRankElement fre = l[i];

            if (r._fi != null && !r._fi.getTypeAndName().equals(fre._typeAndName)) {
                throw new AssertionError("Different ids: " + r._fi.getTypeAndName() + ", " + fre._typeAndName);
            }
            if (fre._countMissing != 0 && fre._countMissing != 1) {
                throw new AssertionError("CountMissing != {0,1}: " + fre._countMissing);
            }
            if (fre._countTotal != 1) {
                throw new AssertionError("CountTotal != 1: " + fre._countTotal);
            }
            if (fre._fridays.size() != 1) {
                throw new AssertionError("Fridays.size() != 1: " + fre._fridays.size());
            }

            r._fi = fre._fi;
            r._typeAndName = fre._typeAndName;
            r._countMissing += fre._countMissing;
            r._countTotal += fre._countTotal;
            r._fridays.add(fre._fridays.get(0));
            r._r1w += fre._r1w;
            r._rank += fre._rank;
        }

        return r;
    }

    //------------------------------------------------------------------------
    private static boolean processElemSequence(int weekCount, D_Analyze_FundRankElement[] l) {

        // Get total # valid during period
        float r1wAcc = 0.0F;
        int total = 0;
        for (D_Analyze_FundRankElement e: l) {
            if (e._countMissing == 0) {
                r1wAcc += e._r1w;
                total++;
            }
        }

        // We need to have >=75% to use this series
        float ratio = (float)(((float)total)/((float)weekCount));
        if (ratio < 0.75F) {
            return false;
        }

        // Assign the average value to the NULL ones
        r1wAcc = r1wAcc / total;
        for (D_Analyze_FundRankElement e: l) {
            if (e._countMissing > 0) {
                e._r1w = r1wAcc;
            }
        }
        return true;
    }


    //------------------------------------------------------------------------
    private static Comparator<D_Analyze_FundRankElement> R1WComparator = new Comparator<D_Analyze_FundRankElement>() {
        @Override
        public int compare(D_Analyze_FundRankElement o1, D_Analyze_FundRankElement o2) {
            if (o1._r1w == D_FundDPDay.FLOAT_NULL && o2._r1w == D_FundDPDay.FLOAT_NULL) {
                return -1;
            } else if (o1._r1w != D_FundDPDay.FLOAT_NULL && o2._r1w == D_FundDPDay.FLOAT_NULL) {
                return -1;
            } else if (o1._r1w == D_FundDPDay.FLOAT_NULL && o2._r1w != D_FundDPDay.FLOAT_NULL) {
                return 1;
            }

            if (o1._r1w > o2._r1w) {
                return -1;
            }
            return 1;
        }
    };

    //------------------------------------------------------------------------
    private static Comparator<D_Analyze_FundRankElement> RankComparator = new Comparator<D_Analyze_FundRankElement>() {
        @Override
        public int compare(D_Analyze_FundRankElement o1, D_Analyze_FundRankElement o2) {
            if (o1._rank == o2._rank) {
                if (o1._countMissing <= o2._countMissing) {
                    return -1;
                }
                return 1;
            }

            if (o1._rank <= o2._rank) {
                return -1;
            }
            return 1;
        }
    };
}