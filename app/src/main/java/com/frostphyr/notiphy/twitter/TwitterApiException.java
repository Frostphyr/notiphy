package com.frostphyr.notiphy.twitter;

public class TwitterApiException extends Exception {

    public static final int CODE_USER_NOT_FOUND = 50;
    public static final int CODE_USER_SUSPENDED = 63;

    private final int code;

    public TwitterApiException(int code) {
        super(Integer.toString(code));

        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
