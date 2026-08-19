package com.iTech.education.repository;

import com.iTech.education.entity.Transaction;
import com.iTech.education.utils.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT DISTINCT t FROM Transaction t JOIN FETCH t.user LEFT JOIN FETCH t.details d LEFT JOIN FETCH d.course WHERE t.transactionCode = :code")
    Optional<Transaction> findByTransactionCode(@Param("code") String code);

    long countByStatus(TransactionStatus status);

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE t.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") TransactionStatus status);

    @Query("SELECT YEAR(t.createdAt), MONTH(t.createdAt), COALESCE(SUM(t.totalAmount), 0), COUNT(t) "
            + "FROM Transaction t WHERE t.status = :status AND t.createdAt >= :from "
            + "GROUP BY YEAR(t.createdAt), MONTH(t.createdAt)")
    List<Object[]> sumMonthlySince(@Param("status") TransactionStatus status,
                                   @Param("from") LocalDateTime from);
}
