package com;

import com.pojo.Item;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RssParser {

    private static DateTimeFormatter logFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static DateTimeFormatter jsonFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm Z");


    private static Map<String, String> feeds =  Collections.synchronizedMap(new HashMap<>());

    static {
        feeds.put("rbc", "https://rssexport.rbc.ru/rbcnews/news/30/full.rss");
        feeds.put("komm", "https://www.kommersant.ru/RSS/news.xml");
        feeds.put("vedom", "https://www.vedomosti.ru/rss/news");
        feeds.put("rg", "https://rg.ru/tema/rss.xml");
        feeds.put("lenta", "https://lenta.ru/rss/news");
        feeds.put("rt", "https://russian.rt.com/rss");
        feeds.put("life", "https://life.ru/rss");
        feeds.put("habr", "https://habr.com/ru/rss/hubs/all/");
        feeds.put("maxim", "https://www.maximonline.ru/main.rss");
        feeds.put("rb", "https://rb.ru/feeds/all/");
        feeds.put("ferra", "https://www.ferra.ru/exports/rss.xml");
        feeds.put("cont", "http://www.content-review.com/news.rdf");
        feeds.put("cnews", "https://www.cnews.ru/inc/rss/news.xml");
        feeds.put("3dnews", "https://3dnews.ru/news/rss/");
        feeds.put("ridus", "https://www.ridus.ru/rss/news");
        feeds.put("vc", "https://vc.ru/rss");
        feeds.put("ria", "https://ria.ru/export/rss2/archive/index.xml");
        feeds.put("inter", "https://www.interfax.ru/rss.asp");
        feeds.put("vesti", "https://www.vesti.ru/vesti.rss");
        feeds.put("aif", "https://aif.ru/rss/news.php");
        feeds.put("regnum", "https://regnum.ru/rss/news.html");
        feeds.put("rtrav", "https://rtraveler.ru/out/public-all-articles.xml");
        feeds.put("postn", "https://postnauka.ru/feed");
        feeds.put("nkj", "https://www.nkj.ru/rss/");
        feeds.put("nksc", "https://naked-science.ru/?yandex_feed=news");

        feeds.put("krsk_nlab", "https://newslab.ru/news/all/rss");
        feeds.put("krsk_sibnov", "https://krsk.sibnovosti.ru/news/rss_for_users.xml");
        feeds.put("krsk_mail", "https://news.mail.ru/rss/main/24/");

        //feeds.put("versia", "https://versia.ru/rss/index.xml");
        //feeds.put("tass", "http://tass.ru/rss/v2.xml");
        //feeds.put("novgaz", "https://novayagazeta.ru/feed/rss");
        //feeds.put("news", "https://news.ru/rss");
        //feeds.put("izv", "https://iz.ru/xml/rss/all.xml");
    }

    private static List<Item> items = Collections.synchronizedList(new ArrayList());
    private static String itemsJSON;

    public static void start() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        parse();
                        //every 30 min
                        Thread.sleep(1800000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private static void parse() throws IOException, ParserConfigurationException, SAXException {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setCoalescing(true);
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        for (Map.Entry<String, String> feed : feeds.entrySet()) {

            try {
                System.out.println(logFormat.format(ZonedDateTime.now()) + " _ " + feed.getKey() + " start parsing");
                URL url = new URL(feed.getValue());
                URLConnection uc = url.openConnection();
                uc.addRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
                uc.setReadTimeout(10000);
                uc.setConnectTimeout(10000);
                InputStream source = uc.getInputStream();

                Document rss = dBuilder.parse(source);
                rss.getDocumentElement().normalize();
                NodeList itemsNodes = rss.getElementsByTagName("item");
                int itemsCount = 0;
                for (int temp = 0; temp < itemsNodes.getLength(); temp++) {
                    Node node = itemsNodes.item(temp);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String link = element.getElementsByTagName("link").item(0).getTextContent();
                        boolean added = false;
                        for (Item item : items) {
                            if (link != null && link.equals(item.link)) {
                                added = true;
                                break;
                            }
                        }
                        if (!added) {
                            Item item = new Item();
                            item.title = element.getElementsByTagName("title").item(0).getTextContent();
                            String pubDate = element.getElementsByTagName("pubDate").item(0).getTextContent();
                            ZonedDateTime pubDateTime = ZonedDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(pubDate));
                            if (pubDateTime.getHour() == 0 && pubDateTime.getMinute() == 0) {
                                System.out.println("PASS");
                                continue;
                            }
                            item.pubDate = pubDateTime;
                            item.link = link;
                            item.feed = feed.getKey();
                            items.add(item);
                            itemsCount++;
                        }
                    }
                }

                System.out.println(logFormat.format(ZonedDateTime.now()) + " _ " + feed.getKey() + " parsed " + itemsCount + " items");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        List<Item> itemsToSend = new ArrayList<>();
        ZonedDateTime now = ZonedDateTime.now();
//
//        for (Item item : items) {
//
//            if ( (item.title.toLowerCase().contains("красноярск") || item.feed.startsWith("krsk_"))
//                && item.pubDate.isAfter(now.minusMinutes(30))) {
//
//                itemsToSend.add(item);
//            }
//        }
//
//        Telegram.sendMessages(itemsToSend);

        List<Item> itemsToRemove = new ArrayList<>();
        for (Item item : items) {
            if (item.pubDate.isBefore(now.minusHours(24))) {
                itemsToRemove.add(item);
            }
        }
        System.out.println("Total " + items.size() + " items _ Expired and removed " + itemsToRemove.size() + " items");
        items.removeAll(itemsToRemove);
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o2.pubDate.compareTo(o1.pubDate);
            }
        });
        JSONObject itemsJSONObj = new JSONObject();
        List<JSONObject> itemsList = new ArrayList<>();
        for (Item item : items) {
            JSONObject itemJSONObj = new JSONObject();
            itemJSONObj.put("title", item.title);
            itemJSONObj.put("link", item.link);
            itemJSONObj.put("feed", item.feed);
            itemJSONObj.put("pubDate", jsonFormat.format(item.pubDate));
            itemsList.add(itemJSONObj);
        }
        itemsJSONObj.put("items", itemsList);
        itemsJSON = itemsJSONObj.toString(4);
    }

    private void sendKrskNow(List<Item> items) {

    }

    public static String getItemsJSON() {
        return itemsJSON;
    }

}
