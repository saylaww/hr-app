package uz.pdp.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.demo.dto.TaskDto;
import uz.pdp.demo.entity.Role;
import uz.pdp.demo.entity.Task;
import uz.pdp.demo.entity.User;
import uz.pdp.demo.entity.enums.RoleName;
import uz.pdp.demo.entity.enums.TaskStatusName;
import uz.pdp.demo.model.Response;
import uz.pdp.demo.repository.RoleRepository;
import uz.pdp.demo.repository.TaskRepository;
import uz.pdp.demo.repository.UserRepository;

import java.util.*;

@Service
public class TaskService {

    UserRepository userRepository;
    TaskRepository taskRepository;
    RoleRepository roleRepository;
    final
    JavaMailSender javaMailSender;

    @Autowired
    public TaskService(UserRepository userRepository, TaskRepository taskRepository, RoleRepository roleRepository, JavaMailSender javaMailSender) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.roleRepository = roleRepository;
        this.javaMailSender = javaMailSender;
    }

    public Response add(TaskDto taskDto) {
        RoleName roleName = null;
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        final User user = (User) authentication.getPrincipal();
        final Set<Role> roles = user.getRoles();

        for (Role role : roles) {
            roleName = role.getName();
        }

        assert roleName != null;
        if (roleName.equals(RoleName.DIRECTOR)) {
            final Task task = getTask(taskDto, 1);
            if (task == null) {
                return new Response("Error", false);
            }
            taskRepository.save(task);
            sendEmail(taskDto);
            return new Response("Tasks saved!", true, task);
        } else if (roleName.equals(RoleName.MANAGER)) {
            final Task task = getTask(taskDto, 2);
            if (task == null) {
                return new Response("Error", false);
            }
            taskRepository.save(task);
            sendEmail(taskDto);

            return new Response("Tasks saved!", true, task);
        } else if (roleName.equals(RoleName.HR_MANAGER)) {
            final Task task = getTask(taskDto, 3);
            if (task == null) {
                return new Response("Error", false);
            }
            taskRepository.save(task);
            sendEmail(taskDto);
            return new Response("Tasks saved!", true, task);
        }
        return null;

    }

    public List<Task> getTasks(UUID id) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User user = (User) authentication.getPrincipal();

        final Role roleByUserId = roleRepository.getRoleByUserId(id);
        final RoleName roleByUserIdName = roleByUserId.getName();

        final Set<Role> roles = user.getRoles();
        RoleName roleName = null;
        for (Role role : roles) {
            roleName = role.getName();
        }
        assert roleName != null;
        if (roleName.equals(RoleName.DIRECTOR)) {
            return taskRepository.getTasksByUserId(id);
        } else if (roleName.equals(RoleName.HR_MANAGER) || roleName.equals(RoleName.MANAGER)) {

            if (roleByUserId.equals(RoleName.DIRECTOR)) {
                return null;
            }
            return taskRepository.getTasksByUserId(id);
        } else if (roleName.equals(RoleName.EMPLOYEE)) {
            if (!roleByUserId.equals(RoleName.EMPLOYEE)) {
                return null;
            }
            return taskRepository.getTasksByUserId(id);
        }

        return null;
    }

    public Response doTask(Integer taskId, Integer status) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User user = (User) authentication.getPrincipal();

        final Set<Role> roles = user.getRoles();
        RoleName roleName = null;
        for (Role role : roles) {
            roleName = role.getName();
        }
        assert roleName != null;
        if (!roleName.equals(RoleName.EMPLOYEE)) {
            return new Response("You don't have privileges", false);
        }
        final Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            return new Response("Task not found", false);
        }

        TaskStatusName taskStatusName = null;

        final Task task = optionalTask.get();
        if (status == 1) {
            taskStatusName = TaskStatusName.IN_PROGRESS;
            task.setStatus(TaskStatusName.IN_PROGRESS);
        }
        if (status == 2) {
            taskStatusName = TaskStatusName.COMPLETED;
            task.setStatus(TaskStatusName.COMPLETED);
        }

        taskRepository.save(task);
        sendEmailAboutTask(taskId, taskStatusName);
        return new Response("Changed status sent to manager via email", true);
    }


    public Task getTask(TaskDto taskDto, Integer roleId) {
        Task task = new Task();
        task.setName(taskDto.getName());
        task.setDescription(taskDto.getDescription());
        task.setDeadline(taskDto.getDeadline());
        List<User> userList = new ArrayList<>();

        for (UUID responsibleUser : taskDto.getResponsibleUsers()) {
            final Optional<User> optionalUser = userRepository.findById(responsibleUser);
            if (optionalUser.isEmpty()) {
                return null;
            }
            final User u = optionalUser.get();
            final Optional<Role> optionalRole = roleRepository.findById(roleId);
            final Set<Role> roles = u.getRoles();
            RoleName commander = optionalRole.get().getName();
            for (Role role : roles) {
                final RoleName subordinate = role.getName();
                if (commander.equals(RoleName.DIRECTOR)) {
                    if (subordinate.equals(RoleName.DIRECTOR))
                        return null;
                }
                if (commander.equals(RoleName.MANAGER)) {
                    if (subordinate.equals(RoleName.DIRECTOR) || subordinate.equals(RoleName.HR_MANAGER))
                        return null;
                }
                if (commander.equals(RoleName.HR_MANAGER)) {
                    if (subordinate.equals(RoleName.DIRECTOR) || subordinate.equals(RoleName.MANAGER))
                        return null;
                }
                if (commander.equals(RoleName.EMPLOYEE)) {
                    return null;
                }
            }
            userList.add(optionalUser.get());
        }

        task.setResponsibleUsers(userList);
        task.setStatus(TaskStatusName.NEW);
        return task;
    }

    public void sendEmail(TaskDto taskDto) {
        SimpleMailMessage message = new SimpleMailMessage();

        final List<UUID> responsibleUsers = taskDto.getResponsibleUsers();
        for (UUID responsibleUser : responsibleUsers) {
            final Optional<User> optionalUser = userRepository.findById(responsibleUser);
            final User user1 = optionalUser.get();

            message.setFrom("noreply@gmail.com");
            message.setTo(user1.getEmail());
            message.setSubject(taskDto.getName());
            String text = "Task: " + taskDto.getDescription() + "\n\nDeadline: " + taskDto.getDeadline();
            message.setText(text);
            javaMailSender.send(message);
        }
    }

    public void sendEmailAboutTask(Integer taskId, TaskStatusName taskStatusName) {

        final Optional<Task> optionalTask = taskRepository.findById(taskId);
        final Task task = optionalTask.get();
        final UUID createdBy = task.getCreatedBy();
        final Optional<User> userOptional = userRepository.findById(createdBy);
        final User user = userOptional.get();

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("noreply@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("Task Status Change");
        String text = "Task status: " + taskStatusName;
        message.setText(text);
        javaMailSender.send(message);
    }
}
