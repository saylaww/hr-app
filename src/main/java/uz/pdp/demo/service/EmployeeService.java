package uz.pdp.demo.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.demo.dto.EmployeeInfoDto;
import uz.pdp.demo.dto.SalaryDto;
import uz.pdp.demo.entity.*;
import uz.pdp.demo.entity.enums.RoleName;
import uz.pdp.demo.entity.enums.TaskStatusName;
import uz.pdp.demo.model.Response;
import uz.pdp.demo.repository.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EmployeeService {
    UserRepository userRepository;
    TurniketRepository turniketRepository;
    TaskRepository taskRepository;
    SalaryHistoryRepository salaryHistoryRepository;
    RoleRepository roleRepository;

    public EmployeeService(UserRepository userRepository, TurniketRepository turniketRepository, TaskRepository taskRepository, SalaryHistoryRepository salaryHistoryRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.turniketRepository = turniketRepository;
        this.taskRepository = taskRepository;
        this.salaryHistoryRepository = salaryHistoryRepository;
        this.roleRepository = roleRepository;
    }

    public EmployeeResponse findOneByData(UUID id, Timestamp start, Timestamp finish) {

        LocalDateTime startLocal = start.toLocalDateTime();
        LocalDateTime finishLocal = finish.toLocalDateTime();

        final Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return new EmployeeResponse("Employee not found!", false);
        }
        final Set<Role> roles = optionalUser.get().getRoles();
        boolean checkEmployee = false;
        for (Role role : roles) {
            if (role.getName().name().equals("EMPLOYEE")) {
                checkEmployee = true;
                break;
            }
        }

        final List<Turniket> allByCreatedByAndEnterDateTimeAndExitDateTimeBefore =
                turniketRepository.findAllByCreatedByAndEnterDateTimeAndExitDateTimeBefore(id, startLocal, finishLocal);

        if (allByCreatedByAndEnterDateTimeAndExitDateTimeBefore.isEmpty()) {
            return new EmployeeResponse("Not found!", false);
        }

        EmployeeResponse response = new EmployeeResponse();
        response.setTurniketList(allByCreatedByAndEnterDateTimeAndExitDateTimeBefore);

        List<User> userList = new ArrayList<>();
        final Optional<User> userOptional = userRepository.findById(id);
        userList.add(userOptional.get());
        if (checkEmployee) {
            final List<Task> taskList = taskRepository.findAllByStatusAndResponsibleUsers(TaskStatusName.COMPLETED, userList);
            response.setTaskList(taskList);
        }

        return response;
    }

    public Response payMonthly(SalaryDto salaryDto) {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {
            final User user = (User) authentication.getPrincipal();
            final Set<Role> roles = user.getRoles();

            boolean checkRole = false;
            for (Role role : roles) {
                if (role.getName().equals(RoleName.DIRECTOR) || role.getName().equals(RoleName.HR_MANAGER)) {
                    checkRole = true;
                    break;
                }
            }

            if (!checkRole) {
                return new Response("You don't have access for this operation", false);
            }

            final Optional<User> optionalUser = userRepository.findById(salaryDto.getUserId());
            if (optionalUser.isEmpty()) {
                return new Response("Employee not found", false);
            }

            SalaryHistory salaryHistory = new SalaryHistory();
            salaryHistory.setUser(optionalUser.get());
            salaryHistory.setAmount(salaryDto.getAmount());
            salaryHistory.setWordStartDate(salaryDto.getWorkStartDate());
            salaryHistory.setWorkEndDate(salaryDto.getWorkEndDate());

            salaryHistoryRepository.save(salaryHistory);
            return new Response("Salary saved! For: " + optionalUser.get().getFirstName(), true);
        }

        return new Response("Authority empty", false);
    }

    public Response getSalariesByUserId(UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {
            User user = (User) authentication.getPrincipal();
            Set<Role> roles = user.getRoles();

            boolean checkRole = false;
            for (Role role : roles) {
                if (role.getName().name().equals("DIRECTOR") || role.getName().name().equals("HR_MANAGER")) {
                    checkRole = true;
                    break;
                }
            }
            if (!checkRole)
                return new Response("You don't have access for this operation", false);
            List<SalaryHistory> salaryHistoryList = salaryHistoryRepository.findAllByUserId(id);
            if (salaryHistoryList.size() == 0)
                return new Response("Such employee did not get salary!", false);

            return new Response("Success!", true, salaryHistoryList);
        }
        return new Response("Authorization empty!", false);
    }

    public Response pay(UUID userId, Integer salary, SalaryDto salaryDto) {
        SalaryHistory salaryHistory = new SalaryHistory();
        salaryHistory.setAmount(salaryDto.getAmount());
        salaryHistory.setWordStartDate(salaryDto.getWorkStartDate());
        salaryHistory.setWordStartDate(salaryDto.getWorkEndDate());

        final Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return new Response("User not found", false);
        }
        salaryHistory.setUser(optionalUser.get());

        salaryHistoryRepository.save(salaryHistory);
        return new Response("Salary history added!", true);
    }

    public List<User> getUserList() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {
            final User user = (User) authentication.getPrincipal();
            final Set<Role> roles = user.getRoles();
            RoleName roleName = null;
            for (Role role : roles) {
                roleName = role.getName();
            }

            assert roleName != null;
            if (roleName.equals(RoleName.DIRECTOR) || roleName.equals(RoleName.HR_MANAGER)) {
                return userRepository.findAllByRoleId(4);
            }
            return null;

        }
        return null;
    }

    public EmployeeInfoDto getEmployeeInfoById(UUID id) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {
            final User user = (User) authentication.getPrincipal();
            final Set<Role> roles = user.getRoles();
            RoleName roleName = null;
            for (Role role : roles) {
                roleName = role.getName();
            }

            assert roleName != null;
            if (roleName.equals(RoleName.DIRECTOR) || roleName.equals(RoleName.HR_MANAGER)) {
                EmployeeInfoDto info = new EmployeeInfoDto();
                final Optional<Turniket> turniket = turniketRepository.findByCreatedBy(id);
                final Turniket t = turniket.get();

                info.setEnterTime(t.getEnterWork());
                info.setExitTime(t.getExitWork());

                final List<Task> taskList = taskRepository.getTasksByUserId(id);
                info.setTaskList(taskList);
                return info;
            }
            return null;

        }
        return null;
    }
}
