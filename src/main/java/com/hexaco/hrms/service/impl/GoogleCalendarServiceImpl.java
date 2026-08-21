package com.hexaco.hrms.service.impl;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.hexaco.hrms.service.GoogleCalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(Calendar.class)
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

    @Autowired(required = false)
    private Calendar googleCalendar;

    @Value("${google.calendar.id:primary}")
    private String calendarId;

    @Override
    public void createEvent(String title, String description, LocalDateTime start, LocalDateTime end, List<String> attendeeEmails) {
        if (googleCalendar == null) {
            log.warn("Google Calendar is not configured. Skipping event creation for: {}", title);
            return;
        }
        try {
            Event event = new Event()
                    .setSummary(title)
                    .setDescription(description);

            // Convert LocalDateTime to java.util.Date then to Google DateTime
            Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
            DateTime googleStart = new DateTime(startDate);
            event.setStart(new EventDateTime().setDateTime(googleStart).setTimeZone(ZoneId.systemDefault().getId()));

            Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
            DateTime googleEnd = new DateTime(endDate);
            event.setEnd(new EventDateTime().setDateTime(googleEnd).setTimeZone(ZoneId.systemDefault().getId()));

            Calendar.Events.Insert insert = googleCalendar.events().insert(calendarId, event);
            Event result = insert.execute();
            log.info("✅ Google Calendar event created successfully: {}", result.getHtmlLink());
        } catch (IOException e) {
            log.error("❌ Failed to create Google Calendar event: {}", e.getMessage(), e);
            throw new RuntimeException("Google Calendar API error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<com.hexaco.hrms.dto.CalendarEventDto> getEvents() {
        if (googleCalendar == null) {
            log.warn("Google Calendar is not configured. Returning empty event list.");
            return java.util.List.of();
        }
        try {
            Calendar.Events.List request = googleCalendar.events().list(calendarId);
            List<Event> googleEvents = request.execute().getItems();
            if (googleEvents == null) {
                return java.util.List.of();
            }

            return googleEvents.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("❌ Failed to list Google Calendar events: {}", e.getMessage(), e);
            throw new RuntimeException("Google Calendar API error: " + e.getMessage(), e);
        }
    }

    private com.hexaco.hrms.dto.CalendarEventDto mapToDto(Event event) {
        String dateStr = "";
        String timeStr = null;

        if (event.getStart() != null) {
            if (event.getStart().getDateTime() != null) {
                // Timed event
                java.time.Instant instant = java.time.Instant.ofEpochMilli(event.getStart().getDateTime().getValue());
                java.time.LocalDateTime localDateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
                dateStr = localDateTime.toLocalDate().toString();
                timeStr = localDateTime.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
            } else if (event.getStart().getDate() != null) {
                // Full day event
                dateStr = event.getStart().getDate().toStringRfc3339().substring(0, 10);
            }
        }

        return com.hexaco.hrms.dto.CalendarEventDto.builder()
                .id(event.getId())
                .title(event.getSummary())
                .date(dateStr)
                .time(timeStr)
                .build();
    }
}
