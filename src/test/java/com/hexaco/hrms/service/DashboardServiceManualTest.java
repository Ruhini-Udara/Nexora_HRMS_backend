package com.hexaco.hrms.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DashboardServiceManualTest {

    @Autowired
    private DashboardService dashboardService;

    @Test
    public void testGetAnalytics() {
        try {
            System.out.println("TESTING GET ANALYTICS...");
            var result = dashboardService.getAnalytics();
            System.out.println("SUCCESS! Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
