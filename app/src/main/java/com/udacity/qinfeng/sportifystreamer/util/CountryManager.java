package com.udacity.qinfeng.sportifystreamer.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by fengqin on 15/7/3.
 */
public class CountryManager {

    public static final String COUNTRY_PARAM="country";
    public static final String DEFAULT_COUNTRY="US";

    public static final List AVAILABLE_MARKETS = Arrays.asList("AD", "AR", "AT", "AU", "BE",
            "BG", "BO", "BR", "CA", "CH", "CL", "CO", "CR", "CY", "CZ", "DE", "DK", "DO", "EC", "EE", "ES", "FI",
            "FR", "GB", "GR", "GT", "HK", "HN", "HU", "IE", "IS", "IT", "LI", "LT", "LU", "LV", "MC", "MT", "MX",
            "MY", "NI", "NL", "NO", "NZ", "PA", "PE", "PH", "PL", "PT", "PY", "RO", "SE", "SG", "SI", "SK", "SV",
            "TR", "TW", "US", "UY");

    public static String getCountryCode(Context context){
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso();
        if(countryCode == null || "".equals(countryCode)){ // fixe for tablette
            countryCode = Locale.getDefault().getCountry();
        }
        if(AVAILABLE_MARKETS.contains(countryCode)){
           return countryCode;
        }
        return DEFAULT_COUNTRY;
    }
}
