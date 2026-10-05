package com.resume.marketplace.dto;

/**
 * 用户信息响应 VO：始终不包含 password，防止敏感信息泄露。
 */
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}