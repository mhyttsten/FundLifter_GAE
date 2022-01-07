package com.pf.fl.shared.fund_db_update;

import com.pf.fl.shared.utils.HtmlRetriever;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;

public class VGD_GetFundName {

//    //------------------------------------------------------------------------
//    public static void main(String[] args) {
//        try {
//            IndentWriter iw = new IndentWriter();
//            String s = getVGDName(iw, "Energy (VGHCX)");
//            System.out.println("Result: " + s);
//            System.out.println("Debug:\n" + iw.getString());
//        } catch(Exception exc) {
//            exc.printStackTrace();
//        }
//    }
//
//    //------------------------------------------------------------------------
//    public static String getVGDName(IndentWriter iw, String fiName) throws Exception {
//        if (iw == null) {
//            iw = new IndentWriter();
//        }
//
//        iw.println("Trying to get VGD name from fund info url: " + fiName);
//        int io1 = fiName.lastIndexOf("(");
//        int io2 = fiName.lastIndexOf(")");
//        if (io1 == -1 || io2 == -1 || io1 + 1 >= fiName.length()) {
//            iw.println("Could not find last ( or ) or could not contain any ticker");
//            return null;
//        }
//        String ticker = fiName.substring(io1+1, io2);
//        String url = "https://www.morningstar.com/funds/xnas/" + ticker + "/quote.html";
////        String url = "https://www.googleapis.com/customsearch/v1?key=" + API_KEY + "&q=" + ticker;
////        String url = "https://www.google.com/search?q=" + ticker;
//        iw.println("Trying to get VGD name through ticker URL: " + url);
//        byte[] htmlDataRaw = HtmlRetriever.htmlGet(
//                iw,
//                url,
//                5000,
//                4);
//        if (htmlDataRaw == null || htmlDataRaw.length == 0) {
//            iw.println("HTTP GET call returned null or 0 bytes");
//            return null;
//        }
//        String htmlDataString = MM.newString(htmlDataRaw, "UTF-8");
//        if (htmlDataString == null || htmlDataString.length() == 0) {
//            iw.println("Data from HTTP GET could not be decoded with UTF-8");
//            return null;
//        }
//
//        String startTag = "<meta name=\"securityName\" content=\"";
//        String endTag = "\" />";
//        io1 = htmlDataString.indexOf(startTag);
//        if (io1 == -1 || io1+1 >= htmlDataString.length()) {
//            iw.println("Could not find start tag: " + startTag);
//            return null;
//        }
//        htmlDataString = htmlDataString.substring(io1+startTag.length());
//        io1 = htmlDataString.indexOf(endTag);
//        if (io1 == -1 || io1+1 >= htmlDataString.length()) {
//            iw.println("Could not find end tag: " + endTag);
//            return null;
//        }
//        String result = htmlDataString.substring(0, io1).trim();
//        iw.println("Resulting name is: [" + result + "]");
//        return result;
//    }
}
