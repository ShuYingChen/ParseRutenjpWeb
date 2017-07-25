package com.pchome.parser;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pchome.data.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.misc.BASE64Decoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by freedomandy on 7/19/17.
 */
public class Parser {
    private static int getPrice(String text) {
        try {
            return text.contains("(+") ? Integer.parseInt(text.substring(text.indexOf("(") + 2, text.length() - 4)) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static List<Attribute> getAdditionalAttributes(List<Attribute> attributeList, Elements tableBlock, int lastIndex) {
        for (Element attri : tableBlock.select("select")) {
            List<String> temp = new ArrayList<String>();
            List<Option> optionItems = new ArrayList();

            for (Element optionEle : attri.select("option")) {
                String text = optionEle.text();
                int price = 0;
                if (text.contains("(+")) {
                    //System.out.print("!!!" + text.substring(text.indexOf("(")+2, text.length()-4));
                    price = Integer.parseInt(text.substring(text.indexOf("(") + 2, text.length() - 4));
                }
                temp.add(text);
                Option optionDetail = new Option();
                optionDetail.setId(text);
                optionDetail.setValue(text);
                if (price != 0)
                    optionDetail.setOriginPrice(price);
                optionItems.add(optionDetail);
            }

            attributeList.add(new Attribute("", "", 0, optionItems));
        }
        List<String> extraAttriItem = new ArrayList<String>();
        Elements options = tableBlock.select("span");
        for (Element option : options) {
            extraAttriItem.add(option.text());
        }

        for (int i = 0; i < extraAttriItem.size(); i++) {
            String title = extraAttriItem.get(i);
            int etraTableIndex = i + lastIndex;
            attributeList.get(etraTableIndex).setName(title);
            attributeList.get(etraTableIndex).setId(title);
            attributeList.get(etraTableIndex).setSort(etraTableIndex + 1);

            for (Option option : attributeList.get(etraTableIndex).getOption()) {
                option.setId(title + ":" + option.getId());
            }
        }

        return attributeList;
    }

    public static List<Attribute> getCartJson(List<Attribute> attributeList, Document doc, List<ExtraBlock> extra, List<List<String>> groupList) {
        Elements tableBlockList = doc.select("div#rakutenLimitedId_aroundCart").
                select("td.floating-cart-sku-table");
        Element attributeTag = doc.select("div#rakutenLimitedId_aroundCart").select("table").select("tbody").select("tr").get(1);
        Elements attributeNodes = attributeTag.select("tbody").first().select("tr").first().children();


        for (Element cartAttri : tableBlockList.select("td.floating-cart-sku-table")) {
            List<String> sizeList = new ArrayList<String>();
            Elements tableBlock = cartAttri.select("table").select("tbody").select("tr");

            System.out.println(tableBlockList.select("span.inventory_title").text());
            String rowAttribute = "";
            String colAttribute = "";
            if (attributeNodes.size() > 0)
                rowAttribute = attributeNodes.get(0).select("span").text();
            if (attributeNodes.size() > 2)
                colAttribute = attributeNodes.get(2).select("span").text();

            String rowAttributeName = rowAttribute;
            String colAttributeName = colAttribute;

            System.out.println(rowAttributeName + " flag " + colAttributeName);

            //System.out.println("go");
            List<Option> colOptionItems = new ArrayList();
            for (int i = 0; i < tableBlock.size(); i++) {
                if (i == 0) { // get size attribute
                    List<Option> optionItems = new ArrayList();
                    for (Element element : tableBlock.get(i).select("span.inventory_choice_name")) {
                        String optionTag = element.text();
                        sizeList.add(optionTag);
                        int price = getPrice(optionTag);
                        optionItems.add(new Option(rowAttributeName + ":" + optionTag, optionTag, price));
                    }
                    if (!rowAttribute.equals(""))
                        attributeList.add(new Attribute(rowAttribute, rowAttributeName, 1, optionItems));
                } else { // get color attribute
                    String color = "";
                    Elements optionTags = tableBlock.get(i).select("td");
                    for (int j = 0; j < optionTags.size(); j++) {
                        if (j == 0) {
                            color = optionTags.get(j).text();
                            int price = getPrice(color);
                            colOptionItems.add(new Option(rowAttributeName + ":" + color, color, price));

                        } else {
                            String size = sizeList.get(j - 1);
                            String value = optionTags.get(j).text();

                            int isAvailable = value.equals("×") ? 0 : 1;
                            List<String> keyList = new ArrayList<String>();
                            keyList.add(rowAttribute + ":" + size);
                            keyList.add(colAttribute + ":" + color);
                            //if (!rowAttribute.equals(colAttribute))
                            extra.add(new ExtraBlock(keyList, isAvailable));
                        }
                    }
                }
            }
            if (!colAttribute.equals(""))
                attributeList.add(new Attribute(colAttribute, colAttributeName, 2, colOptionItems));
            // TODO: Notice the order
            List<String> group = new ArrayList<String>();
            Elements titleList = tableBlockList.select("span.inventory_title");

            group.add(rowAttribute);
            group.add(colAttribute);

            System.out.println("group: " + group);
            groupList.add(group);
        }

        return attributeList;
    }


    public static PageInfo getGlobalInfo(Elements doc) {
        PageInfo result = new PageInfo();

        Elements keywords = doc.first().getElementsByAttributeValue("name", "keywords");
        System.out.println(keywords.attr("content"));
        result.setKeywords(keywords.attr("content"));

        System.out.println("itemNumber" + doc.first().select("span.item_number").text());
        String itemNumber = doc.first().select("span.item_number").text();
        String regx = "[\\w\\d]+";
        if (itemNumber.matches(regx)) {
            result.setItemNumber(itemNumber);
            System.out.println("itemNumber: " + itemNumber);
        }

        String ShipFee = doc.first().select("span.tax_postage shippingCost_free").text();

        if (ShipFee.equals("送料無料")) {
            result.setHasShipFee(0);
        } else if (ShipFee.equals("無料別")) {
            result.setHasShipFee(1);
        }

        // Pictures
        Elements pictures = doc.first().select("div[id^=rakutenLimitedId_ImageList]");
        System.out.println("images: " + pictures.select("div").size());
        List<String> pictureUrls = new ArrayList<String>();
        for (Element pic : pictures.select("div").subList(1, pictures.select("div").size())) {
            pictureUrls.add(pic.attr("src"));
        }
        result.setPictures(pictureUrls);
        //System.out.print("images: " + pictureUrls.toString());

        for (Element meta : doc.select("meta")) {
            //System.out.println(meta.attr("property"));
            String property = meta.attr("property");
            if (property.equals("og:title")) {
                result.setName(meta.attr("content"));
            } else if (property.equals("og:description")) {
                result.setDesc(meta.attr("content"));
                //System.out.println(meta.attr("content"));
            } else if (property.equals("og:url")) {
                result.setUrl(meta.attr("content"));
                //System.out.println(meta.attr("content"));
            } else if (property.equals("apprakuten:item_id")) {
                result.setId(meta.attr("content"));
                //System.out.println(meta.attr("content"));
            } else if (property.equals("apprakuten:item_code")) {
                result.setCode(meta.attr("content"));
                //System.out.println(meta.attr("content"));
            } else if (property.equals("apprakuten:shop_id")) {
                result.setShopId(meta.attr("content"));
                //System.out.println(meta.attr("content"));
            } else if (property.equals("apprakuten:price")) {
                int price = Integer.parseInt(meta.attr("content"));
                result.setPrice(price);
                //System.out.println(meta.attr("content"));
            } else if (property.equals("apprakuten:shop_name")) {
                result.setShopName(meta.attr("content"));
                //System.out.println(meta.attr("content"));
            } else if (property.equals("apprakuten:shop_code")) {
                result.setShopCode(meta.attr("content"));
                //System.out.println(meta.attr("content"));
            } else if (property.equals("apprakuten:shop_url")) {
                result.setShopUrl(meta.attr("content"));
                //System.out.println(meta.attr("content"));
            }
        }

        return result;
    }

    public static void main(String[] argv) {
        String fileName = "./data/base64.txt";
        String path = "." + File.separator + fileName;
        File file = new File(path);
        String charset = "EUC-JP";    // EUC-JP ,UTF-8

        try {
            String content = "", str = new String();
            BufferedReader bfr = new BufferedReader(new FileReader(path));
            while ((str = bfr.readLine()) != null) {
                content += str;
            }
            bfr.close();

            //byte[] base64Content = Base64.getEncoder().encode(content.getBytes());
            //java.nio.file.Files.write(Paths.get(new File("./08_base64.txt").toURI()), base64Content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);


            //String temp = new String(Base64.getDecoder().decode(content),Charset.forName("EUC-JP"));
            //System.out.print("content6" + temp);

            String webContent = new String(new BASE64Decoder().decodeBuffer(content), Charset.forName(charset));
            //String webContent = new String(content.getBytes(), Charset.forName("UTF-8"));


            //System.out.println("content:" + webContent);
            Document doc = Jsoup.parse(webContent);
            ObjectMapper objectMapper = new ObjectMapper();
            List<Attribute> attributeList = new ArrayList<Attribute>();
            List<ExtraBlock> extra = new ArrayList<ExtraBlock>();

            // Cart Block
            Elements tableBlockList = doc.select("div#rakutenLimitedId_aroundCart").
                    select("td.floating-cart-sku-table");
            System.out.println("size: " + tableBlockList.size());


            List<List<String>> groupList = new ArrayList<List<String>>();
            if (tableBlockList.size() > 0)
                attributeList = getCartJson(attributeList, doc, extra, groupList);

            System.out.println("cart done!");
            // Additional Attribute block
            attributeList = getAdditionalAttributes(attributeList, doc.select("td.floating-cart-options-table"), attributeList.size());
            System.out.println("additional done!");
            PageInfo pageInfo = getGlobalInfo(doc.select("html"));
            System.out.println("global done!");
            pageInfo.setAttribute(attributeList);
            pageInfo.setGroup(groupList);
            pageInfo.setExtraBlock(extra);

            objectMapper.writeValueAsString(attributeList);

            ParseInfo result = new ParseInfo(pageInfo.getCode(), pageInfo);
            String jsonStr = objectMapper.writeValueAsString(result);

            if (jsonStr.contains("xKeyxxx"))
                jsonStr = objectMapper.writeValueAsString(result).
                        replace("xKeyxxx", groupList.get(0).get(0)).replace("yKeyyyy", groupList.get(0).get(1));

            System.out.println("test: " + jsonStr);

            /*WebContentParser contentParser = new WebContentParser();
            File file2 = new File("./" + fileName.substring(0, fileName.lastIndexOf(".")) +"_result.txt");
            jsonStr = contentParser.parse("2017/07/20 15:00:00", content, Charset.forName(charset)).getResult();
            java.nio.file.Files.write(Paths.get(file2.toURI()), jsonStr.getBytes(charset), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
*/
        } catch (IOException e) {
            // TODO:
            System.out.println("Error: " + e.getMessage());
        } catch (Throwable t) {
            System.out.println("Error: " + t.getMessage());
        }
    }
}