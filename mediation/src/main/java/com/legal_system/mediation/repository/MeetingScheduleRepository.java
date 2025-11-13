package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.MeetingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MeetingScheduleRepository extends JpaRepository<MeetingSchedule, Integer> {

    // Find meetings for a specific mediator
    List<MeetingSchedule> findByMediatorId(Integer mediatorId);

    // Find ALL meetings for a mediator ordered by date (newest first)
    List<MeetingSchedule> findByMediatorIdOrderByMeetingDateDesc(Integer mediatorId);

    // Find meetings by mediator and status
    List<MeetingSchedule> findByMediatorIdAndStatus(Integer mediatorId, String status);

    // Count meetings by mediator and status
    Long countByMediatorIdAndStatus(Integer mediatorId, String status);

    // Find upcoming meetings (date >= today)
    List<MeetingSchedule> findByMediatorIdAndMeetingDateGreaterThanEqualOrderByMeetingDateAsc(
            Integer mediatorId, LocalDate date);

    // Count upcoming scheduled meetings
    Long countByMediatorIdAndStatusAndMeetingDateAfter(
            Integer mediatorId, String status, LocalDate date);

    // Find upcoming scheduled meetings
    List<MeetingSchedule> findByMediatorIdAndStatusAndMeetingDateAfterOrderByMeetingDateAsc(
            Integer mediatorId, String status, LocalDate date);

    /**
     * NEW: Find all meetings where the user is involved as party1 or party2 in the case
     * This query joins MeetingSchedule -> Cases -> UserDetails
     */
    @Query(value = "SELECT m.* FROM meeting_schedule m " +
            "INNER JOIN cases c ON m.case_id = c.case_id " +
            "WHERE c.party1_id = :userId OR c.party2_id = :userId " +
            "ORDER BY m.meeting_date DESC",
            nativeQuery = true)
    List<MeetingSchedule> findMeetingsByUserId(@Param("userId") Integer userId);

}