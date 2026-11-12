package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.ErrorCode;
import com.example.blog.common.PageResult;
import com.example.blog.dto.CommentCreateDTO;
import com.example.blog.dto.CommentQueryDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.service.CommentService;
import com.example.blog.vo.CommentVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private static final String COMMENT_LIMIT_KEY = "blog:comment:limit:%s";

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${blog.rate-limit.comment-window-seconds:60}")
    private long commentWindowSeconds;
    @Value("${blog.rate-limit.comment-max-per-window:5}")
    private long commentMaxPerWindow;

    public CommentServiceImpl(CommentMapper commentMapper, ArticleMapper articleMapper,
                              RedisTemplate<String, Object> redisTemplate) {
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public PageResult<CommentVO> pagePublic(CommentQueryDTO query) {
        query.setStatus(1);
        return page(query);
    }

    @Override
    public PageResult<CommentVO> pageAdmin(CommentQueryDTO query) {
        return page(query);
    }

    private PageResult<CommentVO> page(CommentQueryDTO query) {
        int current = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : Math.min(query.getSize(), 100);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (query.getArticleId() != null) {
            wrapper.eq(Comment::getArticleId, query.getArticleId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Comment::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Comment::getNickname, kw).or().like(Comment::getContent, kw));
        }
        wrapper.orderByDesc(Comment::getCreatedAt);

        IPage<Comment> result = commentMapper.selectPage(new Page<>(current, size), wrapper);
        List<Comment> records = result.getRecords();

        Map<Long, String> titles = Collections.emptyMap();
        if (!records.isEmpty()) {
            List<Long> articleIds = records.stream().map(Comment::getArticleId)
                    .filter(Objects::nonNull).distinct().toList();
            if (!articleIds.isEmpty()) {
                titles = articleMapper.selectBatchIds(articleIds).stream()
                        .collect(Collectors.toMap(Article::getId, Article::getTitle));
            }
        }

        final Map<Long, String> titleMap = titles;
        List<CommentVO> vos = records.stream().map(c -> {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setArticleId(c.getArticleId());
            vo.setArticleTitle(titleMap.get(c.getArticleId()));
            vo.setNickname(c.getNickname());
            vo.setEmail(c.getEmail());
            vo.setWebsite(c.getWebsite());
            vo.setContent(c.getContent());
            vo.setStatus(c.getStatus());
            vo.setCreatedAt(c.getCreatedAt());
            return vo;
        }).toList();

        return new PageResult<>(vos, result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional
    public CommentVO create(CommentCreateDTO dto, String ip, String userAgent) {
        checkRateLimit(ip);

        Article article = articleMapper.selectById(dto.getArticleId());
        if (article == null || article.getStatus() == null || article.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setNickname(dto.getNickname().trim());
        comment.setEmail(dto.getEmail());
        comment.setWebsite(dto.getWebsite());
        comment.setContent(dto.getContent());
        comment.setStatus(1);
        comment.setIp(ip);
        comment.setUserAgent(userAgent);
        commentMapper.insert(comment);

        articleMapper.refreshCommentCount(dto.getArticleId());

        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setArticleTitle(article.getTitle());
        vo.setNickname(comment.getNickname());
        vo.setEmail(comment.getEmail());
        vo.setWebsite(comment.getWebsite());
        vo.setContent(comment.getContent());
        vo.setStatus(comment.getStatus());
        vo.setCreatedAt(comment.getCreatedAt());
        return vo;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评论不存在");
        }
        comment.setStatus(status);
        commentMapper.updateById(comment);
        articleMapper.refreshCommentCount(comment.getArticleId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评论不存在");
        }
        commentMapper.deleteById(id);
        articleMapper.refreshCommentCount(comment.getArticleId());
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        List<Comment> comments = commentMapper.selectBatchIds(ids);
        commentMapper.deleteBatchIds(ids);
        comments.stream().map(Comment::getArticleId).filter(Objects::nonNull).distinct()
                .forEach(articleMapper::refreshCommentCount);
    }

    private void checkRateLimit(String ip) {
        String key = String.format(COMMENT_LIMIT_KEY, ip);
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(commentWindowSeconds));
            }
            if (count != null && count > commentMaxPerWindow) {
                throw new BusinessException(ErrorCode.RATE_LIMITED,
                        String.format("评论过于频繁，%d 秒内最多 %d 条", commentWindowSeconds, commentMaxPerWindow));
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            // Redis 不可用时降级：不限流
        }
    }
}
