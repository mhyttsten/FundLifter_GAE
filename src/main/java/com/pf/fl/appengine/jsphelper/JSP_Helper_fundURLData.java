package com.pf.fl.appengine.jsphelper;

import com.pf.fl.shared.datamodel.DB_FundInfo;
import com.pf.fl.appengine.extract.GCSWrapper;
import com.pf.fl.shared.Constants;
import com.pf.fl.shared.utils.HtmlManipulator;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;
import com.pf.fl.shared.utils.OTuple2G;
import com.pf.fl.shared.datamodel.D_FundInfo;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by magnushyttsten on 11/8/14.
 */
public class JSP_Helper_fundURLData {
    private static final Logger log = Logger.getLogger(JSP_Helper_fundURLData.class.getName());
    private static final String TAG = MM.getClassName(JSP_Helper_fundURLData.class.getName());

    public static final int FL_MODE_EMPTIES = 1;
    public static final int FL_MODE_FILL_EXISTING = 2;

    //------------------------------------------------------------------------
    public static String urlControl_DisplayFundEmptiesAndList(List<D_FundInfo> l) throws Exception {
        log.info("*** JSP_Helper_fundURLData.urlControl_DisplayFundEmptiesAndList");


        StringBuilder strb = new StringBuilder();

        int count = 0;
        for (int i = 0; i < 5; i++) {
            urlControl_DisplayFund(
                    true,
                    null,
                    false,
                    false,
                    strb, FL_MODE_EMPTIES,
                    null,
                    null,
                    count,
                    -1);
            count++;
        }

        for (int i=0; i < l.size(); i++) {
            D_FundInfo cfi = l.get(i);
            urlControl_DisplayFund(
                    true,
                    null,
                    false,
                    false,
                     strb,
                    FL_MODE_FILL_EXISTING,
                    URLEncoder.encode(cfi.getTypeAndName(), Constants.ENCODING_FILE_READ),
                    cfi,
                    count,
                    i);
            count++;
        }

        return strb.toString();
    }

    //------------------------------------------------------------------------
    public static String urlControl_VerifyOrExecute(
            boolean isVerify,
            HttpServletRequest r) throws Exception {

        String verifyStr = "VERIFY";
        if (!isVerify) {
            verifyStr = "COMMIT";
        }
        log.info("*** JSP_Helper_fundURLData.urlControl_VerifyOrExecute: " + verifyStr);

        StringBuilder strb = new StringBuilder();
        int count = 0;
        int i = -1;
        boolean shownInserts = false;
        boolean shownUpdates = false;
        boolean needsSaving = false;

        while (true) {
            i++;
            // ee.dinfo(log, TAG, "At index: " + i + "................................................");

//            StringBuilder tmpsb = new StringBuilder();
//            Enumeration<String> pnames = r.getParameterNames();
//            while (pnames.hasMoreElements()) {
//                String s = pnames.nextElement();
//                log.info("verifyOrExecute, s is: " + s);
//                String[] sparts = s.split("_");
//                if (sparts == null) {
//                    log.info("...sparts == null");
//                } else {
//                    log.info("...sparts.length == " + sparts.length);
//                    for (String tmps: sparts) {
//                        log.info("......elem: " + tmps);
//                    }
//                }
//
//                if (sparts.length >= 2 && sparts[2].equals(String.valueOf(i))) {  // Bombing w ArrayIndexOutOfBounds
//                    tmpsb.append(s + "\n");
//                }
//            }
            // ee.dinfo(log, TAG, "Parameter names:\n" + tmpsb.toString());

            String p_idStr = JSP_Constants.PARAM2_ID_ + String.valueOf(i);
            String v_idStr = r.getParameter(p_idStr);
            String p_delete = JSP_Constants.PARAM2_DELETE_ + String.valueOf(i);
            String v_delete = r.getParameter(p_delete);
            String p_setvalid = JSP_Constants.PARAM2_SETVALID_ + String.valueOf(i);
            String v_setvalid = r.getParameter(p_setvalid);
            String p_type = JSP_Constants.PARAM2_TYPE_ + String.valueOf(i);
            String v_type = r.getParameter(p_type);
            String p_name = JSP_Constants.PARAM2_NAME_ + String.valueOf(i);
            String v_name = r.getParameter(p_name);
            String p_url = JSP_Constants.PARAM2_URL_ + String.valueOf(i);
            String v_url = r.getParameter(p_url);

            // If everything is empty then we are done
            if (v_idStr == null
                    && v_delete == null
                    && v_type == null
                    && v_setvalid == null
                    && v_name == null
                    && v_url == null) {
                break;
            }

            if (!isVerify) {
                log.info("urlControl_VerifyOrExecute, COMMIT\nInput parameters"
                        + "\nid: " + v_idStr
                        + "\ndelete: " + v_delete
                        + "\nsetvalid: " + v_setvalid
                        + "\ntype: " + v_type
                        + "\nname: " + v_name
                        + "\nurl: " + v_url);
            }

            D_FundInfo fi = null;
            // Updating fund
            if (v_idStr != null) {
                v_idStr = URLDecoder.decode(v_idStr, Constants.ENCODING_FILE_READ);

                int io = v_idStr.indexOf(".");
                String tstr = v_idStr.substring(0, io);
                String nstr = v_idStr.substring(io+1);
                fi = DB_FundInfo.getFundInfosByTypeAndName(tstr, nstr, false);


                log.info("Updating fund, v_idStr: " + v_idStr);
                log.info("tstr: " + tstr + ", nstr: " + nstr);
                log.info("v_type: " + v_type);
                log.info("v_name: " + v_name);
                log.info("v_url: " + v_url);
                log.info("fi: " + fi);
                log.info("FI.name: " + fi.getNameMS());
                log.info("FI.name decoded: " + HtmlManipulator.replaceHtmlEntities(fi.getNameMS()));

                if (fi._type.equals(v_type)
                        && HtmlManipulator.replaceHtmlEntities(fi.getNameMS()).equals(v_name)
                        && fi._url.equals(v_url)
                        && v_delete == null
                        && v_setvalid == null) {
                    continue;
                } else {
                    log.info("Updating fund, v_idStr: " + v_idStr);
                    log.info("tstr: " + tstr + ", nstr: " + nstr);
                    log.info("v_type: " + v_type);
                    log.info("v_name: " + v_name);
                    log.info("v_url: " + v_url);
                    log.info("fi: " + fi);
                    log.info("FI.name: " + fi.getNameMS());
                    log.info("FI.name decoded: " + HtmlManipulator.replaceHtmlEntities(fi.getNameMS()));
                    if (!fi._type.equals(v_type)) {
                        log.info("v_type differed, from HTML: " + v_type + ", DB: " + fi._type);
                    }
                    if (!fi.getNameMS().equals(v_name)) {
                        log.info("v_name differed, from HTML: " + v_name + ", DB: " + fi.getNameMS());
                    }
                    if (!fi._url.equals(v_url)) {
                        log.info("v_url differed, from HTML: " + v_url + ", DB: " + fi._url);
                    }
                }

                if (isVerify) {
                    fi = new D_FundInfo();
                    fi.setNameMS(v_name);
                    fi.setNameOrig(v_name);
                    fi._url = v_url;
                } else {
                    fi.setNameMS(v_name);
                    fi.setNameOrig(v_name);
                    fi._url = v_url;
                }

                log.info("Updating v_idStr to:\n" + fi.toString());
            }
            // Inserting fund
            else {
                if (v_type.length() <= 0 || v_name.length() <= 0 || v_url.length() <= 0) {
                    continue;
                }
                fi = new D_FundInfo();
                fi._type = v_type;
                fi.setNameMS(v_name);
                fi.setNameOrig(v_name);
                fi._url = v_url;
                log.info("Inserting new fund:\n" + fi.toString());
            }

//            IndentWriter iw = new IndentWriter();
//            fi._o1.dumpInfo(iw);
//            ee.dinfo(log, TAG, "FundInfo is: " + iw.getString());



            // Display 'New' / 'Update' row, if we haven't shown it already
            if (isVerify && v_idStr == null && !shownInserts) {
                strb.append("<tr><td colspan=\"6\"><b>New funds to be inserted</b></td></tr>\n");
                shownInserts = true;
            } else if (isVerify && v_idStr != null && !shownUpdates) {
                strb.append("<tr><td colspan=\"6\"><b>Updates to funds</b></td></tr>\n");
                shownUpdates = true;
            }

            // This is a new fund
            if (v_idStr == null) {
                if (isVerify) {
                    D_FundInfo fiDB1 = DB_FundInfo.getFundInfosByTypeAndName(fi._type, fi.getNameMS(), false);
                    D_FundInfo fiDB2 = DB_FundInfo.getFundInfosByTypeAndURL(fi._type, fi._url);
                    if (fiDB1 != null || fiDB2 != null) {
                        String text = "";
                        if (fiDB1 != null) {
                            text = "Type.Name combination already exists will not insert: ";
                        } else if (fiDB2 != null) {
                            text = "Type.URL combination exists will not insert: ";
                        }
                        strb.append("<tr bgcolor=\"#FF0000\"><td colspan=\"5\">");
                        strb.append(text + fi._type + "." + fi.getNameMS());
                        strb.append("</td></tr>\n");
                    } else {
                        urlControl_DisplayFund(
                                true,
                                "",
                                false,
                                false,
                                strb, FL_MODE_FILL_EXISTING,
                                null,
                                fi,
                                count,
                                -1);
                        count++;
                    }
                } else {
                    log.info("Adding new fund to database\n" + fi.toString());
                    DB_FundInfo.addFundInfo(fi);
                    needsSaving = true;
                }
            }

            // This is an existing fund
            else {
                if (!isVerify) log.info("Commit, and existing fund process entered");
                String text = null;
                boolean isDelete = false;
                boolean isValid = false;
                if (v_delete != null) {
                    if (!isVerify) log.info("...Delete");
                    isDelete = true;
                    text = "Delete";
                    // ee.dinfo(log, TAG, "delete is requested");
                } else if (v_setvalid != null) {
                    if (!isVerify) log.info("...Set Valid");
                    isValid = true;
                    text = "Set Valid";
                    // ee.dinfo(log, TAG, "set valid is requested");
                } else {
                    if (!isVerify) log.info("...Updating name or URL....");
                    text = "Updating Name or URL";
                }

                if (isVerify) {
                    urlControl_DisplayFund(
                            true,
                            text,
                            isValid,
                            isDelete,
                            strb,
                            FL_MODE_FILL_EXISTING,
                            v_idStr,
                            fi,
                            count,
                            -1);
                    count++;
                }
                else {
                    if (isDelete) {
                        log.warning("Will now delete entity: " + fi._type + "." + fi.getNameMS());
                        DB_FundInfo.deleteFundInfo(fi);
                        log.warning("...delete performed");
                    } else if (isValid) {
                        fi._isValid = true;
                        fi._errorCode = D_FundInfo.IC_NO_ERROR;
                        log.info("Will now set entity to valid: " + fi._type + "." + fi.getNameMS());
                    } else {
                        log.info("Will update entity: " + fi._type + "." + fi.getNameMS() + ", url: " + fi._url);
                    }
                    needsSaving = true;
                }
            }
        }

        log.info("Have iterated through all funds, needsSaving: " + needsSaving + ", isVerify: " + isVerify);
        if (needsSaving) {
            log.info("Saving the updated database");
            GCSWrapper.gcsWriteBlob(Constants.FUNDINFO_DB_MASTER_BIN, DB_FundInfo.getFundInfosData());
        }
        log.info("Done, now returning string");
        return strb.toString();
    }

    //------------------------------------------------------------------------
    private static void urlControl_DisplayFund(
            boolean doRow,
            String text,
            boolean hiddenSetValid,
            boolean hiddenDelete,
            StringBuilder strb,
            int mode,   // FL_MODE_FILL_EXISTING, FL_MODE_FILL_EMPTIES
            String idToUse,
            D_FundInfo elem,
            int count, // count controls the index of the parameters
            int index) throws Exception { // index is leftmost entry (so existing fund #1 gets index #1)

        if (doRow) {
            if (elem != null && !elem._isValid && text == null) {
                strb.append("<tr bgcolor=\"#FF0000\">\n");
            } else {
                strb.append("<tr>\n");
            }
        }

        // Unique fund this row represent
        String fundId = "";
        if (idToUse != null) {
            fundId = idToUse;
        }

        if (mode == FL_MODE_FILL_EXISTING) {
            if (elem == null) {
                throw new Exception("FL_MODE_FILL_EXISTING, but elem is null");
            }
            if (idToUse != null) {
                strb.append("<input type=\"hidden\" name=\"" + JSP_Constants.PARAM2_ID_ + String.valueOf(count) + "\" value=\"" + fundId + "\"/>");
            }
        }

        // Text is set when showing which updates we plan to perform (in verify stage)
        if (text != null) {
            strb.append("<td colspan=\"4\">" + text + "</td>");
            if (hiddenDelete) {
                strb.append("<input type=\"hidden\" name=\"" + JSP_Constants.PARAM2_DELETE_ + String.valueOf(count) + "\" value=\"" + "\"/>");
            }
            if (hiddenSetValid) {
                strb.append("<input type=\"hidden\" name=\"" + JSP_Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + "\"/>");
            }
//            if (hiddenDelete) {
//                strb.append("<input type=\"hidden\" name=\"" + JSP_Constants.PARAM2_DELETE_ + String.valueOf(count) + "\" value=\"" + fundId + "\"/>");
//            }
//            if (hiddenSetValid) {
//                strb.append("<input type=\"hidden\" name=\"" + JSP_Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + fundId + "\"/>");
//            }
        }

        // Add the empty rows in at the top
        else if (mode == FL_MODE_EMPTIES) {
            strb.append("<td width=\"50\"></td>\n");
            strb.append("<td align=\"left\" width=\"30\"></td>\n");
            strb.append("<td align=\"left\" width=\"70\"></td>\n");
            strb.append("<td align=\"left\" width=\"30\"></td>\n");
        }

        // Fill the row with existing fund information
        else if (mode == FL_MODE_FILL_EXISTING) {
            strb.append("<td width=\"50\" align=\"right\">" +  String.valueOf(index) + ".</td>");
            strb.append("<td width=\"30\" align=\"left\"><input type=\"checkbox\" name=\"" + JSP_Constants.PARAM2_DELETE_ + String.valueOf(count) + "\" value=\"" + "\"/>D</td>");
            if (elem._isValid) {
                strb.append("<td width=\"70\" align=\"left\"><input type=\"checkbox\" disabled name=\"" + JSP_Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + "\"/>SV</td>");
            } else {
                strb.append("<td width=\"70\" align=\"left\"><input type=\"checkbox\" name=\"" + JSP_Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + "\"/>SV/" + elem._errorCode + "</td>");
            }
//            strb.append("<td width=\"30\" align=\"left\"><input type=\"checkbox\" name=\"" + JSP_Constants.PARAM2_DELETE_ + String.valueOf(count) + "\" value=\"" + fundId + "\"/>D</td>");
//            if (elem._isValid) {
//                strb.append("<td width=\"70\" align=\"left\"><input type=\"checkbox\" disabled name=\"" + JSP_Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + fundId + "\"/>SV</td>");
//            } else {
//                strb.append("<td width=\"70\" align=\"left\"><input type=\"checkbox\" name=\"" + JSP_Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + fundId + "\"/>SV/" + elem._errorCode + "</td>");
//            }
            strb.append("<td width=\"30\" align=\"left\">[" +  elem._dpDays.size() + "]</td>\n");
        }

        // Type
        if (mode == FL_MODE_FILL_EXISTING) {
            strb.append("<td width=\"40\">" + elem._type + "</td>\n");
            strb.append("<input type=\"hidden\" name=\"" + JSP_Constants.PARAM2_TYPE_ + String.valueOf(count) + "\" value=\"" + elem._type + "\"/>");
        } else if (mode == FL_MODE_EMPTIES) {
            strb.append("<td width=\"40\">\n");
            strb.append("<select name=\"" + JSP_Constants.PARAM2_TYPE_ + String.valueOf(count) + "\">\n");
            for (int j = 0; j < D_FundInfo.TYPES.length; j++) {
                strb.append("<option value=\"" + D_FundInfo.TYPES[j] + "\">" +
                        D_FundInfo.TYPES[j] + "</option>\n");
            }
            strb.append("</select>\n");
            strb.append("</td>");
        }

        String fundName = "";
        String fundURL = "";
        String readOnly = "";
        if (mode == FL_MODE_FILL_EXISTING) {
            fundName = elem.getNameMS();
            fundURL = elem._url;
        }

        // Fund Name
        strb.append("<td width=\"300\">" +
                "<input style=\"width:300px;\" type=\"text\"" +
                " name=\"" + JSP_Constants.PARAM2_NAME_ + String.valueOf(count) + "\"" +
                " value=\"" + fundName + "\"" +
                " size=\"150\"></td>\n");

        // Fund URL
        strb.append("<td width=\"600\">" +
                "<input style=\"width:600px;\" type=\"text\"" +
                " name=\"" + JSP_Constants.PARAM2_URL_ + String.valueOf(count) + "\"" +
                " value=\"" + fundURL + "\"" +
                " size=\"150\"></td>\n");

        // Morning star URL written out
        // "ms" URL
        // "us" URL
        if (mode == FL_MODE_FILL_EXISTING) {
            String href = "JSP_ExtractDebugger.jsp"
                    + "?doPostProcessing=false"
                    + "&p2_typedotname_=" + elem.getTypeAndNameURLEncoded();
//            log.info("href is: " + href + ", for: " + elem.getTypeAndName() + ", url: " + elem._url);
            strb.append("<td><a href=\"" + elem._url + "\"" + " target=\"_blank\">ms</a></td>");
            strb.append("<td><a href=\"" + href + "\"" + " target=\"_blank\">debug</a></td>");
//            strb.append("<td><p style=\"font-size:11px\">[id:N/A]</p></td>");
        } else if (mode == FL_MODE_EMPTIES) {
            strb.append("<td width=\"20\"></td>");
            strb.append("<td width=\"20\"></td>");
            strb.append("<td width=\"115\"></td>");
        }
        strb.append("</tr>\n");
    }

    //------------------------------------------------------------------------
//    private static OTuple2G<D_FundInfo, D_FundInfo> getD_FundInfo(
//            String v_idStr,
//            String v_type,
//            String v_name,
//            String v_url,
//            String v_delete,
//            String v_setvalid) throws Exception {
//
//        List<D_FundInfo> allFunds = DB_FundInfo.getAllFundInfos();
//        log.info("Entering with, id: " + v_idStr + ", type: " + v_type + ", name: " + v_name + ", url: " + v_url + ", setvalid: " + v_setvalid + ", delete: " + v_delete);
//
////        if (v_name != null && v_name.startsWith("T. Rowe")) {
////            log.info("getD_FundInfo, arguments"
////                    + "\nv_type:     " + v_type
////                    + "\nv_name:     " + v_name
////                    + "\nv_url:      " + v_url
////                    + "\nv_delete:   " + v_delete
////                    + "\nv_setvalid: " + v_setvalid);
////        }
//
//        if (v_type.equals(D_FundInfo.TYPE_VANGUARD)
//                && (v_url == null || v_url.length() == 0 || v_url.equals("null"))) {
//            v_url = JSP_Constants.url_getVanguard(v_name);
//            log.info("Vanguard with null URL\nname: " + v_name + "\n" + "url: " + v_url);
//        }
//
//        if (v_type == null || v_type.trim().length() == 0
//                || v_name == null || v_name.trim().length() == 0
//                || v_url == null || v_url.trim().length() == 0) {
//            return null;
//        }
//
//        D_FundInfo fi = null;
//        for (D_FundInfo fitmp: DB_FundInfo.getAllFundInfos()) {
//            if (v_type.equals(fitmp._type) && v_name.equals(fitmp._nameMS) && v_url.equals(fitmp._url)) {
//                fi = fitmp;
//            }
//        }
//
//        if (v_delete != null || v_setvalid != null) {
//            return new OTuple2G<>(fi, fi);
//        }
//        return null;   // No update
//
//        // This was back in the day when we supported updating fund information
////        D_FundInfo fi = new D_FundInfo();
////        fi._type = v_type;
////        fi._nameMS = v_name;
////        fi._url = v_url;
////        fi._msRating = -1;
////        fi._ppmNumber = "";
////        String lfriday = MM.tgif_getLastFridayTodayExcl(MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM));
////        fi._dateYYMMDD_Updated = lfriday;
////        fi._dateYYMMDD_Update_Attempted = lfriday;
////
////        D_FundInfo fic = fi.duplicate();
////        return new OTuple2G<>(fi, fic);
//    }
}

