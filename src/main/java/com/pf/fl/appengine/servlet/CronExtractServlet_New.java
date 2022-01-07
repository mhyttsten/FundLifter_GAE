package com.pf.fl.appengine.servlet;

import com.pf.fl.appengine.extract.FLOps1_Ext1_Extract_New;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CronExtractServlet_New extends HttpServlet {
    private static final Logger log = Logger.getLogger(CronExtractServlet_New.class.getSimpleName());
    private static final String TAG = CronExtractServlet_New.class.getSimpleName();

    //------------------------------------------------------------------------
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        log.info("CronExtractServlet starting");
        try {
//            IndentWriter iw = new IndentWriter();
//            iw.println("Parameters");
//            Enumeration e = req.getParameterNames();
//            while(e.hasMoreElements()) {
//                String p = e.nextElement().toString();
//                iw.println("..." + p + ": " + req.getParameter(p));
//            }
//            log.info(iw.getString());

            doExtract(req, resp);
        } catch(Exception exc) {
            log.severe("Exception caught: " + exc.getMessage());
            log.severe("Stack trace:\n" + MM.getStackTraceString(exc));
        }
    }

    //------------------------------------------------------------------------
    public static final String P_ignoreSchedule = "ignoreSchedule";
    public static final String P_timeInSBeforeDeadline = "timeInSBeforeDeadline";

    public static void doExtract(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        int timeInSBeforeDeadline = 0;
        boolean ignoreSchedule = false;
        String s = null;
        s = req.getParameter(P_ignoreSchedule);
        if (s != null)  {
            ignoreSchedule = s.trim().toLowerCase().equals("true");
        }
        s = req.getParameter(P_timeInSBeforeDeadline);
        if (s != null)  {
            try {
                timeInSBeforeDeadline = Integer.parseInt(s);
            } catch(Exception exc) {
                log.severe("timeInSBeforeDeadline could not be converted to int: " + s);
                return;
            }
        }

        log.info("Parameters being passed");
        log.info("...ignoreSchedule: " + ignoreSchedule);
        log.info("...timeInSBeforeDeadline: " + timeInSBeforeDeadline);

        if (ignoreSchedule) {
            String date = MM.getNowAs_YYMMDD(null);
            date = date.substring(2);
            log.info("The date is : " + date);
            if (date.equals("0101")) {
                log.info("It is the 1st of January and we're not doing the !cronInitiated");
                log.info("This execution only happens because we are forced to do 1 scheduled entry in cron.xml");
                return;
            }
        }

        Map<String, String> env = System.getenv();
        if (env.containsKey("GAE_MEMORY_MB")) {
            log.info("Memory available: " + env.get("GAE_MEMORY_MB") + "MB");

        } else {
            log.info("Unknown amount if memory available, environment variable: GAE_MEMORY_MB not set");
        }

        log.info("Will now call extraction logic");
        FLOps1_Ext1_Extract_New extract = new FLOps1_Ext1_Extract_New(
                null,
                false,
                ignoreSchedule,
                timeInSBeforeDeadline,
                null);
        extract.doIt();
    }
}
