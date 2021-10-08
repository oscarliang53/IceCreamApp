package edu.shu.icecream.data;

public class ChatMessage {
    public ChatMessage(String messageText, String messageUser, long timestamp) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.timestamp = timestamp;
    }

    private String messageText;
    private String messageUser;
    private long timestamp;
    boolean isMe = true;//預設是自己

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ChatMessage() {

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

}
