package com.pf.fl.appengine.extract;

import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.DB_FundInfo;
import com.pf.fl.shared.datamodel.D_FundInfo_Serializer;
import com.pf.fl.shared.datamodel.D_FundInfo_Validator;
import com.pf.fl.shared.extract.ExtractFromHTML_Helper;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.extract.ExtractStatistics;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.Properties;

public class FLOps1_Ext1_Extract_New {
    private static final Logger log = Logger.getLogger(FLOps1_Ext1_Extract_New.class.getName());
    private static final String TAG = MM.getClassName(FLOps1_Ext1_Extract_New.class.getName());

    private IndentWriter _iwdPrefix = new IndentWriter();
    private IndentWriter _iwdExtractionReportBefore = new IndentWriter();

    private boolean _ignoreSchedule = false;

    private boolean _isDebugOnly = false;
    private IndentWriter _iwd = new IndentWriter();

    private int _timeInSBeforeDeadline;

    private List<D_FundInfo> _argFIToExtract = null;
    private List<D_FundInfo> _fiToExtract  = null;
    private int _fiToExtractCount = 0;

    private String _nowYYMMDD;
    private String _nowYYMMDD_HHMMSS;
    private String _fridayLastYYMMDD;
    private String _fridayLast2YYMMDD;
    private int _nowDayOfWeek;
    private int _nowHourOfDay;
    private String _sunYYMMDD;
    private String _monYYMMDD;
    private String _tueYYMMDD;
    private String _wedYYMMDD;
    private String _phaseTime;

    /**
     * ### Phase 01 (...): Initialization Parameters
     * ### Phase 02 (...): Extraction Report After
     * ### Phase 03 (...): Fund Extraction Details
     * ### Phase 04 (...): Post Extraction Stuff
     * ### Phase 05 (...): Extraction Report Before
     */

    //------------------------------------------------------------------------
    // Then ignoreSchedule, don't do postprocessing, force extraction (i.e. if there is alreay
    // a DPD for this week then remove it just to debug extraction again)
    public FLOps1_Ext1_Extract_New(List<D_FundInfo> argFIToExtract,
                                   boolean isDebugOnly,
                                   boolean ignoreSchedule,
                                   int timeInSBeforeDeadline,
                                   IndentWriter iwd) {
        _argFIToExtract = argFIToExtract;
        _isDebugOnly = isDebugOnly;
        _ignoreSchedule = ignoreSchedule;
        _timeInSBeforeDeadline = timeInSBeforeDeadline;
        if (iwd != null) {
            _iwd = iwd;
        }

        // Set time variables
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_STOCKHOLM));
        _nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        _nowHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        _nowYYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        _nowYYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM);
        _fridayLastYYMMDD  = MM.tgif_getLastFridayTodayExcl(_nowYYMMDD);
        _fridayLast2YYMMDD = MM.tgif_getLastFridayTodayExcl(_fridayLastYYMMDD);
        _sunYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.SUNDAY);
        _monYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.MONDAY);
        _tueYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.TUESDAY);
        _wedYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.WEDNESDAY);
        _phaseTime = " (" + _fridayLastYYMMDD + "/" + MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM) + ") ";

        String s = "Starting extraction, constructor with arguments:"
                + "\n...argFIToExtract.size(): " + (_argFIToExtract != null ? _argFIToExtract.size() : "null")
                + "\n...isDebugOnly: " + _isDebugOnly
                + "\n...ignoreSchedule: " + _ignoreSchedule
                + "\n...timeInSBeforeDeadline: " + _timeInSBeforeDeadline
                + "\n...iwd: " + (_iwd==null ? "null" : "!null");
        _iwdPrefix.println("\n********************************************************");
        _iwdPrefix.println("### Phase 01" + _phaseTime + ": Initialization Parameters");
        _iwdPrefix.println(s);
        log.info(s);
    }

    public void doIt() throws IOException {
        log.info("FLOps1_Ext1_Extract_Now.doIt: Entered");
        // if (true) {
        //     log.info("This is a GHOST implementation that doesn't do any work");
        //     return;
        // }

        _iwdPrefix.println("FLOps1_Ext1_Extract_New, entered");

        MM.timerStart();

        // Check if it is time to extract
        if (!_isDebugOnly && !_ignoreSchedule) {
            boolean scheduledPlay =
                    (_nowDayOfWeek == Calendar.SATURDAY && _nowHourOfDay >= 5)
                            || (_nowYYMMDD.equals(_sunYYMMDD) && _nowHourOfDay >= 5)
                            || (_nowYYMMDD.equals(_monYYMMDD) && _nowHourOfDay >= 5)
                            || (_nowYYMMDD.equals(_tueYYMMDD) && _nowHourOfDay >= 5)
                            || (_nowYYMMDD.equals(_wedYYMMDD) && _nowHourOfDay >= 5);
            if (!scheduledPlay) {
                log.info("Not time for extraction now: " + _nowYYMMDD + ", " + MM.tgif_getDayOfWeekStr(_nowDayOfWeek) + "@" + _nowHourOfDay + ", lastFriday: " + _fridayLastYYMMDD);
                return;
            }
        }
        log.info("Now: " + _nowYYMMDD_HHMMSS + ", " + MM.tgif_getDayOfWeekStr(_nowDayOfWeek) + "@" + _nowHourOfDay + ", lastFriday: " + _fridayLastYYMMDD);
        _iwdPrefix.println("Extracting now: " + _nowYYMMDD_HHMMSS + " (Stockholm time)");
        _iwdPrefix.println("Last Friday is: " + _fridayLastYYMMDD);

        // Read Fund Database
        log.info("About to read fund database, file: " + Constants.FUNDINFO_DB_MASTER_BIN);
        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        DB_FundInfo.initialize(fundInfoBA, true);
        List<D_FundInfo> fiAllFunds = DB_FundInfo.getAllFundInfos();

        // Validate Fund Database
        D_FundInfo_Validator fiv = new D_FundInfo_Validator(fiAllFunds);
        fiv.process();
        if (!fiv._error) {
            String s = "Fund database is valid";
            _iwdPrefix.println(s);
            log.info(s);
        } else {
            String s = "Fund database in invalid, terminating\n" + fiv._iwErrors.getString();
            _iwdPrefix.println(s);
            log.severe(s);
            return;
        }

        // We've been provided a fund list to extract
        if (_argFIToExtract != null && _argFIToExtract.size() > 0) {
            _iwdPrefix.println("Extraction only based on give set of funds: " + _argFIToExtract.size());
            _iwdPrefix.println("OBSERVE: The pre-/post-extraction summary reports will not be accurate");
            _fiToExtract = _argFIToExtract;
        }
        // There is no explicit lst, extract all funds that are due for extraction
        else {
            _iwdExtractionReportBefore.println("\n### Phase 05" + _phaseTime + ": Extraction Report Before");
            ExtractStatistics.getExtractSummary(_iwdExtractionReportBefore,
                    -1, -1,
                    _nowYYMMDD_HHMMSS, fiAllFunds);
            ExtractStatistics es = new ExtractStatistics(fiAllFunds);
            es.extractStats();
            _fiToExtract = es._fiToExtract;
        }
        log.info(_iwd.getString());

        _iwd.println("\n### Phase 03" + _phaseTime + ": Fund Extraction Details");
        _iwd.println("Extracting: " + _fiToExtract.size() + " entries");
        log.info("Extracting: " + _fiToExtract.size() + " entries");
        _fiToExtractCount = _fiToExtract.size();
        if (_fiToExtract.size() == 0) {
            log.info("No entries to extract, extractList was empty: " + _nowYYMMDD + ", " + MM.tgif_getDayOfWeekStr(_nowDayOfWeek) + "@" + _nowHourOfDay + ", lastFriday: " + _fridayLastYYMMDD);
            _iwd.println("No entries to extract, extractList was empty");
            return;
        } else {

        }

        // Extract each Fund
        int countTotal = 0;
        while (_fiToExtract.size() > 0) {
            D_FundInfo fi = _fiToExtract.remove(0);
            _iwd.println("Will now extract: " + fi.getTypeAndName());

            // No debug. Then just extract the fund!
            if (!_isDebugOnly) {
                Random r = new Random();
                int min = 1;
                int max = 3;
                int sleepTime = r.nextInt((max - min) + 1) + min;
                MM.sleepInMS(sleepTime*1000);
                extractSingleFund(countTotal, fi);
            }

            // Debugging. Allow for detailed information
            else {
                if (fi._dpDays.size() == 0 || !fi._dpDays.get(0)._dateYYMMDD.equals(_fridayLastYYMMDD)) {
                    String s = fi._dpDays.size() == 0 ? "N/A" : fi._dpDays.get(0)._dateYYMMDD;
                    _iwd.println("Last DPD in fund (" + s + ") is not last extraction friday (" + _fridayLastYYMMDD + ")");
                    _iwd.println("This means full extraction is needed, so let's do that!");
                    extractSingleFund(countTotal, fi);
                }
                else {
                    _iwd.println("Last DPD in fund is last extraction friday");
                    _iwd.println("Let's try extract with this remaining first");
                    _iwd.println("*** BEGIN: Extract when DPD[0] == last Friday");
                    extractSingleFund(countTotal, fi);
                    _iwd.println("*** END: Extract when DPD[0] == last Friday");
                    fi._dpDays.remove(0);
                    _iwd.println("*** BEGIN: Extract when DPD[0] < last Friday");
                    extractSingleFund(countTotal, fi);
                    _iwd.println("*** END: Extract when DPD[0] < last Friday");
                }
            }

            // Emit count information regularly
            countTotal++;
//            if (countTotal == 1 || (countTotal % 50) == 0) {
                log.info("Processed: " + countTotal + ", latest: " + fi.getTypeAndName() + ", " + fi._url);
//            }

            // For now
//            if (countTotal >= 20) {
//                break;
//            }

            // Wait between 1s and 5s
//            Random r = new Random();
//            int min = 1;
//            int max = 3;
//            int sleepTime = r.nextInt((max - min) + 1) + min;
//            MM.sleepInMS(sleepTime*1000);
            MM.sleepInMS(500);

            // Out of time, then break (normal condition for 10min max cron execution)
            if (!MM.timerContinue(_timeInSBeforeDeadline)) {
                log.info("Maximum processing time reached for cron job, breaking");
                break;
            }
        }
        log.info("Done processing all funds: " + countTotal + ", terminating because of done or time limit");

        _iwd.println("\n### Phase 04" + _phaseTime + ": Post Extraction Stuff");
        IndentWriter iwdExtractionReportAfter = new IndentWriter();
        iwdExtractionReportAfter.println("\n### Phase 02" + _phaseTime + ": Extraction Report After");
        ExtractStatistics.getExtractSummary(iwdExtractionReportAfter,
                -1, -1,
                _nowYYMMDD_HHMMSS, DB_FundInfo.getAllFundInfos());

        // Validate all funds before restoring DB
        fiv = new D_FundInfo_Validator(fiAllFunds);
        fiv.process();
        if (!fiv._error) {
            log.info("Extraction resulting in a valid fund database");
        } else {
            log.severe("Extraction resulted in invalid fund database. Refuse to save.\n" + fiv._iwErrors.getString());
            return;
        }

        // Do not save any extraction state
        if (_isDebugOnly) {
            log.info("We're doing debug only, so now exiting without doing post-processing");
            return;
        }

        // Write fund DB master
        log.info("Writing master DB: " + Constants.FUNDINFO_DB_MASTER_BIN);
        byte[] fundListBA = D_FundInfo_Serializer.crunchFundList(fiAllFunds);
        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, fundListBA);
        String dayMasterDBFilename = Constants.PREFIX_FUNDINFO_DB + _fridayLastYYMMDD + Constants.EXT_BIN;
        log.info("Writing day master DB: " + dayMasterDBFilename);
        GCSWrapper.gcsWriteFile(dayMasterDBFilename, fundListBA);

        // Write debug file
        log.info("Writing debug file");
        String logfileContent = "";
        String fundInfoLogsDebugFilename = Constants.PREFIX_FUNDINFO_LOGS_DEBUG + _fridayLastYYMMDD + Constants.EXT_TXT;
        log.info("Writing debug file: " + fundInfoLogsDebugFilename);
        byte[] logfileBA = GCSWrapper.gcsReadFile(fundInfoLogsDebugFilename);
        if (logfileBA != null && logfileBA.length > 0) {
            logfileContent = new String(logfileBA, Constants.ENCODING_FILE_READ);
        }
        byte[] logfileContentBA = (iwdExtractionReportAfter.getString()
                + _iwd.getString()
                + _iwdExtractionReportBefore.getString()
                + logfileContent)
                .getBytes(Constants.ENCODING_FILE_WRITE);
        GCSWrapper.gcsWriteFile(Constants.PREFIX_FUNDINFO_LOGS_DEBUG + _fridayLastYYMMDD + Constants.EXT_TXT, logfileContentBA);
        GCSWrapper.gcsWriteFile(Constants.PREFIX_WHISTORIC_FUNDINFO_LOGS_DEBUG + _fridayLastYYMMDD + Constants.EXT_TXT, logfileContentBA);
        GCSWrapper.gcsDeleteFiles(Constants.PREFIX_FUNDINFO_LOGS_DEBUG, _fridayLastYYMMDD);  // Delete old files

        // We only emit statistics once this entire day is finished
        if (_fiToExtract.size() != 0) {
            sendMail(_fridayLastYYMMDD, "We're not done yet"
                + "\n   Time now: " + MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM) + " (Stockholm)"
                + "\n   Extracted funds this round: " + String.valueOf(_fiToExtractCount -_fiToExtract.size()) 
                + "\n   Remaining funds to extract: " + _fiToExtract.size());
        } else {
            log.info("Writing extract details files");
            byte[] thisRound = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
            List<D_FundInfo> fiNow = D_FundInfo_Serializer.decrunchFundList(thisRound);
            IndentWriter iwextract = new IndentWriter();
            ExtractStatistics.getExtractSummary(iwextract, 2, -1, _nowYYMMDD_HHMMSS, fiNow);
            String logFridayFilename = Constants.PREFIX_FUNDINFO_LOGS_EXTRACT + _fridayLastYYMMDD + Constants.EXT_TXT;
            byte[] fridayLogContentBA = GCSWrapper.gcsReadFile(logFridayFilename);
            String fridayLogContentStr = "";
            if (fridayLogContentBA != null) {
                fridayLogContentStr = new String(fridayLogContentBA, Constants.ENCODING_FILE_READ);
                fridayLogContentStr = "----------------\n" + fridayLogContentStr;
            }
            fridayLogContentStr = iwextract.getString() + fridayLogContentStr;
            fridayLogContentBA = fridayLogContentStr.getBytes(Constants.ENCODING_FILE_WRITE);
            GCSWrapper.gcsWriteFile(Constants.PREFIX_FUNDINFO_LOGS_EXTRACT + _fridayLastYYMMDD + Constants.EXT_TXT, fridayLogContentBA);
            GCSWrapper.gcsWriteFile(Constants.PREFIX_WHISTORIC_FUNDINFO_LOGS_EXTRACT + _fridayLastYYMMDD + Constants.EXT_TXT, fridayLogContentBA);
            GCSWrapper.gcsDeleteFiles(Constants.PREFIX_FUNDINFO_LOGS_EXTRACT, _fridayLastYYMMDD);  // Delete old files
            sendMail(_fridayLastYYMMDD, fridayLogContentStr);
            log.info("...Done writing extract details files");
        }

        log.info("Returning successfully from extract processing");
        MM.sleepInMS(1000);
    }

    private void extractSingleFund(int currentCount,
                                   D_FundInfo fi) throws IOException {
        String fundBeforeStr = fi.getOneLiner();
        int ec_before = fi._errorCode;

        _iwd.println("[" + currentCount + "]. Extracting: " + fi.getOneLiner());

        // Extracting the fund details
        // Update the attempted date
        IndentWriter iwdetails = new IndentWriter();
        iwdetails.setIndentChar('.');
        iwdetails.push();
        ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
        int errorCode = eh.extractFundDetails(fi, iwdetails);
        _iwd.println("Done extraction, lengths of debug details: " + iwdetails.length());
        fi._dateYYMMDD_Update_Attempted = eh._dateNow_YYMMDD;

        // Manage any error condition
        iwdetails.println("Back in Main Extraction Looper");
        if (errorCode == ExtractFromHTML_Helper.RC_SUCCESS) {
            fi._isValid = true;
            fi._errorCode = D_FundInfo.IC_NO_ERROR;
            _iwd.println("...SUCCESS, printing full fund info:\n" + fi.toString());
            if (_isDebugOnly) {
                _iwd.println(iwdetails.getString());
            }
        } else {
            String s = "Error for: " + fi.getOneLiner();
            iwdetails.println("...ERROR: " + fi.getOneLiner());
            boolean is_error = false;
            switch(errorCode) {
                case ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND:
                    log.severe("*** INVALID: " + fi.getOneLiner());
                    s += "\nFund became invalid";
                    iwdetails.println("...RC_ERROR_INVALID_FUND, setting it to invalid");
                    fi._isValid = false;
                    is_error = true;
                    break;
                case ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED:
                    s += "\nSuccess, but data was updated";
                    iwdetails.println("...RC_SUCCESS_BUT_DATA_WAS_UPDATED");
                    break;
                case ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND:
                    s += "\nNo DPDay found";
                    iwdetails.println("...NO DPDAY FOUND");
                    break;
                default:
                    iwdetails.println("...<UNEXPECTED RETURN CODE>");
                    is_error = true;
                    break;
            }
            _iwd.println(iwdetails.getString());

            if (is_error) {
                log.severe(s + "\n" + iwdetails.getString());
            } else {
                // This gets to be a lot of info for No DPD
                log.info(s + "\n" + iwdetails.getString());
//                log.info(s);
            }
        }

        // Ensure Fund is still valid after extraction
        D_FundInfo_Validator fivSingleFund = new D_FundInfo_Validator();
        fivSingleFund.validateFund(fi);
        if (fivSingleFund._error) {
            String valError = "*** Severe. Encountered validation error for a single fund, will not continue\n"
                    + "\n..." + fivSingleFund._iwErrors.getString()
                    + "\n..." + fi.toString()
                    + "\n...Fund before extraction: " + fundBeforeStr;
            _iwd.println(valError);
            log.severe(valError);
            return;
        }
    }


    public static void sendMail(String dpday, String content) throws IOException {
        String fnameDPDay = "backend/historic/fundinfo-extract-summary-" + dpday + ".txt";
        String fname = "backend/fundinfo-extract-summary" + ".txt";
        byte[] dataPrev = GCSWrapper.gcsReadFile(fnameDPDay);
        if (dataPrev == null) {
            dataPrev = new byte[0];
        }

        content = content + "\n\n\n---------\n";
        byte[] baNewContent = content.getBytes(Constants.ENCODING_FILE_WRITE);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bout.write(baNewContent);
        bout.write(dataPrev);
        byte[] dataToWrite = bout.toByteArray();

        GCSWrapper.gcsDeleteFiles(fnameDPDay, "");
        GCSWrapper.gcsWriteFile(fnameDPDay, dataToWrite);
        GCSWrapper.gcsDeleteFiles(fname, "");
        GCSWrapper.gcsWriteFile(fname, dataToWrite);

        /* using standard javax.mail
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("extractor@allthingsconsidered.appspotmail.com", "Mother v2.0"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("magnus.hyttsten@gmail.com", "Master"));
            msg.setSubject("Your v2.0 extraction report for: " + dpday);
            msg.setText(content);
            Transport.send(msg);
        } catch (Exception exc) {
            log.severe("Could not send mail, exception: " + exc.getMessage());
        }
        */
    }
}

