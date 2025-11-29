package com.rev.playground.results;

public class BenchmarkResults {

    private final BenchmarkResultsElement collectionResults = new BenchmarkResultsElement();
    private final BenchmarkResultsElement listResults = new BenchmarkResultsElement();

    public final BenchmarkResultsElement getCollectionResults() {

        return collectionResults;
    }

    public final BenchmarkResultsElement getListResults() {
        return listResults;
    }
}
