package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.TrainingEventDto;
import com.hexaco.hrms.dto.TrainingFeedbackDto;
import com.hexaco.hrms.dto.TrainingRequestDto;
import com.hexaco.hrms.models.Approval;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.TrainingEvent;
import com.hexaco.hrms.models.TrainingFeedback;
import com.hexaco.hrms.models.TrainingRequest;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.TrainingEventRepository;
import com.hexaco.hrms.repository.TrainingFeedbackRepository;
import com.hexaco.hrms.repository.TrainingRequestRepository;
import com.hexaco.hrms.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class TrainingService {

    @Autowired
    private TrainingEventRepository trainingEventRepository;

    @Autowired
    private TrainingRequestRepository trainingRequestRepository;

    @Autowired
    private TrainingFeedbackRepository trainingFeedbackRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private NotificationService notificationService;

    // --- Training Events ---

    public TrainingEventDto createTrainingEvent(TrainingEventDto dto) {
        // Check for duplicates (same title only - case insensitive)
        if (trainingEventRepository.existsByTitleIgnoreCase(dto.getTitle())) {
            throw new RuntimeException("A training event with this title already exists.");
        }

        //Build TrainingEventDto from TrainingEvent
        TrainingEvent event = TrainingEvent.builder()
                .title(dto.getTitle())
                .category(dto.getCategory())
                .expectedParticipants(dto.getExpectedParticipants())
                .description(dto.getDescription())
                .proposedStartDate(dto.getProposedStartDate())
                .time(dto.getTime())
                .applyBefore(dto.getApplyBefore())
                .location(dto.getLocation())
                .budget(dto.getBudget())
                .instructor(dto.getInstructor())
                .status(dto.getStatus())
                .build();

        event = trainingEventRepository.save(event);
        return mapToDto(event);
    }

    //Fetch all training events
    public List<TrainingEventDto> getAllTrainingEvents() {
        return trainingEventRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    //Fetch training event by ID
    public TrainingEventDto getTrainingEventById(Long id) {
        TrainingEvent event = trainingEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training Event not found"));
        return mapToDto(event);
    }

    //Update TrainingEvent
    public TrainingEventDto updateTrainingEvent(Long id, TrainingEventDto dto) {
        TrainingEvent event = trainingEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training Event not found"));

        // Update event details
        String oldStatus = event.getStatus();

        //Update event details
        event.setTitle(dto.getTitle());
        event.setCategory(dto.getCategory());
        event.setExpectedParticipants(dto.getExpectedParticipants());
        event.setDescription(dto.getDescription());
        event.setProposedStartDate(dto.getProposedStartDate());
        event.setTime(dto.getTime());
        event.setApplyBefore(dto.getApplyBefore());
        event.setLocation(dto.getLocation());
        event.setBudget(dto.getBudget());
        event.setInstructor(dto.getInstructor());
        if (dto.getStatus() != null) {
            event.setStatus(dto.getStatus());
        }
        event.setRejectionReason(dto.getReason());

        event = trainingEventRepository.save(event);

        // If status changed to 'Approved' (Confirmed and Send in frontend)
        if ("Approved".equalsIgnoreCase(event.getStatus()) && !"Approved".equalsIgnoreCase(oldStatus)) {
            event.setApprovedBy(dto.getApprovedBy());
            event.setApprovedAt(LocalDateTime.now());
            event = trainingEventRepository.save(event);
            
            final TrainingEvent finalizedEvent = event;
            List<TrainingRequest> requests = trainingRequestRepository.findByTrainingEventId(event.getId());

            for (TrainingRequest req : requests) {
                // Skip rejected requests
                if ("Rejected".equalsIgnoreCase(req.getStatus())) {
                    continue;
                }

                // Auto-approve pending requests when the final list is confirmed by Admin
                if ("Pending".equalsIgnoreCase(req.getStatus())) {
                    req.setStatus("Approved");
                    trainingRequestRepository.save(req);
                }

                // Send email to all candidates (now all are 'Approved')
                notificationService.sendTrainingFinalizedNotification(
                        req.getEmployee().getFullName(),
                        req.getEmployee().getEmail(),
                        finalizedEvent.getTitle(),
                        finalizedEvent.getProposedStartDate() != null ? finalizedEvent.getProposedStartDate().toString() : "TBD",
                        formatTime(finalizedEvent.getTime()),
                        finalizedEvent.getLocation(),
                        finalizedEvent.getInstructor()
                );
            }
        }

        return mapToDto(event);
    }

    // --- Training Requests ---

    public TrainingRequestDto applyForTraining(TrainingRequestDto dto) {
        TrainingEvent event = trainingEventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Training Event not found"));
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        TrainingRequest request = TrainingRequest.builder()
                .trainingEvent(event)
                .employee(employee)
                .justification(dto.getJustification())
                .attachmentPath(dto.getAttachmentPath())
                .build();

        request = trainingRequestRepository.save(request);
        return mapToDto(request);
    }

    //Fetch training requests by event
    public List<TrainingRequestDto> getRequestsByEvent(Long eventId) {
        return trainingRequestRepository.findByTrainingEventId(eventId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    //Fetch training requests by employee
    public List<TrainingRequestDto> getRequestsByEmployee(Long employeeId) {
        return trainingRequestRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    //Update training request status
    public TrainingRequestDto updateRequestStatus(Long requestId, String status, String rejectionReason, Long approverId) {
        TrainingRequest request = trainingRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Training Request not found"));

        request.setStatus(status);
        if ("Rejected".equalsIgnoreCase(status)) {
            request.setRejectionReason(rejectionReason);
        }

        request = trainingRequestRepository.save(request);

        // Create an audit record in the approval table
        if (approverId != null) {
            Employee approver = employeeRepository.findById(approverId)
                    .orElseThrow(() -> new RuntimeException("Approver not found"));

            Approval approval = Approval.builder()
                    .refId(requestId)
                    .refType("TRAINING_REQUEST")
                    .approvedBy(approver)
                    .decision(status.toUpperCase())
                    .remark("Rejected".equalsIgnoreCase(status) ? rejectionReason : "Approved by HR")
                    .build();

            approvalService.saveApproval(approval);
        }

        return mapToDto(request);
    }

    // --- Training Feedback ---

    // Submit feedback
    public TrainingFeedbackDto submitFeedback(TrainingFeedbackDto dto) {
        TrainingEvent event = trainingEventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Training Event not found"));
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        TrainingFeedback feedback = trainingFeedbackRepository
                .findByTrainingEventIdAndEmployeeId(dto.getEventId(), dto.getEmployeeId())
                .orElse(new TrainingFeedback());

        feedback.setTrainingEvent(event);
        feedback.setEmployee(employee);
        
        if (dto.getAttendanceStatus() != null) {
            feedback.setAttendanceStatus(dto.getAttendanceStatus());
        }
        if (dto.getFeedback() != null) {
            feedback.setFeedback(dto.getFeedback());
        }
        if (dto.getCourseContentRating() != null) {
            feedback.setCourseContentRating(dto.getCourseContentRating());
        }
        if (dto.getInstructorRating() != null) {
            feedback.setInstructorRating(dto.getInstructorRating());
        }
        if (dto.getOverallExperienceRating() != null) {
            feedback.setOverallExperienceRating(dto.getOverallExperienceRating());
        }
        if (dto.getSuggestions() != null) {
            feedback.setSuggestions(dto.getSuggestions());
        }

        feedback = trainingFeedbackRepository.save(feedback);
        return mapToDto(feedback);
    }

    // Fetch feedback by event
    public List<TrainingFeedbackDto> getFeedbackByEvent(Long eventId) {
        return trainingFeedbackRepository.findByTrainingEventId(eventId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Check if training event title already exists
    public boolean existsByTitle(String title) {
        System.out.println("Service: Checking existence for title: [" + title + "]");
        boolean exists = trainingEventRepository.existsByTitleIgnoreCase(title.trim());
        System.out.println("Service: Database match found? " + exists);
        return exists;
    }

    // --- Mappers ---

    // Map TrainingEvent to TrainingEventDto
    private TrainingEventDto mapToDto(TrainingEvent event) {
        return TrainingEventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .category(event.getCategory())
                .expectedParticipants(event.getExpectedParticipants())
                .description(event.getDescription())
                .proposedStartDate(event.getProposedStartDate())
                .time(event.getTime())
                .applyBefore(event.getApplyBefore())
                .location(event.getLocation())
                .budget(event.getBudget())
                .instructor(event.getInstructor())
                .status(event.getStatus())
                .approvedBy(event.getApprovedBy())
                .approvedAt(event.getApprovedAt() != null ? event.getApprovedAt().toString() : null)
                .reason(event.getRejectionReason())
                .build();
    }

    // Map TrainingRequest to TrainingRequestDto
    private TrainingRequestDto mapToDto(TrainingRequest request) {
        return TrainingRequestDto.builder()
                .id(request.getId())
                .eventId(request.getTrainingEvent().getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getFullName() + " " + request.getEmployee().getSurname())
                .epfNumber(request.getEmployee().getEpfNumber())
                .department(request.getEmployee().getDepartment())
                .designation(request.getEmployee().getDesignation() != null ? request.getEmployee().getDesignation().getDesignationName() : null)
                .personalEmail(request.getEmployee().getEmail())
                .workEmail(request.getEmployee().getEmail())
                .trainingTitle(request.getTrainingEvent().getTitle())
                .trainingCategory(request.getTrainingEvent().getCategory())
                .trainingDate(request.getTrainingEvent().getProposedStartDate())
                .trainingTime(request.getTrainingEvent().getTime())
                .dateSubmitted(request.getDateSubmitted())
                .status(request.getStatus())
                .eventStatus(request.getTrainingEvent().getStatus())
                .eventRejectionReason(request.getTrainingEvent().getRejectionReason())
                .age(request.getEmployee().getDateOfBirth() != null ? 
                    java.time.Period.between(request.getEmployee().getDateOfBirth(), LocalDate.now()).getYears() : null)
                .justification(request.getJustification())
                .rejectionReason(request.getRejectionReason())
                .attachmentPath(request.getAttachmentPath())
                .attendanceConfirmed(trainingFeedbackRepository.existsByTrainingEventIdAndEmployeeId(
                        request.getTrainingEvent().getId(), request.getEmployee().getId()))
                .build();
    }

    // Map TrainingFeedback to TrainingFeedbackDto
    private TrainingFeedbackDto mapToDto(TrainingFeedback feedback) {
        return TrainingFeedbackDto.builder()
                .id(feedback.getId())
                .eventId(feedback.getTrainingEvent().getId())
                .employeeId(feedback.getEmployee().getId())
                .employeeName(feedback.getEmployee().getFullName() + " " + feedback.getEmployee().getSurname())
                .workEmail(feedback.getEmployee().getEmail())
                .attendanceStatus(feedback.getAttendanceStatus())
                .feedback(feedback.getFeedback())
                .courseContentRating(feedback.getCourseContentRating())
                .instructorRating(feedback.getInstructorRating())
                .overallExperienceRating(feedback.getOverallExperienceRating())
                .suggestions(feedback.getSuggestions())
                .build();
    }

    // Format time to 12-hour format
    private String formatTime(String time) {
        if (time == null || time.isEmpty() || "TBD".equalsIgnoreCase(time)) {
            return "TBD";
        }
        if (time.contains("AM") || time.contains("PM") || time.contains("am") || time.contains("pm")) {
            return time;
        }
        try {
            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            String ampm = hours >= 12 ? "PM" : "AM";
            int hours12 = hours % 12;
            if (hours12 == 0) hours12 = 12;
            return String.format("%d:%02d %s", hours12, minutes, ampm);
        } catch (Exception e) {
            return time;
        }
    }
}
