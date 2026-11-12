package com.example.blog.controller.admin;

import com.example.blog.common.Result;
import com.example.blog.entity.SysConfig;
import com.example.blog.service.StatsService;
import com.example.blog.service.SysConfigService;
import com.example.blog.vo.HotArticleVO;
import com.example.blog.vo.StatsOverviewVO;
import com.example.blog.vo.TrendItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理-统计/配置")
@RestController
@RequestMapping("/admin")
public class AdminStatsController {

    private final StatsService statsService;
    private final SysConfigService sysConfigService;

    public AdminStatsController(StatsService statsService, SysConfigService sysConfigService) {
        this.statsService = statsService;
        this.sysConfigService = sysConfigService;
    }

    @Operation(summary = "概览统计")
    @GetMapping("/stats/overview")
    public Result<StatsOverviewVO> overview() {
        return Result.success(statsService.overview());
    }

    @Operation(summary = "趋势数据")
    @GetMapping("/stats/trend")
    public Result<List<TrendItemVO>> trend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statsService.trend(days));
    }

    @Operation(summary = "热门文章 Top N")
    @GetMapping("/stats/hot-articles")
    public Result<List<HotArticleVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(statsService.hotArticles(limit));
    }

    @Operation(summary = "全部配置")
    @GetMapping("/config")
    public Result<List<SysConfig>> config() {
        return Result.success(sysConfigService.listAll());
    }

    @Operation(summary = "批量更新配置")
    @PutMapping("/config")
    public Result<Void> updateConfig(@RequestBody Map<String, String> items) {
        sysConfigService.updateBatch(items);
        return Result.success();
    }
}
