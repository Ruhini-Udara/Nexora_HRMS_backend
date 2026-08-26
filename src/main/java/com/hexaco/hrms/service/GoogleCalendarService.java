package com.hexaco.hrms.service;

import java.time.LocalDateTime;
import java.util.List;

public interface GoogleCalendarService {
    void createEvent(String title, String description, LocalDateTime start, LocalDateTime end, List<String> attendeeEmails);
    List<com.hexaco.hrms.dto.CalendarEventDto> getEvents();
}
