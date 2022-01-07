package com.pf.fl.appengine.jsphelper;

import com.pf.fl.shared.utils.MM;

import java.util.logging.Logger;

public class JSP_Helper_Test {
    private static final Logger log = Logger.getLogger(JSP_Helper_Test.class.getName());
    private static final String TAG = MM.getClassName(JSP_Helper_Test.class.getName());

//
//    public static String displayAllFundNames() throws Exception {
//        StringBuilder strb = new StringBuilder();
//        Iterator<FLA_FundInfo> i = ofy().load().type(FLA_FundInfo.class).filter("mType", "SEB").iterator();
//        List<String> r = new ArrayList<>();
//        while (i.hasNext()) {
//            FLA_FundInfo fi = i.next();
//            r.add(fi.mType + "." + fi.mName);
//        }
//        Collections.sort(r);
//        for (String s: r) {
//            strb.append(s + "<br>");
//        }
//        ofy().clear();
//        return strb.toString();
//    }
//
//    /**
//     *
//     */
//    public static String showEuroCharacterTest() throws Exception {
//        List<FLA_Cache_FundInfo> cfil = FLA_Cache.cacheFundInfosByTypeOrNull(null);
//        StringBuilder strb = new StringBuilder();
//        for (int i = 0; i < cfil.size(); i++) {
//            FLA_Cache_FundInfo cfi = cfil.get(i);
//            if (cfi.mName.startsWith("Franklin European Growth A Acc")) {
//                strb.append("Cache: " + cfi.mName + "<br>");
//                strb.append(MM.bytesToHexDumpString(cfi.mName.getBytes(), "<br>"));
//
//                FLA_FundInfo fi = ofy().load().type(FLA_FundInfo.class).id(cfi.mFundInfoId.longValue()).now();
//                strb.append("<br>");
//                strb.append("Real: " + fi.mName + "<br>");
//                strb.append(MM.bytesToHexDumpString(fi.mName.getBytes(), "<br>"));
//
//                byte[] data = MM.getURLContentBA(fi.mURL);
//                byte[] franklin = "Franklin".getBytes();
//
//                strb.append("<br>");
//                int index = 0;
//                index = MM.getByteMatchesStartIndex(data, franklin, index);
//                if (index != -1) {
//                    byte[] newdata = new byte[43];
//                    System.arraycopy(data, index, newdata, 0, 43);
//                    strb.append(MM.bytesToHexDumpString(newdata, "<br>"));
//                    strb.append("<br>");
//                    String newDataStr1 = new String(newdata);
//                    String newDataStr2 = new String(newdata, EE.ENCODING_HTTP_READ);
//                    strb.append(newDataStr1);
//                    strb.append("<br>");
//                    strb.append(newDataStr2);
//                    byte[] newDataStr2BA = newDataStr2.getBytes(EE.ENCODING_FILE_WRITE);
//                    strb.append("<br>");
//                    strb.append(MM.bytesToHexDumpString(newDataStr2BA, "<br>"));
//                } else {
//                    strb.append("Could not find sequence from Internet request");
//                }
//            }
//        }
//
//        return strb.toString();
//    }
}

