package com.dodostore.dodostore.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
@Entity @Getter @Setter
@Table(name="chat_conversation",indexes=@Index(name="idx_chat_owner_updated",columnList="owner_key,updated_at"))
public class ChatConversation {
 @Id @Column(length=36) private String id;
 @Column(name="owner_key",nullable=false,length=100) private String ownerKey;
 @Column(name="user_id") private Long userId;
 @Column(nullable=false,length=120) private String title;
 @Column(name="created_at",nullable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @Column(name="pending_token",length=36) private String pendingToken;
}
