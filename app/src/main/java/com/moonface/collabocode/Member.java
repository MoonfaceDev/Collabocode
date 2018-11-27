package com.moonface.collabocode;

public class Member {
    private String teamId;
    private String userId;
    private boolean admin;
    private boolean starred;


    public Member(){

    }

    public Member(String teamId, String userId, boolean admin){
        this.teamId = teamId;
        this.userId = userId;
        this.admin = admin;
        this.starred = false;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public boolean isStarred() {
        return starred;
    }
}
