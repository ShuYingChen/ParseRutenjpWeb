package com.pchome.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by freedomandy on 7/20/17.
 */
public class ExtraBlock {
    @JsonProperty("Key")
    private List<String> key;
    @JsonProperty("Value")
    private int value;

    public ExtraBlock(List<String> key, int value) {
        this.key = key;
        this.value = value;
    }

    public List<String> getKey() {
        return key;
    }

    public void setKey(List<String> key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
