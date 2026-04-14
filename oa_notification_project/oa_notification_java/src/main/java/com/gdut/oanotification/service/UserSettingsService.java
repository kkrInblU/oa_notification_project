package com.gdut.oanotification.service;

import com.gdut.oanotification.dto.request.UpdateUserSettingsRequest;
import com.gdut.oanotification.vo.UserSettingsResponse;

public interface UserSettingsService {

    UserSettingsResponse getUserSettings(String userEmail);

    UserSettingsResponse updateUserSettings(UpdateUserSettingsRequest request);
}
