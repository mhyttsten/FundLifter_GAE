package com.pf.fl.shared.fund_db_update;

import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.utils.MM;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class FundDBUpdate_TSV {

    //------------------------------------------------------------------------
    public static String update(List<D_FundInfo> fisFiles, String type, byte[] data) {
        System.out.println("FundDBUpdate_TSV, entry with: " + type);

        String c = null;
        try {
            c = new String(data, "UTF-8");
        } catch(UnsupportedEncodingException exc) {
            throw new AssertionError("UnsupportedCodingException caught");
        }
        List<String> lines = MM.splitIntoLines(c);
        if (lines == null) {
            return "Split lines returned null";
        }

        System.out.println("We have " + lines.size() + " entries to process");
        for (String line: lines) {
            if (line.startsWith("\"None") || line.startsWith("None")) {
                System.out.println("Found a comment line starting with None, skipping");
                System.out.println("...line: " + line);
                continue;
            }
            String[] items = line.split("\t");
            D_FundInfo fi = new D_FundInfo();
            fi._type = type;
            fi.setNameOrig(items[2]);
            fi.setNameMS(items[0]);
            fi._url = items[1];
            fi._msRating = -1;
            fi._ppmNumber = "";
            fisFiles.add(fi);
        }
        return null;
    }
}
