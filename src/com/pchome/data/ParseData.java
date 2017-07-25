package com.pchome.data;

/**
 * Created by freedomandy on 7/20/17.
 */
public class ParseData {
    private String id;
    private String result;

    public ParseData(String id, String result) {
        this.id = id;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
