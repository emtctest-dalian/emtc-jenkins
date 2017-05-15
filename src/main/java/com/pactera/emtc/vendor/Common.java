package com.pactera.emtc.vendor;

import java.text.SimpleDateFormat;
import java.util.*;

public class Common {

    public static String getToday() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date);
    }
}

