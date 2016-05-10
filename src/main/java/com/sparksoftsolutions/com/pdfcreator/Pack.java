package com.sparksoftsolutions.com.pdfcreator;
import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;

class Pack{
    private ArrayList<String> files ;
    void setPathList( ArrayList<String> files){
        this.files = files;
    }
    ArrayList<String> getPathList(){
        return files;
    }
}