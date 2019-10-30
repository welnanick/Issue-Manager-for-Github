package com.nickwelna.issuemanagerforgithub.models;

import com.squareup.moshi.Json;

import java.util.List;

public final class SearchResult {
    @Json(name = "total_count")
    int totalCount;
    @Json(name = "incomplete_results")
    boolean incompleteResults;
    private List<Repository> items;

    public List<Repository> getItems() {
        return items;
    }
}
