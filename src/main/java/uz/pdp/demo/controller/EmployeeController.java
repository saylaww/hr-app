package uz.pdp.demo.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.demo.dto.EmployeeInfoDto;
import uz.pdp.demo.dto.SalaryDto;
import uz.pdp.demo.entity.EmployeeResponse;
import uz.pdp.demo.entity.User;
import uz.pdp.demo.model.Response;
import uz.pdp.demo.service.EmployeeService;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/list")
    public HttpEntity<?> getEmployees() {
        final List<User> userList = employeeService.getUserList();
        return ResponseEntity.status(userList != null ? 201 : 409).body(userList);
    }

    @PostMapping("/salary")
    public HttpEntity<?> payMonthly(@RequestBody SalaryDto salaryDto) {
        Response response = employeeService.payMonthly(salaryDto);
        return ResponseEntity.status(response.isStatus() ? 200 : 401).body(response);
    }

    @GetMapping("/byTurniketTask")
    public HttpEntity<?> getAllCompletedTaskByTime(@RequestParam UUID employeeId,
                                                   @RequestParam Timestamp startDateTime,
                                                   @RequestParam Timestamp finishDateTime) {
        EmployeeResponse response = employeeService.findOneByData(employeeId, startDateTime, finishDateTime);
        return ResponseEntity.status(response.isSuccess() ? 200 : 401).body(response);

    }

    @GetMapping("/salary/{id}")
    public HttpEntity<?> getSalariesByEmployeeId(@PathVariable UUID id) {
        Response response = employeeService.getSalariesByUserId(id);
        return ResponseEntity.status(response.isStatus() ? 200 : 401).body(response);
    }

    @GetMapping("/info")
    public HttpEntity<?> getEmployeeInfo(@RequestParam UUID id) {
        final EmployeeInfoDto employeeInfoById = employeeService.getEmployeeInfoById(id);
        return ResponseEntity.status(employeeInfoById != null ? 201 : 409).body(employeeInfoById);
    }

    @PostMapping("/pay")
    public HttpEntity<?> pay(@RequestParam UUID userid, @RequestParam Integer salary, @RequestBody SalaryDto salaryDto) {
        final Response response = employeeService.pay(userid, salary, salaryDto);
        return ResponseEntity.status(response.isStatus() ? 201 : 409).body(response);
    }
}
