package com.frostphyr.notiphy;

public class AsyncTaskResult<T> {

    private T result;
    private Exception exception;

    public AsyncTaskResult(T result) {
        this.result = result;
    }

    public AsyncTaskResult(Exception exception) {
        this.exception = exception;
    }

    public T getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }

}
