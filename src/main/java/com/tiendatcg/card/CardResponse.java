package com.tiendatcg.card;

import java.util.List;

public class CardResponse {
    private List<CardDto> data;
    private int page;
    private int pageSize;
    private int count;
    private int totalCount;

    public CardResponse() {
    }

    public List<CardDto> getData() {
        return data;
    }

    public void setData(List<CardDto> data) {
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
