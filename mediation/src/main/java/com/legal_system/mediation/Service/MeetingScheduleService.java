package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.MeetingSchedule;
import com.legal_system.mediation.repository.MeetingScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MeetingScheduleService {

    @Autowired
    private MeetingScheduleRepository meetingScheduleRepository;

    // Add a new meeting
    public void addMeeting(MeetingSchedule meetingSchedule) {
        meetingScheduleRepository.save(meetingSchedule);
    }

    // Get all meetings
    public List<MeetingSchedule> getAllMeetings() {
        return meetingScheduleRepository.findAll();
    }

    // Find a specific meeting by ID
    public MeetingSchedule findMeetingById(int id) {
        Optional<MeetingSchedule> meeting = meetingScheduleRepository.findById(id);
        return meeting.orElse(null);
    }

    // Delete a meeting by ID
    public void deleteMeeting(int id) {
        meetingScheduleRepository.deleteById(id);
    }

    // Update a meeting
    public MeetingSchedule updateMeeting(MeetingSchedule meetingSchedule) {
        return meetingScheduleRepository.save(meetingSchedule);
    }

    // Get all meetings for a specific mediator
    public List<MeetingSchedule> getMeetingsByMediatorId(Integer mediatorId) {
        return meetingScheduleRepository.findByMediatorId(mediatorId);
    }
}
