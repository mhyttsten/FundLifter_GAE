package com.pf.fl.shared.utils;

public class OTuple3G<A1, A2, A3> {

    public OTuple3G() { }

	public OTuple3G(A1 a1, A2 a2, A3 a3) {
		_o1 = a1;
		_o2 = a2;
        _o3 = a3;
	}
	
	public A1 _o1;
	public A2 _o2;
    public A3 _o3;
}
