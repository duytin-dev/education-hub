package com.iTech.education.repository;

import com.iTech.education.entity.User;
import com.iTech.education.utils.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    long countByRole(RoleType role);

    long countByIsActive(Boolean isActive);

    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countGroupByRole();

    @Query("SELECT YEAR(u.createdAt), MONTH(u.createdAt), COUNT(u) FROM User u "
            + "WHERE u.createdAt >= :from GROUP BY YEAR(u.createdAt), MONTH(u.createdAt)")
    List<Object[]> countMonthlySince(@Param("from") LocalDateTime from);

    @Query("SELECT YEAR(u.createdAt), MONTH(u.createdAt), COUNT(u) FROM User u "
            + "WHERE u.createdAt >= :from AND u.role = :role "
            + "GROUP BY YEAR(u.createdAt), MONTH(u.createdAt)")
    List<Object[]> countMonthlyByRoleSince(@Param("from") LocalDateTime from, @Param("role") RoleType role);
}
