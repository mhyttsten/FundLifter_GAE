package com.pf.fl.shared.datamodel;

import com.pf.fl.shared.utils.Compresser;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;
import com.pf.fl.shared.datamodel.D_Analyze_FundRank.D_Analyze_FundRankElement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class D_Analyze_FundRank_Serializer {
    private static final Logger log = Logger.getLogger(D_Analyze_FundRank_Serializer.class.getName());

    //------------------------------------------------------------------------
    public static byte[] crunch(List<D_Analyze_FundRank> l) {
        return crunch(l, true);
    }
    public static byte[] crunch(List<D_Analyze_FundRank> l, boolean compress) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);

        try {
            for (D_Analyze_FundRank fr : l) {
                D_Analyze_FundRank_Serializer.crunch(dout, fr);
            }
            dout.flush();
            byte[] data = bout.toByteArray();
            if (compress) {
                data = Compresser.dataCompress("FundRank", data);
            }
            return data;
        } catch(IOException exc) {
            throw new AssertionError("IOException caught: " + exc + "\n" + MM.getStackTraceString(exc));
        }
    }

    //------------------------------------------------------------------------
    public static List<D_Analyze_FundRank> decrunch(byte[] data) throws IOException {
        data = Compresser.dataUncompress(data);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(bin);
        List<D_Analyze_FundRank> l = new ArrayList<>();
        while (din.available() > 0) {
            D_Analyze_FundRank fr = D_Analyze_FundRank_Serializer.decrunch(din);
            l.add(fr);
        }
        return l;
    }

    //************************************************************************


    //------------------------------------------------------------------------
    public static final String TAG_FR_START = "DFFRS";
    public static final String TAG_FRE_START = "DPFRES";
    public static final String TAG_FRE_END = "DPFREE";
    public static void crunch(DataOutputStream dout_output, D_Analyze_FundRank fr) throws IOException {
//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//        DataOutputStream dout = new DataOutputStream(bout);
//        dout.writeUTF(fr._type);
//        dout.writeUTF(fr._friday);
//        dout.writeShort(fr._countTotal);
//
//        for (D_Analyze_FundRank.D_Analyze_FundRankElement fre : fr._frs) {
//            dout.writeUTF(TAG_FRE_START);
//            dout.writeUTF(fre._typeAndName);
//            dout.writeUTF(fre._friday);
//            dout.writeShort(fre._countTotal);
//            dout.writeShort(fre._countMissing);
//            dout.writeFloat(fre._rank);
//            dout.writeFloat(fre._r1w);
//        }
//
//        dout.flush();
//        byte[] data = bout.toByteArray();
//        dout_output.writeUTF(TAG_FR_START);
//        dout_output.writeInt(data.length);
//        dout_output.write(data);
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
    public static D_Analyze_FundRank decrunch(DataInputStream din_input) throws IOException {
//        IndentWriter iw = new IndentWriter();
//
        D_Analyze_FundRank fr = new D_Analyze_FundRank();
//        String tag = din_input.readUTF();
//        int length = din_input.readInt();
//        byte[] record = new byte[length];
//        int rlength = din_input.read(record);
//        if (rlength != length) {
//            throw new IOException("Not enough bytes to read entire record");
//        }
//
//        ByteArrayInputStream bin = new ByteArrayInputStream(record);
//        DataInputStream din = new DataInputStream(bin);
//
//        fr._type = din.readUTF();
//        fr._friday = din.readUTF();
//        fr._countTotal = din.readShort();
//
//        while (din.available() > 0) {
//            tag = din.readUTF();
//            if (tag.equals(TAG_FRE_START)) {
//                throw new IOException("Could not find start tag: " + tag);
//            }
//
//            D_Analyze_FundRankElement fre = new D_Analyze_FundRankElement();
//            fre._typeAndName = din.readUTF();
//            fre._friday = din.readUTF();
//            fre._countTotal = din.readShort();
//            fre._countMissing = din.readShort();
//            fre._rank = din.readFloat();
//            fre._r1w = din.readFloat();
//        }
//        assert din.available() == 0: "Did not end with 0 bytes left";
        return fr;
    }

}
