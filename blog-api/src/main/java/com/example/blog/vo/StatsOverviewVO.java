package com.example.blog.vo;

public class StatsOverviewVO {

    private Long articleCount;
    private Long publishedCount;
    private Long draftCount;
    private Long categoryCount;
    private Long tagCount;
    private Long commentCount;
    private Long totalViews;
    /** 今日访问量，来源于 view_log 表 */
    private Long todayViews;

    public Long getArticleCount() { return articleCount; }
    public void setArticleCount(Long articleCount) { this.articleCount = articleCount; }
    public Long getPublishedCount() { return publishedCount; }
    public void setPublishedCount(Long publishedCount) { this.publishedCount = publishedCount; }
    public Long getDraftCount() { return draftCount; }
    public void setDraftCount(Long draftCount) { this.draftCount = draftCount; }
    public Long getCategoryCount() { return categoryCount; }
    public void setCategoryCount(Long categoryCount) { this.categoryCount = categoryCount; }
    public Long getTagCount() { return tagCount; }
    public void setTagCount(Long tagCount) { this.tagCount = tagCount; }
    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }
    public Long getTotalViews() { return totalViews; }
    public void setTotalViews(Long totalViews) { this.totalViews = totalViews; }
    public Long getTodayViews() { return todayViews; }
    public void setTodayViews(Long todayViews) { this.todayViews = todayViews; }
}
