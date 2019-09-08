package com.github.obhen233.exception;

public class FeidNofindException extends RuntimeException{

    public FeidNofindException(){
        super();
    }

    public FeidNofindException(String errorInfo) {
        super(errorInfo);
    }
}
