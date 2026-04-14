package com.gdut.oanotification.controller;

import com.gdut.oanotification.common.api.ResultBody;
import com.gdut.oanotification.dto.request.UpdateUserSettingsRequest;
import com.gdut.oanotification.service.UserSettingsService;
import com.gdut.oanotification.vo.UserSettingsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @GetMapping("/settings")
    public ResultBody<UserSettingsResponse> getUserSettings(@RequestParam("userEmail") String userEmail) {
        return ResultBody.success(userSettingsService.getUserSettings(userEmail));
    }

    @PostMapping("/settings")
    public ResultBody<UserSettingsResponse> updateUserSettings(@Valid @RequestBody UpdateUserSettingsRequest request) {
        return ResultBody.success(userSettingsService.updateUserSettings(request));
    }
}
