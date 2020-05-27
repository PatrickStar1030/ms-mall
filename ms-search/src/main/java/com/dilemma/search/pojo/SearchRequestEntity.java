package com.dilemma.search.pojo;


import java.util.Map;

/**
 * 搜索参数封装的实体类
 */
public class SearchRequestEntity {
    private String key; //搜索条件
    private Integer page; //当前页
    private String sortBy; //排序字段
    private Boolean descending; //是否降序
    private Map<String,String> filter;//过滤条件
    private static final Integer DEFAULT_SIZE = 20; //每页大小，不从页面接收，而是固定大小
    private static final Integer DEFAULT_PAGE = 1; //默认页

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getPage() {
        if (page == null){
            return DEFAULT_PAGE;
        }
        //对页码做一些校验。页码不能小于1
        return Math.max(DEFAULT_PAGE,page);
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize(){
        return DEFAULT_SIZE;
    }



    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    public String getSortBy() {
        return sortBy;
    }

    public void setDescending(Boolean descending) {
        this.descending = descending;
    }
    public Boolean getDescending(){return descending;}

    public Map<String, String> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, String> filter) {
        this.filter = filter;
    }
}
