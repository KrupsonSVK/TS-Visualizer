package app;


import model.Stream;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;


public class XML {

    public static void save(Stream stream, File file) throws Exception {
        XMLEncoder encoder = new XMLEncoder( new BufferedOutputStream( new FileOutputStream(file) ) );
        encoder.writeObject(stream);
        encoder.close();
    }

    public static Stream read(File file) throws Exception {
        XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( new FileInputStream(file) ) );
        Stream output = (Stream)decoder.readObject();
        decoder.close();
        return output;
    }
}
