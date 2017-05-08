package app;


import model.config.Localization;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;


public abstract class XML {


    public static void save(Object object, File path) throws Exception {
        XMLEncoder encoder = new XMLEncoder( new BufferedOutputStream( new FileOutputStream(path)) );
        encoder.writeObject(object);
        encoder.close();
    }


    public static Object read(String path) throws Exception {
        XMLDecoder decoder = new XMLDecoder(Main.class.getResourceAsStream((path)));
        decoder.setExceptionListener(e -> {
            System.out.println("got exception. e=" + e);
            e.printStackTrace();
        });
        Localization localization =(Localization) decoder.readObject();
        decoder.close();
        return localization;
    }


    public static Object read(File path) throws Exception {
        XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( new FileInputStream(path) ) );
        Object output = decoder.readObject();
        decoder.close();
        return output;
    }
}
