package com.frostphyr.notiphy;

public class IllegalInputException extends RuntimeException {

    private int errorMessageResId;

    public IllegalInputException(int errorMessageResId) {
        this.errorMessageResId = errorMessageResId;
    }

    public int getErrorMessageResourceId() {
        return errorMessageResId;
    }

}
