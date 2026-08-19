package com.hexaco.hrms.rest;

import com.hexaco.hrms.service.GoogleCalendarService;
import com.hexaco.hrms.service.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(GoogleCalendarService.class)
public class CalendarController {

    private final Optional<GoogleCalendarService> googleCalendarService;
    private final NotificationService notificationService;

    @PostMapping("/event")
    public ResponseEntity<String> createEvent(@RequestBody EventRequest request) {
        if (googleCalendarService.isEmpty()) {
            log.warn("Google Calendar integration is disabled.");
            return ResponseEntity.internalServerError().body("Google Calendar Service not available");
        }

        log.info("Received request to create event on Google Calendar: {}", request);

        try {
            // Parse date (MM/dd/yyyy) and time (HH:mm)
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate date = LocalDate.parse(request.getEventDate(), dateFormatter);
            LocalTime time = LocalTime.parse(request.getEventTime());
            LocalDateTime start = LocalDateTime.of(date, time);
            LocalDateTime end = start.plusHours(1); // Default to 1 hour event

            // Single recipient for testing, as requested
            List<String> emails = List.of("bandarakasun495@gmail.com");

            googleCalendarService.get().createEvent(
                    request.getEventName(),
                    request.getDescription(),
                    start,
                    end,
                    emails
            );

            // Send custom email notification to bypass the 403 Forbidden constraint
            notificationService.sendCompanyEventNotification(
                    "bandarakasun495@gmail.com",
                    request.getEventName(),
                    request.getDescription(),
                    request.getEventDate(),
                    request.getEventTime(),
                    request.getEventType()
            );

            return ResponseEntity.ok("Event created and invitation sent to bandarakasun495@gmail.com successfully.");
        } catch (Exception e) {
            log.error("Error creating Google Calendar event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/events")
    public ResponseEntity<List<com.hexaco.hrms.dto.CalendarEventDto>> getEvents() {
        if (googleCalendarService.isEmpty()) {
            log.warn("Google Calendar integration is disabled.");
            return ResponseEntity.internalServerError().build();
        }

        log.info("Received request to fetch all company events/holidays from Google Calendar");
        try {
            List<com.hexaco.hrms.dto.CalendarEventDto> events = googleCalendarService.get().getEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error fetching Google Calendar events: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/holiday")
    public ResponseEntity<String> createHoliday(@RequestBody HolidayRequest request) {
        if (googleCalendarService.isEmpty()) {
            log.warn("Google Calendar integration is disabled.");
            return ResponseEntity.internalServerError().body("Google Calendar Service not available");
        }

        log.info("Received request to create holiday on Google Calendar: {}", request);

        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate startDate = LocalDate.parse(request.getStartDate(), dateFormatter);
            LocalDate endDate = LocalDate.parse(request.getEndDate(), dateFormatter);
            
            // Full day event
            LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIDNIGHT);
            LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);

            // Single recipient for testing, as requested
            List<String> emails = List.of("bandarakasun495@gmail.com");

            googleCalendarService.get().createEvent(
                    request.getHolidayName() + " (" + request.getHolidayType() + ")",
                    request.getDescription(),
                    start,
                    end,
                    emails
            );

            // Send custom email notification to bypass the 403 Forbidden constraint
            notificationService.sendCompanyEventNotification(
                    "bandarakasun495@gmail.com",
                    request.getHolidayName(),
                    request.getDescription(),
                    request.getStartDate() + " to " + request.getEndDate(),
                    "All Day",
                    request.getHolidayType()
            );

            return ResponseEntity.ok("Holiday created and invitation sent to bandarakasun495@gmail.com successfully.");
        } catch (Exception e) {
            log.error("Error creating Google Calendar holiday event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @Data
    public static class EventRequest {
        private String eventName;
        private String eventDate;
        private String eventTime;
        private String eventType;
        private String description;
    }

    @Data
    public static class HolidayRequest {
        private String holidayName;
        private String startDate;
        private String endDate;
        private String holidayType;
        private boolean repeatYearly;
        private String description;
    }
}
