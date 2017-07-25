package com.pchome.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by freedomandy on 7/19/17.
 */
public class Option {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Name")
    private String value;
    @JsonProperty("OriginPrice")
    private int originPrice;

    public Option(){};

    public Option(String id, String value, int originPrice) {
        this.id = id;
        this.value = value;
        this.originPrice = originPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(int originPrice) {
        this.originPrice = originPrice;
    }
}
