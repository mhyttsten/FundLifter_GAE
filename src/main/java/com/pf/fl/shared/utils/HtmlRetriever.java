package com.pf.fl.shared.utils;

import java.io.IOException;
import java.util.logging.Logger;

public class HtmlRetriever {
	private static final Logger log = Logger.getLogger(HtmlRetriever.class.getName());
	private static final String TAG = MM.getClassName(HtmlRetriever.class.getName());
	
	public static class Response {
		public byte[] _pageContentBA;
		public String _httpURL;
		public String _accountType;
	}

    public static byte[]  htmlGet(
            IndentWriter iwd,
            String url,
            int retryIntervalInMS, int retryCount) throws IOException {
        iwd.println("HtmlRetriever, entered with URL:   " + url + "    retryIntervalMS: " + retryIntervalInMS + ", retryCount: " + retryCount);
		byte[] result = htmlGetImpl(iwd, url, retryIntervalInMS, retryCount);
        if (result != null) {
            iwd.println("Returning: " + result.length + " number of bytes");
        } else {
            iwd.println("Returning null");
        }
        return result;
    }

	private static byte[] htmlGetImpl(
			IndentWriter iwd,
			String url,
			int retryIntervalInMS, int retryCount) throws IOException {

		byte[] pageContent = null;
		do {
			try {
				iwd.println("Now issuing URL get network call, retryCount: " + retryCount + ", retryInterval: " + retryIntervalInMS);
				pageContent = MM.getURLContentBA(url);
				if (pageContent != null) {
					iwd.println("...Done successfully, returning: " + pageContent.length + " bytes");
				} else {
					iwd.println("...Successful return but result data was null");
				}
				return pageContent;
			} catch(Exception exc) {
				log.warning("*****************************************************\n" +
						"    ERROR ERROR ERROR, error, error, error\n" +
						"    Exception caught while extracting the HTML pages\n" + 
						"    Retry count: " + retryCount + "\n" +
						"    Exception: " + exc.toString() + "\n" +
						"    " + MM.getStackTraceString(exc));

				String s = "FLMain, exception caught while extracting the HTML pages\n" +
						"   Exception error: " + exc.toString() + "\n" +
						"  " + MM.getStackTraceString(exc) + "\n" +
						"   Retry count: " + retryCount;
				iwd.println(s);
				retryCount--;
				if (retryCount == 0) {
					return null;
				}
				MM.sleepInMS(retryIntervalInMS);
			}
		} while(retryCount > 0);
		return null;
	}
}
