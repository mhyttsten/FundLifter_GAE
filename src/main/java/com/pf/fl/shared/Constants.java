package com.pf.fl.shared;

/**
 * Created by magnushyttsten on 2/19/18.
 */

public class Constants {

    public static final long TIMEINS_BEFORE_DEADLINE = 7*60;

    public static final String TIMEZONE_STOCKHOLM = "Europe/Stockholm";
    public static final String TIMEZONE_NEW_YORK  = "America/New_York";
    public static final String TIMEZONE_LOS_ANGELES  = "America/Los_Angeles";

    public static final String ENCODING_HTTP_READ = "UTF-8";
    public static final String ENCODING_FILE_READ = "UTF-8";
    public static final String ENCODING_FILE_WRITE = "UTF-8";
    public static final String ENCODING_ISO = "iso-8859-1";

//    public static final String BUCKET_PF = "mh-pffundlifter";
//    public static final String BUCKET_QL = "ql-magnushyttsten.appspot.com";
//    public static final String BUCKET = BUCKET_PF;
//    public static final String BUCKET = BUCKET_QL;
    public static final String BUCKET = "allthingsconsidered.appspot.com";

    public static final String PORTFOLIOS  = "backend/portfolios.bin";

    public static final String FUNDLIST_SEB = "backend/fundlist_seb.txt";
    public static final String FUNDLIST_PPM = "backend/fundlist_ppm.txt";

    public static final String PORTFOLIO_DB_MASTER_BIN  = "backend/fundportfolio-db-master.bin";
    public static final String FUNDINFO_DB_MASTER_BIN_APP           = "fundinfo-db-master-app.bin";
    public static final String FUNDINFO_DB_MASTER_BIN               = "backend/fundinfo-db-master.bin";
    public static final String FUNDINFO_LOGS_EXTRACT_MASTER_TXT = "backend/fundinfo-logs-extract-master.txt";
    public static final String FUNDINFO_LOGS_EXTRACT_LAST_TXT   = "backend/fundinfo-logs-extract-last-";
    public static final String PREFIX_FUNDINFO_DB      = "backend/historic/fundinfo-db-date-";
    public static final String PREFIX_WHISTORIC_FUNDINFO_LOGS_DEBUG   = "backend/historic/fundinfo-logs-debug-day-";
    public static final String PREFIX_WHISTORIC_FUNDINFO_LOGS_EXTRACT = "backend/historic/fundinfo-logs-extract-day-";
    public static final String PREFIX_FUNDINFO_LOGS_DEBUG   = "backend/fundinfo-logs-debug-day-";
    public static final String PREFIX_FUNDINFO_LOGS_EXTRACT = "backend/fundinfo-logs-extract-day-";
    public static final String EXT_BIN   = ".bin";
    public static final String EXT_TXT   = ".txt";
}
