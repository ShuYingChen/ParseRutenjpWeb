package com.pchome.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by freedomandy on 7/20/17.
 */
public class PageInfo {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Code")
    private String code;
    @JsonProperty("ItemNumber")
    private String itemNumber;
    @JsonProperty("URL")
    private String url;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("ShopId")
    private String shopId;
    @JsonProperty("ShopCode")
    private String shopCode;
    @JsonProperty("ShopName")
    private String shopName;
    @JsonProperty("ShopUrl")
    private String shopUrl;
    @JsonProperty("Keywords")
    private String keywords;
    @JsonProperty("Desc")
    private String desc;
    @JsonProperty("Price")
    private int price;
    private int hasShipFee;
    @JsonProperty("Pictures")
    private List<String> pictures;
    @JsonProperty("Attribute")
    private List<Attribute> attribute;
    @JsonProperty("Group")
    private List<List<String>> group;
    @JsonProperty("Extra")
    private List<ExtraBlock> extraBlock;
    @JsonProperty("AwsDtm")
    private String awsDtm;

    public PageInfo() {}

    public PageInfo(String id, String code, String itemNumber, String url, String name, String shopId, String shopCode, String shopName, String shopUrl, String keywords, String desc, int price, int hasShipFee, List<String> pictures, List<Attribute> attribute, List<List<String>> group, List<ExtraBlock> extraBlock, String awsDtm) {
        this.id = id;
        this.code = code;
        this.itemNumber = itemNumber;
        this.url = url;
        this.name = name;
        this.shopId = shopId;
        this.shopCode = shopCode;
        this.shopName = shopName;
        this.shopUrl = shopUrl;
        this.keywords = keywords;
        this.desc = desc;
        this.price = price;
        this.hasShipFee = hasShipFee;
        this.pictures = pictures;
        this.attribute = attribute;
        this.group = group;
        this.extraBlock = extraBlock;
        this.awsDtm = awsDtm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getShopCode() {
        return shopCode;
    }

    public void setShopCode(String shopCode) {
        this.shopCode = shopCode;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getHasShipFee() {
        return hasShipFee;
    }

    public void setHasShipFee(int hasShipFee) {
        this.hasShipFee = hasShipFee;
    }

    public List<String> getPictures() {
        return pictures;
    }

    public void setPictures(List<String> pictures) {
        this.pictures = pictures;
    }

    public List<Attribute> getAttribute() {
        return attribute;
    }

    public void setAttribute(List<Attribute> attribute) {
        this.attribute = attribute;
    }

    public List<List<String>> getGroup() {
        return group;
    }

    public void setGroup(List<List<String>> group) {
        this.group = group;
    }

    public List<ExtraBlock> getExtraBlock() {
        return extraBlock;
    }

    public void setExtraBlock(List<ExtraBlock> extraBlock) {
        this.extraBlock = extraBlock;
    }

    public String getAwsDtm() {
        return awsDtm;
    }

    public void setAwsDtm(String awsDtm) {
        this.awsDtm = awsDtm;
    }
}
