package com.project.team9.repo;
import com.project.team9.model.reservation.VacationHouseReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface VacationHouseReservationRepository extends JpaRepository<VacationHouseReservation, Long> {

    @Query("FROM VacationHouseReservation WHERE isQuickReservation = false AND isBusyPeriod = false")
    List<VacationHouseReservation> findStandardReservations();

    @Query("FROM VacationHouseReservation  WHERE resource.id = ?1 ")
    List<VacationHouseReservation> findPossibleCollisionReservations(Long resourceId);

    @Query("FROM VacationHouseReservation WHERE isBusyPeriod = true and resource.id = ?1")
    List<VacationHouseReservation> findBusyPeriodForVacationHouse(Long id);

    @Query("FROM VacationHouseReservation  WHERE resource.id = ?1 AND client.id = ?2")
    List<VacationHouseReservation> findReservationsForClient(Long resourceId, Long clientId);

    @Query("FROM VacationHouseReservation  WHERE resource.owner.id = ?1 AND client.id = ?2")
    List<VacationHouseReservation> findReservationsForClientAndVendor(Long vendorId, Long clientId);

    @Query("FROM VacationHouseReservation WHERE resource.id=?1 ")
    List<VacationHouseReservation> findReservationsByResourceId(Long id);

    @Query("FROM VacationHouseReservation WHERE resource.owner.id=?1 ")
    List<VacationHouseReservation> findVacationHouseReservationForVendorId(Long id);

    @Query("FROM VacationHouseReservation WHERE client.id=?1 ")
    List<VacationHouseReservation> findVacationHouseReservationForClientId(Long id);
}
