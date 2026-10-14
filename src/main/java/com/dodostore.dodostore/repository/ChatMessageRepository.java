package com.dodostore.dodostore.repository;
import com.dodostore.dodostore.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
 List<ChatMessage> findByConversationIdOrderByIdAsc(String id);
 List<ChatMessage> findTop20ByConversationIdAndRoleNotOrderByIdDesc(String id,String role);
}
