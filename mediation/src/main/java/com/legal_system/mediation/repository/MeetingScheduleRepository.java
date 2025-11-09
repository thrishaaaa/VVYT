package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.MeetingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingScheduleRepository extends JpaRepository<MeetingSchedule, Integer> {

    // Custom finder method to get meetings for a specific mediator
    List<MeetingSchedule> findByMediatorId(Integer mediatorId);
}
