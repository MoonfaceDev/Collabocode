package com.moonface.collabocode;


public class User {

    private String name;
    private String profilePicUrl;

    public User(){

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }
}
