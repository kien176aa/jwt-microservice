$(document).ready(function() {
    let isScrolledToBottom = true;
    let newMessageCount = 0;

    // Check if scrolled to bottom
    function checkScrollPosition() {
        const container = $('#ai-chat-messages-container');
        const scrollTop = container.scrollTop();
        const scrollHeight = container[0].scrollHeight;
        const clientHeight = container[0].clientHeight;

        isScrolledToBottom = scrollTop + clientHeight >= scrollHeight - 10;

        if (isScrolledToBottom) {
            $('#ai-chat-scroll-indicator').hide();
            newMessageCount = 0;
        } else if (newMessageCount > 0) {
            $('#ai-chat-scroll-indicator').show();
        }
    }

    // Smooth scroll to bottom
    function scrollToBottom(force = false) {
        const container = $('#ai-chat-messages-container');
        if (force || isScrolledToBottom) {
            container.animate({
                scrollTop: container[0].scrollHeight
            }, 300);
            isScrolledToBottom = true;
            $('#ai-chat-scroll-indicator').hide();
            newMessageCount = 0;
        }
    }

    // Format AI response with proper styling
    function formatAIResponse(text) {
        // Convert line breaks to HTML
        let formatted = text.replace(/\n/g, '<br>');

        // Convert **bold** to <strong>
        formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

        // Convert *italic* to <em>
        formatted = formatted.replace(/\*(.*?)\*/g, '<em>$1</em>');

        // Convert `code` to <code>
        formatted = formatted.replace(/`(.*?)`/g, '<code>$1</code>');

        // Convert numbered lists (1. item)
        formatted = formatted.replace(/^(\d+\.\s+)(.*)$/gm, '<div class="ai-chat-list-item"><span class="ai-chat-list-number">$1</span>$2</div>');

        // Convert bullet points (- item or * item)
        formatted = formatted.replace(/^[-*]\s+(.*)$/gm, '<div class="ai-chat-list-item"><span class="ai-chat-bullet">•</span> $1</div>');

        // Convert headings (### heading)
        formatted = formatted.replace(/^###\s+(.*)$/gm, '<h4 class="ai-chat-heading">$1</h4>');
        formatted = formatted.replace(/^##\s+(.*)$/gm, '<h3 class="ai-chat-heading">$1</h3>');
        formatted = formatted.replace(/^#\s+(.*)$/gm, '<h2 class="ai-chat-heading">$1</h2>');

        // Wrap in a container for better styling
        return `<div class="ai-chat-formatted-content">${formatted}</div>`;
    }

    // Handle scroll events
    $('#ai-chat-messages-container').on('scroll', function() {
        checkScrollPosition();
    });

    // Click scroll indicator to scroll to bottom
    $('#ai-chat-scroll-indicator').click(function() {
        scrollToBottom(true);
    });

    // Toggle chat window
    $('#ai-chat-floating-btn').click(function() {
        $('#ai-chat-window-container').toggle();
        if ($('#ai-chat-window-container').is(':visible')) {
            scrollToBottom(true);
            $('#ai-chat-input-field').focus();
        }
    });

    // Close chat
    $('#ai-chat-close-btn').click(function() {
        $('#ai-chat-window-container').hide();
    });

    // Send message function
    function sendMessage() {
        const message = $('#ai-chat-input-field').val().trim();
        if (!message) return;

        // Add user message
        $('#ai-chat-messages-container').append(`
                    <div class="ai-chat-message ai-chat-user-message">${escapeHtml(message)}</div>
                `);

        // Clear input
        $('#ai-chat-input-field').val('');

        // Show loading
        $('#ai-chat-messages-container').append(`
                    <div class="ai-chat-message ai-chat-loading">AI đang suy nghĩ</div>
                `);

        // Disable send button
        $('#ai-chat-send-btn').prop('disabled', true);

        // Auto scroll for user messages
        scrollToBottom(true);

        // Call API
        $.ajax({
            url: `${API_BASE}/orders/ask`,
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` },
            data: { q: message },
            success: function(response) {
                // Remove loading message
                $('.ai-chat-loading').remove();

                // Add AI response with formatting
                const formattedResponse = formatAIResponse(response);
                $('#ai-chat-messages-container').append(`
                            <div class="ai-chat-message ai-chat-ai-message">${formattedResponse}</div>
                        `);

                // Handle scroll for AI response
                if (isScrolledToBottom) {
                    scrollToBottom();
                } else {
                    newMessageCount++;
                    $('#ai-chat-scroll-indicator')
                        .text(`↓ ${newMessageCount} tin nhắn mới`)
                        .show();
                }
            },
            error: function(xhr, status, error) {
                // Remove loading message
                $('.ai-chat-loading').remove();

                // Add error message
                $('#ai-chat-messages-container').append(`
                            <div class="ai-chat-message ai-chat-ai-message">❌ Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại!</div>
                        `);

                // Handle scroll for error message
                if (isScrolledToBottom) {
                    scrollToBottom();
                } else {
                    newMessageCount++;
                    $('#ai-chat-scroll-indicator')
                        .text(`↓ ${newMessageCount} tin nhắn mới`)
                        .show();
                }
            },
            complete: function() {
                // Re-enable send button
                $('#ai-chat-send-btn').prop('disabled', false);
                $('#ai-chat-input-field').focus();
            }
        });
    }

    // Escape HTML to prevent XSS (for user messages)
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // Send message on button click
    $('#ai-chat-send-btn').click(sendMessage);

    // Send message on Enter key
    $('#ai-chat-input-field').keypress(function(e) {
        if (e.which === 13 && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // Auto-resize input (if needed)
    $('#ai-chat-input-field').on('input', function() {
        this.style.height = 'auto';
        this.style.height = Math.min(this.scrollHeight, 100) + 'px';
    });
});