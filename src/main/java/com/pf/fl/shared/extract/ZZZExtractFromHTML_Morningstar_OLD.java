package com.pf.fl.shared.extract;

import com.pf.fl.shared.datamodel.D_FundDPDay;
import com.pf.fl.shared.datamodel.D_FundDPYear;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.utils.IndentWriter;
import com.pf.fl.shared.utils.MM;
import com.pf.fl.shared.utils.OTuple2G;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ZZZExtractFromHTML_Morningstar_OLD {
	private static final Logger log = Logger.getLogger(ZZZExtractFromHTML_Morningstar_OLD.class.getName());
	private static final String TAG = "FL_MSExtractDetails";

	//------------------------------------------------------------------------
	public static int extractFundDetails(
			IndentWriter iwd,
			D_FundInfo fi,
			String pageContent) throws Exception {

		OTuple2G ot = null;
		String findTagLoc = null;
		String findAfter = null;
		String findTo = null;
		String result = null;
		int returnCode = ExtractFromHTML_Helper.RC_SUCCESS;

		iwd.println("Finding fund name");
		ot = new OTuple2G<String, String>(null, pageContent);
		String fundName = MM.assignAndReturnNextTagValue(ot, "<h2");
		if(fundName == null || (fundName.startsWith("S") && fundName.endsWith("k Fonder"))) {
			iwd.println("Invalid URL content encountered, indicating that the URL data is not the fund");
			fi._errorCode = D_FundInfo.IC_INVALID_URL_IRRECOVERABLE;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}

		fundName = MM.htmlReplaceHTMLCodes(fundName);
		if (fundName.toLowerCase().startsWith("ett fel uppstod")) {
			fi._errorCode = D_FundInfo.IC_INVALID_URL_IRRECOVERABLE;
			iwd.println("Error for fund, it's name is not valid: \"Ett fel uppstod\"");
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}
		iwd.println("...Done, fundName: " + fundName);
		if (!fi.getNameMS().equals(fundName)) {
			iwd.println("Warning: Fund changed name, before: " + fi.getNameMS() + ", now: " + fundName);
			returnCode = ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED;
		}
		fi.setNameMS(fundName);

		// MS Category
		iwd.println("Finding category name");
		findTagLoc = "<span class=\"quicktakecolContainer\" title=\"Morningstar Kategori";
		findAfter = ">";
		findTo = "</span>";
		String ahref = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		findTagLoc = "<a";
		findAfter = "href=\"";
		findTo = "\"";
		if (ahref == null) {
			iwd.println("Error: Could not find category name ahref was null");
			fi._errorCode = D_FundInfo.IC_HTML_MS_CATEGORY;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}
		String msCategoryHTML = MM.getRegExp(null, ahref, findTagLoc, findAfter, findTo, true);
		findTagLoc = ">";
		findAfter = "";
		findTo = "</a>";
		String msCategoryText = MM.getRegExp(null, ahref, findTagLoc, findAfter, findTo, true);
		msCategoryText = MM.htmlReplaceHTMLCodes(msCategoryText);
		if (!fi.getCategoryName().equals(msCategoryText)) {
			iwd.println("Warning, Fund category changed, before: " + fi.getCategoryName() + ", now: " + msCategoryText);
			returnCode = ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED;
		}
		fi.setCategoryName(msCategoryText);
		iwd.println("...Done, categoryName: " + fi.getCategoryName());

		// MS Rating
		// <span class="quicktakecolContainer" title="Morningstar Rating"
		iwd.println("Finding MS Rating");
		findTagLoc = "<span class=\"quicktakecolContainer\" title=\"Morningstar Rating";
		findAfter = "class=\"";
		findTo = "\"";
		String msRatingHTML = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		msRatingHTML = MM.htmlReplaceHTMLCodes(msRatingHTML);
		if (msRatingHTML != null && msRatingHTML.startsWith("section_separator")) {
			msRatingHTML = "-";
		}
		String msRating = msRatingHTML.trim();
		int rating = -1;
		if (msRating != null && msRating.length() > 0) {
			if (msRating.equals("stars1")) {
				rating = 1;
			} else if (msRating.equals("stars2")) {
				rating = 2;
			} else if (msRating.equals("stars3")) {
				rating = 3;
			} else if (msRating.equals("stars4")) {
				rating = 4;
			} else if (msRating.equals("stars5")) {
				rating = 5;
			} else if (msRating.equals("-")) {
				rating = -1;
			} else {
				iwd.println("Error, FundInfo, rating could not be parsed: " + msRating);
				fi._errorCode = D_FundInfo.IC_HTML_MS_RATING;
				return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
			}
		} else {
			iwd.println("Error, FundInfo, rating could not be parsed because it was null");
			fi._errorCode = D_FundInfo.IC_HTML_MS_RATING;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		}
		fi._msRating = rating;
		iwd.println("...Done, rating: " + rating);

		// PPM Number
		iwd.println("Finding PPM Number");
		findTagLoc = "<span class=\"quicktakecolContainer\" title=\"PPM-nummer\"";
		findAfter = ">";
		findTo = "</span>";
		String ppmNumber = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		ppmNumber = MM.htmlReplaceHTMLCodes(ppmNumber).trim();
        fi._ppmNumber = ppmNumber;
		iwd.println("...Done, number: " + ppmNumber);

        D_FundDPDay dpd = new D_FundDPDay();

		iwd.println("Parsing Yearly return table");
		// Yearly returns (table), I??r!!!
		findTagLoc = "<th class=\"decimal\">Fond</th><th class=\"decimal\">Kategori</th><th class=\"decimal\">Index</th>";
		findAfter = "</tr>";
		findTo = "</table>";
		String yearlyReturnTable = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		iwd.push();
		int dpy_rc = processYearlyReturnTable(iwd, fi, dpd, yearlyReturnTable);
		iwd.pop();
		if (dpy_rc != ExtractFromHTML_Helper.RC_SUCCESS) {
			iwd.println("...error when parsing yearly return table");
			return dpy_rc;
		}
		iwd.println("...Done successfully parsing yearly return table");

		iwd.println("Parsing Kursdatum");
		findTagLoc = "<b>Kursdatum";
		findAfter = "</b>:";
		findTo = "<br />";
		iwd.println("...Done");
//		String yearlyReturnsLastUpdatedDate = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
//		iwd.println("...Done, result length: " + yearlyReturnsLastUpdatedDate.length() + ", content: " + yearlyReturnsLastUpdatedDate);

		// Comparison category
		iwd.println("Parsing Index name");
		findTagLoc = "<b>Kategorins j";
		findAfter = "</b>:";
		findTo = "<br />";
		String indexCompare = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		fi.setIndexName(indexCompare);
		iwd.println("...Done, indexName: " + indexCompare);

		// Get currency in which NAV is measured
		iwd.println("Parsing Senaste NAV");
		findTagLoc = "<td>Senaste NAV</td>";
		findAfter = "<td>";
		findTo = "</td>";
		result = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		result = getFromFirstLetter(result);
		if (!fi._currencyName.equals(result)) {
			iwd.println("Warning, Fund currency change, before: " + fi._currencyName + ", now: " + result);
			returnCode = ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED;
		}
		fi._currencyName = result;
		iwd.println("...Done, currency: " + result);

		// Return (daily, month, 3m, 6m, 1y)
		iwd.println("Parsing Avkastning");
		iwd.push();
		findTagLoc = "Avkastning %";
		findAfter = "<table class=\"alternatedtoplist\" cellspacing=\"0\" cellpadding=\"0\">";
		findTo = "</table>";
		String recentReturnTable = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		int dpd_rv = processRecentReturnsTable(iwd, fi, dpd, recentReturnTable);
		iwd.pop();
		if (dpd_rv != ExtractFromHTML_Helper.RC_SUCCESS) {
			iwd.println("...error: " + dpd_rv);
			return dpd_rv;
		}
		iwd.println("...Done, recent returns");

		// Returns: Date of last recorded data point
		iwd.println("Avkastning %");
		findTagLoc = "Avkastning %";
		findAfter = "<table class=\"alternatedtoplist\" cellspacing=\"0\" cellpadding=\"0\">";
		findTo = "</html>";
		result = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		findTagLoc = "<b>Kursdatum";
		findAfter = "</b>:";
		findTo = "</div>";
		String returnsLastDate = MM.getRegExp(null, result, findTagLoc, findAfter, findTo, true);
		returnsLastDate = returnsLastDate.trim();
		returnsLastDate = MM.dateConvert_YYYYDMMDDD_To_YYMMDD(returnsLastDate);
		fi._dpDays.get(0)._dateYYMMDD_Actual = returnsLastDate;
		iwd.println("...Done, avkastning %, returnsLastDate: " + returnsLastDate);
		return ExtractFromHTML_Helper.RC_SUCCESS;
	}

	// ***********************************************************************

	//------------------------------------------------------------------------
	public static int processRecentReturnsTable(
			IndentWriter iwd,
			D_FundInfo fi,
			D_FundDPDay dpdIn,
			String recentReturnTable) throws Exception {
		iwd.println("processRecentReturnsTable");

		if (recentReturnTable == null) {
			iwd.println("Recent returns table was no, no dpday found");
			return ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND;
		}

		/*
		<tr>
		   <th>&nbsp;</th>
		   <th class="decimal">GBP</th>
		   <th class="decimal">SEK</th>
		</tr>
		<tr>
			<td title="1 dag">1 dag</td>
			<td title="-0,2" class="decimal">-0,2</td>
			<td title="-0,4" class="decimal">-0,4</td>
		</tr>
		<tr class="alternate">
			<td title="1 vecka">1 vecka</td>
			<td title="-0,1" class="decimal">-0,1</td>
			<td title="1,0" class="decimal">1,0</td>
		</tr>		
		*/

		String rrTable = "null";
		rrTable = "length: " + String.valueOf(recentReturnTable.trim().length());
		int ioTR = recentReturnTable.indexOf("<tr");
		rrTable = ", indexOf: " + ioTR;

		List<D_FundDPDay> dpds = new ArrayList<>();
		List<String> entries = MM.getTagValues(recentReturnTable, "<tr");

		iwd.println("...entries: " + entries.size());

		// Each column header is its separate currency
		List<String> headers = MM.getTagValues(entries.get(0), "<th");
		iwd.println("...headers: " + entries.size());
		for(int i=1; i < headers.size(); i++) {
			String header = headers.get(i);
			D_FundDPDay dpd = new D_FundDPDay();
			dpd._rYTDFund = dpdIn._rYTDFund;
			dpd._rYTDCategory = dpdIn._rYTDCategory;
			dpd._rYTDIndex = dpdIn._rYTDIndex;
			dpd._currency = header.trim();
			dpds.add(dpd);
		}

		// Get the daily for each currency (each currency is its separate column)
		for(int i=1; i < entries.size(); i++) {
			ArrayList<String> tdColumns = MM.getTagValues(entries.get(i), "<td");
			for (String s: tdColumns) {
				iwd.println("..." + s);
			}

			String timePeriod = tdColumns.get(0);
			for(int j=1; j < tdColumns.size(); j++) {
				D_FundDPDay dpd = dpds.get(j-1);

				String returns = tdColumns.get(j);
				returns = MM.replaceArgTo(returns, " ", "");
				returns = MM.replaceArgTo(returns, "%", "");
				returns = MM.replaceArgTo(returns, ",", ".");

				OTuple2G<Boolean, Float> rv = ExtractFromHTML_Helper.validFloat(returns);
				if (!rv._o1) {
					iwd.println("Error, dpd, a result could not be converted to a number: " + returns);
					fi._errorCode = D_FundInfo.IC_HTML_MS_DAILY_TABLE;
					return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
				}
				float rfloat = D_FundDPDay.FLOAT_NULL;
				if (rv._o2 != null) {
					rfloat = rv._o2.floatValue();
				}

				if(Pattern.matches("1 dag", timePeriod)) {
					dpd._r1d = rfloat;
				}
				else if(Pattern.matches("1 vecka", timePeriod)) {
					dpd._r1w = rfloat;
				}
				else if(Pattern.matches("1 m.nad", timePeriod)) {
					dpd._r1m = rfloat;
				}
				else if(Pattern.matches("3 m.nader", timePeriod)) {
					dpd._r3m = rfloat;
				}
				else if(Pattern.matches("6 m.nader", timePeriod)) {
					dpd._r6m = rfloat;
				}
				else if(Pattern.matches("1 .r", timePeriod)) {
					dpd._r1y = rfloat;
				}
				else if(Pattern.matches("3 .r", timePeriod)) {
					dpd._r3y = rfloat;
				}
				else if(Pattern.matches("5 .r", timePeriod)) {
					dpd._r5y = rfloat;
				}
				else if(Pattern.matches("10 .r", timePeriod)) {
					dpd._r10y = rfloat;
				}
			}
		}

		// Get the Swedish currency or error if there is no Swedish
		D_FundDPDay r = null;
		for (D_FundDPDay dpd: dpds) {
			if (dpd._currency.equals("SEK")) {
				iwd.println("Found SEK currency");
				r = dpd;
			}
		}

		if (r == null) {
			IndentWriter iwt = new IndentWriter();
			iwt.println("FundDPDay, could not find result in SEK currency");
			iwt.println("Out of: " + dpds.size() + ", entries");
			iwt.push();
			for (D_FundDPDay dpd: dpds) {
				dpd.dumpInfo(iwt);
			}
			iwt.pop();
			String m = iwt.getString();
			iwd.println(m);
			fi._errorCode = D_FundInfo.IC_HTML_MS_DAILY_SEK_CURRENCY;
			return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
		} else {
			iwd.println("Adding result to fi._dpDays at index 0: " + r);
			fi._dpDays.add(0, r);
		}

		return ExtractFromHTML_Helper.RC_SUCCESS;
	}
	
	//------------------------------------------------------------------------
	public static int processYearlyReturnTable(
			IndentWriter iwd,
			D_FundInfo fi,
			D_FundDPDay dpd,
			String table) throws Exception {
		boolean found = true;

		// <tr>
		//   <td title="I ??r*">I ??r*</td>
		//   <td title="13,5" class="decimal">13,5</td>
		//   <td title="12,8" class="decimal">12,8</td>
		//   <td title="13,9" class="decimal">13,9</td>
  	    // </tr>

		// First get each individual TR tag
		iwd.println("Now parsing tags");
		List<String> trTags = new ArrayList<>();
		found = true;
		OTuple2G<String, String> otTR = new OTuple2G<>(null, table);
		do {
			String tagValue = MM.assignAndReturnNextTagValue(otTR, "<tr");
			if(tagValue == null) {
				found = false;
			}
			else {
				String trTag = (String)otTR._o1;
				trTags.add(trTag);
			}
		} while(found);
		iwd.println("...done, size: " + trTags.size());

		// Then parse all TDs within the TR
		int errorCode = ExtractFromHTML_Helper.RC_SUCCESS;
		for(int i=0; i < trTags.size(); i++) {
			iwd.println("Now processing tag: " + i);
			String trTag = trTags.get(i);
			OTuple2G<String, String> otTDs = new OTuple2G<>(null, trTag);

			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String yearStr = (String) otTDs._o1.trim();
			iwd.println("...yearStr: " + yearStr);

			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String fundStr = (String) otTDs._o1.trim();
			iwd.println("...fundStr: " + fundStr);

			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String categoryStr = (String) otTDs._o1.trim();
			iwd.println("...categoryStr: " + categoryStr);

			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String indexStr = (String) otTDs._o1.trim();
			iwd.println("...indexStr: " + indexStr);

			D_FundDPYear dpy = new D_FundDPYear();
			short year = -1;
			boolean error = false;
			if (yearStr == null || yearStr.length() == 0) {
				iwd.println("Year was null or had length == 0: " + yearStr);
				fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_YEAR;
				return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
			} else if (yearStr.startsWith("I ")) {
				iwd.println("...yearStr started with I , setting to 9999");
				year = 9999;
			} else if (yearStr.length() != 4) {
				iwd.println("Year is incorrect, was not Y2D and did not have length 4: " + yearStr);
				fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_YEAR;
				return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
			} else {
				try {
					year = Short.parseShort(yearStr.substring(0, 4));
				} catch (NumberFormatException exc) {
					iwd.println("Year could not be converted to a number: " + yearStr);
					return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
				}
			}
			dpy._year = year;

			OTuple2G<Boolean, Float> rv = null;

			rv = ExtractFromHTML_Helper.validFloat(fundStr);
			if (!rv._o1) {
				iwd.println("FundDPYear, fund could not be converted to a number: " + fundStr);
				fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_FUND;
				return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
			}
			dpy._resultFund = rv._o2;

			rv = ExtractFromHTML_Helper.validFloat(categoryStr);
			if (!rv._o1) {
				iwd.println("FundDPYear, category could not be converted to a number: " + fundStr);
				fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_CATEGORY;
				return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
			}
			dpy._resultCategory = rv._o2;

			rv = ExtractFromHTML_Helper.validFloat(indexStr);
			if (!rv._o1) {
				iwd.println("FundDPYear, index could not be converted to a number: " + fundStr);
				fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_INDEX;
				return ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND;
			}
			dpy._resultIndex = rv._o2;

			if (dpy.isYearToDate()) {
				iwd.println("...it's y2d");
				dpd._rYTDFund = dpy._resultFund;
				dpd._rYTDCategory = dpy._resultCategory;
				dpd._rYTDIndex = dpy._resultIndex;
			} else {
				iwd.println("...it's not y2d");
				boolean foundYear = false;
				for (D_FundDPYear dpyO : fi._dpYears) {
					if (dpyO._year == dpy._year) {
						foundYear = true;
						// If we have a year e.g. 2018 already recorded already
						// And extracted one differs, then that is a bit weird
						// But this has actually happened, so this error management code has been removed with a warning
						if (dpyO._resultFund != dpy._resultFund ||
								dpyO._resultCategory != dpy._resultCategory ||
								dpyO._resultIndex != dpy._resultIndex) {
							log.warning("FundDPYear, existing D_FundInfo differed from extracted\n" +
									"Existing:  " + dpyO.toString() + "\n" +
									"Extracted: " + dpy.toString());
						}
						// Use the new one anyhow!
						dpyO._resultFund = dpy._resultFund;
						dpyO._resultCategory = dpy._resultCategory;
						dpyO._resultIndex = dpy._resultIndex;
					}
				}
				if (!foundYear) {
					iwd.println("FundDPYear, adding a new year to FundInfo: " + dpy.toString() + ", previously existed");
					iwd.push();
					for (D_FundDPYear dpyO : fi._dpYears) {
						iwd.println(dpyO.toString());
					}
					iwd.pop();

					fi._dpYears.add(dpy);
					Collections.sort(fi._dpYears, D_FundDPYear.COMPARATOR);
				}
			}
		}

		return errorCode;
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
