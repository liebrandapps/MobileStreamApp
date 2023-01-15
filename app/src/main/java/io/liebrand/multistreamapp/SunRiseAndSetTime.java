package io.liebrand.multistreamapp;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class SunRiseAndSetTime {

    double _lon, _lat;

    public SunRiseAndSetTime(double latitude, double longitude) {
        _lon = longitude;
        _lat = latitude;
    }

    public Calendar CalcSunTime(Date when, boolean calcRise)  {
        double zenith = 90 + (50.0/60.0);
        double t;
        double lngHour = _lon / 15;
        double toRad = Math.PI / 180.0;

        Calendar cal = new GregorianCalendar();
        cal.setTime(when);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);


        if(calcRise) {
            t = cal.get(Calendar.DAY_OF_YEAR) + ((6 - lngHour) / 24);
        }
        else {
            t = cal.get(Calendar.DAY_OF_YEAR) + ((18 - lngHour) / 24);
        }
        double m = (0.9856 * t) - 3.289;

        double l = m + (1.916 * Math.sin(toRad*m)) + (0.02 * Math.sin(toRad*2*m)) + 282.634;
        if (l<0) l=l+360.0;
        else if (l>=360) l=l-360;

        double ra = (1/toRad) * Math.atan(0.91764 * Math.tan(toRad*l));
        if (ra<0) ra=ra+360.0;
        else if (ra>=360) ra=ra-360;

        double lquad = (Math.floor(l/90)) * 90;
        double raquad = (Math.floor(ra/90)) * 90;
        ra = (ra+(lquad -raquad)) / 15;

        double sinDec = 0.39782 * Math.sin(toRad*l);
        double cosDec = Math.cos(Math.asin(sinDec));
        double cosH = (Math.cos(toRad*zenith) - (sinDec * Math.sin(toRad*_lat))) / (cosDec * Math.cos(toRad*_lat));

        if (cosH>1 || cosH <-1) return null; // sun never rises or sets on the date

        double h = (1/toRad) * Math.acos(cosH);
        if(calcRise) {
            h = 360 - h;
        }
        h = h / 15;

        t = h + ra - (0.06571 * t) - 6.622;


        double ut = t - lngHour;
        if (ut<0) ut=ut+24.0;
        else if (ut>=24.0) ut=ut-24.0;

        int hr = (int)ut;
        if (hr<0) hr=hr+24;
        else if (hr>=24) hr=hr-24;
        int min = (int) Math.round((ut - (int)ut)*60);
        if (min==60) {
            hr+=1;
            min = 0;
        }
        if(hr==24) {
            hr=0;
            day+=1;

            if (day > cal.getActualMaximum(Calendar.DATE)) {
                day = 1;
                month +=1;
                if(month>12){
                    month = 1;
                    year +=1;
                }
            }
        }
        cal =  new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hr);
        cal.set(Calendar.MINUTE, min);
        return cal;
    }



}
