package com.example.blog.controller.admin;

import com.example.blog.common.Result;
import com.example.blog.dto.CategorySaveDTO;
import com.example.blog.dto.TagSaveDTO;
import com.example.blog.service.CategoryService;
import com.example.blog.service.TagService;
import com.example.blog.vo.CategoryVO;
import com.example.blog.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理-分类/标签")
@RestController
@RequestMapping("/admin")
public class AdminTaxonomyController {

    private final CategoryService categoryService;
    private final TagService tagService;

    public AdminTaxonomyController(CategoryService categoryService, TagService tagService) {
        this.categoryService = categoryService;
        this.tagService = tagService;
    }

    @Operation(summary = "分类列表")
    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.success(categoryService.listAll());
    }

    @Operation(summary = "新增分类")
    @PostMapping("/categories")
    public Result<CategoryVO> createCategory(@Valid @RequestBody CategorySaveDTO dto) {
        return Result.success(categoryService.create(dto));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody CategorySaveDTO dto) {
        categoryService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @Operation(summary = "标签列表")
    @GetMapping("/tags")
    public Result<List<TagVO>> tags() {
        return Result.success(tagService.listAll());
    }

    @Operation(summary = "新增标签")
    @PostMapping("/tags")
    public Result<TagVO> createTag(@Valid @RequestBody TagSaveDTO dto) {
        return Result.success(tagService.create(dto));
    }

    @Operation(summary = "更新标签")
    @PutMapping("/tags/{id}")
    public Result<Void> updateTag(@PathVariable Long id, @Valid @RequestBody TagSaveDTO dto) {
        tagService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/tags/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }
}
