package com.gdut.oanotification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdut.oanotification.common.exception.BizException;
import com.gdut.oanotification.config.OaProperties;
import com.gdut.oanotification.dto.request.UpdateCrawlerConfigRequest;
import com.gdut.oanotification.entity.CrawlJobLog;
import com.gdut.oanotification.entity.CrawlerRuntimeConfig;
import com.gdut.oanotification.mapper.CrawlJobLogMapper;
import com.gdut.oanotification.mapper.CrawlerRuntimeConfigMapper;
import com.gdut.oanotification.service.AdminCrawlerService;
import com.gdut.oanotification.util.TimeFormatUtils;
import com.gdut.oanotification.vo.AdminCrawlerConfigResponse;
import com.gdut.oanotification.vo.AdminCrawlerRunResponse;
import com.gdut.oanotification.vo.AdminCrawlerRunState;
import com.gdut.oanotification.vo.CrawlJobDetailResponse;
import com.gdut.oanotification.vo.CrawlJobItemResponse;
import com.gdut.oanotification.vo.CrawlJobListResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCrawlerServiceImpl implements AdminCrawlerService {

    private final CrawlerRuntimeConfigMapper crawlerRuntimeConfigMapper;
    private final CrawlJobLogMapper crawlJobLogMapper;
    private final OaProperties oaProperties;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile String lastStartedAt = "";
    private volatile String lastFinishedAt = "";
    private volatile String lastStatus = "";
    private volatile String lastMessage = "";

    @Override
    public AdminCrawlerConfigResponse getCrawlerConfig() {
        Map<String, CrawlerRuntimeConfig> configMap = loadRuntimeConfigMap();
        return toConfigResponse(configMap, true);
    }

    @Override
    @Transactional
    public AdminCrawlerConfigResponse updateCrawlerConfig(UpdateCrawlerConfigRequest request) {
        validateCrawlerConfigRequest(request);
        upsertConfig("SCHEDULER_ENABLED", request.getSchedulerEnabled(), "bool");
        upsertConfig("SCHEDULER_INTERVAL_MINUTES", request.getSchedulerIntervalMinutes(), "float");
        upsertConfig("SCHEDULER_MAX_RUNS", request.getSchedulerMaxRuns(), "int");
        upsertConfig("MAX_RECORDS", request.getMaxRecords(), "int");
        upsertConfig("REQUEST_DELAY_MIN", request.getRequestDelayMin(), "float");
        upsertConfig("REQUEST_DELAY_MAX", request.getRequestDelayMax(), "float");
        return toConfigResponse(loadRuntimeConfigMap(), false);
    }

    @Override
    public CrawlJobListResponse getCrawlerJobs(int limit, int page) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        int safePage = Math.max(page, 1);
        Page<CrawlJobLog> result = crawlJobLogMapper.selectPage(
            new Page<>(safePage, safeLimit),
            new LambdaQueryWrapper<CrawlJobLog>()
                .orderByDesc(CrawlJobLog::getStartedAt)
                .orderByDesc(CrawlJobLog::getId)
        );
        List<CrawlJobItemResponse> items = result.getRecords().stream().map(this::toJobItem).toList();
        return CrawlJobListResponse.builder()
            .items(items)
            .page(safePage)
            .pageSize(safeLimit)
            .totalCount(Math.toIntExact(result.getTotal()))
            .hasMore((safePage - 1L) * safeLimit + items.size() < result.getTotal())
            .runState(buildRunState())
            .build();
    }

    @Override
    public CrawlJobDetailResponse getCrawlerJobDetail(long jobId) {
        CrawlJobLog row = crawlJobLogMapper.selectById(jobId);
        if (row == null) {
            throw BizException.notFound("job not found");
        }
        return CrawlJobDetailResponse.builder()
            .id(row.getId())
            .jobType(defaultString(row.getJobType()))
            .triggerMode(defaultString(row.getTriggerMode()))
            .status(defaultString(row.getStatus()))
            .incrementalMode(defaultInt(row.getIncrementalMode()))
            .schedulerEnabled(row.getSchedulerEnabled() != null && row.getSchedulerEnabled() == 1)
            .intervalHours(row.getIntervalHours())
            .startedAt(TimeFormatUtils.formatMinute(row.getStartedAt()))
            .finishedAt(TimeFormatUtils.formatMinute(row.getFinishedAt()))
            .durationSeconds(defaultInt(row.getDurationSeconds()))
            .notificationsCount(defaultInt(row.getNotificationsCount()))
            .attachmentsCount(defaultInt(row.getAttachmentsCount()))
            .dbNotificationsCount(defaultInt(row.getDbNotificationsCount()))
            .dbAttachmentsCount(defaultInt(row.getDbAttachmentsCount()))
            .message(defaultString(row.getMessage()))
            .errorMessage(defaultString(row.getErrorMessage()))
            .createdAt(TimeFormatUtils.formatMinute(row.getCreatedAt()))
            .updatedAt(TimeFormatUtils.formatMinute(row.getUpdatedAt()))
            .build();
    }

    @Override
    public AdminCrawlerRunResponse triggerCrawlerRun() {
        if (!running.compareAndSet(false, true)) {
            throw new BizException(409, "crawler run already in progress");
        }
        lastStartedAt = formatSecond(LocalDateTime.now());
        lastFinishedAt = "";
        lastStatus = "running";
        lastMessage = "Crawler run started from admin API";
        executor.submit(this::runCrawlerProcess);
        return AdminCrawlerRunResponse.builder()
            .started(true)
            .runState(buildRunState())
            .build();
    }

    private void runCrawlerProcess() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                oaProperties.getCrawler().getPythonCommand(),
                oaProperties.getCrawler().getScriptPath()
            );
            if (oaProperties.getCrawler().getWorkingDirectory() != null) {
                processBuilder.directory(new File(oaProperties.getCrawler().getWorkingDirectory()));
            }
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                    log.info("[crawler-process] {}", line);
                }
            }
            int exitCode = process.waitFor();
            lastFinishedAt = formatSecond(LocalDateTime.now());
            lastStatus = exitCode == 0 ? "success" : "failed";
            lastMessage = exitCode == 0 ? "Crawler run completed successfully" : trimMessage(output.toString());
        } catch (Exception ex) {
            log.error("Admin crawler run failed", ex);
            lastFinishedAt = formatSecond(LocalDateTime.now());
            lastStatus = "failed";
            lastMessage = ex.getMessage() == null ? "crawler run failed" : ex.getMessage();
        } finally {
            running.set(false);
        }
    }

    private Map<String, CrawlerRuntimeConfig> loadRuntimeConfigMap() {
        return crawlerRuntimeConfigMapper.selectList(
            new LambdaQueryWrapper<CrawlerRuntimeConfig>().orderByAsc(CrawlerRuntimeConfig::getConfigKey)
        ).stream().collect(Collectors.toMap(CrawlerRuntimeConfig::getConfigKey, item -> item));
    }

    private AdminCrawlerConfigResponse toConfigResponse(Map<String, CrawlerRuntimeConfig> configMap, boolean includeUpdatedKeys) {
        List<String> updatedKeys = configMap.keySet().stream().filter(Objects::nonNull).sorted(Comparator.naturalOrder()).toList();
        return AdminCrawlerConfigResponse.builder()
            .schedulerEnabled(parseBoolean(configMap.get("SCHEDULER_ENABLED")))
            .schedulerIntervalMinutes(parseDouble(configMap.get("SCHEDULER_INTERVAL_MINUTES")))
            .schedulerMaxRuns(parseInteger(configMap.get("SCHEDULER_MAX_RUNS")))
            .maxRecords(parseInteger(configMap.get("MAX_RECORDS")))
            .requestDelayMin(parseDouble(configMap.get("REQUEST_DELAY_MIN")))
            .requestDelayMax(parseDouble(configMap.get("REQUEST_DELAY_MAX")))
            .updatedKeys(includeUpdatedKeys ? updatedKeys : null)
            .build();
    }

    private void validateCrawlerConfigRequest(UpdateCrawlerConfigRequest request) {
        if (request.getSchedulerIntervalMinutes() != null && request.getSchedulerIntervalMinutes() <= 0) {
            throw BizException.badRequest("schedulerIntervalMinutes must be greater than 0");
        }
        if (request.getMaxRecords() != null && request.getMaxRecords() <= 0) {
            throw BizException.badRequest("maxRecords must be greater than 0");
        }
        if (request.getRequestDelayMin() != null && request.getRequestDelayMin() < 0) {
            throw BizException.badRequest("requestDelayMin must be greater than or equal to 0");
        }
        if (request.getRequestDelayMax() != null && request.getRequestDelayMax() < 0) {
            throw BizException.badRequest("requestDelayMax must be greater than or equal to 0");
        }
        if (request.getRequestDelayMin() != null
            && request.getRequestDelayMax() != null
            && request.getRequestDelayMin() > request.getRequestDelayMax()) {
            throw BizException.badRequest("requestDelayMin cannot be greater than requestDelayMax");
        }
    }

    private void upsertConfig(String key, Object value, String type) {
        if (value == null) {
            return;
        }
        CrawlerRuntimeConfig existing = crawlerRuntimeConfigMapper.selectById(key);
        if (existing == null) {
            existing = new CrawlerRuntimeConfig();
            existing.setConfigKey(key);
            existing.setConfigType(type);
            existing.setDescription("");
            existing.setConfigValue(normalizeConfigValue(value));
            crawlerRuntimeConfigMapper.insert(existing);
            return;
        }
        existing.setConfigValue(normalizeConfigValue(value));
        if (existing.getConfigType() == null) {
            existing.setConfigType(type);
        }
        crawlerRuntimeConfigMapper.updateById(existing);
    }

    private String normalizeConfigValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue ? "1" : "0";
        }
        return String.valueOf(value);
    }

    private Boolean parseBoolean(CrawlerRuntimeConfig item) {
        if (item == null || item.getConfigValue() == null) {
            return false;
        }
        String value = item.getConfigValue().trim();
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    private Integer parseInteger(CrawlerRuntimeConfig item) {
        if (item == null || item.getConfigValue() == null || item.getConfigValue().isBlank()) {
            return 0;
        }
        return (int) Double.parseDouble(item.getConfigValue());
    }

    private Double parseDouble(CrawlerRuntimeConfig item) {
        if (item == null || item.getConfigValue() == null || item.getConfigValue().isBlank()) {
            return 0D;
        }
        return Double.parseDouble(item.getConfigValue());
    }

    private CrawlJobItemResponse toJobItem(CrawlJobLog row) {
        return CrawlJobItemResponse.builder()
            .id(row.getId())
            .jobType(defaultString(row.getJobType()))
            .triggerMode(defaultString(row.getTriggerMode()))
            .status(defaultString(row.getStatus()))
            .startedAt(TimeFormatUtils.formatMinute(row.getStartedAt()))
            .finishedAt(TimeFormatUtils.formatMinute(row.getFinishedAt()))
            .durationSeconds(defaultInt(row.getDurationSeconds()))
            .notificationsCount(defaultInt(row.getNotificationsCount()))
            .attachmentsCount(defaultInt(row.getAttachmentsCount()))
            .dbNotificationsCount(defaultInt(row.getDbNotificationsCount()))
            .dbAttachmentsCount(defaultInt(row.getDbAttachmentsCount()))
            .message(defaultString(row.getMessage()))
            .errorMessage(defaultString(row.getErrorMessage()))
            .build();
    }

    private AdminCrawlerRunState buildRunState() {
        return AdminCrawlerRunState.builder()
            .running(running.get())
            .lastStartedAt(lastStartedAt)
            .lastFinishedAt(lastFinishedAt)
            .lastStatus(lastStatus)
            .lastMessage(lastMessage)
            .build();
    }

    private String formatSecond(LocalDateTime time) {
        return time == null ? "" : time.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String trimMessage(String message) {
        if (message == null || message.isBlank()) {
            return "crawler run failed";
        }
        String normalized = message.trim();
        return normalized.length() > 500 ? normalized.substring(0, 500) : normalized;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
