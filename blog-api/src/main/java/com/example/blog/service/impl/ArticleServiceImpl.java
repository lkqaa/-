package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.ErrorCode;
import com.example.blog.common.PageResult;
import com.example.blog.dto.ArticleQueryDTO;
import com.example.blog.dto.ArticleSaveDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;
import com.example.blog.entity.ViewLog;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.mapper.ViewLogMapper;
import com.example.blog.service.ArticleService;
import com.example.blog.vo.ArticleBriefVO;
import com.example.blog.vo.ArticleDetailVO;
import com.example.blog.vo.ArticleVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private static final String VIEW_DEDUP_KEY = "blog:view:dedup:%d:%s";

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ViewLogMapper viewLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StatsServiceImpl statsService;

    @Value("${blog.view.dedup-window-seconds:1800}")
    private long dedupWindowSeconds;

    public ArticleServiceImpl(ArticleMapper articleMapper, ArticleTagMapper articleTagMapper,
                              CategoryMapper categoryMapper, TagMapper tagMapper,
                              ViewLogMapper viewLogMapper,
                              RedisTemplate<String, Object> redisTemplate,
                              StatsServiceImpl statsService) {
        this.articleMapper = articleMapper;
        this.articleTagMapper = articleTagMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.viewLogMapper = viewLogMapper;
        this.redisTemplate = redisTemplate;
        this.statsService = statsService;
    }

    @Override
    public PageResult<ArticleVO> pagePublic(ArticleQueryDTO query) {
        query.setStatus(1);
        return page(query);
    }

    @Override
    public PageResult<ArticleVO> pageAdmin(ArticleQueryDTO query) {
        return page(query);
    }

    private PageResult<ArticleVO> page(ArticleQueryDTO query) {
        int current = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : Math.min(query.getSize(), 100);

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (query.getStatus() != null) {
            wrapper.eq(Article::getStatus, query.getStatus());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, query.getCategoryId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Article::getTitle, kw).or().like(Article::getSummary, kw));
        }
        if (query.getTagId() != null) {
            List<Long> articleIds = articleTagMapper.selectList(
                            new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, query.getTagId()))
                    .stream().map(ArticleTag::getArticleId).distinct().toList();
            if (articleIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L, 0L, (long) current, (long) size);
            }
            wrapper.in(Article::getId, articleIds);
        }

        if ("views".equalsIgnoreCase(query.getOrderBy())) {
            wrapper.orderByDesc(Article::getViews);
        } else {
            wrapper.orderByDesc(Article::getIsTop).orderByDesc(Article::getPublishedAt).orderByDesc(Article::getId);
        }

        IPage<Article> result = articleMapper.selectPage(new Page<>(current, size), wrapper);
        List<ArticleVO> records = toVOList(result.getRecords());
        return new PageResult<>(records, result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
    }

    private List<ArticleVO> toVOList(List<Article> articles) {
        if (articles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> articleIds = articles.stream().map(Article::getId).toList();
        List<Long> categoryIds = articles.stream().map(Article::getCategoryId)
                .filter(java.util.Objects::nonNull).distinct().toList();

        Map<Long, String> categoryNames = categoryIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        Map<Long, List<Long>> articleTagIds = new HashMap<>();
        articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIds))
                .forEach(at -> articleTagIds.computeIfAbsent(at.getArticleId(), k -> new ArrayList<>()).add(at.getTagId()));

        List<Long> tagIds = articleTagIds.values().stream().flatMap(List::stream).distinct().toList();
        Map<Long, Tag> tagMap = tagIds.isEmpty() ? Collections.emptyMap()
                : tagMapper.selectBatchIds(tagIds).stream().collect(Collectors.toMap(Tag::getId, Function.identity()));

        List<ArticleVO> list = new ArrayList<>(articles.size());
        for (Article a : articles) {
            ArticleVO vo = new ArticleVO();
            vo.setId(a.getId());
            vo.setTitle(a.getTitle());
            vo.setSummary(a.getSummary());
            vo.setCover(a.getCover());
            vo.setCategoryId(a.getCategoryId());
            vo.setCategoryName(a.getCategoryId() == null ? null : categoryNames.get(a.getCategoryId()));
            vo.setStatus(a.getStatus());
            vo.setViews(a.getViews() == null ? 0 : a.getViews());
            vo.setCommentCount(a.getCommentCount() == null ? 0 : a.getCommentCount());
            vo.setIsTop(a.getIsTop());
            vo.setSlug(a.getSlug());
            vo.setPublishedAt(a.getPublishedAt());
            vo.setCreatedAt(a.getCreatedAt());

            List<ArticleVO.TagBrief> tags = articleTagIds.getOrDefault(a.getId(), Collections.emptyList())
                    .stream().map(tagMap::get).filter(java.util.Objects::nonNull)
                    .map(t -> new ArticleVO.TagBrief(t.getId(), t.getName(), t.getColor()))
                    .toList();
            vo.setTags(tags);
            list.add(vo);
        }
        return list;
    }

    @Override
    public ArticleDetailVO detail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getStatus() == null || article.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        return buildDetail(article, true);
    }

    @Override
    public ArticleDetailVO adminDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        return buildDetail(article, false);
    }

    private ArticleDetailVO buildDetail(Article article, boolean withNeighbor) {
        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setContent(article.getContent());
        vo.setHtmlContent(article.getHtmlContent());
        vo.setCover(article.getCover());
        vo.setCategoryId(article.getCategoryId());
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            vo.setCategoryName(category == null ? null : category.getName());
        }
        vo.setStatus(article.getStatus());
        vo.setViews(article.getViews() == null ? 0 : article.getViews());
        vo.setCommentCount(article.getCommentCount() == null ? 0 : article.getCommentCount());
        vo.setIsTop(article.getIsTop());
        vo.setSlug(article.getSlug());
        vo.setPublishedAt(article.getPublishedAt());
        vo.setCreatedAt(article.getCreatedAt());

        List<ArticleTag> relations = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, article.getId()));
        if (!relations.isEmpty()) {
            List<Long> tagIds = relations.stream().map(ArticleTag::getTagId).toList();
            vo.setTags(tagMapper.selectBatchIds(tagIds).stream()
                    .map(t -> new ArticleVO.TagBrief(t.getId(), t.getName(), t.getColor()))
                    .toList());
        } else {
            vo.setTags(Collections.emptyList());
        }

        if (withNeighbor && article.getPublishedAt() != null) {
            Article prev = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                    .eq(Article::getStatus, 1)
                    .lt(Article::getPublishedAt, article.getPublishedAt())
                    .orderByDesc(Article::getPublishedAt)
                    .last("LIMIT 1"));
            Article next = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                    .eq(Article::getStatus, 1)
                    .gt(Article::getPublishedAt, article.getPublishedAt())
                    .orderByAsc(Article::getPublishedAt)
                    .last("LIMIT 1"));
            vo.setPrev(prev == null ? null : new ArticleBriefVO(prev.getId(), prev.getTitle()));
            vo.setNext(next == null ? null : new ArticleBriefVO(next.getId(), next.getTitle()));
        }
        return vo;
    }

    @Override
    public Integer incrViews(Long id, String ip, String userAgent) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getStatus() == null || article.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        String dedupKey = String.format(VIEW_DEDUP_KEY, id, ip);
        boolean shouldCount;
        try {
            Boolean first = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1",
                    Duration.ofSeconds(dedupWindowSeconds));
            shouldCount = first != null && first;
        } catch (Exception e) {
            // Redis 不可用时降级：始终计数
            shouldCount = true;
        }

        if (shouldCount) {
            articleMapper.incrViews(id);
            ViewLog log = new ViewLog();
            log.setArticleId(id);
            log.setIp(ip);
            log.setUserAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent);
            viewLogMapper.insert(log);
            statsService.evictHotArticles();
        }

        Integer views = article.getViews();
        int current = (views == null ? 0 : views) + (shouldCount ? 1 : 0);
        return current;
    }

    @Override
    @Transactional
    public Long create(ArticleSaveDTO dto) {
        checkSlug(dto.getSlug(), null);
        Article article = new Article();
        fillArticle(article, dto);
        article.setViews(0);
        article.setCommentCount(0);
        if (dto.getStatus() != null && dto.getStatus() == 1 && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.insert(article);
        saveTags(article.getId(), dto.getTagIds());
        return article.getId();
    }

    @Override
    @Transactional
    public void update(Long id, ArticleSaveDTO dto) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        checkSlug(dto.getSlug(), id);
        LocalDateTime publishedAt = article.getPublishedAt();
        fillArticle(article, dto);
        article.setId(id);
        if (dto.getStatus() != null && dto.getStatus() == 1 && publishedAt == null) {
            article.setPublishedAt(LocalDateTime.now());
        } else {
            article.setPublishedAt(publishedAt);
        }
        articleMapper.updateById(article);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        saveTags(id, dto.getTagIds());
    }

    private void fillArticle(Article article, ArticleSaveDTO dto) {
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCover(dto.getCover());
        article.setCategoryId(dto.getCategoryId());
        article.setSlug(StringUtils.hasText(dto.getSlug()) ? dto.getSlug().trim() : null);
        article.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        article.setIsTop(dto.getIsTop() == null ? 0 : dto.getIsTop());
    }

    private void checkSlug(String slug, Long excludeId) {
        if (!StringUtils.hasText(slug)) {
            return;
        }
        Long count = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getSlug, slug.trim())
                .ne(excludeId != null, Article::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "slug 已存在：" + slug);
        }
    }

    private void saveTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds.stream().distinct().toList()) {
            ArticleTag relation = new ArticleTag();
            relation.setArticleId(articleId);
            relation.setTagId(tagId);
            articleTagMapper.insert(relation);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        articleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        }
        articleMapper.deleteBatchIds(ids);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        article.setStatus(status);
        if (status == 1 && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.updateById(article);
    }
}
