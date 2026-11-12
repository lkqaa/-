package com.example.blog.controller.admin;

import com.example.blog.common.PageResult;
import com.example.blog.common.Result;
import com.example.blog.dto.*;
import com.example.blog.service.ArticleService;
import com.example.blog.vo.ArticleDetailVO;
import com.example.blog.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理-文章")
@RestController
@RequestMapping("/admin/articles")
public class AdminArticleController {

    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Operation(summary = "文章列表（含草稿）")
    @GetMapping
    public Result<PageResult<ArticleVO>> list(ArticleQueryDTO query) {
        return Result.success(articleService.pageAdmin(query));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.success(articleService.adminDetail(id));
    }

    @Operation(summary = "新建文章")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ArticleSaveDTO dto) {
        return Result.success(articleService.create(dto));
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleSaveDTO dto) {
        articleService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "更新文章状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        articleService.updateStatus(id, dto.getStatus());
        return Result.success();
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    @Operation(summary = "批量删除文章")
    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@Valid @RequestBody IdsDTO dto) {
        articleService.batchDelete(dto.getIds());
        return Result.success();
    }
}
