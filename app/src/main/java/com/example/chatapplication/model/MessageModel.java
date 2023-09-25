package com.example.chatapplication.model;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class MessageModel {
    String messageID, message, senderID, chatImageId;
    int reaction = -1;
    long timeStamp;
    String algorithm, cipherText;
    SecretKey key;
    IvParameterSpec ivParameterSpec;

    public String getChatImageId() {
        return chatImageId;
    }

    public void setChatImageId(String chatImageId) {
        this.chatImageId = chatImageId;
    }

    public MessageModel() {
    }

    public MessageModel(String message, String senderID, long timeStamp, int reaction) {
        this.message = message;
        this.senderID = senderID;
        this.timeStamp = timeStamp;
        this.reaction = reaction;
    }

    public MessageModel(long timeStamp, String senderID, String chatImageId, int reaction) {
        this.timeStamp = timeStamp;
        this.chatImageId = chatImageId;
        this.senderID = senderID;
        this.reaction = reaction;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public int getReaction(){return this.reaction;}

    public void setReaction(int i){
        this.reaction = i;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
