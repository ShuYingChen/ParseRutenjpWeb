package com.pchome.data;

/**
 * Created by freedomandy on 7/20/17.
 */
public class ParseInfo {
    private String id;
    private PageInfo pageInfo;

    public ParseInfo(String id, PageInfo pageInfo) {
        this.id = id;
        this.pageInfo = pageInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
}