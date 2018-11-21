package com.moonface.collabocode;

import com.google.firebase.Timestamp;
import java.util.List;

public class Post {

    private String title;
    private List<Code> codes;
    private Timestamp dateSent;
    private String content;
    private String id;
    private String userId;

    public Post(){
        super();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCodes(List<Code> codes) {
        this.codes = codes;
    }

    public List<Code> getCodes() {
        return codes;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setDateSent(Timestamp dateSent) {
        this.dateSent = dateSent;
    }

    public Timestamp getDateSent() {
        return dateSent;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
