package uz.pdp.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.demo.entity.SalaryHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaryHistoryRepository extends JpaRepository<SalaryHistory, Integer> {
    List<SalaryHistory> findAllByUserId(UUID user_id);
}
