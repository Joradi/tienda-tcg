package com.tiendatcg.cardset;

import java.util.List;

public class CardSetResponse {

    private List<CardSetDto> data;
    private int page;
    private int pageSize;
    private int count;
    private int totalCount;

    public CardSetResponse() {
    }

    public List<CardSetDto> getData() {
        return data;
    }

    public void setData(List<CardSetDto> data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
