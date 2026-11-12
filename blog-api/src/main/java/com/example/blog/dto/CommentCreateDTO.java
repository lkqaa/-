package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentCreateDTO {

    /** 由路径参数 /articles/{id}/comments 注入，不参与请求体校验 */
    private Long articleId;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private String email;
    private String website;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
