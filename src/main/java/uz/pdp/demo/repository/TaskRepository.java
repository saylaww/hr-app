package uz.pdp.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.demo.entity.Task;
import uz.pdp.demo.entity.User;
import uz.pdp.demo.entity.enums.TaskStatusName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query(value = "select * from task " +
            "join task_responsible_users tru on task.id = tru.task_id " +
            "where tru.responsible_users_id = ?1", nativeQuery = true)
    List<Task> getTasksByUserId(UUID id);

    List<Task> findAllByStatusAndResponsibleUsers(TaskStatusName status, List<User> responsibleUsers);

}
