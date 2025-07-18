
function loadProducts(req) {
    if (!token) {
        console.log("Please log in to view products.");
        return;
    }
    $("#product-list").html("<div class='text-center my-4'><div class='spinner-border text-primary' role='status'><span class='visually-hidden'>Loading...</span></div></div>");
    $.ajax({
        url: `${API_BASE}/products/e-search`,
        method: "POST",
        headers: { 'Authorization': `Bearer ${token}` },
        data: JSON.stringify(req),
        contentType: "application/json",
        success: function(response) {
            products = response.data;
            currentPageProducts = response.pageIndex;
            itemsPerPageProducts = response.pageSize;
            totalProducts = response.totalRecords;
            renderProducts();
        },
        error: function(xhr, status, error) {
            console.error("Error fetching orders:", error);
            showError("Fail to fetch product!!");
        }
    });
}

function renderProducts() {
    // let start = (currentPageProducts - 1) * itemsPerPageProducts;
    // let end = start + itemsPerPageProducts;
    // let paginatedProducts = products.slice(start, end);
    let html = "";
    products.forEach(product => {
        html += `<div class="col-md-3 mb-3">
                      <div class="card h-100">
                        <img src="${product.image}" class="card-img-top" alt="${product.name}">
                        <div class="card-body">
                          <h5 class="card-title">${product.name}</h5>
                          <p class="card-text">$${product.price}</p>
                          <p class="card-text">Quantity: <span id="product-qty-${product.id}">${product.quantity}</span></p>
                          <button class="btn btn-primary add-to-cart" data-id="${product.id}">Add to Cart</button>
                          <button class="btn btn-secondary edit-product ms-2" data-id="${product.id}">Edit</button>
                        </div>
                      </div>
                    </div>`;
    });
    $("#product-list").html(html);
    renderProductPagination();
}


function renderProductPagination() {
    let totalPages = Math.ceil(totalProducts / itemsPerPageProducts);
    let paginationHtml = "";
    for(let i = 1; i <= totalPages; i++){
        paginationHtml += `<li class="page-item ${i === currentPageProducts+1 ? 'active' : ''}"><a class="page-link product-page" href="#" data-page="${i-1}">${i}</a></li>`;
    }
    $("#productPagination").html(paginationHtml);
    $(".product-page").off("click").on("click",function(e) {
        e.preventDefault();
        currentPageProducts = parseInt($(this).data("page"));
        searchProductReq.page = currentPageProducts;
        loadProducts(searchProductReq);
    });
}

function resetProductForm() {
    $("#productId").val("");
    $("#productName").val("");
    $("#productPrice").val("");
    $("#productQuantity").val("");
    $("#productImage").val("");
    $("#attributesContainer").empty();
    addAttributeRow(); // Thêm 1 row trống
}

// Xử lý thêm attribute row
function addAttributeRow(key = '', value = '') {
    let attributeRow = `
        <div class="attribute-row mb-2">
            <div class="row">
                <div class="col-5">
                    <input type="text" class="form-control attribute-key" placeholder="Attribute name" value="${key}">
                </div>
                <div class="col-5">
                    <input type="text" class="form-control attribute-value" placeholder="Attribute value" value="${value}">
                </div>
                <div class="col-2">
                    <button type="button" class="btn btn-danger btn-sm remove-attribute">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        </div>
    `;
    $("#attributesContainer").append(attributeRow);
}

function handleAddEditProduct(){
    let attributes = {};
    $(".attribute-row").each(function() {
        let key = $(this).find(".attribute-key").val().trim();
        let value = $(this).find(".attribute-value").val().trim();
        if (key && value) {
            attributes[key] = value;
        }
    });

    let prod = {
        id: $("#productId").val(),
        name: $("#productName").val(),
        price: parseFloat($("#productPrice").val()),
        quantity: $("#productQuantity").val(),
        image: $("#productImage").val(),
        description: '',
        attributes: attributes  // Thêm attributes vào object
    };

    let spinner = `<div id="productSpinner" class="spinner-overlay">
                 <div class="spinner-border text-primary" role="status">
                   <span class="visually-hidden">Loading...</span>
                 </div>
               </div>`;
    $("#productModal .modal-body").append(spinner);
    $("#saveProduct").prop("disabled", true);

    if(!prod.id){
        prod.id = null;
        $.ajax({
            url: `${API_BASE}/products/add`,
            method: "POST",
            headers: { 'Authorization': `Bearer ${token}` },
            contentType: "application/json",
            data: JSON.stringify(prod),
            success: function(response) {
                console.log("Product added:", response);

                // Thêm product mới vào array products
                if (response && response.id) {
                    products.push(response);

                    // Render lại danh sách products
                    renderProducts();

                    // Đóng modal và reset form
                    $("#productModal").modal("hide");
                    resetProductForm();

                    // Hiển thị thông báo thành công
                    showSuccess("Product added successfully!");
                } else {
                    showError(response);
                }
            },
            error: function() {
                $("#productSpinner").remove();
                $("#saveProduct").prop("disabled", false);
                showError("Fail to add product.");
            },
            complete: function() {
                $("#productSpinner").remove();
                $("#saveProduct").prop("disabled", false);
            }
        });
    } else {
        $.ajax({
            url: `${API_BASE}/products/update/${prod.id}`,
            method: "PUT",
            headers: { 'Authorization': `Bearer ${token}` },
            contentType: "application/json",
            data: JSON.stringify(prod),
            success: function(response) {
                console.log("Product updated:", response);

                // Tìm và cập nhật product trong array
                let productIndex = products.findIndex(p => p.id == prod.id);
                if (productIndex !== -1) {
                    // Cập nhật product với dữ liệu từ response
                    products[productIndex] = response && response.id ? response : {
                        id: prod.id,
                        name: prod.name,
                        price: prod.price,
                        quantity: prod.quantity,
                        image: prod.image,
                        description: prod.description,
                        attributes: prod.attributes
                    };

                    // Render lại danh sách products
                    renderProducts();

                    // Đóng modal và reset form
                    $("#productModal").modal("hide");
                    resetProductForm();

                    // Hiển thị thông báo thành công
                    showSuccess("Product updated successfully!");
                } else {
                    showError("Product not found in local data.");
                }
            },
            error: function(xhr, status, error) {
                console.error("Error updating product:", error);
                showError("Failed to update product.");
            },
            complete: function() {
                $("#productSpinner").remove();
                $("#saveProduct").prop("disabled", false);
            }
        });
    }
}

function syncProducts(token) {
    $.ajax({
        url: `${API_BASE}/products/sync-products`,
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(response) {
            console.log('Sync successful:', response);
            showSuccess('Đồng bộ sản phẩm thành công!');
        },
        error: function(xhr, status, error) {
            console.error('Sync failed:', error);
            showError('Lỗi khi đồng bộ sản phẩm: ' + (xhr.responseText || error));
        }
    });
}

function updateQuantityWithAnimation(productId, newQuantity, oldQuantity) {
    var quantityElement = $(`#product-qty-${productId}`);

    // Xóa các class animation cũ
    quantityElement.removeClass('quantity-decrease quantity-pulse quantity-low quantity-zero');

    // Cập nhật số
    quantityElement.text(newQuantity);

    // Thêm animation dựa trên tình huống
    if (newQuantity < oldQuantity) {
        quantityElement.addClass('quantity-decrease');

        // Nếu số lượng thấp (dưới 5)
        if (newQuantity > 0 && newQuantity <= 5) {
            setTimeout(() => {
                quantityElement.addClass('quantity-low');
            }, 600);
        }

        // Nếu hết hàng
        if (newQuantity === 0) {
            setTimeout(() => {
                quantityElement.addClass('quantity-zero');
            }, 600);
        }
    } else {
        // Nếu số lượng tăng (có thể do restock)
        quantityElement.addClass('quantity-pulse');
    }

    // Xóa animation sau khi hoàn thành
    setTimeout(() => {
        quantityElement.removeClass('quantity-decrease quantity-pulse quantity-low quantity-zero');
    }, 1000);
}
