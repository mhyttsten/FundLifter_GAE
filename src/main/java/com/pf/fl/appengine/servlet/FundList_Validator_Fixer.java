package com.pf.fl.appengine.servlet;

import com.pf.fl.appengine.extract.GCSWrapper;
import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.DB_FundInfo;
import com.pf.fl.shared.datamodel.D_FundInfo;
import com.pf.fl.shared.datamodel.D_FundInfo_Serializer;
import com.pf.fl.shared.utils.FundList_Validator;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class FundList_Validator_Fixer {
    private static final Logger log = Logger.getLogger(FundList_Validator.class.getName());

    public static void doit() throws IOException {
//        insert_Entries(D_FundInfo.TYPE_PPM);
//        delete_URLsOfExisting_PPM();
    }

    private static void insert_Entries(String type) throws IOException {
        String[] toinsert = {
                "Aktia Emerging Market Bond+ B","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000821O&programid=0000000000",
                "Aktia Emerging Market Local Ccy Bd B","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000HP3N&programid=0000000000",
                "Aktia Europe Small Cap B","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001520F&programid=0000000000",
                "Aktia Nordic B","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00005U1H&programid=0000000000",
                "Aktia Nordic Micro Cap B","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P000195NK&programid=0000000000",
                "Aktia Nordic Small Cap B","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000U4K7&programid=0000000000",
                "Aktia Secura B","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000NBR&programid=0000000000",
                "Aktia Solida B","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000NC5&programid=0000000000",
                "Aktie-Ansvar Saxxum Aktiv","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000YXMW&programid=0000000000",
                "Alfred Berg Gambak","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000MVR&programid=0000000000",
                "Alfred Berg Global Quant","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000O9F&programid=0000000000",
                "Cicero World 0-100","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000Y0Q2&programid=0000000000",
                "DNB Global Indeks","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000PS3V&programid=0000000000",
                "DNB Grönt Norden","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000MW8&programid=0000000000",
                "DNB USA","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000O4H&programid=0000000000",
                "Danske Invest Aktiv Förmögenhetsför SI","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C308&programid=0000000000",
                "Danske Invest Alloc Hori. Aktie SA","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C31X&programid=0000000000",
                "Danske Invest Alloc Hori. Balanserad SA","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C327&programid=0000000000",
                "Danske Invest Alloc Hori. Försiktig SA","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C329&programid=0000000000",
                "Danske Invest Alloc Hori. Offensiv SA","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C321&programid=0000000000",
                "Danske Invest Alloc Hori. Ränta SA","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C323&programid=0000000000",
                "Danske Invest Europa (SEK) SI","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C30B&programid=0000000000",
                "Danske Invest Global Index SI","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C30J&programid=0000000000",
                "Danske Invest SRI Global SA","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C30K&programid=0000000000",
                "Danske Invest Sverige Europa SI","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C30H&programid=0000000000",
                "Danske Invest Sverige Fokus SI","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C30P&programid=0000000000",
                "Danske Invest Sverige Kort Ränta SI","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C31V&programid=0000000000",
                "Danske Invest Sverige Ränta SI","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C30T&programid=0000000000",
                "Danske Invest Sverige SI","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001C30Z&programid=0000000000",
                "East Capital Sustainable EM A EUR","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001ANT7&programid=0000000000",
                "Enter Return A","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000IWCK&programid=0000000000",
                "Evli Global B SEK","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000YU5B&programid=0000000000",
                "FIM Euro","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000NSL&programid=0000000000",
                "Fondita European Micro Cap B","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00019WV0&programid=0000000000",
                "Handelsbanken Rysslandsfond","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00009V39&programid=0000000000",
                "IKC Global Healthcare A","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00018MBP&programid=0000000000",
                "Indecap Guide Q30 C","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001BVEG&programid=0000000000",
                "Investerum Basic Value","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00015HXW&programid=0000000000",
                "JRS Global Edge RC SEK","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P000195N6&programid=0000000000",
                "Jämställda Bolag Europa A","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001AL4M&programid=0000000000",
                "Jämställda Bolag Global A","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001AL4P&programid=0000000000",
                "Jämställda Bolag Sverige A","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001AL50&programid=0000000000",
                "Lannebo Komplett","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00012DRJ&programid=0000000000",
                "Movestic Bear 2.0 C","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P000199N5&programid=0000000000",
                "Movestic Bull 2.0 C","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P000199N7&programid=0000000000",
                "Nordea Japan","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000YA86&programid=0000000000",
                "ODIN Energi C","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000O7Z&programid=0000000000",
                "OPM Global Quality Companies A","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00016MU2&programid=0000000000",
                "Pacific Extraordinary Brands A","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001868A&programid=0000000000",
                "Pictet - Robotics R USD","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00016UT9&programid=0000000000",
                "PriorNilsson Smart Global","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00019QOC&programid=0000000000",
                "SEB Pensionsfond Extra SEK - Lux","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001AH55&programid=0000000000",
                "SEB Pensionsfond Plus SEK- Lux","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00017QWM&programid=0000000000",
                "SEF Aktiv Finans EGAS Dyn Port A SEK","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00018DCL&programid=0000000000",
                "SEF Case Power Play","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00019JWF&programid=0000000000",
                "SEF EME European Best Ideas P SEK","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00017N3B&programid=0000000000",
                "SEF Kuylenstierna & Skog Equities P","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00009RDA&programid=0000000000",
                "SPP Sverige Plus A","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00018N0G&programid=0000000000",
                "Simplicity Småbolag Sverige A","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00018Z03&programid=0000000000",
                "Sparinvest SICAV Eth HY Value Bds EUR R","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000T0KL&programid=0000000000",
                "Sparinvest SICAV Eth HY Value Bds SEK R","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000X5PL&programid=0000000000",
                "Swedbank Robur Global Emerging Markets","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000805P&programid=0000000000",
                "Taaleri Nordic Value Equity A","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00014LQ5&programid=0000000000",
                "Tellus Fonder Bank & Finansfond","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00019ORD&programid=0000000000",
                "UBS (Lux) ES Euro Countrs Inc€ P-acc","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00015AJB&programid=0000000000",
                "Ålandsbanken Dynamisk Ränta SEK","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001A1WW&programid=0000000000",
                "Ålandsbanken Premium 100 Gbl Aktie B","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001AORK&programid=0000000000",
                "Öhman Obligationsfond SEK A","http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0001244U&programid=0000000000"
        };

        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        DB_FundInfo.initialize(fundInfoBA, true);
        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();

        log.info("*** About to insert entries, size before: " + fis.size());

        int total_inserted = 0;
        int index = 0;
        while (index < toinsert.length) {
            String ln = toinsert[index];
            index++;
            String lu = toinsert[index];
            index++;

            for (D_FundInfo fi: fis) {
                if (fi._type.equals(type) &&
                        (fi.getNameMS().equals(ln) || fi.getNameOrig().equals(ln) || fi._url.equals(lu))) {
                    throw new AssertionError("Such fund already existed"
                            + "\nln: " + ln + ", lu: " + lu
                            + "\n" + fi.toString());
                }
            }
            D_FundInfo fi = new D_FundInfo();
            fi._type = type;
            fi.setNameMS(ln);
            fi.setNameOrig(ln);
            fi._url = lu;
            fis.add(fi);
            total_inserted++;
        }
        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
        log.info("*** Insert completed, size after: " + fis.size());
    }

    private static void delete_URLsOfExisting_PPM() throws IOException {
        String[] todelete = {
                "BL-Fund Selection 50-100 B", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000KWN&programid=0000000000",
                "BL-Fund Selection Equities B", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000KW9&programid=0000000000",
                "Granit Kina", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000S2D5&programid=0000000000",
                "Invesco Global Equity Income R USD Acc", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000U1YS&programid=0000000000",
                "Invesco Japanese Value Equity R JPY Acc", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000U1YY&programid=0000000000",
                "Invesco US Value Equity R USD Acc", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000U1YO&programid=0000000000",
                "NPG Life: Gustavia Global Tillväxt", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000P2HR&programid=0000000000",
                "NPG Life: Gustavia Kazakstan/Centralasien", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000PRDQ&programid=0000000000",
                "ODIN Energi C SEK", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000KBB&programid=0000000000",
                "Solidar SICAV Global Fokus I", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000PWCJ&programid=0000000000",
                "UB Amerika A", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00011X7O&programid=0000000000",
                "Ålandsbanken Premium 70 B", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000N9V&programid=0000000000",
                "Öhman Realräntefond A", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00001NJM&programid=0000000000"
        };

        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        DB_FundInfo.initialize(fundInfoBA, true);
        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();

        int index = 0;
        int total_deleted = 0;
        while (index < todelete.length) {
            String dn = todelete[index];
            index++;
            String du = todelete[index];
            index++;

            int index2 = 0;
            boolean found = false;
            while (index2 < fis.size()) {
                D_FundInfo fi = fis.get(index2);
                if (fi._type.equals(D_FundInfo.TYPE_PPM)
                        && fi.getNameOrig().equals(dn)
                        && fi._url.equals(du)) {
                    log.info("*** Deleting 2: " + fi.getTypeAndName()
                            + "\n...old (DB) URL: " + fi._url
                            + "\n...dn:           " + dn
                            + "\n...du:           " + du);
                    fis.remove(index2);
                    total_deleted++;
                    found = true;
                    break;
                }
                index2++;
            }
            if (!found) {
                log.info("*** Could not delete, but could not find it: "
                        + "\n...dn:           " + dn
                        + "\n...du:           " + du);
            }
        }
        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
        log.info("*** Total funds deleted2: " + total_deleted);
    }

    private static void update_URLsOfExisting_PPM() throws IOException {
        log.info("*** Will now fix the URLs");
        String[] list = {
                "DNB Health Care", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000MUY&programid=0000000000",
                "ODIN Emerging Markets C", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000O8C&programid=0000000000",
                "ODIN Global C", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000O88&programid=0000000000",
                "SKAGEN m2 A", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000X09F&programid=0000000000"
        };

        String[] db = {
                "DNB Health Care", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000TIM8&programid=0000000000",
                "ODIN Emerging Markets C", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000UHAX&programid=0000000000",
                "ODIN Global C", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00009PNY&programid=0000000000",
                "SKAGEN m2 A", "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000X09G&programid=0000000000"
        };

        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        DB_FundInfo.initialize(fundInfoBA, true);
        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();
        int index = 0;
        int total_updated = 0;
        while (index < list.length) {
            String ln = list[index];
            String dn = db[index];
            index++;
            String lu = list[index];
            String du = db[index];
            index++;

            if (!ln.equals(dn)) {
                throw new AssertionError("Names were different, ln: " + ln + ", dn: " + dn);
            }
            if (lu.equals(du)) {
                throw new AssertionError("URLs were the same, lu: " + lu + ", du: " + du);
            }

            int index2 = 0;
            while (index2 < fis.size()) {
                D_FundInfo fi = fis.get(index2);
                if (fi._type.equals(D_FundInfo.TYPE_PPM)
                        && fi.getNameOrig().equals(ln)
                        && fi._url.equals(du)) {
                    log.info("*** Updating: " + fi.getTypeAndName()
                            + "\n...old (DB) URL: " + fi._url
                            + "\n...du:           " + du
                            + "\n...lu:           " + lu);
                    fi._url = lu;
                    total_updated++;
                    break;
                } else if (fi._type.equals(D_FundInfo.TYPE_PPM)
                        && fi.getNameOrig().equals(ln)
                        && !fi._url.equals(du)) {
                    log.info("*** MISMATCH ONLY BY URL: " + fi.getTypeAndName()
                            + "\n...old (DB) URL: " + fi._url
                            + "\n...du:           "
                            + "\n...lu:           " + lu);
                }

                index2++;
            }
        }
//        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
//        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
//        log.info("*** Total funds updated: " + total_updated);
    }
}
