package com.example.antifraud.repo;

import com.example.antifraud.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
  Optional<TransactionEntity> findByTransactionId(String transactionId);

  List<TransactionEntity> findTop200ByAccountIdOrderByCreatedAtDesc(Long accountId);

  Optional<TransactionEntity>
  findTopByAccountIdAndTransactionIdNotOrderByCreatedAtDesc(
          Long accountId,
          String transactionId
  );
  @Modifying
  @Query("""
    update TransactionEntity t
       set t.status = :status
     where t.transactionId = :txId
  """)
  void updateStatusByTransactionId(
          @Param("txId") String transactionId,
          @Param("status") String status
  );

  @Query("select count(t) from TransactionEntity t where t.accountId = :accountId and t.createdAt >= :fromTs")
  long countByAccountSince(Long accountId, Instant fromTs);

  @Query("select t from TransactionEntity t where t.accountId = :accountId order by t.createdAt desc limit 1")
  List<TransactionEntity> lastByAccount(Long accountId);
}
