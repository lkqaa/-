package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.ViewLog;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.mapper.ViewLogMapper;
import com.example.blog.service.StatsService;
import com.example.blog.vo.HotArticleVO;
import com.example.blog.vo.StatsOverviewVO;
import com.example.blog.vo.TrendItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsServiceImpl implements StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsServiceImpl.class);
    private static final String HOT_CACHE_KEY_PREFIX = "blog:stats:hot:";

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;
    private final ViewLogMapper viewLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${blog.cache.hot-ttl-seconds:300}")
    private long hotTtlSeconds;

    public StatsServiceImpl(ArticleMapper articleMapper, CategoryMapper categoryMapper,
                            TagMapper tagMapper, CommentMapper commentMapper,
                            ViewLogMapper viewLogMapper,
                            RedisTemplate<String, Object> redisTemplate) {
        this.articleMapper = articleMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.commentMapper = commentMapper;
        this.viewLogMapper = viewLogMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public StatsOverviewVO overview() {
        Long published = articleMapper.selectCount(new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1));
        Long draft = articleMapper.selectCount(new LambdaQueryWrapper<Article>().eq(Article::getStatus, 0));

        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setArticleCount((published == null ? 0 : published) + (draft == null ? 0 : draft));
        vo.setPublishedCount(published == null ? 0 : published);
        vo.setDraftCount(draft == null ? 0 : draft);
        vo.setCategoryCount(categoryMapper.selectCount(null));
        vo.setTagCount(tagMapper.selectCount(null));
        vo.setCommentCount(commentMapper.selectCount(null));
        Long totalViews = articleMapper.sumViews();
        vo.setTotalViews(totalViews == null ? 0 : totalViews);
        Long todayViews = viewLogMapper.selectCount(new LambdaQueryWrapper<ViewLog>()
                .ge(ViewLog::getCreatedAt, LocalDate.now().atStartOfDay()));
        vo.setTodayViews(todayViews == null ? 0 : todayViews);
        return vo;
    }

    @Override
    public List<TrendItemVO> trend(int days) {
        int range = days <= 0 ? 7 : Math.min(days, 90);
        LocalDateTime start = LocalDate.now().minusDays(range - 1L).atStartOfDay();
        return articleMapper.trend(start);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HotArticleVO> hotArticles(int limit) {
        int size = limit <= 0 ? 10 : Math.min(limit, 50);
        String key = HOT_CACHE_KEY_PREFIX + size;

        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                log.debug("[cache] HIT hot articles size={}", size);
                return (List<HotArticleVO>) cached;
            }
        } catch (Exception e) {
            log.warn("[cache] hot articles read failed, fallback to DB: {}", e.getMessage());
        }

        List<HotArticleVO> fresh = articleMapper.hotArticles(size);

        try {
            redisTemplate.opsForValue().set(key, fresh, Duration.ofSeconds(hotTtlSeconds));
            log.debug("[cache] PUT hot articles size={}, ttl={}s", size, hotTtlSeconds);
        } catch (Exception e) {
            log.warn("[cache] hot articles write ignored: {}", e.getMessage());
        }
        return fresh;
    }

    /** 文章阅读量变化时，清除所有 size 的热门文章缓存（前缀扫描删除）。 */
    public void evictHotArticles() {
        try {
            var keys = redisTemplate.keys(HOT_CACHE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("[cache] EVICT hot articles ({} keys)", keys.size());
            }
        } catch (Exception e) {
            log.warn("[cache] hot articles evict ignored: {}", e.getMessage());
        }
    }
}
