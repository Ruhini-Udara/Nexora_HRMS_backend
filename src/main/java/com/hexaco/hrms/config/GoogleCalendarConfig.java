package com.hexaco.hrms.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarConfig.class);

    private final ResourceLoader resourceLoader;

    @Value("${google.calendar.credentials.path:classpath:google-credentials.json}")
    private String credentialsPath;

    public GoogleCalendarConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public Calendar googleCalendar() throws IOException, GeneralSecurityException {
        Resource resource = resourceLoader.getResource(credentialsPath);
        if (!resource.exists()) {
            logger.warn("Google credentials file not found at '{}'. Google Calendar integration will be disabled.", credentialsPath);
            return null;
        }
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream())
                    .createScoped(Collections.singleton(CalendarScopes.CALENDAR));
            return new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("Nexora HRMS")
                    .build();
        } catch (Exception e) {
            logger.warn("Failed to initialize Google Calendar: {}. Google Calendar integration will be disabled.", e.getMessage());
            return null;
        }
    }
}
