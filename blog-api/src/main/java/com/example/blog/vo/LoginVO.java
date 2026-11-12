package com.example.blog.vo;

public class LoginVO {

    private String token;
    private Long expiresIn;
    private AdminInfoVO adminInfo;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    public AdminInfoVO getAdminInfo() { return adminInfo; }
    public void setAdminInfo(AdminInfoVO adminInfo) { this.adminInfo = adminInfo; }

    public static class AdminInfoVO {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String email;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
