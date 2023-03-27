package org.example;

import java.io.FileInputStream;
import java.sql.SQLOutput;
import java.util.Properties;

public class Proper extends Properties{
    protected static String s_IniFile = "";
    protected static Properties prop = null;

    public Proper (String filename)
    {
        this.s_IniFile = filename;
    }
    public boolean init()
    {
        try
        {
            this.prop = new Properties();
            this.prop.load(new FileInputStream((this.s_IniFile)));
            return true;
        }
        catch(Exception e)
        {
            System.err.println("Wyjatek przy probie otwarcia pliku ini : " + this.s_IniFile);
            System.err.println(e.getMessage());
            return false;
        }
    }
    public static String getProper(String PropertyName)
    {
        String temp;
        temp = prop.getProperty(PropertyName);
        System.out.println("Dane " + PropertyName + ": " + temp);
        if(temp == null)
        {
            System.err.println("Nie udało się odczytać z pliku ini wartości parametru: " + PropertyName);
        }
        return temp;
    }

    public static boolean loadProperties(){
        try
        {
            GLOBALS.MATRIX_DATE = getProper("MATRIX_DATE");
            GLOBALS.MATRIX_OPERATOR = getProper("MATRIX_OPERATOR");
            GLOBALS.MATRIX_WYRÓŻNIK_FIRMY= getProper("MATRIX_WYROZNIK_FIRMY");
            GLOBALS.MATRIX_OPERATOR = getProper("MATRIX_OPERATOR");
            GLOBALS.MATRIX_UNIQUE_CLIENT_ID = getProper("MATRIX_UNIQUE_CLIENT_ID");
            GLOBALS.FONT_SIZE_DEFAULT = Integer.parseInt(getProper("FONT_SIZE_DEFAULT"));

            return true;
        }
        catch (Exception e)
        {
            System.err.println("Wyjatek podczas wczytywania parametrow z pliku ini");
            System.err.println(e.getMessage());
            return false;
        }

    }
}
