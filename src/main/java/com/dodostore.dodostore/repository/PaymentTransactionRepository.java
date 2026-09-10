package com.dodostore.dodostore.repository;
import com.dodostore.dodostore.entity.PaymentTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByRequestKey(String key);
    Optional<PaymentTransaction> findByTxnRef(String ref);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentTransaction p where p.txnRef = :ref")
    Optional<PaymentTransaction> lockByRef(@Param("ref") String ref);
}
