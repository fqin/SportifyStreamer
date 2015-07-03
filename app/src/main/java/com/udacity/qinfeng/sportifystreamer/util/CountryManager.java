package com.udacity.qinfeng.sportifystreamer.util;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by fengqin on 15/7/3.
 */
public class CountryManager {

    public static final String COUNTRY_PARAM="country";

    public static String getCountryCode(Context context){
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimCountryIso();
    }
}
