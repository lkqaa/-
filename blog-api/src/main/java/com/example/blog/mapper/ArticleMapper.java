package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Article;
import com.example.blog.vo.CountItemVO;
import com.example.blog.vo.HotArticleVO;
import com.example.blog.vo.TrendItemVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleMapper extends BaseMapper<Article> {

    @Select("SELECT category_id AS id, COUNT(*) AS cnt FROM article " +
            "WHERE deleted = 0 AND status = 1 AND category_id IS NOT NULL GROUP BY category_id")
    List<CountItemVO> countByCategory();

    @Select("SELECT at.tag_id AS id, COUNT(*) AS cnt FROM article_tag at " +
            "JOIN article a ON a.id = at.article_id AND a.deleted = 0 AND a.status = 1 " +
            "GROUP BY at.tag_id")
    List<CountItemVO> countByTag();

    @Select("SELECT COALESCE(SUM(views), 0) FROM article WHERE deleted = 0")
    Long sumViews();

    @Update("UPDATE article SET views = views + 1 WHERE id = #{id} AND deleted = 0")
    int incrViews(@Param("id") Long id);

    @Update("UPDATE article SET comment_count = " +
            "(SELECT COUNT(*) FROM comment c WHERE c.article_id = #{articleId} AND c.status = 1) " +
            "WHERE id = #{articleId}")
    int refreshCommentCount(@Param("articleId") Long articleId);

    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS date, " +
            "COUNT(*) AS article_count, COALESCE(SUM(views), 0) AS views " +
            "FROM article WHERE deleted = 0 AND created_at >= #{start} " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d') ORDER BY date")
    List<TrendItemVO> trend(@Param("start") LocalDateTime start);

    @Select("SELECT id, title, views FROM article " +
            "WHERE deleted = 0 AND status = 1 ORDER BY views DESC LIMIT #{limit}")
    List<HotArticleVO> hotArticles(@Param("limit") int limit);
}
