package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.common.ErrorCode;
import com.example.blog.dto.TagSaveDTO;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.Tag;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.service.TagService;
import com.example.blog.vo.CountItemVO;
import com.example.blog.vo.TagVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleMapper articleMapper;

    public TagServiceImpl(TagMapper tagMapper, ArticleTagMapper articleTagMapper, ArticleMapper articleMapper) {
        this.tagMapper = tagMapper;
        this.articleTagMapper = articleTagMapper;
        this.articleMapper = articleMapper;
    }

    @Override
    public List<TagVO> listAll() {
        Map<Long, Long> counts = articleMapper.countByTag().stream()
                .collect(Collectors.toMap(CountItemVO::getId, CountItemVO::getCnt));
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getId))
                .stream().map(t -> {
                    TagVO vo = new TagVO();
                    vo.setId(t.getId());
                    vo.setName(t.getName());
                    vo.setColor(t.getColor());
                    vo.setArticleCount(counts.getOrDefault(t.getId(), 0L));
                    vo.setCreatedAt(t.getCreatedAt());
                    return vo;
                }).toList();
    }

    @Override
    public TagVO create(TagSaveDTO dto) {
        checkName(dto.getName(), null);
        Tag tag = new Tag();
        tag.setName(dto.getName().trim());
        tag.setColor(dto.getColor());
        tagMapper.insert(tag);

        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setColor(tag.getColor());
        vo.setArticleCount(0L);
        return vo;
    }

    @Override
    public void update(Long id, TagSaveDTO dto) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }
        checkName(dto.getName(), id);
        tag.setName(dto.getName().trim());
        tag.setColor(dto.getColor());
        tagMapper.updateById(tag);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, id));
        tagMapper.deleteById(id);
    }

    private void checkName(String name, Long excludeId) {
        Long count = tagMapper.selectCount(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, name.trim())
                .ne(excludeId != null, Tag::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "标签已存在：" + name);
        }
    }
}
