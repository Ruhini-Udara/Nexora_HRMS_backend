package com.hexaco.hrms.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.hexaco.hrms.models.OverseasLeave;
import com.hexaco.hrms.service.LeaveService;
import com.hexaco.hrms.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final LeaveService leaveService;

    @Override
    public void exportBoardMeetingPdf(HttpServletResponse response) throws IOException {
        List<OverseasLeave> pendingLeaves = leaveService.getOverseasLeavesByStatus("PENDING_DIRECTOR_REVIEW");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Board Meeting Leave Approvals", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);
        
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(10);

        writeTableHeader(table);
        writeTableData(table, pendingLeaves);

        document.add(table);
        document.close();
    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(5);
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

        cell.setPhrase(new Phrase("Leave ID", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Reason", font));
        table.addCell(cell);
        
        cell.setPhrase(new Phrase("Branch", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Total Days", font));
        table.addCell(cell);
    }

    private void writeTableData(PdfPTable table, List<OverseasLeave> leaves) {
        for (OverseasLeave leave : leaves) {
            table.addCell(leave.getId() != null ? String.valueOf(leave.getId()) : "N/A");
            table.addCell(leave.getReason() != null ? leave.getReason() : "N/A");
            table.addCell(leave.getBranch() != null ? leave.getBranch() : "N/A");
            table.addCell(leave.getTotalDays() != null ? String.valueOf(leave.getTotalDays()) : "N/A");
        }
    }
}
