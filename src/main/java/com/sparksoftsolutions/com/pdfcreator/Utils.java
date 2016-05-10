package com.sparksoftsolutions.com.pdfcreator;

/**
 * Created by Юрий on 30.04.2016.
 */
public class Utils {
    public String PREFERENCES_NAME = "mypref";
    public String PREFERENCES_CARDLIST = "cards1";
    public String PREFERENCES_IMAGEFOLDER = "imagefolder";
    public String PREFERENCES_PDFFILE = "pdffile";
    private static Utils utils = null;
    public static Utils getInstance(){
        if(utils==null){
            utils = new Utils();
        }
        return utils;
    }
}
