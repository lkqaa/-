package com.example.blog.controller.admin;

import com.example.blog.common.PageResult;
import com.example.blog.common.Result;
import com.example.blog.dto.CommentQueryDTO;
import com.example.blog.dto.IdsDTO;
import com.example.blog.dto.StatusDTO;
import com.example.blog.service.CommentService;
import com.example.blog.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理-评论")
@RestController
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "评论列表")
    @GetMapping
    public Result<PageResult<CommentVO>> list(CommentQueryDTO query) {
        return Result.success(commentService.pageAdmin(query));
    }

    @Operation(summary = "更新评论状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        commentService.updateStatus(id, dto.getStatus());
        return Result.success();
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }

    @Operation(summary = "批量删除评论")
    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@Valid @RequestBody IdsDTO dto) {
        commentService.batchDelete(dto.getIds());
        return Result.success();
    }
}
