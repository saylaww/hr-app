package uz.pdp.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.demo.entity.Turniket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TurniketRepository extends JpaRepository<Turniket, Integer> {

    Optional<Turniket> findByCreatedByAndStatus(UUID createdBy, boolean status);

    Optional<Turniket> findByCreatedBy(UUID createdBy);

    @Query("select tur from Turniket tur " +
            "where tur.createdBy = :employeeId and (tur.enterWork >= :start or tur.enterWork <= :finish)")
    List<Turniket> findAllByCreatedByAndEnterDateTimeAndExitDateTimeBefore(UUID employeeId, LocalDateTime start, LocalDateTime finish);
}
