package org.ovirt.mobile.movirt.util.message;

import android.support.annotation.NonNull;

import org.ovirt.mobile.movirt.util.ObjectUtils;

public class Message {
    private ErrorType type = ErrorType.NORMAL;
    private String detail;
    private String header;
    private Integer logPriority;

    public Message setType(ErrorType type) {
        this.type = type;
        return this;
    }

    public Message setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public Message setDetail(Throwable throwable) {
        this.detail = ObjectUtils.throwableToString(throwable);
        return this;
    }

    public Message setHeader(String header) {
        this.header = header;
        return this;
    }

    public Message setLogPriority(Integer logPriority) {
        this.logPriority = logPriority;
        return this;
    }

    @NonNull
    public ErrorType getType() {
        return type;
    }

    @NonNull
    public String getDetail() {
        return detail == null ? "" : detail;
    }

    public String getHeader() {
        return header;
    }

    public Integer getLogPriority() {
        return logPriority;
    }
}
