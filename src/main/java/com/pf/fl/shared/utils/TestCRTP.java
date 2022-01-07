package com.pf.fl.shared.utils;

interface MHComparable<T> {
    public int mhCompareTo(MHComparable<T> other);
}

class MHC_Integer implements MHComparable<MHC_Integer> {
    int _v;
    public MHC_Integer(int a)  { _v = a; }

    @Override
    public int mhCompareTo(MHComparable<MHC_Integer> other) {
        return -1;
    }
}

//class MHC_String implements MHComparable<MHC_String> {
//    String _v;
//    public MHC_String(String a)  { _v = a; }
//
//    @Override
//    public int mhCompareTo(MHComparable<MHC_String> other) {
//        return 1;
//    }
//}

class TestCRTP {
    public static void main(String[] args) {
        MHComparable<MHC_Integer> i1 = new MHC_Integer(10);
        MHComparable<MHC_Integer> i2 = new MHC_Integer(20);
        System.out.println("i1 < i2: " + i1.mhCompareTo(i2));



//        MHComparable<MHC_Integer> a = min("cat", "dog");
//        MHComparable<MHC_String> b = min(new Integer(10), new Integer(3));
//        String str = Fmin("cat", "dog");
//        Integer i = Fmin(new Integer(10), new Integer(3));
    }
//    public static <S extends Comparable> S min(S a, S b) {
//        if (a.compareTo(b) <= 0)
//            return a;
//        else
//            return b;
//    }
//    public static <T extends Comparable<T>> T Fmin(T a, T b) {
//        if (a.compareTo(b) <= 0)
//            return a;
//        else
//            return b;
//    }
}


