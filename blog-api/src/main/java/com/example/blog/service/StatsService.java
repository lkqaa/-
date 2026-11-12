package com.example.blog.service;

import com.example.blog.vo.HotArticleVO;
import com.example.blog.vo.StatsOverviewVO;
import com.example.blog.vo.TrendItemVO;

import java.util.List;

public interface StatsService {

    StatsOverviewVO overview();

    List<TrendItemVO> trend(int days);

    List<HotArticleVO> hotArticles(int limit);
}
