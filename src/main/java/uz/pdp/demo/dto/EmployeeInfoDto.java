package uz.pdp.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.demo.entity.Task;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeInfoDto {

    private List<Task> taskList;
    private LocalDateTime enterTime;
    private LocalDateTime exitTime;

}
