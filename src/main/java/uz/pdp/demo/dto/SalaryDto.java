package uz.pdp.demo.dto;

import lombok.Data;

import java.sql.Date;
import java.util.UUID;


@Data
public class SalaryDto {

    private UUID userId;
    private Integer amount;
    private Date workEndDate;
    private Date workStartDate;
}
