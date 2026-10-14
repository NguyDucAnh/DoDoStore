package com.dodostore.dodostore.repository;
import com.dodostore.dodostore.entity.ChatConversation;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface ChatConversationRepository extends JpaRepository<ChatConversation,String> {
 List<ChatConversation> findTop5ByOwnerKeyOrderByUpdatedAtDescIdDesc(String ownerKey);
 Optional<ChatConversation> findByIdAndOwnerKey(String id,String ownerKey);
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @Query("select c from ChatConversation c where c.id=:id and c.ownerKey=:owner")
 Optional<ChatConversation> lockOwned(@Param("id") String id,@Param("owner") String owner);
}
