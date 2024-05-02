package com.dongkuksystems.dbox.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;


public class MessageUtils {

    private MessageSourceAccessor messageSourceAccessor;

    private static MessageUtils instance = new MessageUtils();

    public static MessageUtils getInstance() {
        return instance;
    }

    public String getMessage(String key) {
        return messageSourceAccessor.getMessage(key);
    }

    public String getMessage(String key, Object... params) {
        return messageSourceAccessor.getMessage(key, params);
    }

    @Autowired
    public void setMessageSourceAccessor(MessageSourceAccessor messageSourceAccessor) {
        this.messageSourceAccessor = messageSourceAccessor;
    }

}