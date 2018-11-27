package com.nickwelna.issuemanagerforgithub.models;

import com.squareup.moshi.Json;

import java.util.List;

public class SearchResultMoshi {
    @Json(name = "total_count")
    int totalCount;
    @Json(name = "incomplete_results")
    boolean incompleteResults;
    private List<RepositoryMoshi> items;

    public List<RepositoryMoshi> getItems() {
        return items;
    }
}
