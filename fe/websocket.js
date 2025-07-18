// Enhanced WebSocket connection with better error handling and debugging
function connectSocket(token) {
    console.log('Connecting WebSocket with token:', token);

    var socket = new SockJS(WS_URL);
    var stompClient = Stomp.over(socket);

    // Enable debug mode
    stompClient.debug = function(str) {
        console.log('STOMP Debug: ' + str);
    };

    // Connection headers
    var headers = {
        'Authorization': 'Bearer ' + token
    };

    // Connect with error handling
    stompClient.connect(headers,
        function(frame) {
            console.log('✓ WebSocket Connected: ' + frame);

            // Subscribe to user-specific channel
            stompClient.subscribe('/user/queue/order-status', function(message) {
                console.log('✓ Received user message:', message.body);
                var order = JSON.parse(message.body);
                console.log('✓ Parsed order:', order);

                if(order.status === "COMPLETED" || order.status === "") {
                    showSuccessAndReload("Checkout successful! Order completed.", 1.5);
                } else {
                    showInfo(order.status);
                    $("#checkout").prop("disabled", false);
                }
                $("#checkoutSpinner").remove();
            });

            // Subscribe to notifications
            stompClient.subscribe('/user/queue/notifications', function(message) {
                console.log('✓ Received notifications:', message.body);
                var notifications = JSON.parse(message.body);
                handleNotifications(notifications);
            });

            // Subscribe to notifications
            stompClient.subscribe('/topic/product-quantity', function(message) {
                console.log('✓ Received product quantity:', message.body);
                var productQuantity = JSON.parse(message.body);
                if(productQuantity && productQuantity.length > 0){
                    productQuantity.forEach(function(cartItem) {
                        var product = products.find(p => p.id === cartItem.productId);
                        if (product) {
                            var oldQuantity = product.quantity;
                            product.quantity -= cartItem.quantity;

                            // Cập nhật với animation
                            updateQuantityWithAnimation(product.id, product.quantity, oldQuantity);

                            console.log('→ Cập nhật số lượng sản phẩm ID', product.id, 'thành', product.quantity);
                        }
                    });
                }
            });

            console.log('✓ Subscriptions completed');
        },
        function(error) {
            console.error('✗ WebSocket connection error:', error);
            // Retry connection after 5 seconds
            setTimeout(function() {
                console.log('Retrying WebSocket connection...');
                connectSocket(token);
            }, 5000);
        }
    );

    // Handle WebSocket close
    socket.onclose = function(event) {
        console.log('WebSocket closed:', event);
    };

    return stompClient;
}
