let products = [];
let orders = [];
let token;
const API_BASE = "http://localhost:8080";
const WS_URL = 'http://localhost:8083/ws';
// Các biến phân trang cho sản phẩm
let currentPageProducts = 1, itemsPerPageProducts = 8, totalProducts = 0;
let searchProductReq = {};
// Các biến phân trang cho đơn hàng
let currentPageOrders = 1, itemsPerPageOrders = 4000, totalRecords = 0;


function getCookie(name) {
    let match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return match ? match[2] : null;
}