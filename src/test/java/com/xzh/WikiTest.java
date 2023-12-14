package com.xzh;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WikiTest {

    public static  void main(String args[]) throws IOException {

        // create a new instance of scraper
        WikiTest scraper = new WikiTest();

        String urlToScrape = "https://epic7.gamekee.com/";
        // get the HTML document from the target url
        Document document = scraper.getDocumentFromURL(new URL(urlToScrape));
        // 获取所有的链接
        Elements links = document.select("a[href]");
        // 打印每个链接的文本和URL
        for (Element link : links) {
            if(!link.attr("class").equals("item")){
                continue;
            }
            String linkText = link.text();
            String linkUrl = link.attr("href");
            System.out.println("链接文本：" + linkText);
            System.out.println("链接URL：" + linkUrl);
        }
        System.out.println("链接数量：" + links.size());

//        String classNameForProductCard = "item-wrapper icon-size-2 pc-item-group epic7-item-group";
//        List<Element> productCards = scraper.getElementsByIdentifier(htmlDocument,classNameForProductCard, IdentifierType.CLASS);
//        for (Element element : productCards) {
//            Elements item = element.getElementsByClass("item");
//            System.out.println(item.attr("title"));
//        }
//        System.out.println(productCards.size());
//        System.out.println(productCards);
    }


    // Enum for our wrapper function. you can omit this and pass a //string or just create separate method
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
