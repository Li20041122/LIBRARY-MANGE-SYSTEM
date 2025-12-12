package edu.fosu.book.dto;

public class UserInfo {
    private String userid;
    private String username;
    private String role;
    private String departid;

    public UserInfo() {}

    public UserInfo(String userid, String username, String role, String departid) {
        this.userid = userid;
        this.username = username;
        this.role = role;
        this.departid = departid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartid() {
        return departid;
    }

    public void setDepartid(String departid) {
        this.departid = departid;
    }
}
