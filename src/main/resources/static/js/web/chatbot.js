// Chatbot JS
(() => {
function initChatbot() {
  const chatButton = document.getElementById("chatButton");
  const chatBox = document.getElementById("chatBox");
  const chatClose = document.getElementById("chatClose");
  const chatMessages = document.getElementById("chatMessages");
  const chatInput = document.getElementById("chatInput");
  const chatSend = document.getElementById("chatSend");
  const chatTyping = document.getElementById("chatTyping");
  
  if (!chatButton || !chatBox || !chatMessages || !chatInput || !chatSend || !chatTyping) return;
  if (chatButton.dataset.chatInitialized === "true") return;
  chatButton.dataset.chatInitialized = "true";
  let sending = false;
  let conversationId = null;
  let loadingHistory = false;
  let historySequence = 0;
  const historyStatus = document.getElementById("chatHistoryStatus");

  function controls() {
    chatSend.disabled = sending || loadingHistory;
    chatInput.disabled = loadingHistory;
  }
  async function getJson(url) {
    const response = await fetch(url, {cache:"no-store", headers:{"Accept":"application/json"}});
    if (!response.ok || !(response.headers.get("content-type") || "").includes("application/json")) throw new Error("Không tải được lịch sử. Vui lòng mở lại chatbot hoặc đăng nhập lại.");
    return response.json();
  }
  async function refreshHistory() {
    const sequence = ++historySequence;
    loadingHistory = true; controls();
    conversationId = null; chatMessages.replaceChildren();
    if(historyStatus) historyStatus.textContent = "Đang tải lịch sử…";
    try {
      const items = (await getJson("/api/v1/chat/conversations")).slice(0,5);
      const threads = await Promise.all(items.map(item => getJson("/api/v1/chat/conversations/" + encodeURIComponent(item.id))));
      if(sequence !== historySequence) return;
      for(const thread of [...threads].reverse()) {
        const divider = document.createElement("div");
        divider.className = "chat-conversation-divider";
        const time = thread.messages[0]?.createdAt;
        divider.textContent = "Cuộc trò chuyện" + (time ? " · " + new Date(time).toLocaleString("vi-VN",{timeZone:"Asia/Ho_Chi_Minh",day:"2-digit",month:"2-digit",year:"numeric",hour:"2-digit",minute:"2-digit"}) : "");
        chatMessages.appendChild(divider);
        for(const message of thread.messages) {
          addMessage(message.content,message.role === "user" ? "user" : "bot");
          addProducts(message.products);
        }
      }
      conversationId = threads[0]?.id || null;
      if(!threads.length) addMessage("Xin chào! Tôi có thể giúp gì cho bạn hôm nay?","bot");
      if(historyStatus) historyStatus.textContent = threads.length ? "Kéo lên để xem các cuộc trò chuyện trước." : "Bắt đầu trò chuyện với DoDo AI.";
      chatMessages.scrollTop = chatMessages.scrollHeight;
    } catch(error) {
      if(sequence === historySequence) {
        conversationId = null; chatMessages.replaceChildren();
        if(historyStatus) historyStatus.textContent = error.message;
      }
    } finally { if(sequence === historySequence) { loadingHistory=false; controls(); } }
  }
  chatButton.addEventListener("click",()=>{
    chatBox.classList.toggle("open");
    chatButton.setAttribute("aria-expanded", String(chatBox.classList.contains("open")));
    if(chatBox.classList.contains("open") && !sending) refreshHistory();
  });

  chatClose?.addEventListener("click", () => {
    chatBox.classList.remove("open");
    chatButton.setAttribute("aria-expanded", "false");
  });

  function addMessage(text, sender) {
    const msgDiv = document.createElement("div");
    msgDiv.classList.add("chat-message", sender);
    
    if (sender === "bot" && typeof marked !== 'undefined') {
        const template = document.createElement("template");
        template.innerHTML = marked.parse(text);
        const allowed = new Set(["P","BR","STRONG","EM","UL","OL","LI","A","CODE","PRE","BLOCKQUOTE"]);
        function clean(parent) {
          for (const node of Array.from(parent.childNodes)) {
            if (node.nodeType === Node.COMMENT_NODE) { node.remove(); continue; }
            if (node.nodeType !== Node.ELEMENT_NODE) continue;
            if (["SCRIPT","STYLE","IFRAME","OBJECT","SVG","MATH"].includes(node.tagName)) { node.remove(); continue; }
            clean(node);
            if (!allowed.has(node.tagName)) { node.replaceWith(...node.childNodes); continue; }
            const href = node.getAttribute("href") || "";
            for (const attr of Array.from(node.attributes)) node.removeAttribute(attr.name);
            if (node.tagName === "A") {
              if (/^\/home\/product\/\d+$/.test(href)) { node.href = href; node.className = "chat-product-link"; }
              else node.replaceWith(...node.childNodes);
            }
          }
        }
        clean(template.content);
        msgDiv.appendChild(template.content);
    } else {
        msgDiv.textContent = text;
    }
    
    chatMessages.appendChild(msgDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  function addProducts(products) {
    if (!Array.isArray(products) || !products.length) return;
    const list = document.createElement("div"); list.className = "chat-product-list";
    for (const product of products.slice(0, 10)) {
      if (!/^\/home\/product\/\d+$/.test(product.url || "")) continue;
      const card = document.createElement("a"); card.className = "chat-product-card"; card.href = product.url;
      if (product.image) {
        try {
          const url = new URL(product.image, location.origin);
          if (["https:", "http:"].includes(url.protocol)) {
            const img = document.createElement("img"); img.src = url.href; img.alt = product.name || "Sản phẩm"; img.loading = "lazy";
            img.addEventListener("error", () => img.remove(), {once:true}); card.appendChild(img);
          }
        } catch (_) {}
      }
      const info = document.createElement("div"); info.className = "chat-product-info";
      const name = document.createElement("strong"); name.textContent = product.name; info.appendChild(name);
      const money = value => Number(value).toLocaleString("en-US") + " Vnđ";
      if (product.discount > 0) { const old = document.createElement("del"); old.textContent = money(product.price); info.appendChild(old); }
      const price = document.createElement("span"); price.className = "chat-product-price"; price.textContent = money(product.salePrice); info.appendChild(price);
      const action = document.createElement("small"); action.textContent = "Xem sản phẩm →"; info.appendChild(action);
      card.appendChild(info); list.appendChild(card);
    }
    chatMessages.appendChild(list); chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  function sendMessage() {
    const text = chatInput.value.trim();
    if (!text || sending || loadingHistory) return;
    sending = true; controls();

    addMessage(text, "user");
    chatInput.value = "";
    chatTyping.style.display = "block";

    const headers = {"Content-Type":"application/json"};
    const csrf = document.getElementById("chatCsrf");
    if (csrf && csrf.value && csrf.dataset.header) headers[csrf.dataset.header] = csrf.value;
    fetch("/api/v1/chat", {
      method: "POST",
      headers,
      body: JSON.stringify({
        message: text,
        conversationId: conversationId
      })
    })
    .then(async response => { if (!response.ok) {
      if(response.status === 409) throw new Error("Cuộc trò chuyện đang xử lý câu trả lời. Vui lòng đợi rồi mở lại chatbot.");
      throw new Error("Không gửi được tin nhắn. Hãy mở lại chatbot để kiểm tra lịch sử trước khi gửi lại.");
    } return response.json(); })
    .then(data => {
      conversationId = data.conversationId;
      chatTyping.style.display = "none";
      addMessage(data.response || "Chưa có phản hồi.", "bot");
      addProducts(data.products);
      if(historyStatus) historyStatus.textContent="Kéo lên để xem các cuộc trò chuyện trước.";
    })
    .catch(error => {
      console.error("Error:", error);
      chatTyping.style.display = "none";
      addMessage(error.message || "Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.", "bot");
    }).finally(() => { sending = false; controls(); chatTyping.style.display = "none"; });
  }

  chatSend.addEventListener("click", sendMessage);
  chatInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter") sendMessage();
  });

}
if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", initChatbot, {once:true});
else initChatbot();
})();
