package io.liebrand.multistreamapp;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

public class Substitute {

    private static final String KEY_SUBSTITUTE_ID = "subId";
    private static final String KEY_ORG = "orgUrl";
    private static final String KEY_REPLACE = "replaceUrl";
    private static final String KEY_M3_TYPE = "TYPE";
    private static final String TYPE_M3_SUBS = "SUBSTITUTE";

    public Substitute() {

    }

    public String storeAsM3U() {
        final String SEP1 = "=\"";
        final String SEP2 = "\"";
        final String NL = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append("#EXT-X-Media:");
        sb.append(KEY_M3_TYPE).append(SEP1).append(TYPE_M3_SUBS).append(",");
        sb.append(NL);
        return sb.toString();
    }
}
