package com.pf.fl.shared.datamodel;

import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class D_FundInfo_Validator {

    private List<D_FundInfo> _fis;

    public IndentWriter _iwErrors = new IndentWriter();
    public boolean _error;

    public String _fridayMin_YYMMDD;
    public String _fridayMax_YYMMDD;

    //------------------------------------------------------------------------
    public D_FundInfo_Validator(List<D_FundInfo> fis) {
        _fis = fis;
    }
    public D_FundInfo_Validator() { }

    //------------------------------------------------------------------------
    public void process() {
        for (D_FundInfo fi: _fis) {
            validateFund(fi);
        }
    }

    //------------------------------------------------------------------------
    public void validateFund(D_FundInfo fi) {
        List<D_FundDPDay> dpds = fi._dpDays;
        if (dpds == null || dpds.size() == 0) return;

        if (fi.getNameMS() == null) {
            _iwErrors.println("*** Field _nameMS became null");
            _error = true;
        }
        if (fi.getNameMS().toLowerCase().contains("ett fel uppstod")) {
            _iwErrors.println("*** Field _nameMS became \"ett fel uppstod\"");
            _error = true;
        }

        String last = null;
        for (D_FundDPDay dp: fi._dpDays) {
            String s = dp._dateYYMMDD;
            String s2 = dp._dateYYMMDD_Actual;

            if (s == null || s.length() != 6 || s2 == null || s2.length() != 6) {
                _iwErrors.println("*** A date was null or had length != 6 for fund\n" + fi.toString());
                _error = true;
                return;
            }

            if (!MM.tgif_isFriday(s)) {
                _iwErrors.println("*** A D_FundDPDay was not a friday for fund\n" + fi.toString());
                _error = true;
                return;
            }

            if (_fridayMax_YYMMDD == null || s.compareTo(_fridayMax_YYMMDD) > 0) _fridayMax_YYMMDD = s;
            if (_fridayMin_YYMMDD == null || s.compareTo(_fridayMin_YYMMDD) < 0) _fridayMin_YYMMDD = s;
            if (last == null) {
                last = s;
            } else if (s.compareTo(last) >= 0) {
                _iwErrors.println("*** Order of D_FundDPDay was not correct\n" + fi.toString());
                _error = true;
                return;
            }
        }
    }
}
