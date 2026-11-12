package com.example.blog.controller.api;

import com.example.blog.common.PageResult;
import com.example.blog.common.Result;
import com.example.blog.dto.ArticleQueryDTO;
import com.example.blog.dto.CommentCreateDTO;
import com.example.blog.dto.CommentQueryDTO;
import com.example.blog.service.ArticleService;
import com.example.blog.service.CommentService;
import com.example.blog.vo.ArticleDetailVO;
import com.example.blog.vo.ArticleVO;
import com.example.blog.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "公开-文章")
@RestController
@RequestMapping("/articles")
public class ArticleApiController {

    private final ArticleService articleService;
    private final CommentService commentService;

    public ArticleApiController(ArticleService articleService, CommentService commentService) {
        this.articleService = articleService;
        this.commentService = commentService;
    }

    @Operation(summary = "文章列表（已发布，分页）")
    @GetMapping
    public Result<PageResult<ArticleVO>> list(ArticleQueryDTO query) {
        return Result.success(articleService.pagePublic(query));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.success(articleService.detail(id));
    }

    @Operation(summary = "增加阅读量")
    @PostMapping("/{id}/view")
    public Result<Integer> view(@PathVariable Long id, HttpServletRequest request) {
        return Result.success(articleService.incrViews(id, clientIp(request), request.getHeader("User-Agent")));
    }

    @Operation(summary = "文章评论列表")
    @GetMapping("/{id}/comments")
    public Result<PageResult<CommentVO>> comments(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        CommentQueryDTO query = new CommentQueryDTO();
        query.setArticleId(id);
        query.setPage(page);
        query.setSize(size);
        return Result.success(commentService.pagePublic(query));
    }

    @Operation(summary = "提交评论")
    @PostMapping("/{id}/comments")
    public Result<CommentVO> addComment(@PathVariable Long id,
                                        @Valid @RequestBody CommentCreateDTO dto,
                                        HttpServletRequest request) {
        dto.setArticleId(id);
        return Result.success(commentService.create(dto, clientIp(request), request.getHeader("User-Agent")));
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
