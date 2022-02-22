package ru.vironit.other;

import com.ibm.icu.text.Transliterator;

public final class ToEnglishFileName {

    public static String toEnglish(String fileName){
        String newFileName = fileName.replace(' ', '_');
        newFileName.replace('ь', '-');
        newFileName.replace('ъ','+');
        var CYRILLIC_TO_LATIN = "Russian-Latin/BGN";
        Transliterator toLatinTrans = Transliterator.getInstance(CYRILLIC_TO_LATIN);
        return toLatinTrans.transform(newFileName);
    }
}
