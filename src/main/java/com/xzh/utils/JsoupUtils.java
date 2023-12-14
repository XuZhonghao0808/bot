package com.xzh.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JsoupUtils {

    public enum IdentifierType {
        ATTRIBUTE,
        ID,
        CLASS,
        TAG
    }

    public Document getDocumentFromURL(URL resourceUrl) throws IOException {
        return  Jsoup.connect(resourceUrl.toString()).get();
    }

    public List<Element> getElementsByIdentifier(Document document , String identifier, IdentifierType identifiertype){
        List<Element> elements = new ArrayList<>();

        switch (identifiertype){
            case ID:
                elements.add(document.getElementById(identifier));
                return elements;
            case TAG:
                return document.getElementsByTag(identifier);
            case ATTRIBUTE:
                return document.getElementsByAttribute(identifier);
            case CLASS:
                return document.getElementsByClass(identifier);
            default:
                System.out.println("Not a valid Identifier type");
        }

        return elements;
    }
}
