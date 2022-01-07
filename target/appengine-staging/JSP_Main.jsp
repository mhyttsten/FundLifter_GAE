<%@ page import="com.pf.fl.appengine.extract.GCSWrapper" %>
<%@ page import="com.pf.fl.shared.datamodel.DB_FundInfo" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.appengine.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.fl.shared.datamodel.D_FundInfo" %>

<%
    String hrefFundData_all = "JSP_FundURLData_Display.jsp";
    String hrefFundData_invalid = "JSP_FundURLData_Display.jsp?" +
        JSP_Constants.PARAM_TYPE + "=" + D_FundInfo.TYPE_INVALID;
    String hrefFundData_ppm = "JSP_FundURLData_Display.jsp?" +
        JSP_Constants.PARAM_TYPE + "=" + D_FundInfo.TYPE_PPM;
    String hrefFundData_seb = "JSP_FundURLData_Display.jsp?" +
        JSP_Constants.PARAM_TYPE + "=" + D_FundInfo.TYPE_SEB;
    String hrefFundData_spp = "JSP_FundURLData_Display.jsp?" +
        JSP_Constants.PARAM_TYPE + "=" + D_FundInfo.TYPE_SPP;
    String hrefFundData_vgd = "JSP_FundURLData_Display.jsp?" +
        JSP_Constants.PARAM_TYPE + "=" + D_FundInfo.TYPE_VANGUARD;
    String hrefFundData_vg_addNames = "JSP_FundURLData_VGAddNames.jsp?";

    String hrefWeekly_all = "JSP_Report02_Weekly_Display.jsp" +
        "?" + JSP_Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_TYPE +
        "&" + JSP_Constants.PARAM_ID + "=" + D_FundInfo.TYPE_ALL;
    String hrefWeekly_seb = "JSP_Report02_Weekly_Display.jsp" +
        "?" + JSP_Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_TYPE +
        "&" + JSP_Constants.PARAM_ID + "=" + D_FundInfo.TYPE_SEB;
    String hrefWeekly_vanguard = "JSP_Report02_Weekly_Display.jsp" +
        "?" + JSP_Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_TYPE +
        "&" + JSP_Constants.PARAM_ID + "=" + D_FundInfo.TYPE_VANGUARD;
    String hrefWeekly_spp = "JSP_Report02_Weekly_Display.jsp" +
        "?" + JSP_Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_TYPE +
        "&" + JSP_Constants.PARAM_ID + "=" + D_FundInfo.TYPE_SPP;
    String hrefWeekly_ppm = "JSP_Report02_Weekly_Display.jsp" +
        "?" + JSP_Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_TYPE +
        "&" + JSP_Constants.PARAM_ID + "=" + D_FundInfo.TYPE_PPM;

    String fl_v_seb = "JSP_FundList_Validate.jsp?" + JSP_Constants.PARAM_ID + "=" + D_FundInfo.TYPE_SEB;
    String fl_v_vgd = "JSP_FundList_Validate.jsp?" + JSP_Constants.PARAM_ID + "=" + D_FundInfo.TYPE_VANGUARD;
    String fl_v_ppm = "JSP_FundList_Validate.jsp?" + JSP_Constants.PARAM_ID + "=" + D_FundInfo.TYPE_PPM;
    String fl_v_spp = "JSP_FundList_Validate.jsp?" + JSP_Constants.PARAM_ID + "=" + D_FundInfo.TYPE_SPP;
%>

<html>

<head><title>Main</title></head>
<meta charset="utf-8"/>
<body>
  <h3>Initializing DB</h3>
  <%
    String reinit = request.getParameter("reinitialize");
    if (reinit != null && reinit.trim().equals("true")) {
       JSP_Helper._isInitialized = false;
    }
    JSP_Helper.initialize();
  %>
  Is initialized: <%=JSP_Helper._isInitialized%>

  <h1>Available alternatives 190512</h1>
  <a href="JSP_Main.jsp?reinitialize=true">Reinitialize DB</a><br>
  <br>
  <a href="JSP_Report_LastFriday.jsp">Report Last Friday</a><br>
  <a href="JSP_ExtractDebugger.jsp?doPostProcessing=false&p2_typedotname_=">ExtractDebugger</a><br>
  <a href="JSP_Index_DisplayAll.jsp">Show Indexes</a><br>
  <br>
  <a href="JSP_Operation.jsp?op_name=doThing">Do Thing</a><br>
  <a href="JSP_Operation.jsp?op_name=rewindAttempted">Clear Attempted Date</a><br>
  <a href="cronExtractNew?doPostProcessing=false&ignoreSchedule=true&timeInSBeforeDeadline=30">Execute cron extract (ignoreSchedule, !doPostProcessing)</a><br>
  <a href="cronExtractNew?doPostProcessing=true&ignoreSchedule=true&timeInSBeforeDeadline=30">Execute cron extract (ignoreSchedule, doPostProcessing)</a><br>
  <br>
  <a href="JSP_Funds_Display.jsp">DB: Show All Funds as OneLiners</a><br>
  <a href="JSP_DisplayFundDB.jsp">DB: Show All Funds in detail</a><br>
  <a href=<%=hrefFundData_all%>>Show Fund URL Data</a><br>
  <a href=<%=hrefFundData_invalid%>>Show Invalid Fund URL Data</a><br>
  <a href=<%=hrefFundData_seb%>>Show SEB Fund URL Data</a><br>
  <a href=<%=hrefFundData_vgd%>>Show VGD Fund URL Data</a><br>
  <a href=<%=hrefFundData_ppm%>>Show PPM Fund URL Data</a><br>
  <a href=<%=hrefFundData_spp%>>Show SPP Fund URL Data</a><br>
  <a href=<%=hrefFundData_vg_addNames%>>Add VG names</a><br>
  <br>

  <a href=<%=hrefWeekly_all%>>Weekly: Type ALL</a><br>
  <a href=<%=hrefWeekly_seb%>>Weekly: Type SEB</a><br>
  <a href=<%=hrefWeekly_vanguard%>>Weekly: Type Vanguard</a><br>
  <a href=<%=hrefWeekly_spp%>>Weekly: Type SPP</a><br>
  <a href=<%=hrefWeekly_ppm%>>Weekly: Type PPM</a><br>
  <br>
  <a href=<%=fl_v_seb%>>Fundlist Validation SEB</a><br>
  <a href=<%=fl_v_vgd%>>Fundlist Validation Vanguard</a><br>
  <a href=<%=fl_v_ppm%>>Fundlist Validation PPM</a><br>
  <a href=<%=fl_v_spp%>>Fundlist Validation SPP</a><br>
  <br>
</body>
</html>
