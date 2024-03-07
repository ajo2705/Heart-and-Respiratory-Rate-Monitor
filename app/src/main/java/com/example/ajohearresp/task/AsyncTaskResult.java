package com.example.ajohearresp.task;

public class AsyncTaskResult {
    private String resultString;
    private long value1;
    private long value2;

    public AsyncTaskResult(String resultString, long value1, long value2) {
        this.resultString = resultString;
        this.value1 = value1;
        this.value2 = value2;
    }

    public String getResultString() {
        return resultString;
    }

    public long getValue1() {
        return value1;
    }

    public long getValue2() {
        return value2;
    }
}
