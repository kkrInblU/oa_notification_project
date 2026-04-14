package com.gdut.oanotification.service;

import com.gdut.oanotification.vo.ReadRemindersResponse;
import com.gdut.oanotification.vo.RemindersResponse;
import java.util.List;

public interface ReminderService {

    RemindersResponse getReminders(String userEmail, int limit);

    ReadRemindersResponse markRead(String userEmail, List<Long> deliveryIds);
}
