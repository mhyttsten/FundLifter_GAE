package com.pf.fl.shared.utils;

import com.pf.fl.shared.Constants;
import com.pf.fl.shared.datamodel.D_FundInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FundList_Validator {
    private static final Logger log = Logger.getLogger(FundList_Validator.class.getName());

    private String _text;
    public List<D_FundInfo> _fis;
    public List<OTuple2G<String, String>> _nameAndURLList = new ArrayList<>();

    public List<D_FundInfo> _fiInDBButNotList = new ArrayList<>();
    public List<OTuple2G<String,String>> _fiInListButNotDB = new ArrayList<>();
    public List<OTuple2G<String,String>> _fiNameMatchURLMismatch = new ArrayList<>();
    public List<OTuple2G<String,String>> _fiURLMatchNameMismatch = new ArrayList<>();

    //------------------------------------------------------------------------
    public static void main(String[] args) {
        try {
            mainImpl(args);
        } catch (Exception exc) {
            System.out.println(exc);
            exc.printStackTrace();
        }
    }
    public static void mainImpl(String[] args) throws Exception {
        System.out.println("1");
        byte[] data = MM.fileReadFrom("/Users/magnushyttsten/Desktop/ppm/fundlist_ppm.txt");
        System.out.println("2");
        String input = new String(data, Constants.ENCODING_FILE_READ);
        StringBuffer strb = new StringBuffer(input);
        System.out.println("3, size: " + input.length());
    }


    //------------------------------------------------------------------------
    public FundList_Validator(String text, List<D_FundInfo> fis) {
        _text = text;
        _fis = fis;
    }

    //------------------------------------------------------------------------
    public void process_SEB() throws IOException {
        String eoe = "Submit";
        String nl = "\n";
        int io;
        int sindex = 0;
        StringBuffer sb = new StringBuffer(_text);
        Map<String, Void> hmdups = new HashMap<>();
        while (true) {
            io = sb.indexOf(eoe, sindex);
            if (io == -1) {
                String rem = sb.substring(sindex);
                if (rem.trim().length() > 0) {
                    throw new IOException("Trailing information after last submit: [" + rem + "]");
                }
                break;
            }

            String result = sb.substring(sindex, io);
            sindex = io;
            io = result.indexOf("\t");
            if (io == -1) {
                throw new IOException("Not found tab, which was expected: [" + result + "]");
            }

            result = result.substring(0, io).trim();
            if (hmdups.containsKey(result)) {
                throw new IOException("Duplicate fund name found: " + result);
            }
            hmdups.put(result, null);
            _nameAndURLList.add(new OTuple2G<String, String>(result, null));
            log.info("Found fund: " + result);

            io = sb.indexOf(nl, sindex);
            if (io == -1) {
                break;
            }
            io += nl.length();
            if (io > sb.length()) {
                throw new IOException("Found newline after Submit, but nothing after: [" + result + "]");
            }
            sindex = io;
        }
        process();
    }

    //------------------------------------------------------------------------
    public void process_PPM() throws IOException {
        List<OTuple2G<String, String>> nameAndURLList = new ArrayList<>();

        StringBuffer strb = new StringBuffer(_text);
        int io = -1;
        List<StringBuffer> segments = new ArrayList<>();
        do {
            io = strb.indexOf("Snitt:");
            if (io == -1) {
                break;
            }
            strb.replace(0, io, "");
            io = strb.indexOf("/table");
            if (io == -1) {
                throw new IOException("Could not find terminating /table after finding a Snitt:");
            }
            String segment = strb.substring(0, io);
            segments.add(new StringBuffer(segment));
            strb.replace(0, io, "");
        } while(true);

        if (segments.size() <= 0) {
            throw new IOException("Could not find a single Snitt: segment");
        }

        log.info("Processed segments, found: " + segments.size());


        while (segments.size() > 0) {
            strb = segments.remove(0);
            OTuple2G<String, String> nameAndURL = null;
            Map<String, Void> hmdups = new HashMap<>();
            do {
                nameAndURL = getNextPPM(strb);
                if (nameAndURL != null) {
                    if (hmdups.containsKey(nameAndURL._o1)) {
                        throw new IOException("Found duplicate PPM name: " + nameAndURL._o1);
                    }
                    hmdups.put(nameAndURL._o1, null);
                    _nameAndURLList.add(nameAndURL);
                }
            } while (nameAndURL != null);
        }

        log.info("Processed all entries, total fund count: " + _nameAndURLList.size());
        process();
    }

    private static OTuple2G<String, String> getNextPPM(StringBuffer strb) throws IOException {
        // <a class="html-attribute-value html-external-link" target="_blank" href="http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid="
        String tag = "<a class=\"html-attribute-value html-external-link\" target=\"_blank\" href=\"http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=";
        int io = -1;

        io = strb.indexOf(tag);
        if (io == -1) {
            return null;
        }
        strb.replace(0, io, "");

        tag = "href=\"";
        io = strb.indexOf(tag);
        if (io == -1) {
            throw new IOException("Expected href: " + MM.getString(strb.toString(), 200));
        }
        strb.replace(0, io+tag.length(), "");
        tag = "\">";
        io = strb.indexOf(tag);
        if (io == -1) {
            throw new IOException("Expected end of href: " + MM.getString(strb.toString(), 200));
        }
        String url = strb.substring(0, io);
        url = url.replace("&amp;", "&").trim();
        strb.replace(0, io, "");

        tag = "</span>";
        io = strb.indexOf(tag);
        if (io == -1) {
            throw new IOException("Expected </span>: " + MM.getString(strb.toString(), 200));
        }
        strb.replace(0, io+tag.length(), "");
        tag = "<span";
        io = strb.indexOf(tag);
        if (io == -1) {
            throw new IOException("Expected <span: " + MM.getString(strb.toString(), 200));
        }
        String nameOrig = strb.substring(0, io);
        nameOrig = nameOrig.replace("&amp;", "&").trim();
        strb.replace(0, io, "");

        OTuple2G<String, String> ot = new OTuple2G<>();
        ot._o1 = nameOrig;
        ot._o2 = url;
        return ot;
    }

    //------------------------------------------------------------------------
    private void process() throws IOException {
        log.info("Now in process, fis: " + _fis.size() + ", nameOrigs: " + _nameAndURLList.size());

        _nameAndURLList.sort(new Comparator<OTuple2G<String, String>>() {
            @Override
            public int compare(OTuple2G<String, String> o1, OTuple2G<String, String> o2) {
                return o1._o1.compareTo(o2._o1);
            }
        });

        List<OTuple2G<String, String>> nameAndURLList = new ArrayList<>();
        for (OTuple2G<String, String> s: _nameAndURLList) {
            nameAndURLList.add(s);
        }

        for (D_FundInfo fi: _fis) {
            boolean found = false;
            for (int i=0; i < nameAndURLList.size(); i++) {
                OTuple2G<String,String> nAu = nameAndURLList.get(i);

                if (nAu._o2 != null && nAu._o2.equals(fi._url) && !nAu._o1.equals(fi.getNameOrig())) {
                    log.info("*** n_n: " + nAu._o1 + ", url: " + nAu._o2);
                    log.info("*** f_n: " + fi.getNameOrig() + ", url: " + fi._url);

                    if (nAu._o1.equals(fi.getNameOrig())) {
                        log.info("...equal");
                    } else  {
                        log.info("...not equal");
                    }

                    _fiURLMatchNameMismatch.add(nAu);
                }
                else if (nAu._o2 != null && !nAu._o2.equals(fi._url) && nAu._o1.equals(fi.getNameOrig())) {
                    log.info("List name: " + nAu._o1 + ", url: " + nAu._o2
                        + "Fi   name: " + fi.getNameOrig() + ", url: " + nAu._o2);
                    _fiNameMatchURLMismatch.add(nAu);
                }
                else if (nAu._o1.equals(fi.getNameOrig())) {
                    nameAndURLList.remove(i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                _fiInDBButNotList.add(fi);
            }
        }

        for (OTuple2G<String, String> nAu: nameAndURLList) {
            _fiInListButNotDB.add(nAu);
        }

        log.info("Resulting mismatch stats"
                + "\nInDBButNotList: " + _fiInDBButNotList.size()
                + "\nInListButNotInDB: " + _fiInListButNotDB.size()
                + "\nNameMatchURLMismatch: " + _fiNameMatchURLMismatch.size()
                + "\nURLMatchNameMismatch: " + _fiURLMatchNameMismatch.size());
    }
}
