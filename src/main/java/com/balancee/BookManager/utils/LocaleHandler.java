package com.balancee.BookManager.utils;


import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class LocaleHandler {

    public static String getMessage(String messageKey, Locale locale){
        if(Objects.isNull(locale))
            locale = Locale.ENGLISH;
        return ResourceBundle.getBundle("appmessages", locale).getString(messageKey);
    }

    public static String getMessage(String messageKey){
        return getMessage(messageKey, null);
    }
}

