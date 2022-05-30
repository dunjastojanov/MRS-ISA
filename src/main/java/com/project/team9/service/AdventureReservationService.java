package com.project.team9.service;

import com.project.team9.model.reservation.AdventureReservation;
import com.project.team9.repo.AdventureReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdventureReservationService {

    private final AdventureReservationRepository repository;

    @Autowired
    public AdventureReservationService(AdventureReservationRepository repository) {
        this.repository = repository;
    }

    public List<AdventureReservation> getStandardReservations() {
        return repository.findStandardReservations();
    }

    public List<AdventureReservation> getPossibleCollisionReservations(Long resourceId, Long ownerId) {
        return repository.findPossibleCollisionReservations(resourceId, ownerId);
    }

    public List<AdventureReservation> getBusyPeriods() {
        return repository.findBusyPeriods();
    }


    public Long save(AdventureReservation reservation) {
        return repository.save(reservation).getId();
    }

    public AdventureReservation getById(Long reservationID) {
        return repository.getById(reservationID);
    }

    public List<AdventureReservation> getBusyPeriodsForFishingInstructor(Long id) {
        return repository.findBusyPeriodsForFishingInstructor(id);
    }

    public List<AdventureReservation> getBusyPeriodsForAdventure(Long id, Long ownerId) {
        return repository.findBusyPeriodsForAdventure(id, ownerId);
    }

    public boolean clientHasReservations(Long resourceId, Long clientId) {
        return repository.findReservationsForClient(resourceId, clientId).size() > 0;
    }

    public boolean clientHasReservationsWithVendor(Long vendorId, Long clientId) {
        return repository.findReservationsForVendorAndClient(vendorId, clientId).size() > 0;
    }
}