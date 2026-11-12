package com.example.blog.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class IdsDTO {

    @NotEmpty(message = "请选择至少一条记录")
    private List<Long> ids;

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
}
