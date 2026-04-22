package com.hexaco.hrms.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ReportService {
    void exportBoardMeetingPdf(HttpServletResponse response) throws IOException;
}
