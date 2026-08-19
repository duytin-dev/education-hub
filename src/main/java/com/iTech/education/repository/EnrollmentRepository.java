package com.iTech.education.repository;

import com.iTech.education.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.user.email = :email ORDER BY e.enrolledAt DESC")
    List<Enrollment> findByUser_EmailOrderByEnrolledAtDesc(@Param("email") String email);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    @Query("SELECT YEAR(e.enrolledAt), MONTH(e.enrolledAt), COUNT(e) FROM Enrollment e "
            + "WHERE e.enrolledAt >= :from GROUP BY YEAR(e.enrolledAt), MONTH(e.enrolledAt)")
    List<Object[]> countMonthlySince(@Param("from") LocalDateTime from);

    @Query("SELECT e.course.id, e.course.title, COUNT(e) FROM Enrollment e "
            + "GROUP BY e.course.id, e.course.title ORDER BY COUNT(e) DESC")
    List<Object[]> countTopCoursesByStudents();
}
