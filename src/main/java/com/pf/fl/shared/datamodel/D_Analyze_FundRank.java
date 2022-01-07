package com.pf.fl.shared.datamodel;

import java.util.ArrayList;
import java.util.List;

//------------------------------------------------------------------------
public class D_Analyze_FundRank {

    public String _type;
    public String[] _fridays;
    public List<List<D_Analyze_FundRankElement>> _frEachFriday = new ArrayList<>();
    public List<D_Analyze_FundRankElement>       _frSummaryForAllFridays = new ArrayList<>();  // 0=best

    //------------------------------------------------------------------------
    public static class D_Analyze_FundRankElement {
        public D_FundInfo _fi;       // Used when in memory
        public String _typeAndName;  // Used in files
        public List<String> _fridays = new ArrayList<>();

        public float _rank;       // Sum over all fridays, -1 if we don't have r1w
        public float _r1w = D_FundDPDay.FLOAT_NULL;  // Sum over all fridays
        public int _countMissing;  // Number of missing over friday range
        public int _countTotal;    // Total over friday range

        public String toString() { return getFridays(_fridays) + ", " + _fi.getTypeAndName() + ", rank: " + _rank + "(" + getAverageRank_2F() + "), r1w: " + _r1w + "(" + getAverageR1W_2F() + "), cT: " + _countTotal + ", cM: " + _countMissing; }
        public String getAverageRank_2F() { return String.format("%.2f", ((float)_rank)/((float)_countTotal)); }
        public String getAverageR1W_2F()  { return String.format("%.2f", _r1w/(float)_countTotal); }
    }

    private static String getFridays(List<String> fs) {
        if (fs.size() == 0) { return "<empty>"; }
        if (fs.size() == 1) { return fs.get(0); }
        StringBuffer strb = new StringBuffer();
        for (int i=0; i < fs.size(); i++) {
            strb.append(fs.get(i));
            if (i+1 < fs.size()) {
                strb.append(", ");
            }
        }
        return strb.toString();
    }
}
