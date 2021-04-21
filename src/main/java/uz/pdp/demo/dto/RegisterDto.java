package uz.pdp.demo.dto;


import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class RegisterDto {
    @Size(min = 3, max = 50)
    private String firstName;

    @Size(min = 3, max = 50)
    private String lastName;

    @Email
    private String email;

    private String password;

    private Set<Integer> rolesId;

    private Integer salary;
}
