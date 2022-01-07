package com.pf.fl.shared.utils;

import java.util.ArrayList;
import java.util.List;

public class Timer {

    private List<OTuple2G<String, Long>> _stats = new ArrayList<>();
    private long _tstart = -1;

    public static void main(String[] args) {
        Timer t = new Timer();
        t.start();
        MM.sleepInMS(1500);
        t.mark("1");
        MM.sleepInMS(3000);
        t.mark("2");
        MM.sleepInMS(2000);
        t.mark("3");
        MM.sleepInMS(4300);
        t.mark("4");
        System.out.println(t);
    }

    public void start() {
        _tstart = System.currentTimeMillis();
        mark("Start");
    }

    public void mark(String s) {
        OTuple2G<String,Long> e = new OTuple2G<>();
        long tnow = System.currentTimeMillis();
        e._o1 = s;
        e._o2 = tnow;
        _stats.add(e);
    }

    public String toString() {
        IndentWriter iw = new IndentWriter();
        iw.println("Reporting Timer");
        iw.push();

        for (int i=0; i < _stats.size(); i++) {
            OTuple2G<String,Long> ot = _stats.get(i);
            if (i==0) {
                iw.println(ot._o1 + ", took: 0ms");
            } else {
                OTuple2G<String,Long> otbefore = _stats.get(i-1);
                long diff = ot._o2 - otbefore._o2;
                iw.println(ot._o1 + ", took: " + diff + "ms");
            }
        }
        return iw.getString();
    }



}

