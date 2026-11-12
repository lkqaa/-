package com.example.blog.controller.api;

import com.example.blog.common.Result;
import com.example.blog.service.CategoryService;
import com.example.blog.service.SysConfigService;
import com.example.blog.service.TagService;
import com.example.blog.vo.CategoryVO;
import com.example.blog.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "公开-分类/标签/配置")
@RestController
public class SiteApiController {

    private final CategoryService categoryService;
    private final TagService tagService;
    private final SysConfigService sysConfigService;

    public SiteApiController(CategoryService categoryService, TagService tagService,
                             SysConfigService sysConfigService) {
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.sysConfigService = sysConfigService;
    }

    @Operation(summary = "全部分类（含文章数）")
    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.success(categoryService.listAll());
    }

    @Operation(summary = "全部标签（含文章数）")
    @GetMapping("/tags")
    public Result<List<TagVO>> tags() {
        return Result.success(tagService.listAll());
    }

    @Operation(summary = "公开站点配置")
    @GetMapping("/config")
    public Result<Map<String, String>> config() {
        return Result.success(sysConfigService.publicConfig());
    }
}
