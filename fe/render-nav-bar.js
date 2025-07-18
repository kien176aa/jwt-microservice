
function renderNavbarItems(hasToken) {
    // Tạo phần <ul> chứa các item
    let navHtml = `
    <ul class="navbar-nav ms-auto">
        <li class="nav-item"><a class="nav-link" href="#" id="view-products">Products</a></li>
        <li class="nav-item"><a class="nav-link" href="#myOrders" id="my-orders-btn">My Orders</a></li>
        <li class="nav-item"><a class="nav-link" href="#" id="cart-icon" data-bs-toggle="modal" data-bs-target="#cartModal">Cart</a></li>
`;

    // Thêm thông báo chỉ khi user đã đăng nhập
    if (hasToken) {
        navHtml += `
        <li class="nav-item"><a class="nav-link" href="#" id="sync-products">Sync Products</a></li>
        <!-- Notification Dropdown -->
        <li class="nav-item dropdown">
            <a class="nav-link position-relative" href="#" id="notificationDropdown"
               role="button" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="fas fa-bell fs-5"></i>
                <span id="notification-badge" class="notification-badge d-none">0</span>
            </a>

            <div class="dropdown-menu dropdown-menu-end notification-dropdown"
                 aria-labelledby="notificationDropdown">

                <!-- Header -->
                <div class="notification-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <h6 class="mb-0">
                            <i class="fas fa-bell me-2"></i>Thông báo
                        </h6>
                        <button class="btn btn-sm" onclick="loadAllNotifications()">
                            <i class="fas fa-refresh"></i>
                        </button>
                    </div>
                </div>

                <!-- Notifications Container -->
                <div id="notifications-container">
                    <!-- Loading state -->
                    <div class="notification-loading">
                        <i class="fas fa-spinner fa-spin"></i>
                        <div>Đang tải thông báo...</div>
                    </div>
                </div>

                <!-- Footer -->
<!--                <div class="notification-footer">-->
<!--                    <a href="/notifications" class="btn btn-sm btn-link">-->
<!--                        <i class="fas fa-external-link-alt me-1"></i>-->
<!--                        Xem tất cả thông báo-->
<!--                    </a>-->
<!--                </div>-->
            </div>
        </li>
        `;
    }

    if (hasToken) {
        navHtml += `
        <li class="nav-item"><a class="nav-link" href="#" id="logout-btn">Logout</a></li>
    `;
    } else {
        navHtml += `
        <li class="nav-item"><a class="nav-link" href="#" data-bs-toggle="modal" data-bs-target="#loginModal">Login</a></li>
        <li class="nav-item"><a class="nav-link" href="#" data-bs-toggle="modal" data-bs-target="#registerModal">Register</a></li>
    `;
    }

    navHtml += `</ul>`;

    // Render vào div#navbarNav
    $('#navbarNav').html(navHtml);

    // Gán sự kiện logout nếu có
    $('#logout-btn').click(function (e) {
        e.preventDefault();
        document.cookie = "token=; Max-Age=0; path=/;";
        location.reload();
    });

    $('#sync-products').click(function (e) {
        e.preventDefault();
        syncProducts(hasToken);
    });
}
