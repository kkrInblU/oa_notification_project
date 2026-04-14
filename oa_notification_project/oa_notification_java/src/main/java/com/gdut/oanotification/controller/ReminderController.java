package com.gdut.oanotification.controller;

import com.gdut.oanotification.common.api.ResultBody;
import com.gdut.oanotification.service.ReminderService;
import com.gdut.oanotification.vo.ReadRemindersResponse;
import com.gdut.oanotification.vo.RemindersResponse;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public ResultBody<RemindersResponse> getReminders(
        @RequestParam("userEmail") String userEmail,
        @RequestParam(value = "limit", defaultValue = "20") Integer limit
    ) {
        return ResultBody.success(reminderService.getReminders(userEmail, limit));
    }

    @GetMapping("/read")
    public ResultBody<ReadRemindersResponse> markRead(
        @RequestParam("userEmail") String userEmail,
        @RequestParam(value = "deliveryIds", defaultValue = "") String deliveryIds
    ) {
        List<Long> ids = Arrays.stream(deliveryIds.split(","))
            .map(String::trim)
            .filter(item -> !item.isBlank() && item.chars().allMatch(Character::isDigit))
            .map(Long::parseLong)
            .toList();
        return ResultBody.success(reminderService.markRead(userEmail, ids));
    }
}
