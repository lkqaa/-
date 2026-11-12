package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;

public class TagSaveDTO {

    @NotBlank(message = "标签名称不能为空")
    private String name;

    private String color;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
