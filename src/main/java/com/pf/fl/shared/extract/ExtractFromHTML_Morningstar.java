package com.pf.fl.shared.extract;

import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.datamodel.D_FundDPYear;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;
import com.pf.fl.shared.utils.OTuple2G;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ExtractFromHTML_Morningstar {
	private static final Logger log = Logger.getLogger(ExtractFromHTML_Morningstar.class.getName());
	private static final String TAG = "FL_MSExtractDetails";

	//------------------------------------------------------------------------
	public static int extractFundDetails(IndentWriter iwd, D_FundInfo fi) throws Exception {

		// Extract HTML information
		StringBuffer pageContentSB = new StringBuffer();
		OTuple2G<Integer, String> ec = ExtractFromHTML_Helper.htmlGet(pageContentSB, iwd, fi._url);
		if (ec._o1 != D_FundInfo.IC_NO_ERROR) {
			fi._errorCode = ec._o1;
			fi._lastExtractInfo = ec._o2;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}

		// *** MAIN HTML PAGE
		String pageContent = pageContentSB.toString();
		int returnCode = ExtractFromHTML_Helper.RC_SUCCESS;

		// URL cannot be served at all
		if (pageContent.contains("404 - File or directory not found.")) {
			iwd.println("Error: Received 404, this URL does not exist");
			fi._errorCode = D_FundInfo.IC_INVALID_URL_IRRECOVERABLE;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}

		iwd.println("*** Here is all the HTML");
		String ds = pageContent.replace("<", "&lt;");
		ds = ds.replace(">", "&gt;");
		iwd.println(ds);

		// Fund name
		iwd.println("Finding fund name");
		String fundName = MM.getRegExp(null, pageContent,
				"<div class=\"snapshotTitleBox\"><h1>",
				"",
				"</h1>",
				true);
		if (fundName == null) {
			iwd.println("Error: Could not find fundName in document");
			fi._errorCode = D_FundInfo.IC_HTML_MS_FUNDNAME;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}
		fundName = MM.htmlReplaceHTMLCodes(fundName);

		iwd.println("...Done, fundName: " + fundName + ", in DB: " + fi.getNameMS());
		if (!fi.getNameMS().equals(fundName)) {
			iwd.println("Warning: Fund changed name, before: " + fi.getNameMS() + ", now: " + fundName);
			returnCode = ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED;
		}
		fi.setNameMS(fundName);

		// MS Category
		iwd.println("Finding category name");
		String msCategory = MM.getRegExp(null, pageContent,
				"Morningstar kategori:",
				"<span class=\"value\">",
				"</span>",
				true);
		if (msCategory == null) {
			iwd.println("Error: Could not find category name ahref was null");
			fi._errorCode = D_FundInfo.IC_HTML_MS_CATEGORY;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}
		if (!fi.getCategoryName().equals(msCategory)) {
			iwd.println("Warning, Fund category changed, before: " + fi.getCategoryName() + ", now: " + msCategory);
			returnCode = ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED;
		}
		fi.setCategoryName(msCategory);
		iwd.println("...Done, categoryName: " + fi.getCategoryName());

		// Morningstar Rating: Don't know how to extract this from HTML
		fi._msRating = fi._msRating;

		// PPM number: Don't know how to extract this from HTML
		fi._ppmNumber = fi._ppmNumber;

		// Currency
		iwd.println("Finding currency");
//		String currency = MM.getRegExp(null, pageContent,
//				"Andelskurs ",
//				"<td class=\"line text\">",
//				"</td>",
//				true);
		int i_cstr = pageContent.indexOf("Andelsklassens storlek");
		if (i_cstr != -1) {
			String cdebug = pageContent.substring(i_cstr, i_cstr+1000);
			cdebug = cdebug.replace("<", "&lt;");
			cdebug = cdebug.replace(">", "&gt;");
			iwd.println("DEBUG CURRENCE: " + cdebug);
		} else {
			iwd.println("DEBUG CURRENCE: NOT FOUND");
		}

		IndentWriter iw = new IndentWriter();
		String currency = MM.getRegExp(iw, pageContent,
				"Andelsklassens storlek",
				"<td class=\"line text\">",
				"</td>",
				true);
		iwd.println("Result from getRegExp:\n" + iw.getString());

		if (currency == null || currency.length() < 3) {
			iwd.println("Error: Could not find currency field");
			fi._errorCode = D_FundInfo.IC_HTML_MS_CURRENCY;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}
		currency = currency.substring(0, 3);
		if (!fi._currencyName.equals(currency)) {
			iwd.println("Warning, Fund currency changed, before: " + fi._currencyName + ", now: " + currency);
			returnCode = ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED;
		}
		fi._currencyName = currency;
		iwd.println("...Done, currency: " + fi._currencyName);

		iwd.println("Finding indexes");
		String indexFund = null;
		String indexMS = null;
		String index = MM.getRegExp(null, pageContent,
				">Morningstars index f",
				"<td ",
				"</tr>",
				true);
		boolean error = false;
		if (index.indexOf(">") == -1 || index.indexOf("</td><td ") == -1) { error = true; }
		else {
			indexFund = index.substring(index.indexOf(">")+1, index.indexOf("</td>"));
		}
		index = index.substring(index.indexOf("</td><td ")+8);  // Move past those things
		if (index.indexOf(">") == -1 || index.indexOf("</td>") == -1) { error = true; }
		else {
			indexMS = index.substring(index.indexOf(">")+1, index.indexOf("</td>"));
		}
		if (error) {
			iwd.println("Error: Could not find indexes");
			fi._errorCode = D_FundInfo.IC_HTML_MS_INDEX;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}
		iwd.println("...Done indexes, " + "indexFund: " + indexFund + ", indexMS: " + indexMS);
		fi.setIndexName(indexMS);

		// *** HISTORY HTML PAGE
		// Extract HTML information
		pageContentSB = new StringBuffer();
		ec = ExtractFromHTML_Helper.htmlGet(pageContentSB, iwd, fi._url + "&tab=1");
		if (ec._o1 != D_FundInfo.IC_NO_ERROR) {
			fi._errorCode = ec._o1;
			fi._lastExtractInfo = ec._o2;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}
		pageContent = pageContentSB.toString();
		iwd.println("Size of history page: " + pageContent.length());

		// "rlig avkastning "
		String yearlyReturn = MM.getRegExp(null, pageContent,
				"rlig avkastning",
				"</tr>",
				"</table>",
				true);
		if (yearlyReturn == null) {
			iwd.println("Error: Yearly return could not find HTML table, null");
			fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_NOT_FOUND;
			fi._isValid = false;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}
		iwd.println("Yearly return HTML table: " + yearlyReturn);
        D_FundDPDay dpd = new D_FundDPDay();  // To fill in YearToDate
		int dpy_rc = processYearlyReturnTable(iwd, fi, dpd, yearlyReturn);
		if (dpy_rc != ExtractFromHTML_Helper.RC_SUCCESS) {
			iwd.println("ERROR when parsing Yearly Return Table: " + dpy_rc);
			return dpy_rc;
		}

		// Move past yearly table
		int io = pageContent.indexOf("rlig avkastning");
		pageContent = pageContent.substring(io);
		pageContent = pageContent.substring(pageContent.indexOf("</table>"));
		// Get the daily dp
		String dpdTable = MM.getRegExp(null, pageContent,
				"Avkastning ",
				"</td>",
				"</table>",
				true);
		iwd.println("DPDay return HTML table now processed"); // : " + dpdTable);
		// Find todays date
		io = dpdTable.indexOf(">");
		int io2 = dpdTable.indexOf("</td>");
		if (io == -1 || io2 == -1) {
			iwd.println("Error: Could not find DPDay date");
			fi._errorCode = D_FundInfo.IC_HTML_MS_DPDAY_NULLDATE;
			fi._isValid = false;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}
		String dpdDate = dpdTable.substring(io+1, io2);
		iwd.println("Found dpd date: " + dpdDate);
		String actualDate = MM.dateConvert_YYYYDMMDDD_To_YYMMDD(dpdDate);
		dpd._dateYYMMDD_Actual = actualDate;
		iwd.println("Setting the actual DPD date: " + actualDate);
		io = dpdTable.indexOf("</tr>");
		dpdTable = dpdTable.substring(io+5);
		iwd.println("DPD will now process returns HTML table"); //  + dpdTable);
		int dpd_rv = processRecentReturnsTable(iwd, fi, dpd, dpdTable);
		if (dpy_rc != ExtractFromHTML_Helper.RC_SUCCESS) {
			iwd.println("ERROR when parsing DPD Return Table: " + dpd_rv);
			return dpd_rv;
		}
		iwd.println("Done with DPD table");

		return ExtractFromHTML_Helper.RC_SUCCESS;
	}

	// ***********************************************************************

	//------------------------------------------------------------------------
	public static int processRecentReturnsTable(
			IndentWriter iwd,
			D_FundInfo fi,
			D_FundDPDay dpdIn,
			String table) throws Exception {
		iwd.println("processRecentReturnsTable entered");
		iwd.push();
		int rc = processRecentReturnsTableImpl(iwd, fi, dpdIn, table);
		iwd.println("Resulting DPD");
		dpdIn.dumpInfo(iwd);
		iwd.println();
		iwd.pop();
		iwd.println("processRecentReturnsTable exit");
		return rc;
	}
	public static int processRecentReturnsTableImpl(
			IndentWriter iwd,
			D_FundInfo fi,
			D_FundDPDay dpdIn,
			String table) throws Exception {

		//          Avkastning%   +/-Kategori    +/-Index
		// 1 dag
		// 1 vecka
		// iwd.println("Returns table:\n" + table);

		StringBuffer strb = new StringBuffer(table);
		List<String> categories = getTDs(strb);
		if (categories.size() != 4
				|| !categories.get(1).toLowerCase().contains("avkastning")
				|| !categories.get(2).toLowerCase().contains("kategori")
				|| !categories.get(3).toLowerCase().contains("index")) {
			fi._errorCode = D_FundInfo.IC_HTML_MS_DAILY_TABLE;
			fi._isValid = false;
			iwd.println("Error: categories row did not have 4 entries");
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}

		boolean hasR1W = false;
		while(true) {
			List<String> v = getTDs(strb);
			if (v == null || v.size() < 4) {
				iwd.println("Encountered a row with <4 columns");
				if (v == null) {
					iwd.println("...got null for columns");
				} else {
					for (String s : v) {
						iwd.println("...: " + s);
					}
				}
				iwd.println("Done with all the rows, breaking");
				break;
			}

			String title = v.get(0).toLowerCase();
			OTuple2G<Boolean, Float> rv = ExtractFromHTML_Helper.validFloat(v.get(1));
			iwd.println("Processing title: " + title + ", valid: " + rv._o1 + ", value: " + rv._o2);
			if (!rv._o1) {
				iwd.println("Invalid float: " + v.get(1) + " for column: " + title);
				fi._errorCode = D_FundInfo.IC_HTML_MS_DAILY_TABLE;
				fi._isValid = false;
				return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
			}

			title = title.trim();
			if (title.endsWith("*")) {
				title = title.substring(0, title.length()-1);
			}
			if (title.startsWith("1 dag")) {
				dpdIn._r1d = rv._o2;
			}
			else if (title.startsWith("1 vecka")) {
				dpdIn._r1w = rv._o2;
				if (dpdIn._r1w != D_FundDPDay.FLOAT_NULL) {
					hasR1W = true;
				}
			}
			else if (title.startsWith("1 m")) {
				dpdIn._r1m = rv._o2;
			}
			else if (title.startsWith("3 m")) {
				dpdIn._r3m = rv._o2;
			}
			else if (title.startsWith("6 m")) {
				dpdIn._r6m = rv._o2;
			}
			else if (title.startsWith("1 ") && title.endsWith("r")) {
				dpdIn._r1y = rv._o2;
			}
			else if (title.startsWith("3 ") && title.endsWith("r")) {
				dpdIn._r3y = rv._o2;
			}
			else if (title.startsWith("5 ") && title.endsWith("r")) {
				dpdIn._r5y = rv._o2;
			}
			else if (title.startsWith("10 ") && title.endsWith("r")) {
				dpdIn._r10y = rv._o2;
			}
			else if (title.startsWith("i ") && title.endsWith("r")) {
				// Nothing, we use yearly table for this...
			}
			else {
				iwd.println("Unknown return column: " + title);
				fi._errorCode = D_FundInfo.IC_HTML_MS_DAILY_TABLE;
				fi._isValid = false;
				return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
			}
		}

		if (hasR1W) {
			fi._dpDays.add(0, dpdIn);
		} else {
			iwd.println("Processed DPD table but could not find R1W so it's a no go");
		}
		return ExtractFromHTML_Helper.RC_SUCCESS;
	}

	//------------------------------------------------------------------------
	private static List<String> getTDs(StringBuffer strb) {
		String all = strb.toString();
		int io_start = all.indexOf("<tr>");
		int io_end = all.indexOf("</tr>");
		if (io_start == -1 || io_end == -1) {
			return null;
		}
		String row = all.substring(io_start, io_end);
		strb.delete(0, strb.length());
		if (io_end+5 < all.length()) {
			strb.append(all.substring(io_end+5));
		}
		List<String> l = new ArrayList<>();
		io_start = row.indexOf("<td ");
		while (io_start != -1) {
			row = row.substring(io_start);
			io_start = row.indexOf(">");
			io_end = row.indexOf("</td>");
			if (io_start == -1 || io_end == -1) return null;
			l.add(row.substring(io_start+1, io_end).trim());
			row = row.substring(io_end);
			io_start = row.indexOf("<td ");
		}
		return l;
	}

	//------------------------------------------------------------------------
	public static int processYearlyReturnTable(
			IndentWriter iwd,
			D_FundInfo fi,
			D_FundDPDay dpd,
			String table) throws Exception {
		iwd.println("processYearlyReturnTable entered");
		iwd.push();
		int rc = processYearlyReturnTableImpl(iwd, fi, dpd, table);
		iwd.println("Resulting dpys");
		for (D_FundDPYear dpy: fi._dpYears) {
			dpy.dumpInfo(iwd);
			iwd.println();
		}
		iwd.pop();
		iwd.println("processYearlyReturnTable exit");
		return rc;
	}
	public static int processYearlyReturnTableImpl(
			IndentWriter iwd,
			D_FundInfo fi,
			D_FundDPDay dpd,
			String table) throws Exception {
		//                    2013 2014 ... MM-DD(YTD)
		// +/- Kategori
		// +/- Index
		// %-rank i kategorin
		// System.out.println("Yearly table\n" + table);
		// if (true) return ExtractFromHTML_Helper.RC_SUCCESS;

		// Parse Years row
		StringBuffer strb = new StringBuffer(table);
		List<String> years = getTDs(strb);
		if (years == null || years.size() <= 1) {
			iwd.println("Years == null or had just 1 entry");
			return ExtractFromHTML_Helper.RC_SUCCESS;
		}
		iwd.println("Number of columns in years row: " + years.size());
		years.remove(0);

		List<D_FundDPYear> ys = new ArrayList<>();
		for (String ystr: years) {
			if (ystr.length() == 0) {
				ys.add(null);
				continue;
			}

			if (ystr.endsWith("*")) {
				ystr = ystr.substring(0, ystr.length()-1);
			}

			D_FundDPYear dpy = new D_FundDPYear();
			short year = 9999;  // that is also YTD
			try {
				year = (short)Integer.parseInt(ystr);
			} catch(NumberFormatException exc) {
				if (!ystr.contains("-")) {
					iwd.println("Error, expected Y2D but did not contain -: [" + ystr + "]");
					fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_YEAR;
					fi._isValid = false;
					return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
				}
			}
			iwd.println("Adding D_FundDPYear with year: " + year);
			dpy._year = year;
			ys.add(dpy);
		}

		// Parse rows: Avkastning %, +/- Kategori, +/- index, %-rank i kategorin
		iwd.println("Year row parsed, will now get the floats");
		boolean foundAvkastning = false;
		while (true) {
			List<String> row = getTDs(strb);
			if (row == null) {
				iwd.println("No more floats to get");
				break;
			}

			if (ys.size()+1 != row.size()) {
				iwd.println("Float row count mismatch, ys: " + ys.size() + ", row: " + row.size());
				fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_ROW_MISMATCH;
				fi._isValid = false;
				return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
			}
			String rtitle = row.remove(0);
			iwd.println("Parsing row for title: " + rtitle);
			for (int i=0; i < row.size(); i++) {
				// Is this a null column, then move on...
				if (ys.get(i) == null) {
					continue;
				}

				// Not a null column, get values
				String col = row.get(i);
				OTuple2G<Boolean, Float> rv = ExtractFromHTML_Helper.validFloat(col);
				if (!rv._o1) {
					iwd.println("Invalid float: " + col);
					fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_ROW_MISMATCH;
					fi._isValid = false;
					return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
				}
				if (rtitle.toLowerCase().contains("avkastning")) {
					iwd.println("Found avkastning: " + col);
					foundAvkastning = true;
					ys.get(i)._resultFund = rv._o2;
				} else if (rtitle.toLowerCase().contains("+/- kategori")) {
					iwd.println("Found kategori: " + col);
					ys.get(i)._resultCategory = rv._o2;
				} else if (rtitle.toLowerCase().contains("+/- index")) {
					iwd.println("Found index: " + col);
					ys.get(i)._resultIndex = rv._o2;
				} else if (rtitle.toLowerCase().contains("%-rank i kategorin")) {
					iwd.println("Found rank i kategorin: " + col);
					// TODO: Add code here
				}
			}
		}

		// At least we must have found Avkastning (otherwise, what's the point)
		if (!foundAvkastning) {
			iwd.println("Error: Did not find avkastning");
			fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_FUND;
			fi._isValid = false;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}

		// Filter out null years
		int index = 0;
		while (index < ys.size()) {
			if (ys.get(index) == null) {
				ys.remove(index);
			} else {
				index++;
			}
		}

		// Reassign Y2D to DPD
		for (int i=0; i < ys.size(); i++) {
			D_FundDPYear dpy = ys.get(i);
			if (dpy.isYearToDate()) {
				iwd.println("Found Y2D, converting it to DPD, result");
				dpd._rYTDFund = dpy._resultFund;
				dpd._rYTDCategory = dpy._resultCategory;
				dpd._rYTDIndex = dpy._resultIndex;
				ys.remove(i);
				dpd.dumpInfo(iwd);
				iwd.println();
				break;
			}
		}
		fi._dpYears = ys;
		Collections.sort(fi._dpYears, D_FundDPYear.COMPARATOR);
		return ExtractFromHTML_Helper.RC_SUCCESS;
	}

	//------------------------------------------------------------------------
	private static String getFromFirstLetter(String result) throws Exception {
		result = result.trim();
		for(int i=0; i < result.length(); i++) {
			if(Character.isLetter(result.charAt(i))) {
				result = result.substring(i);
				result = result.trim();
				break;
			}
		}
		if(result.trim().length() == 0) {
			result = "-";
		}
		return result;
	}
}
