package com.dodostore.dodostore.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
@Entity @Getter @Setter
@Table(name="chat_message",indexes=@Index(name="idx_chat_message_conversation",columnList="conversation_id,id"))
public class ChatMessage {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="conversation_id",nullable=false) private ChatConversation conversation;
 @Column(nullable=false,length=16) private String role;
 @Lob @Column(nullable=false,columnDefinition="LONGTEXT") private String content;
 @Lob @Column(name="products_json",columnDefinition="LONGTEXT") private String productsJson;
 @Column(name="created_at",nullable=false) private Instant createdAt;
}
