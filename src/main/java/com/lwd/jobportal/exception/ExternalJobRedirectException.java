package com.lwd.jobportal.exception;
public class ExternalJobRedirectException extends RuntimeException {
    private final String redirectUrl;

    public ExternalJobRedirectException(String redirectUrl) {
        super("External job redirect");
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
