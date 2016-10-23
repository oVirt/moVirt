package org.ovirt.mobile.movirt.util.message;

import android.support.annotation.NonNull;

public class Message {
    private final ErrorType type;
    private final String detail;
    private final String header;
    private Integer logPriority;


    public Message(String detail) {
        this(ErrorType.NORMAL, detail);
    }

    public Message(ErrorType type, String detail) {
        this(type, detail, null);
    }

    public Message(ErrorType type, String detail, String header) {
        this(type, detail, header, null); // unknown log priority
    }

    public Message(ErrorType type, String detail, int logPriority) {
        this(type, detail, null, logPriority);
    }

    public Message(ErrorType type, String detail, String header, Integer logPriority) {
        if (type == null) {
            throw new IllegalArgumentException("null type");
        }
        if (detail == null) {
            throw new IllegalArgumentException("null detail");
        }

        this.type = type;
        this.detail = detail;
        this.header = header;
        this.logPriority = logPriority;
    }

    @NonNull
    public ErrorType getType() {
        return type;
    }

    @NonNull
    public String getDetail() {
        return detail;
    }


    public String getHeader() {
        return header;
    }

    public void setLogPriority(Integer logPriority) {
        this.logPriority = logPriority;
    }

    public Integer getLogPriority() {
        return logPriority;
    }
}
