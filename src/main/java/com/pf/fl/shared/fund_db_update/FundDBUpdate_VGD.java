package com.pf.fl.shared.fund_db_update;

import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.DB_FundInfo;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.datamodel.D_FundInfo_Validator;
import com.pf.fl.shared.extract.ExtractFromHTML_Helper;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FundDBUpdate_VGD {

    //------------------------------------------------------------------------
    public static String update(List<D_FundInfo> fisFiles,
                                String fileName,
                                byte[] data) throws Exception {
        System.out.println("\nFundDBUpdate_VGD, entered with filename: " + fileName);

        // Go through each files HTML content
        List<D_FundInfo> fisFilesTestExtraction = new ArrayList<>();
        byte[] fileDataBA = data;
        String fileDataStr = new String(fileDataBA, "Windows-1252");

        // Reason is Vanguard HTML content needs specific parsing
        boolean isVanguard = false;
        if (fileName.toLowerCase().contains("vanguard")) {
            isVanguard = true;
        }

        // Parse the HTML content
        IndentWriter iw = new IndentWriter();
        List<D_FundInfo> fis = doItImpl(iw, fileDataStr, isVanguard);
        if (fis == null) {
            return "Parsing: " + fileName + "returned null as result\nDetailed debug:\n" + iw.getString();
        }

//        System.out.println("\nRead file: " + fileName + ", found: " + fis.size() + " entries");
        for (int i=0; i < fis.size(); i++) {
            D_FundInfo fi = fis.get(i);
//            System.out.println((i+1) + "/" + fis.size() + " Orig: " + fi._nameOrig + ", MS: " + fi._nameMS + ", " + fi._url);
        }
//        System.out.println("...done reading file: " + fileName + "\n");

        fisFiles.addAll(fis);
        return null;
    }

    //------------------------------------------------------------------------
    private static List<D_FundInfo> doItImpl(IndentWriter iw, String html, boolean isVanguard) throws Exception {
        if (iw == null) iw = new IndentWriter();

        List<D_FundInfo> l = new ArrayList<>();

        int count = 0;
        boolean first = true;
        while (true) {
            int io1 = -1;
            int io2 = -1;
            String stag = null;
            String etag = null;

            // Start of fund entry
            stag = "<a href=\"https://personal.vanguard.com/us/funds/snapshot?";
            // <a href=\"https://personal.vanguard.com/us/funds/snapshot?FundId=Y914&amp;FundIntExt=EXT">

            io1 = html.indexOf(stag);
            if (io1 == -1) {
                iw.println("No more <a href, total count: " + count);
                return l;
            }
            html = html.substring(io1);

            if (first) {
                first = false;
                stag = "</table>";
                io1 = html.indexOf(stag);
                if (io1 == -1) {
                    iw.println("Could not find </table>");
                    return null;
                }
                html = html.substring(0, io1);
            }

            // Name
            stag = ">";
            etag = "</a>";
            io1 = html.indexOf(stag);
            io2 = html.indexOf(etag);
            if (io1 == -1 || io2 == -1) {
                iw.println("Could not find start of name: " + io1 + ", or end tag: " + io2);
                return null;
            }
            String name = html.substring(io1 + stag.length(), io2);
            name = name.replace("<span>", "");
            name = name.replace("</span>", "");
            html = html.substring(io2+etag.length());

            // Ticker
            if (isVanguard) {
                stag = "class=\"ng-binding\">";
                io1 = html.indexOf(stag);
                html = html.substring(io1+stag.length());
            } else {
                io1 = html.indexOf("<td");
                html = html.substring(io1);
                io1 = html.indexOf(">");
                html = html.substring(io1+1);
            }
            if (io1 == -1) {
                iw.println("Could not find start ticker name");
                return null;
            }
            stag = "</td>";
            io1 = html.indexOf(stag);
            if (io1 == -1) {
                iw.println("Could not find end of ticker");
                return null;
            }
            String ticker = html.substring(0, io1);
            if (ticker.length() > 5) {
                iw.println("Ticker did not have length 5: " + ticker);
                return null;
            }

            String url = url_getVanguard(name, ticker);
            D_FundInfo fi = new D_FundInfo();
            fi._type = D_FundInfo.TYPE_VANGUARD;
            fi.setNameOrig(name + " (" + ticker + ")");
            fi.setNameMS(fi.getNameOrig());
            fi._url = url;
            fi._msRating = -1;
            fi._ppmNumber = "";

            String today = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
            String lfriday = MM.tgif_getLastFridayTodayExcl(today);

            fi._dateYYMMDD_Updated = lfriday;
            fi._dateYYMMDD_Update_Attempted = lfriday;
            l.add(fi);

            // System.out.println("Found: " + fi.getTypeAndName() + ", url: " + fi._url);
            count++;
        }
    }

    //--------------------------------------------------------------
    public static final String url_getVanguard(String name, String ticker) {
//        int tEnd = name.lastIndexOf(")");
//        int tStart = name.lastIndexOf("(");
//        if (tEnd == -1 || tStart == -1) {
//            return null;
//        }
//        String ticker = name.substring(tStart+1, tEnd);

        String vgURL = null;
        if (name.toLowerCase().contains("etf")) {
            vgURL = "https://performance.morningstar.com/perform/Performance/etf/trailing-total-returns.action?"
                    + "&t=" + ticker
                    + "&region=usa&culture=en-US&cur=&ops=clear&s=0P00001MJB&ndec=2&ep=true&align=d&annlz=true&comparisonRemove=false&loccat=&taxadj=&benchmarkSecId=&benchmarktype=";
        } else {
            vgURL = "https://performance.morningstar.com/perform/Performance/fund/trailing-total-returns.action?&t=XNAS:"
                    + ticker
                    + "&region=usa&culture=en-US&cur=&ops=clear&s=0P00001MJB&ndec=2&ep=true&align=d&annlz=true&comparisonRemove=false&loccat=&taxadj=&benchmarkSecId=&benchmarktype=";
        }
        return vgURL;
    }

}
