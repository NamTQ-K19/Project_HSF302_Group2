package hsf302.se2033jv.project_hsf302_group2.admin.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Role;
import hsf302.se2033jv.project_hsf302_group2.common.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
    Optional<Role> findByRoleNameIgnoreCase(String roleName);
    boolean existsByRoleNameIgnoreCase(String roleName);
}