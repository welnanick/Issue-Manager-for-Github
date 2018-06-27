package com.nickwelna.issuemanagerforgithub.models;

import java.util.List;

public class SearchResult {

    public int total_count;
    boolean incomplete_results;
    List<Repository> items;

    public int getTotal_count() {

        return total_count;

    }

    public boolean isIncomplete_results() {

        return incomplete_results;

    }

    public List<Repository> getItems() {

        return items;

    }

}
