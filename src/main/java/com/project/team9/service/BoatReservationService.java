package com.project.team9.service;

import com.project.team9.model.reservation.BoatReservation;
import com.project.team9.model.reservation.VacationHouseReservation;
import com.project.team9.repo.BoatReservationRepository;
import com.project.team9.repo.VacationHouseReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoatReservationService {
    private final BoatReservationRepository repository;

    @Autowired
    public BoatReservationService(BoatReservationRepository repository) {
        this.repository = repository;
    }

    public void addReservation(BoatReservation reservation) {
        this.repository.save(reservation);
    }
    public BoatReservation getBoatReservation(Long id) {
        return repository.getById(id);
    }
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}