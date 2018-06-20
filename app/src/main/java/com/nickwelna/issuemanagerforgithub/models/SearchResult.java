package com.nickwelna.issuemanagerforgithub.models;

public class SearchResult {

    public int total_count;
    boolean incomplete_results;
    Repository[] items;

    public int getTotal_count() {

        return total_count;

    }

    public boolean isIncomplete_results() {

        return incomplete_results;

    }

    public Repository[] getItems() {

        return items;

    }
}
