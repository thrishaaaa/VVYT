package com.legal_system.mediation.Service;

import com.legal_system.mediation.repository.CasesRepository;
import com.legal_system.mediation.repository.MediatorsRepository;
import com.legal_system.mediation.repository.MeetingScheduleRepository;
import com.legal_system.mediation.model.Cases;
import com.legal_system.mediation.model.MeetingSchedule;
import com.legal_system.mediation.model.Mediators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
public class MediatorDashboardService {

    @Autowired
    private MediatorsRepository mediatorsRepository;

    @Autowired
    private CasesRepository casesRepository;

    @Autowired
    private MeetingScheduleRepository meetingScheduleRepository;

    public Mediators getMediatorById(Integer id) {
        return mediatorsRepository.findById(id).orElse(null);
    }

    public Long getTotalCases(Integer mediatorId) {
        return casesRepository.countByMediatorId(mediatorId);
    }

    public Long getActiveCases(Integer mediatorId) {
        return casesRepository.countByMediatorIdAndStatusNot(mediatorId, "Resolved");
    }

    public Long getResolvedCases(Integer mediatorId) {
        return casesRepository.countByMediatorIdAndStatus(mediatorId, "Resolved");
    }

    public Long getUpcomingMeetingsCount(Integer mediatorId) {
        return meetingScheduleRepository.countByMediatorIdAndStatusAndMeetingDateAfter(
                mediatorId, "Scheduled", LocalDate.now().minusDays(1));
    }

    public List<Cases> getMediatorCases(Integer mediatorId) {
        return casesRepository.findByMediatorId(mediatorId);
    }

    public List<MeetingSchedule> getUpcomingMeetings(Integer mediatorId) {
        return meetingScheduleRepository.findByMediatorIdAndStatusAndMeetingDateAfterOrderByMeetingDateAsc(
                mediatorId, "Scheduled", LocalDate.now().minusDays(1));
    }

    public List<MeetingSchedule> getAllMeetings(Integer mediatorId) {
        return meetingScheduleRepository.findByMediatorIdOrderByMeetingDateDesc(mediatorId);
    }

    public void updateCaseStatus(Integer caseId, String status) {
        Cases caseEntity = casesRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (status.equals("Resolved")) {
            // FIXED: Use setResolved_at instead of setResolvedAt (matches your getter/setter naming)
            caseEntity.setResolved_at(LocalDateTime.now());
        }

        caseEntity.setStatus(status);
        casesRepository.save(caseEntity);
    }

    public void createMeeting(Integer mediatorId, Integer caseId, LocalDate date, LocalTime time, String description) {
        Mediators mediator = mediatorsRepository.findById(mediatorId)
                .orElseThrow(() -> new RuntimeException("Mediator not found"));

        Cases caseEntity = casesRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // FIXED: Use Objects.equals() for null-safe Integer comparison
        if (!Objects.equals(caseEntity.getMediator().getId(), mediatorId)) {
            throw new RuntimeException("You can only schedule meetings for your own cases");
        }

        MeetingSchedule meeting = new MeetingSchedule();
        meeting.setMeetingDate(date);
        meeting.setMeetingTime(time);
        meeting.setDescription(description);
        meeting.setStatus("Scheduled");
        meeting.setMediator(mediator);
        meeting.setCaseEntity(caseEntity);
        meeting.setCreatedAt(LocalDateTime.now());

        meetingScheduleRepository.save(meeting);
    }

    public void cancelMeeting(Integer meetingId) {
        MeetingSchedule meeting = meetingScheduleRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        meeting.setStatus("Cancelled");
        meetingScheduleRepository.save(meeting);
    }
}