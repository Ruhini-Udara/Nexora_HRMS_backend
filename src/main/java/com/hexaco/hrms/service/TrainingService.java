package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.TrainingEventDto;
import com.hexaco.hrms.dto.TrainingFeedbackDto;
import com.hexaco.hrms.dto.TrainingRequestDto;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.TrainingEvent;
import com.hexaco.hrms.models.TrainingFeedback;
import com.hexaco.hrms.models.TrainingRequest;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.TrainingEventRepository;
import com.hexaco.hrms.repository.TrainingFeedbackRepository;
import com.hexaco.hrms.repository.TrainingRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingService {

    @Autowired
    private TrainingEventRepository trainingEventRepository;

    @Autowired
    private TrainingRequestRepository trainingRequestRepository;

    @Autowired
    private TrainingFeedbackRepository trainingFeedbackRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // --- Training Events ---

    public TrainingEventDto createTrainingEvent(TrainingEventDto dto) {
        TrainingEvent event = TrainingEvent.builder()
                .title(dto.getTitle())
                .category(dto.getCategory())
                .expectedParticipants(dto.getExpectedParticipants())
                .description(dto.getDescription())
                .proposedStartDate(dto.getProposedStartDate())
                .applyBefore(dto.getApplyBefore())
                .location(dto.getLocation())
                .budget(dto.getBudget())
                .instructor(dto.getInstructor())
                .status(dto.getStatus())
                .build();

        event = trainingEventRepository.save(event);
        return mapToDto(event);
    }

    public List<TrainingEventDto> getAllTrainingEvents() {
        return trainingEventRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TrainingEventDto getTrainingEventById(Long id) {
        TrainingEvent event = trainingEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training Event not found"));
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

    public List<TrainingRequestDto> getRequestsByEvent(Long eventId) {
        return trainingRequestRepository.findByTrainingEventId(eventId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<TrainingRequestDto> getRequestsByEmployee(Long employeeId) {
        return trainingRequestRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TrainingRequestDto updateRequestStatus(Long requestId, String status, String rejectionReason) {
        TrainingRequest request = trainingRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Training Request not found"));

        request.setStatus(status);
        if ("Rejected".equalsIgnoreCase(status)) {
            request.setRejectionReason(rejectionReason);
        }

        request = trainingRequestRepository.save(request);
        return mapToDto(request);
    }

    // --- Training Feedback ---

    public TrainingFeedbackDto submitFeedback(TrainingFeedbackDto dto) {
        TrainingEvent event = trainingEventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Training Event not found"));
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        TrainingFeedback feedback = TrainingFeedback.builder()
                .trainingEvent(event)
                .employee(employee)
                .attendanceStatus(dto.getAttendanceStatus())
                .feedback(dto.getFeedback())
                .courseContentRating(dto.getCourseContentRating())
                .instructorRating(dto.getInstructorRating())
                .overallExperienceRating(dto.getOverallExperienceRating())
                .suggestions(dto.getSuggestions())
                .build();

        feedback = trainingFeedbackRepository.save(feedback);
        return mapToDto(feedback);
    }

    public List<TrainingFeedbackDto> getFeedbackByEvent(Long eventId) {
        return trainingFeedbackRepository.findByTrainingEventId(eventId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // --- Mappers ---

    private TrainingEventDto mapToDto(TrainingEvent event) {
        return TrainingEventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .category(event.getCategory())
                .expectedParticipants(event.getExpectedParticipants())
                .description(event.getDescription())
                .proposedStartDate(event.getProposedStartDate())
                .applyBefore(event.getApplyBefore())
                .location(event.getLocation())
                .budget(event.getBudget())
                .instructor(event.getInstructor())
                .status(event.getStatus())
                .build();
    }

    private TrainingRequestDto mapToDto(TrainingRequest request) {
        return TrainingRequestDto.builder()
                .id(request.getId())
                .eventId(request.getTrainingEvent().getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getFullName() + " " + request.getEmployee().getSurname())
                .epfNumber(request.getEmployee().getEpfNumber())
                .department(request.getEmployee().getDepartment())
                .designation(request.getEmployee().getDesignation())
                .workEmail(request.getEmployee().getEmail())
                .dateSubmitted(request.getDateSubmitted())
                .status(request.getStatus())
                .justification(request.getJustification())
                .rejectionReason(request.getRejectionReason())
                .attachmentPath(request.getAttachmentPath())
                .build();
    }

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
}
