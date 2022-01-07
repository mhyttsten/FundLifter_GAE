package com.pf.fl.shared.datamodel;

import com.pf.fl.shared.Constants;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class D_FundInfo implements Cloneable {

    public static final int IC_NO_ERROR = 0;

    public static final int IC_COM_NO_URL_DATA = 101;
    public static final int IC_COM_NO_DECODABLE_DATA = 102;
    public static final int IC_INVALID_URL_IRRECOVERABLE = 103;
    public static final int IC_NO_RECENT_DPDAY = 104;

    public static final int IC_HTML_MS_DAILY_TABLE = 201;
    public static final int IC_HTML_MS_DAILY_SEK_CURRENCY = 202;
    public static final int IC_HTML_MS_RATING = 203;
    public static final int IC_HTML_MS_CATEGORY = 204;
    public static final int IC_HTML_MS_YEARLY_TABLE_YEAR = 210;
    public static final int IC_HTML_MS_YEARLY_TABLE_FUND = 211;
    public static final int IC_HTML_MS_YEARLY_TABLE_INDEX = 212;
    public static final int IC_HTML_MS_YEARLY_TABLE_CATEGORY = 213;
    public static final int IC_HTML_MS_YEARLY_TABLE_ROW_MISMATCH = 214;
    public static final int IC_HTML_MS_YEARLY_TABLE_NOT_FOUND = 215;
    public static final int IC_HTML_MS_DPDAY_NULLDATE = 220;
    public static final int IC_HTML_MS_FUNDNAME = 221;
    public static final int IC_HTML_MS_CURRENCY = 222;
    public static final int IC_HTML_MS_INDEX = 223;

    public static final int IC_VG_NOT_MF_NOR_ETF = 301;
    public static final int IC_HTML_VG_DATA_TICKER_MISMATCH = 302;
    public static final int IC_HTML_VG_TOTAL_RETURNS_NOT_FOUND = 303;
    public static final int IC_HTML_VG_DATE_NOT_FOUND = 304;
    public static final int IC_HTML_VG_DP_PARSING = 305;
    public static final int IC_HTML_VG_DP_ROWS_NO_INDEX = 306;
    public static final int IC_HTML_VG_DP_ROWS_INCONSISTENCY = 307;

    public String error2str() {
        switch(_errorCode) {
            case IC_NO_ERROR: return "No Error";
            case IC_COM_NO_URL_DATA: return "No URL Data";
            case IC_COM_NO_DECODABLE_DATA: return "No Decodable Data";
            case IC_INVALID_URL_IRRECOVERABLE: return "Invalid URL (irrecoverable)";
            case IC_NO_RECENT_DPDAY: return "No recent DPDay";
            case IC_HTML_MS_DAILY_TABLE: return "MS, HTML error on Daily Table";
            case IC_HTML_MS_DAILY_SEK_CURRENCY: return "MS, HTML SEK error";
            case IC_HTML_MS_RATING: return "MS, HTML rating error";
            case IC_HTML_MS_CATEGORY: return "MS, HTML category error";
            case IC_HTML_MS_YEARLY_TABLE_YEAR: return "MS, HTML yearly table year error";
            case IC_HTML_MS_YEARLY_TABLE_FUND: return "MS, HTML yearly table fund error";
            case IC_HTML_MS_YEARLY_TABLE_INDEX: return "MS, HTML yearly table index error";
            case IC_HTML_MS_YEARLY_TABLE_CATEGORY: return "MS, HTML yearly table category error";
            case IC_HTML_MS_DPDAY_NULLDATE: return "MS, HTML dpday null date error";
            case IC_VG_NOT_MF_NOR_ETF: return "VG, Not MF nor ETF";
            case IC_HTML_VG_DATA_TICKER_MISMATCH: return "VG, HTML data table mismatch error";
            case IC_HTML_VG_TOTAL_RETURNS_NOT_FOUND: return "VG, HTML total returns not found error";
            case IC_HTML_VG_DATE_NOT_FOUND: return "VG, HTML date not found error";
            case IC_HTML_VG_DP_PARSING: return "VG, HTML dp parsing error";
            case IC_HTML_VG_DP_ROWS_NO_INDEX: return "VG, HTML dp rows no index error";
            case IC_HTML_VG_DP_ROWS_INCONSISTENCY: return "VG, HTML dp rows inconsistency error";
            default: return "<Software error, unknown error code>";
        }
    }

    public static final String TYPE_PPM = "PPM";
    public static final String TYPE_SEB = "SEB";
    public static final String TYPE_SPP = "SPP";
    public static final String TYPE_VANGUARD = "VGD";
    public static final String TYPE_ALL = "ALL";
    public static final String TYPE_INVALID = "INVALID";
    public static final String[] TYPES = new String[] { TYPE_SEB, TYPE_SPP, TYPE_VANGUARD, TYPE_PPM };

    public boolean _notUsed; // But kept since DB structure stores / reads it
    public boolean _isValid = true;
    public int _errorCode;
    public String _lastExtractInfo = "<No Info>";

    public String _type = "";
    private String _nameMS = "";
    public void setNameMS(String s) { _nameMS = MM.htmlEntitiesRemove(s); }
    public String getNameMS() { return MM.htmlEntitiesRemove(_nameMS); }

    public String _url = "";
    private String _nameOrig = "";
    public void setNameOrig(String s) { _nameOrig = MM.htmlEntitiesRemove(s); }
    public String getNameOrig() { return MM.htmlEntitiesRemove(_nameOrig); }
    public String _dateYYMMDD_Updated = "";
    public String _dateYYMMDD_Update_Attempted = "";
    public int _msRating = -1;
    public String _ppmNumber = "";
    private String _categoryName = "";
    public void setCategoryName(String s) { _categoryName = MM.htmlEntitiesRemove(s); }
    public String getCategoryName() { return  MM.htmlEntitiesRemove(_categoryName); }
    private String _indexName = "";
    public void setIndexName(String s) { _indexName = MM.htmlEntitiesRemove(s); }
    public String getIndexName() { return MM.htmlEntitiesRemove(_indexName); }
    public String _currencyName = "";
    public List<D_FundDPDay>  _dpDays = new ArrayList<>();
    public List<D_FundDPYear> _dpYears = new ArrayList<>();

    public String getTypeAndName() { return _type + "." + getNameMS(); }
    public String getTypeAndNameURLEncoded() {
        try {
            return URLEncoder.encode(_type + "." + _nameMS, Constants.ENCODING_FILE_WRITE);
        } catch(UnsupportedEncodingException exc) {
            throw new AssertionError("Unsupported encoding: " + exc.toString());
        }
    }
    public void dumpInfo(IndentWriter iw) {
        iw.println("Fund: " + _type + "." + _nameMS);
        iw.push();
        iw.println("url: " + _url);
        iw.println("isV: " + _isValid);
        iw.println("ec: " + _errorCode);
        iw.println("lastExtractInfo: " + _lastExtractInfo);
        iw.println("t: " + _type);
        iw.println("nMS: " + getNameMS());
        iw.println("nOrig: " + getNameOrig());
        iw.println("updated: " + _dateYYMMDD_Updated);
        iw.println("updateAttempted: " + _dateYYMMDD_Update_Attempted);
        iw.println("rating: " + _msRating);
        iw.println("ppm: " + _ppmNumber);
        iw.println("category: " + getCategoryName());
        iw.println("index: " + getIndexName());
        iw.println("currency: " + _currencyName);

        iw.println("DPYears, length: " + _dpYears.size());
        iw.push();
        for (D_FundDPYear dpy: _dpYears) {
            dpy.dumpInfo(iw);
            iw.println();
        }
        iw.pop();

        iw.println("DPDays, length: " + _dpDays.size());
        iw.push();
        if (_dpDays.size() > 13) {
            for (int i=0; i < 12; i++) {
                D_FundDPDay dpd = _dpDays.get(i);
                dpd.dumpInfo(iw);
                iw.println();
            }
            iw.println("...");
            D_FundDPDay dpd = _dpDays.get(_dpDays.size()-1);
            dpd.dumpInfo(iw);
            iw.println();
        } else {
            for (D_FundDPDay dpd : _dpDays) {
                dpd.dumpInfo(iw);
                iw.println();
            }
        }
        iw.pop();
        iw.pop();
    }

    public String toString() {
        IndentWriter iw = new IndentWriter();
        dumpInfo(iw);
        return iw.toString();
    }

    public String toString(String nl) {
        IndentWriter iw = new IndentWriter();
        iw.setNewline(nl);
        dumpInfo(iw);
        return iw.toString();
    }

    private String dpday(int index) {
        if (_dpDays.size() < index+1) {
            return "null";
        }
        D_FundDPDay dpd = _dpDays.get(index);
        String r1w = dpd._r1w == D_FundDPDay.FLOAT_NULL ? "null" : String.format("%.2f", dpd._r1w);
        String r1m = dpd._r1m == D_FundDPDay.FLOAT_NULL ? "null" : String.format("%.2f", dpd._r1m);
        return dpd._dateYYMMDD + "," + dpd._dateYYMMDD_Actual + "," + r1w + "," + r1m;
    }
    public String getDPDOneLiner() {
        return dpday(0) + ", " + dpday(1) + ", " + dpday(2) + ", " + dpday(3);
    }
    public String getOneLiner() {
        StringBuffer strb = new StringBuffer();
        strb.append(getTypeAndName()
                + ", url: " + _url
                + ", ec: " + _errorCode
                + ", iv: " + _isValid
                + ", du: " + _dateYYMMDD_Updated
                + ", dua: " + _dateYYMMDD_Update_Attempted
                + ", mn: " + getNameMS()
                + ", on: " + getNameOrig()
                + ", mr: " + _msRating
                + ", p#: " + _ppmNumber
                + ", cur: " + _currencyName
                + ", 1:" + dpday(0)
                + ", 2:" + dpday(1)
                + ", 3:" + dpday(2)
                + ", 4:" + dpday(3)
                + ", in: " + getIndexName()
                + ", cn: " + getCategoryName());
        return strb.toString();
    }

    public D_FundInfo duplicate() throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        D_FundInfo_Serializer.crunch_D_FundInfo(dout, this);
        dout.flush();
        byte[] ba = bout.toByteArray();

        ByteArrayInputStream bin = new ByteArrayInputStream(ba);
        DataInputStream din = new DataInputStream(bin);
        return D_FundInfo_Serializer.decrunch_D_FundInfo(din);
    }

    public String getLastestDPDate() {
        if (_dpDays == null) return "null";
        if (_dpDays.size() == 0) return "";
        String date = _dpDays.get(0)._dateYYMMDD;
        if (date == null) return "null_date";
        return date;
    }

    //------------------------------------------------------------------------
    public D_FundInfo cloneMe() {
        try {
            D_FundInfo fi = (D_FundInfo)clone();
            List<D_FundDPDay> dpds = new ArrayList<>();
            List<D_FundDPYear> dpys = new ArrayList<>();
            fi._dpDays = dpds;
            fi._dpYears = dpys;
            for (D_FundDPDay dpd: _dpDays) {
                dpds.add(dpd.cloneMe());
            }
            for (D_FundDPYear dpy: _dpYears) {
                dpys.add(dpy.cloneMe());
            }
            return fi;
        } catch(CloneNotSupportedException exc) {
            throw new AssertionError("Assumed this was a clonable object");
        }
    }

    //------------------------------------------------------------------------
    public static List<D_FundInfo> cloneList(List<D_FundInfo> fis) {
        List<D_FundInfo> r = new ArrayList<>();
        for (D_FundInfo fi: fis) {
            r.add(fi.cloneMe());
        }
        return r;
    }

}
