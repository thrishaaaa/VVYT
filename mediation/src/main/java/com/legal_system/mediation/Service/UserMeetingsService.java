package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.MeetingSchedule;
import com.legal_system.mediation.repository.MeetingScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserMeetingsService {

    @Autowired
    private MeetingScheduleRepository meetingScheduleRepository;

    public List<MeetingSchedule> getUserMeetings(Integer userId) {
        System.out.println("=== UserMeetingsService: getUserMeetings called ===");
        System.out.println("User ID: " + userId);

        List<MeetingSchedule> meetings = meetingScheduleRepository.findMeetingsByUserId(userId);

        System.out.println("Meetings found: " + meetings.size());
        for (MeetingSchedule m : meetings) {
            System.out.println("  - Meeting ID: " + m.getId() + ", Description: " + m.getDescription());
        }

        return meetings;
    }
}