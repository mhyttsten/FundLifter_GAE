package com.pf.fl.appengine.jsphelper;

import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.utils.MM;
import com.pf.fl.shared.utils.OTuple2G;
import com.pf.fl.shared.utils.OTuple3G;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class JSP_Helper_DataSorter {
    private static final Logger log = Logger.getLogger(JSP_Helper_DataSorter.class.getName());
    private static final String TAG = MM.getClassName(JSP_Helper_DataSorter.class.getName());


//    public static List<D_FundInfo> getSEBMatches(
//            List<String> dates,
//            List<D_FundInfo> list) throws Exception {
//        EE ee = EE.getEE();
//        List<D_FundInfo> rlCFIs = new ArrayList<>();
//
//        List<D_FundInfo> sebs = new ArrayList<>();
//        List<D_FundInfo> others = new ArrayList<>();
//
//        // Sort out the SEBs
//        for (D_FundInfo cfi : list) {
//            if (cfi._type.equals(FLA_FundInfo.TYPE_SEB)) {
//                sebs.add(cfi);
//            } else {
//                others.add(cfi);
//            }
//        }
//
//        // Create suggestions to SEBs
//        for (D_FundInfo cfi : sebs) {
//            rlCFIs.add(cfi);
//            ee.dinfo(log, TAG, "Adding SEB: " + cfi + ", result size: " + rlCFIs.size());
//
//            for (String t: D_FundInfo.TYPES) {
//                if (!t.equals(D_FundInfo.TYPE_SEB)) {
//                    ee.dinfo(log, TAG, "...type: " + t);
//                    // Find matching alternative
//                    if (t.equals(D_FundInfo.TYPE_PPM)) {
//                        D_FundInfo cfiPPM = DB_FundInfo.getFundByPPMNumber(cfi.mPPMNumber);
//                        if (cfiPPM == null) {
//                            cfiPPM.mType = FLA_FundInfo.TYPE_PPM;
//                            cfiPPM.mName = "ERR:#NotFound:" + cfi.mPPMNumber + " " + cfiPPM.mName;
//
//                            ee.dinfo(log, TAG, "...PPM number ERROR, could not find PPM fund");
//                        } else {
//                            ee.dinfo(log, TAG, "...PPM number SUCCESS");
//                        }
//                        ee.dinfo(log, TAG, "...Add PPM fund by number: " + cfiPPM.mType + "." + cfiPPM.mName);
//                        rlCFIs.add(cfiPPM);
//                    } else if (cfi.mIndexCompare != null) {
//                        FLA_FundIndex fundIndex = cfi.mIndexCompare.get();
//                        if (fundIndex.mKey_IndexName == null || fundIndex.mKey_IndexName.trim().equals("-")) {
//                            ee.dinfo(log, TAG, "...Add null, index not found");
//                            rlCFIs.add(null);
//                        } else {
//                            // Sort by best results last two weeks added together
//                            List<D_FundInfo> cfiIndexes = DB_FundInfo.getByTypeAndIndex(t, fundIndex);
//                            List<OTuple3G<FLA_Cache_FundInfo, List<Double>, String>> rfull = new ArrayList<>();
//                            getCachedFundInfoWithinDates(
//                                    dates,
//                                    cfiIndexes,
//                                    null,
//                                    rfull);
//                            rfull = dataSort_GetBestLastXWeeks(10, 2, rfull);
//                            if (rfull.size() > 0) {
//                                FLA_Cache_FundInfo cfiBestIndexMatch = rfull.get(0)._o1;
//                                ee.dinfo(log, TAG, "...Add best match from index: " + cfiBestIndexMatch.mType + "." + cfiBestIndexMatch.mName);
//                                rlCFIs.add(cfiBestIndexMatch);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Add all other, null to separate pack
//        if (others.size() > 0) {
//            ee.dinfo(log, TAG, "...Add null because we had others");
//            rlCFIs.add(null);
//        }
//        for (FLA_Cache_FundInfo cfi : others) {
//            ee.dinfo(log, TAG, "...Add others: " + cfi.mType + "." + cfi.mName);
//            rlCFIs.add(cfi);
//        }
//
//        return rlCFIs;
//    }

    /**
     *
     */
//    public static OTuple2G<String[], List<D_FundInfo>>
//    convertToTableRows(List<OTuple2G<D_FundInfo, List<Double>>> arg) {
//        List<String> cols1 = new ArrayList<>();
//        List<D_FundInfo> fcfi = new ArrayList<>();
//
//        for (int i=0; i < arg.size(); i++) {
//            cols1.add(arg.get(i)._o3);
//            fcfi.add(arg.get(i)._o1);
//        }
//        String[] cols1A = cols1.toArray(new String[cols1.size()]);
//
//        OTuple2G<String[], List<D_FundInfo>> r = new OTuple2G<>();
//        r._o1 = cols1A;
//        r._o2 = fcfi;
//        return r;
//    }

    /**
     *
     */
    public static List<String> getDateSequence(int numberOfFridaysAgo, int count)  throws Exception {
        if (numberOfFridaysAgo < 0) {
            throw new Exception("numberOfFridaysAgo should be 0 or larger: " + numberOfFridaysAgo);
        }

        String nowYYMMDD = MM.getNowAs_YYMMDD(null);
        String lastFriday = MM.tgif_getLastFridayTodayIncl(nowYYMMDD);
        for (int i=0; i < numberOfFridaysAgo; i++) {
            lastFriday = MM.tgif_getLastFridayTodayExcl(lastFriday);
        }

        List<String> dates = new ArrayList<>();
        dates.add(lastFriday);
        for (int i=1; i < count; i++) {
            lastFriday = MM.tgif_getLastFridayTodayExcl(lastFriday);
            dates.add(lastFriday);
        }

        StringBuilder strb = new StringBuilder();
        for (String d : dates) {
            strb.append(d + ",");
        }
        log.info("Dates: " + strb.toString());

        return dates;
    }

    /**
     *
     */
    public static List<OTuple2G<D_FundInfo, List<Double>>> getCachedFundInfoWithinDates(
            List<String> dates,
            List<D_FundInfo> args) throws Exception {

        List<OTuple2G<D_FundInfo, List<Double>>> result = new ArrayList<>();

        for (D_FundInfo cfi : args) {
            List<D_FundDPDay> fws = cfi._dpDays;
            List<Double> doubleList = new ArrayList<>();
            List<D_FundDPDay> dpws = cfi._dpDays;
            boolean foundDate = false;
            for (String d : dates) {
                Double r1w = null;
                for (D_FundDPDay dpw : dpws) {
                    if (dpw._dateYYMMDD.equals(d) && dpw._r1w != D_FundDPDay.FLOAT_NULL) {
                        r1w = new Double(dpw._r1w);
                    }
                }
                doubleList.add(r1w);
            }
        }
        return result;
    }

    /**
     *
     */
    public static List<OTuple3G<D_FundInfo, List<Double>, String>>
    dataSort_GetBestLastXWeeks(
            int limitCount,
            final int weekCount,
            List<OTuple3G<D_FundInfo, List<Double>, String>> arg) throws Exception {

        List<OTuple3G<D_FundInfo, List<Double>, String>> l = new ArrayList<>();
        l.addAll(arg);

        Collections.sort(l, new Comparator<OTuple3G<D_FundInfo, List<Double>, String>>() {
            @Override
            public int compare(OTuple3G<D_FundInfo, List<Double>, String> o1, OTuple3G<D_FundInfo, List<Double>, String> o2) {
                double o1d = 0.0D;
                double o2d = 0.0D;
                for (int i=0; i < weekCount; i++) {
                    Double o1delem = o1._o2.get(i);
                    Double o2delem = o2._o2.get(i);
                    if (o1delem == null && o2delem != null) {
                        return 1;
                    } else if (o2delem == null && o1delem != null) {
                        return -1;
                    } else if (o1delem == null && o2delem == null) {
                        return 0;
                    }

                    o1d += o1delem.doubleValue();
                    o2d += o2delem.doubleValue();
                }
                if (o1d >= o2d) {
                    return -1;
                }
                return 1;
            }
        });
        if (limitCount > l.size()) {
            return l;
        }
        return l.subList(0, limitCount);
    }

    /**
     *
     */
    public static List<OTuple3G<D_FundInfo, List<Double>, String>>
    dataSort_GetBestScoreLast2Weeks(
            int limitCount,
            List<OTuple3G<D_FundInfo, List<Double>, String>> arg) throws Exception {

        List<OTuple3G<D_FundInfo, List<Double>, List<Integer>>> l = new ArrayList<>();
        for (OTuple3G<D_FundInfo, List<Double>, String> e : arg) {
            OTuple3G<D_FundInfo, List<Double>, List<Integer>> ne = new OTuple3G<>();
            ne._o1 = e._o1;
            ne._o2 = e._o2;
            ne._o3 = new ArrayList<Integer>();
            l.add(ne);
        }

        // Sort according to week 1
        Collections.sort(l, new Comparator<OTuple3G<D_FundInfo, List<Double>, List<Integer>>>() {
            @Override
            public int compare(OTuple3G<D_FundInfo, List<Double>, List<Integer>> o1,
                               OTuple3G<D_FundInfo, List<Double>, List<Integer>> o2) {
                if (o1._o2.get(0) > o2._o2.get(0)) {
                    return -1;
                }
                return 1;
            }
        });
        // Assign the positions
        for (int i=0; i < l.size(); i++) {
            OTuple3G<D_FundInfo, List<Double>, List<Integer>> e = l.get(i);
            e._o3.add(new Integer(i+1));
        }

        // Sort according to week 2
        Collections.sort(l, new Comparator<OTuple3G<D_FundInfo, List<Double>, List<Integer>>>() {
            @Override
            public int compare(OTuple3G<D_FundInfo, List<Double>, List<Integer>> o1,
                               OTuple3G<D_FundInfo, List<Double>, List<Integer>> o2) {
                if (o1._o2.get(1) > o2._o2.get(1)) {
                    return -1;
                }
                return 1;
            }
        });
        // Assign the positions
        for (int i=0; i < l.size(); i++) {
            OTuple3G<D_FundInfo, List<Double>, List<Integer>> e = l.get(i);
            e._o3.add(new Integer(i+1));
        }

        // Sort according to positions
        Collections.sort(l, new Comparator<OTuple3G<D_FundInfo, List<Double>, List<Integer>>>() {
            @Override
            public int compare(OTuple3G<D_FundInfo, List<Double>, List<Integer>> o1,
                               OTuple3G<D_FundInfo, List<Double>, List<Integer>> o2) {
                int o1Pos = o1._o3.get(0) + o1._o3.get(1);
                int o2Pos = o2._o3.get(0) + o2._o3.get(1);
                if (o1Pos < o2Pos) {
                    return -1;
                }
                return 1;
            }
        });

        List<OTuple3G<D_FundInfo, List<Double>, String>> rlist = new ArrayList<>();
        for (int i=0; i < l.size(); i++) {
            OTuple3G<D_FundInfo, List<Double>, List<Integer>> e = l.get(i);
            OTuple3G<D_FundInfo, List<Double>, String> r = new OTuple3G<>();
            StringBuilder strb = new StringBuilder();
            for (int j=0; j < e._o3.size(); j++) {
                strb.append(String.valueOf(e._o3.get(j)));
                if (j+1 < e._o3.size()) {
                    strb.append(",");
                }
            }
            r._o1 = e._o1;
            r._o2 = e._o2;
            r._o3 = strb.toString();
            rlist.add(r);
        }
        if (limitCount > l.size()) {
            return rlist;
        }
        return rlist.subList(0, limitCount);
    }
}

