package com.pf.fl.shared.analyze;

import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.utils.D_Utils;
import com.pf.fl.shared.utils.MM;

import java.util.ArrayList;
import java.util.List;

public class DPSeries {
    public String _name;
    public List<DPFriday> _dps = new ArrayList<>();

    public float getReturnAcc() {
        float r = .0F;
        for (DPFriday dpf: _dps) {
            if (dpf._r1w != D_FundDPDay.FLOAT_NULL) {
                r += dpf._r1w;
            }
        }
        return r;
    }
    public int getCountTotal() {
        int c = 0;
        for (DPFriday dpf: _dps) {
            c += dpf.countTotal;
        }
        return c;
    }
    public int getCountMissing() {
        int c = 0;
        for (DPFriday dpf: _dps) {
            c += dpf.countMissing;
        }
        return c;
    }

    public static class DPFriday {
        public String _fridayYYMMDD;
        public float _r1w = D_FundDPDay.FLOAT_NULL;
        public int countTotal;    // Number of values _r1w was deduced from
        public int countMissing;  // Of countTotal, number of _r1ws that were null and not used
    }

    // ***********************************************************************
    // Support functions to get a DPSeries

    //------------------------------------------------------------------------
    public static DPSeries getDPSeriesForDPSerieses(String name, List<DPSeries> dpss) {
        // Observe:
        //   - This algorithm assumes equal investment distribution across all series
        //   - It returns the absolute change (not the average) across investments

        DPSeries r = new DPSeries();
        r._name = name;

        if (dpss == null) {
            return null;
        }
        if (dpss.size() == 0) {
            return r;
        }

        boolean first = true;
        String[] fridays = null;
        float[] value = null;
        int[] countTotal = null;
        int[] countMissing = null;
        for (DPSeries dps: dpss) {
            List<DPFriday> dpfs = dps._dps;
            if (first) {
                fridays = MM.array_String(dpfs.size(), null);
                countTotal =  MM.array_int(dpfs.size(), 0);
                countMissing = MM.array_int(dpfs.size(), 0);
                value =  MM.array_float(dpfs.size(), 0.0F);
            }

            if (dpfs.size() != fridays.length) {
                throw new AssertionError("Different sequence lengths across DPSeriess");
            }

            for (int i=0; i < dpfs.size(); i++) {
                DPFriday dpf = dpfs.get(i);
                if (first) {
                    fridays[i] = dpf._fridayYYMMDD;
                }
                else if (!fridays[i].equals(dpf._fridayYYMMDD)){
                    throw new AssertionError("Friday mismatch");
                }
                countTotal[i]++;
                if (dpf._r1w != D_FundDPDay.FLOAT_NULL) {
                    value[i] += dpf._r1w;
                } else {
                    countMissing[i]++;
                }
            }
            first = false;

        }

        for (int i=0; i < fridays.length; i++) {
            DPFriday dpf = new DPFriday();
            dpf._fridayYYMMDD = fridays[i];
            if (countTotal[i] == countMissing[i]) {
                dpf._r1w = D_FundDPDay.FLOAT_NULL;
            }
            else {
                dpf._r1w = value[i] / (float)(countTotal[i] - countMissing[i]);
            }
            dpf.countMissing = countMissing[i];
            dpf.countTotal = countTotal[i];
            r._dps.add(dpf);
        }
        return r;
    }

    //------------------------------------------------------------------------
    public static List<DPSeries> getDPSeriesForFunds(List<D_FundInfo> fis, int fridayCount) {
        List<DPSeries> dpss = new ArrayList<>();
        for (D_FundInfo fi: fis) {
            dpss.add(getDPSeriesForFund(fi, fridayCount));
        }
        return dpss;
    }

    //------------------------------------------------------------------------
    public static DPSeries getDPSeriesForFund(D_FundInfo fi, int fridayCount) {
        if (fridayCount == 0) return null;

        List<String> fridays = new ArrayList<>();
        String curr = D_Utils.getLastExtractedFriday();
        fridays.add(curr);

        for (int i=0; i < (fridayCount-1); i++) {
            curr = MM.tgif_getLastFridayTodayExcl(curr);
            fridays.add(curr);
        }

        DPSeries r = new DPSeries();
        r._name = fi.getNameMS();
        for (String f: fridays) {
            List<D_FundDPDay> dpds = fi._dpDays;
            float r1w = D_FundDPDay.FLOAT_NULL;
            int countMissing = 0;
            for (D_FundDPDay dpd : dpds) {
                // We could not find this friday, break out early
                if (f.compareTo(dpd._dateYYMMDD) > 0) {
                    countMissing = 1;
                    break;
                }
                // Friday found
                else if (f.compareTo(dpd._dateYYMMDD) == 0) {
                    if (dpd._r1w != D_FundDPDay.FLOAT_NULL) {
                        r1w = dpd._r1w;
                    } else {
                        countMissing = 1;
                    }
                    break;
                }
            }
            DPFriday df = new DPFriday();
            df._fridayYYMMDD = f;
            df._r1w = r1w;
            df.countTotal = 1;
            df.countMissing = countMissing;
            r._dps.add(df);
        }
        return r;
    }
}

