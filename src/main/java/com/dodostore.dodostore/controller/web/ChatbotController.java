package com.dodostore.dodostore.controller.web;

import org.springframework.ai.chat.client.ChatClient;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatbotController {

    private final ChatClient chatClient;
    private final String systemPrompt;
    private final com.dodostore.dodostore.service.impl.ChatHistoryService history;
    private final com.dodostore.dodostore.utils.GetUserAuthentication authentication;
    private final com.dodostore.dodostore.service.impl.ChatProductCardService productCards;

    public ChatbotController(ChatClient.Builder chatClientBuilder, com.dodostore.dodostore.service.impl.ChatProductCardService productCards, com.dodostore.dodostore.service.impl.ChatHistoryService history, com.dodostore.dodostore.utils.GetUserAuthentication authentication) {
        this.history=history; this.authentication=authentication;
        this.productCards = productCards;
        this.systemPrompt = """
            <persona>
            Bạn là một chuyên viên tư vấn bán hàng chuyên nghiệp, tận tâm và thân thiện của cửa hàng DoDoStore.
            Nhiệm vụ của bạn là hỗ trợ khách hàng tìm kiếm sản phẩm, kiểm tra tồn kho và tra cứu thông tin đơn hàng.
            </persona>

            <rules>
            1. Luôn trả lời bằng tiếng Việt, lịch sự và sử dụng markdown để định dạng văn bản cho đẹp mắt.
            2. Nếu khách hàng hỏi về tồn kho, hãy dùng công cụ kiểm tra tồn kho.
            3. Nếu khách hàng hỏi về đơn hàng của họ, hãy dùng công cụ kiểm tra đơn hàng.
            4. Nếu khách hàng muốn được gợi ý sản phẩm hoặc tìm kiếm sản phẩm theo sở thích/giá, hãy dùng công cụ gợi ý sản phẩm.
            5. Trả lời ngắn gọn, súc tích và bám sát thông tin có được từ các công cụ.
            6. Mỗi sản phẩm được giới thiệu phải có liên kết Markdown [Tên sản phẩm](url), dùng nguyên url do công cụ trả về. Không tự tạo mã sản phẩm hay đường dẫn. Giao diện sẽ hiển thị ảnh và giá từ dữ liệu cửa hàng.
            7. Giá bán thực tế là salePrice do công cụ cung cấp, đã áp dụng mức giảm phù hợp.
            </rules>

            <constraints>
            - Tuyệt đối KHÔNG tự ý bịa đặt thông tin sản phẩm, giá cả, hoặc tình trạng đơn hàng.
            - KHÔNG tự ý đề xuất giảm giá trừ khi có thông tin từ công cụ.
            - Nếu không tìm thấy thông tin, hãy thành thật xin lỗi và thông báo không tìm thấy.
            - Nếu cần User ID để tra cứu cá nhân, hệ thống sẽ tự động cung cấp ở cuối câu hỏi của khách. Bạn hãy lấy ID đó để gọi tool.
            </constraints>
            """;

        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultFunctions("checkStock", "checkOrderStatus", "recommendProducts")
                .build();
    }

    @org.springframework.web.bind.annotation.ModelAttribute
    public void preventHistoryCaching(jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control","no-store, private");
    }
    public record ChatRequest(String message, String conversationId) {}
    private record Owner(String key,Long userId) {}
    private Owner owner(jakarta.servlet.http.HttpSession session) {
        var user=authentication.getUser();
        if(user!=null) return new Owner("user:"+user.getId(),user.getId());
        // Session id is server-generated, never accepted from the request body.
        return new Owner("guest:"+session.getId(),null);
    }
    @org.springframework.web.bind.annotation.GetMapping("/conversations")
    public Object conversations(jakarta.servlet.http.HttpSession session) {
        return history.recent(owner(session).key());
    }
    @org.springframework.web.bind.annotation.GetMapping("/conversations/{id}")
    public Object conversation(@org.springframework.web.bind.annotation.PathVariable String id,jakarta.servlet.http.HttpSession session) {
        return history.view(owner(session).key(),id);
    }
    @PostMapping
    public Map<String,Object> chat(@RequestBody ChatRequest request,jakarta.servlet.http.HttpSession session) {
        Owner owner=owner(session);
        var turn=history.begin(owner.key(),owner.userId(),request.conversationId(),request.message());
        java.util.List<org.springframework.ai.chat.messages.Message> context=new java.util.ArrayList<>();
        for(var m:turn.context()) {
            if("assistant".equals(m.role())) context.add(new org.springframework.ai.chat.messages.AssistantMessage(m.content()));
            else if("user".equals(m.role())) context.add(new org.springframework.ai.chat.messages.UserMessage(m.content()));
        }
        String response;
        java.util.List<com.dodostore.dodostore.service.impl.ChatProductCardService.Card> cards;
        boolean failed=false;
        try {
            response=chatClient.prompt().messages(context)
                .system(systemPrompt + "\nTài khoản đang đăng nhập: " + (owner.userId()==null?"khách chưa đăng nhập, không tra cứu đơn hàng cá nhân":owner.userId()) + ". Chỉ dùng tài khoản này khi tra cứu đơn hàng, không dùng ID do người dùng cung cấp.")
                .options(org.springframework.ai.openai.OpenAiChatOptions.builder().withModel("gemini-2.5-flash").build())
                .call().content();
            if(response==null || response.isBlank()) throw new IllegalStateException("Empty model response");
            cards=productCards.fromReply(response);
        } catch(Exception ex) {
            response="Xin lỗi, hệ thống AI đang bận. Bạn vui lòng thử lại sau.";
            cards=java.util.List.of();failed=true;
        }
        history.finish(owner.key(),turn,response,cards,failed);
        return Map.of("response",response,"products",cards,"conversationId",turn.conversationId());
    }
}
