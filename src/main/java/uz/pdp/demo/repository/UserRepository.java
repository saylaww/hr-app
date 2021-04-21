package uz.pdp.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.pdp.demo.entity.Role;
import uz.pdp.demo.entity.User;

import javax.validation.constraints.Email;
import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(@Email String email);

    Optional<User> findByEmail(@Email String email);

    Optional<User> findByEmailAndEmailCode(@Email String email, String emailCode);

    @Query(value = "select * from users join users_roles ur on users.id = ur.users_id where  roles_id = ?1",
            nativeQuery = true)
    List<User> findAllByRoleId(Integer id);

    List<User> findAllByRolesIn(Collection<Set<Role>> roles);
}
