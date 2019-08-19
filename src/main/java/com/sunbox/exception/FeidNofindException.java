package com.sunbox.exception;

public class FeidNofindException extends RuntimeException{

    public FeidNofindException(){
        super();
    }

    public FeidNofindException(String errorInfo) {
        super(errorInfo);
    }
}
