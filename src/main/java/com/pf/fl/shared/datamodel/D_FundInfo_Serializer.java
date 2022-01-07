package com.pf.fl.shared.datamodel;

import com.pf.fl.shared.utils.Compresser;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class D_FundInfo_Serializer {
    private static final Logger log = Logger.getLogger(D_FundInfo_Serializer.class.getName());

    //------------------------------------------------------------------------
    public static byte[] crunchFundList(List<D_FundInfo> l) {
        return crunchFundList(l, true);
    }
    public static byte[] crunchFundList(List<D_FundInfo> l, boolean compress) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);

        try {
            for (D_FundInfo fi : l) {
                D_FundInfo_Serializer.crunch_D_FundInfo(dout, fi);
            }
            dout.flush();
            byte[] data = bout.toByteArray();
            if (compress) {
                data = Compresser.dataCompress("FundList", data);
            }
            return data;
        } catch(IOException exc) {
            throw new AssertionError("IOException caught: " + exc + "\n" + MM.getStackTraceString(exc));
        }
    }

    //------------------------------------------------------------------------
    public static List<D_FundInfo> decrunchFundList(byte[] data) throws IOException {
        data = Compresser.dataUncompress(data);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(bin);
        List<D_FundInfo> l = new ArrayList<>();
        while (din.available() > 0) {
            D_FundInfo fi = D_FundInfo_Serializer.decrunch_D_FundInfo(din);
            l.add(fi);
        }
        return l;
    }

    //************************************************************************

    //------------------------------------------------------------------------
    public static final String TAG_POR_START = "DPORS";
    public static D_Portfolio decrunch_D_Portfolio(DataInputStream din) throws IOException {
        if (din.available() <= 0) {
            return null;
        }
        String tag = din.readUTF();
        if (!tag.equals(TAG_POR_START)) {
            throw new IOException("Could not find tag, found: " + tag);
        }
        D_Portfolio p = new D_Portfolio();
        p._name = din.readUTF();
        int urlCount = din.readInt();
        for (int i=0; i < urlCount; i++) {
            String url = din.readUTF();
            p._urls.add(url);
        }
        return p;
    }

    //------------------------------------------------------------------------
    public static void crunch_D_Portfolio(DataOutputStream dout, D_Portfolio p) throws IOException {
        dout.writeUTF(TAG_POR_START);
        dout.writeUTF(p._name);
        dout.writeInt(p._urls.size());
        for (String url: p._urls) {
            dout.writeUTF(url);
        }
    }

    //************************************************************************


    //------------------------------------------------------------------------
    public static final String TAG_FI_START = "DFIS";
    public static final String TAG_DPD_START = "DPDS";
    public static final String TAG_FI_END = "DFIE";
    public static final String TAG_DPY_START = "DPYS";
    public static void crunch_D_FundInfo(DataOutputStream dout_output, D_FundInfo fi) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        dout.writeBoolean(fi._notUsed);
        dout.writeUTF(fi._url);
        dout.writeBoolean(fi._isValid);
        dout.writeInt(fi._errorCode);
        dout.writeUTF(fi._lastExtractInfo);
        dout.writeUTF(fi._type);
        dout.writeUTF(fi.getNameMS());
        dout.writeUTF(fi.getNameOrig());
        dout.writeUTF(fi._dateYYMMDD_Updated);
        dout.writeUTF(fi._dateYYMMDD_Update_Attempted);
        dout.writeInt(fi._msRating);
        dout.writeUTF(fi._ppmNumber);
        dout.writeUTF(fi.getCategoryName());
        dout.writeUTF(fi.getIndexName());
        dout.writeUTF(fi._currencyName);

        for (D_FundDPYear dpy : fi._dpYears) {
            dout.writeUTF(TAG_DPY_START);
            dout.writeShort(dpy._year);
            dout.writeFloat(dpy._resultFund);
            dout.writeFloat(dpy._resultCategory);
            dout.writeFloat(dpy._resultIndex);
        }

        for (D_FundDPDay dpd : fi._dpDays) {
            dout.writeUTF(TAG_DPD_START);
            dout.writeUTF(dpd._dateYYMMDD);
            dout.writeUTF(dpd._dateYYMMDD_Actual);
            dout.writeUTF(dpd._currency);
            dout.writeFloat(dpd._r1d);
            dout.writeFloat(dpd._r1w);
            dout.writeFloat(dpd._r1m);
            dout.writeFloat(dpd._r3m);
            dout.writeFloat(dpd._r6m);
            dout.writeFloat(dpd._r1y);
            dout.writeFloat(dpd._r3y);
            dout.writeFloat(dpd._r5y);
            dout.writeFloat(dpd._r10y);
            dout.writeFloat(dpd._rYTDFund);
            dout.writeFloat(dpd._rYTDCategory);
            dout.writeFloat(dpd._rYTDIndex);
        }
        dout.writeUTF(TAG_FI_END);

        dout.flush();
        byte[] data = bout.toByteArray();
        dout_output.writeUTF(TAG_FI_START);
        dout_output.writeInt(data.length);
        dout_output.write(data);
    }

    private static IndentWriter _iw;
    private static void debug(String s) {
//        if (_iw == null) {
//            _iw = new IndentWriter();
//        }
//        _iw.println(s);
        log.info(s);
    }
    private static void debug_end() {

    }

    //------------------------------------------------------------------------
    public static D_FundInfo decrunch_D_FundInfo(DataInputStream din_input) throws IOException {
        IndentWriter iw = new IndentWriter();

        D_FundInfo fi = new D_FundInfo();
//        debug("Decrunching D_FundInfo");

        String tag = din_input.readUTF();
//        debug(tag);

        int length = din_input.readInt();
//        debug(String.valueOf(length));

        byte[] record = new byte[length];
//        debug(MM.bytesToHexDumpString(record, "\n"));

        int rlength = din_input.read(record);
        if (rlength != length) {
            throw new IOException("Not enough bytes to read entire record");
        }

        ByteArrayInputStream bin = new ByteArrayInputStream(record);
        DataInputStream din = new DataInputStream(bin);

        fi._notUsed = din.readBoolean();
        fi._url = din.readUTF();
        fi._isValid = din.readBoolean();
        fi._errorCode = din.readInt();
        fi._lastExtractInfo = din.readUTF();
        fi._type = din.readUTF();
        fi.setNameMS(din.readUTF());
        fi.setNameOrig(din.readUTF());
        fi._dateYYMMDD_Updated = din.readUTF();
        fi._dateYYMMDD_Update_Attempted = din.readUTF();
        fi._msRating = din.readInt();
        fi._ppmNumber = din.readUTF();
        fi.setCategoryName(din.readUTF());
        fi.setIndexName(din.readUTF());
        fi._currencyName = din.readUTF();

        while (true) {
            tag = din.readUTF();
            if (tag.equals(TAG_FI_END)) {
                break;
            }

            if (tag.equals(TAG_DPY_START)) {
                D_FundDPYear dpy = new D_FundDPYear();
                fi._dpYears.add(dpy);
                dpy._year = din.readShort();
                dpy._resultFund = din.readFloat();
                dpy._resultCategory = din.readFloat();
                dpy._resultIndex = din.readFloat();
            }

            else if (tag.equals(TAG_DPD_START)) {
                D_FundDPDay dpd = new D_FundDPDay();
                fi._dpDays.add(dpd);
                dpd._dateYYMMDD = din.readUTF();
                dpd._dateYYMMDD_Actual = din.readUTF();
                dpd._currency = din.readUTF();
                dpd._r1d = din.readFloat();
                dpd._r1w = din.readFloat();
                dpd._r1m = din.readFloat();
                dpd._r3m = din.readFloat();
                dpd._r6m = din.readFloat();
                dpd._r1y = din.readFloat();
                dpd._r3y = din.readFloat();
                dpd._r5y = din.readFloat();
                dpd._r10y = din.readFloat();
                dpd._rYTDFund = din.readFloat();
                dpd._rYTDCategory = din.readFloat();
                dpd._rYTDIndex = din.readFloat();
            }

            else {
                throw new AssertionError("Unlexpected tag: " + tag + " for fund: " + fi.getTypeAndName());
            }
        }
        assert din.available() == 0: "Did not end with 0 bytes left";
        return fi;
    }

    //************************************************************************

    private static float D2f(Double d) {
        if (d == null) {
            return D_FundDPDay.FLOAT_NULL;
        }
        return (float)d.doubleValue();
    }
}
