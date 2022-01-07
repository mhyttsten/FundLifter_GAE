package com.pf.fl.shared.datamodel;

import com.pf.fl.shared.utils.IndentWriter;

import java.util.ArrayList;
import java.util.List;

public class D_FundDPDay implements Cloneable {

	public static final float FLOAT_NULL = Float.MAX_VALUE;

	public String _dateYYMMDD = "";
	public String _dateYYMMDD_Actual = "";
	public String _currency = "";
	public float _r1d = FLOAT_NULL;
	public float _r1w = FLOAT_NULL;
	public float _r1m = FLOAT_NULL;
	public float _r3m = FLOAT_NULL;
	public float _r6m = FLOAT_NULL;
	public float _r1y = FLOAT_NULL;
	public float _r3y = FLOAT_NULL;
	public float _r5y = FLOAT_NULL;
	public float _r10y = FLOAT_NULL;

	public float _rYTDFund     = FLOAT_NULL;
	public float _rYTDCategory = FLOAT_NULL;
	public float _rYTDIndex    = FLOAT_NULL;

	public static String f2s(float f) {
		if (f == FLOAT_NULL) return "-";
		return String.format("%.2f", f);
	}
	
	public void dumpInfo(IndentWriter iw) {
		iw.print(_dateYYMMDD + ", act: " + _dateYYMMDD_Actual + ", " +_currency + ": ");
		iw.print("[" + f2s(_r1d));
		iw.print(", " + f2s(_r1w));
		iw.print(", " + f2s(_r1m));
		iw.print(", " + f2s(_r3m));
		iw.print(", " + f2s(_r6m));
		iw.print(", " + f2s(_r1y));
		iw.print(", " + f2s(_r3y));
		iw.print(", " + f2s(_r5y));
		iw.print(", " + f2s(_r10y));
		iw.print(", " + f2s(_rYTDFund));
		iw.print(", " + f2s(_rYTDCategory));
		iw.print(", " + f2s(_rYTDIndex));
		iw.print("]");
	}

	public String toString_Date_R1W_R1M() {
		String r1wStr = "null";
		if (_r1w != FLOAT_NULL) {
			r1wStr = String.format("%.2f", _r1w);
		}
		String r1mStr = "null";
		if (_r1m != FLOAT_NULL) {
			r1mStr = String.format("%.2f", _r1m);
		}
		return _dateYYMMDD + ", r1w: " + r1wStr + ", r1m: " + r1mStr;
	}

	public String toString() {
		IndentWriter iw = new IndentWriter();
		dumpInfo(iw);
		iw.println();
		return iw.toString();
	}

	//------------------------------------------------------------------------
	public D_FundDPDay cloneMe() {
		try {
			return (D_FundDPDay)clone();
		} catch(CloneNotSupportedException exc) {
			throw new AssertionError("Assumed this was a clonable object");
		}
	}
}
