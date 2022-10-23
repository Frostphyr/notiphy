package com.frostphyr.notiphy;

public interface Callback<T> {

    void onComplete(Result<T> result);

    class Result<T> {

        private T data;
        private Exception exception;

        public Result(T data) {
            this.data = data;
        }

        public Result(Exception exception) {
            this.exception = exception;
        }

        public Result() {
        }

        public T getData() {
            return data;
        }

        public Exception getException() {
            return exception;
        }

    }

}
