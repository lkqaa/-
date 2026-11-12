package com.example.blog.vo;

import java.time.LocalDateTime;
import java.util.List;

public class ArticleDetailVO {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String htmlContent;
    private String cover;
    private Long categoryId;
    private String categoryName;
    private List<ArticleVO.TagBrief> tags;
    private Integer status;
    private Integer views;
    private Integer commentCount;
    private Integer isTop;
    private String slug;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private ArticleBriefVO prev;
    private ArticleBriefVO next;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public List<ArticleVO.TagBrief> getTags() { return tags; }
    public void setTags(List<ArticleVO.TagBrief> tags) { this.tags = tags; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Integer getIsTop() { return isTop; }
    public void setIsTop(Integer isTop) { this.isTop = isTop; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public ArticleBriefVO getPrev() { return prev; }
    public void setPrev(ArticleBriefVO prev) { this.prev = prev; }
    public ArticleBriefVO getNext() { return next; }
    public void setNext(ArticleBriefVO next) { this.next = next; }
}
