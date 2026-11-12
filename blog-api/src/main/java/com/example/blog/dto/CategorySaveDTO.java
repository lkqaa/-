package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;

public class CategorySaveDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotBlank(message = "slug 不能为空")
    private String slug;

    private String description;

    private Integer sortOrder = 0;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
