package com.iTech.education.repository;

import com.iTech.education.entity.Course;
import com.iTech.education.utils.CourseStatus;
import com.iTech.education.utils.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    Optional<Course> findByTitle(String title);

    long countByStatus(CourseStatus status);

    long countByLevel(Level level);

    @Query("SELECT c.status, COUNT(c) FROM Course c GROUP BY c.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT c.level, COUNT(c) FROM Course c GROUP BY c.level")
    List<Object[]> countGroupByLevel();

    @Query("SELECT c.category.name, COUNT(c) FROM Course c GROUP BY c.category.name ORDER BY COUNT(c) DESC")
    List<Object[]> countGroupByCategory();

    @Query("SELECT YEAR(c.createdAt), MONTH(c.createdAt), COUNT(c) FROM Course c "
            + "WHERE c.createdAt >= :from GROUP BY YEAR(c.createdAt), MONTH(c.createdAt)")
    List<Object[]> countMonthlySince(@Param("from") LocalDateTime from);
}
