package com.example.blog.vo;

public class ArticleBriefVO {

    private Long id;
    private String title;

    public ArticleBriefVO() {}
    public ArticleBriefVO(Long id, String title) { this.id = id; this.title = title; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
