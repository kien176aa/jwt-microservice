
// Notification handling functions
function handleNotifications(notifications) {
    if (notifications && notifications.length > 0) {
        // Update notification badge
        updateNotificationBadge(notifications.length);

        // Render notifications in dropdown
        renderNotifications(notifications);

        // Show toast for new notifications (optional)
        showNotificationToast(notifications[0]);
    }
}

function renderNotifications(notifications) {
    var container = $('#notifications-container');
    container.empty();

    if (notifications.length === 0) {
        container.append(`
            <div class="dropdown-item text-center py-3">
                <i class="fas fa-bell-slash"></i>
                <div>Không có thông báo mới</div>
            </div>
        `);
        return;
    }

    notifications.forEach(function(notification) {
        var timeAgo = formatTimeAgo(notification.createdAt);
        var readClass = notification.isRead ? 'read' : 'unread';

        var notificationHtml = `
            <div class="dropdown-item notification-item ${readClass}" data-id="${notification.id}">
                <div class="d-flex align-items-start">
                    <div class="notification-icon me-3">
                        <i class="fas fa-bell text-primary"></i>
                    </div>
                    <div class="notification-content flex-grow-1">
                        <h6 class="notification-title mb-1">${escapeHtml(notification.title)}</h6>
                        <p class="notification-message mb-1">${escapeHtml(notification.message)}</p>
                        <small class="notification-time">
                            <i class="fas fa-clock me-1"></i>${timeAgo}
                        </small>
                    </div>
                    ${!notification.isRead ? '<div class="notification-dot"></div>' : ''}
                </div>
            </div>
        `;

        container.append(notificationHtml);
    });

    // Add click handlers
    $('.notification-item').on('click', function() {
        var notificationId = $(this).data('id');
        markNotificationAsRead(notificationId, $(this));
    });
}

function markNotificationAsRead(notificationId, element) {
    // Make API call to mark as read
    $.ajax({
        url: `/api/notifications/${notificationId}/read`,
        method: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function() {
            // Update UI
            element.removeClass('unread').addClass('read');
            element.find('.notification-dot').remove();

            // Update badge count
            var currentCount = parseInt($('#notification-badge').text()) || 0;
            if (currentCount > 0) {
                updateNotificationBadge(currentCount - 1);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error marking notification as read:', error);
        }
    });
}

function showNotificationToast(notification) {
    var timeAgo = formatTimeAgo(notification.createdAt);
    // Show bootstrap toast for new notification
    var toastHtml = `
        <div class="toast notification-toast" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="toast-header">
                <i class="fas fa-bell text-primary me-2"></i>
                <strong class="me-auto">${escapeHtml(notification.title)}</strong>
                <small class="">${timeAgo}</small>
                <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
            </div>
            <div class="toast-body">
                ${escapeHtml(notification.message)}
            </div>
        </div>
    `;

    // Add to toast container
    if (!$('#toast-container').length) {
        $('body').append('<div id="toast-container" class="toast-container position-fixed top-0 end-0 p-3"></div>');
    }

    var $toast = $(toastHtml);
    $('#toast-container').append($toast);

    // Initialize and show toast
    var toast = new bootstrap.Toast($toast[0], {
        autohide: true,
        delay: 5000
    });
    toast.show();

    // Remove toast element after it's hidden
    $toast.on('hidden.bs.toast', function() {
        $(this).remove();
    });
}

// Utility functions
function formatTimeAgo(dateString) {
    var date = new Date(dateString);
    var now = new Date();
    var diffInSeconds = Math.floor((now - date) / 1000);

    if (diffInSeconds < 60) {
        return 'Vừa xong';
    } else if (diffInSeconds < 3600) {
        return Math.floor(diffInSeconds / 60) + ' phút trước';
    } else if (diffInSeconds < 86400) {
        return Math.floor(diffInSeconds / 3600) + ' giờ trước';
    } else {
        return Math.floor(diffInSeconds / 86400) + ' ngày trước';
    }
}

function escapeHtml(text) {
    var map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, function(m) { return map[m]; });
}

// Load all notifications function
function loadAllNotifications() {
    $.ajax({
        url: '/api/notifications',
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(notifications) {
            renderNotifications(notifications);
            var unreadCount = notifications.filter(n => !n.isRead).length;
            updateNotificationBadge(unreadCount);
        },
        error: function(xhr, status, error) {
            console.error('Error loading notifications:', error);
        }
    });
}

function updateNotificationBadge() {
    const unreadCount = $('.notification-item.unread').length;
    const badge = $('#notification-badge');

    if (unreadCount > 0) {
        badge.text(unreadCount > 99 ? '99+' : unreadCount).removeClass('d-none');
    } else {
        badge.addClass('d-none');
    }
}
