/**
 * Hiển thị thông báo với Bootstrap Alert
 * @param {string} message - Nội dung thông báo
 * @param {string} type - Loại thông báo (success, danger, warning, info, primary, secondary, dark, light)
 * @param {boolean} showCloseBtn - Hiển thị nút đóng (mặc định: false)
 * @param {number} autoClose - Tự động đóng sau x giây (mặc định: 5 giây, 0 = không tự đóng)
 */
function showMessage(message, type = 'success', showCloseBtn = false, autoClose = 5) {
    // Tạo ID unique cho alert
    const alertId = 'alert-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);

    // Icon tương ứng với từng loại thông báo
    const icons = {
        success: '✓',
        danger: '✗',
        warning: '⚠',
        info: 'ℹ',
        primary: '●',
        secondary: '●',
        dark: '●',
        light: '●'
    };

    // Tạo HTML cho alert
    const alertHtml = `
                <div id="${alertId}" class="alert alert-${type} message-alert fade-in" role="alert">
                    <span class="me-2">${icons[type] || '●'}</span>
                    <span>${message}</span>
                    ${showCloseBtn ? '<button type="button" class="btn-close" onclick="closeMessage(\'' + alertId + '\')"></button>' : ''}
                </div>
            `;

    // Thêm alert vào container
    $('#messageContainer').append(alertHtml);

    // Tự động đóng nếu được thiết lập
    if (autoClose > 0) {
        setTimeout(() => {
            closeMessage(alertId);
        }, autoClose * 1000);
    }
}

/**
 * Đóng thông báo
 * @param {string} alertId - ID của alert cần đóng
 */
function closeMessage(alertId) {
    const alert = $('#' + alertId);
    if (alert.length) {
        alert.removeClass('fade-in').addClass('fade-out');
        setTimeout(() => {
            alert.remove();
        }, 300);
    }
}

/**
 * Đóng tất cả thông báo
 */
function closeAllMessages() {
    $('.message-alert').each(function() {
        closeMessage($(this).attr('id'));
    });
}

// Các hàm shortcut cho từng loại thông báo
function showSuccess(message, autoClose = 5) {
    showMessage(message, 'success', false, autoClose);
}

function showError(message, autoClose = 5) {
    showMessage(message, 'danger', false, autoClose);
}

function showWarning(message, autoClose = 5) {
    showMessage(message, 'warning', false, autoClose);
}

function showInfo(message, autoClose = 5) {
    showMessage(message, 'info', false, autoClose);
}

/**
 * Hiển thị thông báo và reload trang sau một khoảng thời gian
 * @param {string} message - Nội dung thông báo
 * @param {string} type - Loại thông báo
 * @param {number} delaySeconds - Số giây delay trước khi reload (mặc định: 3s)
 */
function showMessageAndReload(message, type = 'success', delaySeconds = 3) {
    // Hiển thị thông báo (không tự động đóng)
    showMessage(message, type, false, 0);

    // Reload sau delay
    setTimeout(() => {
        location.reload();
    }, delaySeconds * 1000);
}

/**
 * Shortcut functions với reload
 */
function showSuccessAndReload(message, delaySeconds = 3) {
    showMessageAndReload(message, 'success', delaySeconds);
}

function showErrorAndReload(message, delaySeconds = 3) {
    showMessageAndReload(message, 'danger', delaySeconds);
}
