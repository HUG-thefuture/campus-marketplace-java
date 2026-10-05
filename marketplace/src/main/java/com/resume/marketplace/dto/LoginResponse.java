package com.resume.marketplace.dto;

/**
 * 登录响应 DTO：返回 token 与简要用户信息。
 */
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String role;

    public LoginResponse(String token, Long userId, String username, String nickname, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getNickname() { return nickname; }
    public String getRole() { return role; }
}