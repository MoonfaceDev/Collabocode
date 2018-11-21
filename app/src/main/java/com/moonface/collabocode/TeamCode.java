package com.moonface.collabocode;

class TeamCode {

    private String code;
    private String teamId;
    private boolean enabled = true;

    TeamCode(){

    }

    String getCode() {
        return code;
    }

    void setCode(String code) {
        this.code = code;
    }

    String getTeamId() {
        return teamId;
    }

    void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    boolean isEnabled() {
        return enabled;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
