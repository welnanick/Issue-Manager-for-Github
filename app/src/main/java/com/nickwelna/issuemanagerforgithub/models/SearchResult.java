package com.nickwelna.issuemanagerforgithub.models;

import java.util.ArrayList;

public class SearchResult {

    public int total_count;
    boolean incomplete_results;
    ArrayList<Repository> items;

    public int getTotal_count() {

        return total_count;

    }

    public boolean isIncomplete_results() {

        return incomplete_results;

    }

    public ArrayList<Repository> getItems() {

        return items;

    }

}
