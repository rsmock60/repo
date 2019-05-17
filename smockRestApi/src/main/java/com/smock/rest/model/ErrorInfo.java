package com.smock.rest.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ErrorInfo {

    private ErrorCode code;

    private String field;

    private String message;

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}