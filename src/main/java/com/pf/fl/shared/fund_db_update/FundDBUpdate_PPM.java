package com.pf.fl.shared.fund_db_update;

import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;

import java.util.ArrayList;
import java.util.List;

public class FundDBUpdate_PPM {

    //------------------------------------------------------------------------
    public static String update(List<D_FundInfo> fisFiles,
                                String fileName,
                                byte[] data) throws Exception {
        System.out.println("FundDBUpdate_PPM, entered with filename: " + fileName);

        // Go through each files HTML content
        List<D_FundInfo> fisFilesTestExtraction = new ArrayList<>();
        byte[] fileDataBA = data;
        String fileDataStr = new String(fileDataBA, "UTF-8");

        // Parse the HTML content
        IndentWriter iw = new IndentWriter();
        List<D_FundInfo> fis = doItImpl(iw, fileDataStr);
        if (fis == null) {
            return "Parsing: " + fileName + ", returned null as result\nDetailed debug:\n" + iw.getString();
        }
        fisFiles.addAll(fis);
        return null;
    }

    //------------------------------------------------------------------------
    private static List<D_FundInfo> doItImpl(IndentWriter iw, String html) throws Exception {
        if (iw == null) iw = new IndentWriter();

        List<D_FundInfo> r = new ArrayList<>();

        // Get the table containing the fund entries
        int io1 = html.indexOf("<table cellspacing=\"0\" class=\"rgMasterTable\"");
        if (io1 == -1)  {
            iw.println("Tag not found: " + "<table...");
            return null;
        }
        html = html.substring(io1);
        io1 = html.indexOf("<tbody>");
        if (io1 == -1)  {
            iw.println("Tag not found: " + "<tbody>");
            return null;
        }
        html = html.substring(io1);
        io1 = html.indexOf("</tbody>");
        if (io1 == -1)  {
            iw.println("Tag not found: " + "</tbody>");
            return null;
        }
        html = html.substring(0, io1);


        while (true) {
//            System.out.println(MM.getString(html, 1000));

            io1 = html.indexOf("<tr");
            if (io1 == -1) {
                break;
            }
            html.substring(io1);

            String t = "<a href=\"";
            io1 = html.indexOf(t);
            if (io1 == -1) {
                iw.println("Tag not found: <a href=...");
                return null;
            }
            html = html.substring(io1+t.length());

            // URL
            t = "\">";
            io1 = html.indexOf(t);
            if (io1 == -1) {
                iw.println("Tag not found: " + t);
                return null;
            }
            String url = html.substring(0, io1);
            url = MM.htmlReplaceHTMLCodes(url).trim();
            html = html.substring(io1+t.length());

            // Name
            t = "</a>";
            io1 = html.indexOf(t);
            if (io1 == -1) {
                iw.println("Tag not found: " + t);
                return null;
            }
            String name = html.substring(0, io1);
            name = MM.htmlReplaceHTMLCodes(name).trim();
            html = html.substring(io1+t.length());

            D_FundInfo fi = new D_FundInfo();
            fi._type = D_FundInfo.TYPE_PPM;
            fi.setNameOrig(name);
            fi.setNameMS(fi.getNameOrig());
            fi._url = url;
            fi._msRating = -1;
            fi._ppmNumber = "";
//            System.out.println("Adding: " + fi.getOneLiner());

            r.add(fi);
        }
        return r;
    }
}
