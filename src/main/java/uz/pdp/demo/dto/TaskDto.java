package uz.pdp.demo.dto;

import lombok.Data;
import uz.pdp.demo.entity.enums.TaskStatusName;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
public class TaskDto {
    private String name;
    private String description;
    private Timestamp deadline;
    private List<UUID> responsibleUsers;
    private TaskStatusName statusName;
}
