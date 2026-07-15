document.addEventListener('DOMContentLoaded', function () {
    const toggleBtn = document.getElementById('aiChatToggleBtn');
    const closeBtn  = document.getElementById('aiChatCloseBtn');
    const chatWindow = document.getElementById('aiChatWindow');
    const form = document.getElementById('aiChatForm');
    const input = document.getElementById('aiChatInput');
    const messages = document.getElementById('aiChatMessages');

    if (!toggleBtn || !form) return;

    toggleBtn.addEventListener('click', () => {
        chatWindow.classList.toggle('is-hidden');
        chatWindow.classList.toggle('is-open');

        if (chatWindow.classList.contains('is-open')) {
            input.focus();

            // Khung chat vừa chuyển từ display:none -> flex.
            // Cần đợi trình duyệt hoàn tất 1 chu kỳ render (layout/paint)
            // thì scrollHeight mới đo được chính xác, nên dùng double rAF.
            requestAnimationFrame(() => {
                requestAnimationFrame(scrollToBottom);
            });
        }
    });

    closeBtn.addEventListener('click', () => {
        chatWindow.classList.remove('is-open');
        chatWindow.classList.add('is-hidden');
    });

    // ── VẤN ĐỀ 1: render bằng innerHTML (đã an toàn vì server đã escape) ──
    function scrollToBottom() {
        messages.scrollTop = messages.scrollHeight;
    }

    function appendMessage(html, fromUser) {
        const bubble = document.createElement('div');
        bubble.className = 'ai-chat-bubble ' + (fromUser ? 'user bg-primary text-white' : 'ai bg-surface-container');
        bubble.innerHTML = html;
        messages.appendChild(bubble);
        scrollToBottom();
        return bubble;
    }

    function getCsrf() {
        const token = document.querySelector('meta[name="_csrf"]');
        const header = document.querySelector('meta[name="_csrf_header"]');
        return token && header ? { token: token.content, header: header.content } : null;
    }

    // ── VẤN ĐỀ 2: khôi phục lịch sử chat khi trang vừa load ────────────
    function loadHistory() {
        fetch('/ai-chat/history')
            .then(res => res.json())
            .then(history => {
                if (history && history.length > 0) {
                    messages.innerHTML = ''; // bỏ bubble chào mừng mặc định
                    history.forEach(turn => {
                        appendMessage(turn.content, turn.sender === 'user');
                    });
                }
                // Nếu history rỗng → giữ nguyên bubble chào mừng có sẵn trong HTML
                scrollToBottom();
            })
            .catch(() => { /* im lặng bỏ qua, giữ bubble chào mừng mặc định */ });
    }
    loadHistory();

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        const message = input.value.trim();

        if (!message) {
            appendMessage('Vui lòng nhập tin nhắn.', false);
            return;
        }

        // Escape khi hiển thị tin nhắn user (client tự gõ, phòng XSS phía hiển thị)
        const escapedMsg = message.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
        appendMessage(escapedMsg, true);
        input.value = '';
        input.disabled = true;

        const typingBubble = appendMessage('Đang trả lời...', false);

        const csrf = getCsrf();
        const headers = { 'Content-Type': 'application/json' };
        if (csrf) headers[csrf.header] = csrf.token;

        fetch('/ai-chat/send', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ message: message })
        })
            .then(res => res.json())
            .then(data => {
                typingBubble.remove();
                appendMessage(data.success ? data.reply : (data.errorMessage || 'Đã có lỗi xảy ra.'), false);
            })
            .catch(() => {
                typingBubble.remove();
                appendMessage('Không thể kết nối tới trợ lý AI. Vui lòng thử lại.', false);
            })
            .finally(() => {
                input.disabled = false;
                input.focus();
            });
    });
});