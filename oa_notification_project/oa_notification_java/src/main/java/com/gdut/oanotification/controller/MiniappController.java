package com.gdut.oanotification.controller;

import com.gdut.oanotification.common.api.ResultBody;
import com.gdut.oanotification.dto.request.MiniappSendTestRequest;
import com.gdut.oanotification.dto.request.MiniappSessionRequest;
import com.gdut.oanotification.service.MiniappService;
import com.gdut.oanotification.vo.MiniappSendTestResponse;
import com.gdut.oanotification.vo.MiniappSubscribeStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/miniapp")
@RequiredArgsConstructor
public class MiniappController {

    private final MiniappService miniappService;

    @GetMapping("/subscribe/status")
    public ResultBody<MiniappSubscribeStatusResponse> getSubscribeStatus(@RequestParam("userEmail") String userEmail) {
        return ResultBody.success(miniappService.getSubscribeStatus(userEmail));
    }

    @PostMapping("/session")
    public ResultBody<MiniappSubscribeStatusResponse> bindSession(@Valid @RequestBody MiniappSessionRequest request) {
        return ResultBody.success(miniappService.bindSession(request));
    }

    @PostMapping("/send-test")
    public ResultBody<MiniappSendTestResponse> sendTest(@RequestBody MiniappSendTestRequest request) {
        return ResultBody.success(miniappService.sendTest(request));
    }
}
