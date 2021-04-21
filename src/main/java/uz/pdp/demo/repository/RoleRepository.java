package uz.pdp.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.demo.entity.Role;
import uz.pdp.demo.entity.User;
import uz.pdp.demo.entity.enums.RoleName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query(value = "select * from role join users_roles ur on role.id = ur.roles_id " +
            "where ur.users_id = ?1", nativeQuery = true)
    Role getRoleByUserId(UUID uuid);

    Optional<Role> findByRoleName(RoleName roleName);

    List<User> findAllByRoleName(RoleName roleName);

}
