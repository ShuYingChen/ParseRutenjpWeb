package com.pchome.data;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * 欲新增的樂天商品物件
 *
 * @author JoanChen
 * @date 2017/7/20
 */
public class RakutenProdInfo {
    @JsonProperty("Id")
    String Id = "";

    @JsonProperty("Url")
    String Url = "";

    @JsonProperty("SellerId")
    String SellerId = "";

    @JsonProperty("ItemCode")
    String ItemCode = "";

    @JsonProperty("Content")
    String Content = "";

    @JsonProperty("AwsDtm")
    String AwsDtm = "";

    public RakutenProdInfo() {

    }

    public void setId(String id) {
        Id = id;
    }

    public void setUrl(String url) {
        this.Url = url;
    }

    public void setSellerId(String sellerId) {
        SellerId = sellerId;
    }

    public void setItemCode(String itemCode) {
        ItemCode = itemCode;
    }

    public void setContent(String content) {
        Content = content;
    }

    public void setAwsDtm(String awsDtm) {
        AwsDtm = awsDtm;
    }

    public String getId() {
        return Id;
    }

    public String getUrl() {
        return Url;
    }

    public String getSellerId() {
        return SellerId;
    }

    public String getItemCode() {
        return ItemCode;
    }

    public String getContent() {
        return Content;
    }

    public String getAwsDtm() {
        return AwsDtm;
    }

    public static void main(String[] args) throws IOException {
        String json = "[{\"Url\":\"http://item.rakuten.co.jp/unionspo/vpbh14589\",\"SellerId\":\"unionspo\",\"ItemCode\":\"unionspo:10031301\",\"Content\":\"conten1\",\"AwsDtm\":\"2017/07/20 10:26:24\"},{\"Url\":\"http://item.rakuten.co.jp/naturum/2823398\",\"SellerId\":\"naturum\",\"ItemCode\":\"naturum:16718890\",\"Content\":\"conten2\",\"AwsDtm\":\"2017/07/20 10:26:24\"}]";
        ObjectMapper mapper = new ObjectMapper();
        RakutenProdInfo[] rakutenProdInfoList = mapper.readValue(json, TypeFactory.defaultInstance().constructArrayType(RakutenProdInfo.class));
        for (RakutenProdInfo rakutenProdInfo : rakutenProdInfoList) {
            System.out.println(rakutenProdInfo.getSellerId());
        }
    }
}
