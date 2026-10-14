package com.dodostore.dodostore.service.impl;
import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.*;
import java.util.*;
@Service @RequiredArgsConstructor
public class ChatHistoryService {
 private final ChatConversationRepository conversations;
 private final ChatMessageRepository messages;
 private final ObjectMapper json;
 public record Summary(String id,String title,Instant updatedAt) {}
 public record MessageView(String role,String content,Instant createdAt,List<ChatProductCardService.Card> products) {}
 public record ThreadView(String id,String title,List<MessageView> messages) {}
 public record Turn(String conversationId,String token,List<MessageView> context) {}
 private ChatConversation owned(String owner,String id,boolean lock) {
  return (lock?conversations.lockOwned(id,owner):conversations.findByIdAndOwnerKey(id,owner))
   .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy cuộc trò chuyện."));
 }
 @Transactional(readOnly=true)
 public List<Summary> recent(String owner) {
  return conversations.findTop5ByOwnerKeyOrderByUpdatedAtDescIdDesc(owner).stream().map(c->new Summary(c.getId(),c.getTitle(),c.getUpdatedAt())).toList();
 }
 @Transactional(readOnly=true)
 public ThreadView view(String owner,String id) {
  var c=owned(owner,id,false);
  return new ThreadView(c.getId(),c.getTitle(),messages.findByConversationIdOrderByIdAsc(id).stream().map(this::viewMessage).toList());
 }
 private MessageView viewMessage(ChatMessage m) {
  List<ChatProductCardService.Card> cards=List.of();
  if(m.getProductsJson()!=null)try {cards=json.readValue(m.getProductsJson(),new TypeReference<List<ChatProductCardService.Card>>(){});}catch(Exception ignored){}
  return new MessageView(m.getRole(),m.getContent(),m.getCreatedAt(),cards);
 }
 @Transactional
 public Turn begin(String owner,Long userId,String id,String text) {
  if(text==null || text.isBlank() || text.length()>4000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Nhập tin nhắn từ 1 đến 4.000 ký tự.");
  Instant now=Instant.now();
  ChatConversation c;
  if(id==null || id.isBlank()) {
   c=new ChatConversation();c.setId(UUID.randomUUID().toString());c.setOwnerKey(owner);c.setUserId(userId);
   String title=text.strip().replaceAll("\\s+"," ");c.setTitle(new String(title.codePoints().limit(100).toArray(),0,Math.min(title.codePointCount(0,title.length()),100)));
   c.setCreatedAt(now);
  } else c=owned(owner,id,true);
  if(c.getPendingToken()!=null && c.getUpdatedAt().isAfter(now.minusSeconds(180)))
   throw new ResponseStatusException(HttpStatus.CONFLICT,"Cuộc trò chuyện đang có câu trả lời được xử lý. Vui lòng đợi.");
  String token=UUID.randomUUID().toString();c.setPendingToken(token);c.setUpdatedAt(now);conversations.save(c);
  ChatMessage message=new ChatMessage();message.setConversation(c);message.setRole("user");message.setContent(text.strip());message.setCreatedAt(now);messages.saveAndFlush(message);
  List<ChatMessage> context=new ArrayList<>(messages.findTop20ByConversationIdAndRoleNotOrderByIdDesc(c.getId(),"error"));
  Collections.reverse(context);
  return new Turn(c.getId(),token,context.stream().map(this::viewMessage).toList());
 }
 @Transactional
 public void finish(String owner,Turn turn,String reply,List<ChatProductCardService.Card> cards,boolean failed) {
  ChatConversation c=owned(owner,turn.conversationId(),true);
  if(!Objects.equals(c.getPendingToken(),turn.token())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Lượt trả lời đã hết hạn. Hãy mở lại cuộc trò chuyện.");
  ChatMessage message=new ChatMessage();message.setConversation(c);message.setRole(failed?"error":"assistant");message.setContent(reply);message.setCreatedAt(Instant.now());
  try {message.setProductsJson(json.writeValueAsString(cards));} catch(Exception e){throw new IllegalStateException("Không thể lưu thẻ sản phẩm",e);}
  messages.save(message);c.setPendingToken(null);c.setUpdatedAt(message.getCreatedAt());conversations.save(c);
 }
}
