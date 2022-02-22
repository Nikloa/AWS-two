package ru.vironit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;


import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.parser.jpeg.JpegParser;
import org.xml.sax.SAXException;

public class GetMetadata {

    public static void main(final String[] args) throws IOException, TikaException, SAXException {

        //Assume that boy.jpg is in your current directory
        File file = new File("C:/Users/User/Desktop/boy.jpg");

        //Parser method parameters
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(file);
        ParseContext pcontext = new ParseContext();

        //Jpeg Parse
        Parser jpegParser = new AutoDetectParser();
        jpegParser.parse(inputstream, handler, metadata,pcontext);
        System.out.println("Contents of the document:" + handler.toString());
        System.out.println(metadata.get(Metadata.ORIGINAL_DATE));
        System.out.println("Metadata of the document:");
        String[] metadataNames = metadata.names();

        for(String name : metadataNames) {
            System.out.println(name + ": " + metadata.get(name));
        }
    }
}
