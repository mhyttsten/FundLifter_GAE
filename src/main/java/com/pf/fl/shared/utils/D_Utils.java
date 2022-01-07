package com.pf.fl.shared.utils;


import com.pf.fl.shared.Constants;
import com.pf.fl.shared.analyze.DPSeries;
import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.datamodel.D_FundInfo;

import java.util.ArrayList;
import java.util.List;

public class D_Utils {

    //------------------------------------------------------------------------
    public static String getLastExtractedFriday() {
        String now = MM.getNowAs_YYMMDD(Constants.TIMEZONE_LOS_ANGELES);
        String lastFriday = MM.tgif_getLastFridayTodayIncl(now);
        int dayDiff = MM.tgif_dayCountDiff(now, lastFriday);
        if (dayDiff == 0) {
            lastFriday = MM.tgif_getLastFridayTodayExcl(lastFriday);
        }
        return lastFriday;
    }

    //------------------------------------------------------------------------
    public static String[] getRecentDates(int count) {
        List<String> l = new ArrayList<>();
        String current = getLastExtractedFriday();
        l.add(current);
        count--;
        while (count > 0) {
            current = MM.tgif_getLastFridayTodayExcl(current);
            l.add(current);
            count--;
        }
        return (String[])l.toArray(new String[l.size()]);
    }

    //------------------------------------------------------------------------
    public static String getOldestFriday(List<D_FundInfo> l) {
        String friday = null;
        for (D_FundInfo fi: l) {
            for (D_FundDPDay dpd: fi._dpDays) {
                if (friday == null) {
                    friday = dpd._dateYYMMDD;
                }
                else if (friday.compareTo(dpd._dateYYMMDD) > 0) {
                    friday = dpd._dateYYMMDD;
                }
            }
        }
        return friday;
    }


    //------------------------------------------------------------------------
    public static String getR1WsAsCSV(String[] fridays, D_FundInfo fi) {
        for (String friday: fridays) {
            System.out.println("friday: " + friday);
        }
        for (D_FundDPDay dpd: fi._dpDays) {
            System.out.println("dpdays: " + dpd._dateYYMMDD);
        }

        StringBuffer strb = new StringBuffer();
        boolean first = true;
        for (String friday: fridays) {
            if (!first) {
                strb.append(", ");
            }
            first = false;
            boolean found = false;
            for (D_FundDPDay dpd: fi._dpDays) {
                if (friday.equals(dpd._dateYYMMDD)) {
                    found = true;
                    if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
                        strb.append("-");
                    } else {
                        strb.append(String.valueOf(dpd._r1w));
                    }
                }
            }
            if (!found) {
                strb.append("-");
            }
        }
        return strb.toString();
    }

    //------------------------------------------------------------------------
    public static OTuple2G<Integer,Integer>  getStartAndEndP1Indexes(
            List<D_FundDPDay> dpds,
            String[] fridays) {
        OTuple2G<Integer,Integer> r = new OTuple2G<>();

        for(int i=0; i < dpds.size(); i++) {
            if (dpds.get(i)._dateYYMMDD.equals(fridays[0])) {
                r._o1 = i;
            }
            if (dpds.get(i)._dateYYMMDD.equals(fridays[fridays.length-1])) {
                r._o2 = i+1;
            }
            if (r._o1 != null && r._o2 != null) {
                break;
            }
        }
        return r;
    }

}
