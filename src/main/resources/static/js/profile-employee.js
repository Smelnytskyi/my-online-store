let currentPage = 1;
const itemsPerPage = 20;

const OrderStatus = {
    PENDING: "Ожидающие",
    CONFIRMED: "Подтвержденные",
    SHIPPED: "Отправленные",
    DELIVERED: "Доставленные",
    CANCELED: "Отмененные"
};

document.addEventListener("DOMContentLoaded", function () {

    loadEmployeeProfile();
    loadOrders();
    showTab("personal-info");
    fetchProducts();

    function loadEmployeeProfile() {
        fetchWithAuth('/client/profile', {})
            .then(response => response.json())
            .then(data => {
                document.getElementById("employee-firstName").textContent = data.firstName;
                document.getElementById("employee-lastName").textContent = data.lastName;
                document.getElementById("employee-phone").textContent = data.phone;
            })
            .catch(error => console.error("Ошибка загрузки профиля:", error));
    }

    document.getElementById("edit-personal-info").addEventListener("click", function () {
        document.getElementById("edit-info-form").style.display = "block";
    });


    document.getElementById("save-info").addEventListener("click", function () {
        const updatedData = {
            firstName: document.getElementById("input-firstName").value || null,
            lastName: document.getElementById("input-lastName").value || null,
            phone: document.getElementById("input-phone").value || null
        };

        fetchWithAuth('/client/profile/update', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedData)
        })
            .then(response => {
                if (response.ok) {
                    loadEmployeeProfile();
                    toggleEditSection('edit-info-form');
                    displayValidationErrors([], 'error-messages');
                    return response.json().catch(() => ({}));
                } else {
                    return response.json().then(result => {
                        if (result) {
                            displayValidationErrors(result, 'error-messages');
                        } else {
                            displayValidationErrors(["Неизвестная ошибка"], 'error-messages');
                        }
                    });
                }
            })
            .catch(error => {
                document.getElementById("error-messages").textContent = error.message;
                console.error("Ошибка:", error);
            });
    });

    document.getElementById("cancel-edit").addEventListener("click", function () {
        toggleEditSection('edit-info-form');
    });

    // Password update
    document.getElementById("update-password").addEventListener("click", function () {
        const oldPassword = document.getElementById("old-password").value;
        const newPassword = document.getElementById("new-password").value;

        fetchWithAuth('/client/profile/change-password', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({oldPassword, newPassword})
        })
            .then(response => {
                if (response.ok) {
                    alert("Пароль успешно обновлен");
                } else {
                    document.getElementById("password-error-messages").textContent = "Неверный пароль";
                }
            })
            .catch(error => console.error("Ошибка:", error));

    });

    function toggleEditSection(sectionId) {
        const section = document.getElementById(sectionId);
        const isVisible = section.style.display === "block";
        section.style.display = isVisible ? "none" : "block";
        document.getElementById("personal-info").style.display = isVisible ? "block" : "none";
    }

    // Filtering by status
    document.getElementById("order-status").addEventListener("change", function () {
        loadOrders(this.value);
    });

    // Event handler for the search button
    document.getElementById("search-button").addEventListener("click", function () {
        const orderId = document.getElementById("order-search").value;
        if (orderId) {
            searchOrderById(orderId);
        } else {
            loadOrders();
        }
    });

    // Event handler for pressing Enter
    document.getElementById("order-search").addEventListener("keyup", function (e) {
        if (e.key === "Enter" && this.value) {
            searchOrderById(this.value);
        }
    });

    // Event handler for the add product button
    document.getElementById('add-product').addEventListener('click', function () {
        document.getElementById('productForm').reset();
        document.getElementById('productId').value = '';
        document.getElementById('attributesContainer').innerHTML = '';
        openModal();
    });

    // Event handler for saving the product
    document.getElementById('saveProductButton').addEventListener('click', async function () {
        const productId = document.getElementById('productId').value;
        const productData = {
            name: document.getElementById('productName').value,
            category: document.getElementById('productCategory').value,
            description: document.getElementById('productDescription').value,
            price: parseFloat(document.getElementById('productPrice').value),
            quantity: parseInt(document.getElementById('productQuantity').value),
            attributes: {},
            imageUrl: null
        };

        if (!productData.category) {
            displayValidationErrors(['Пожалуйста, выберите категорию товара.'], 'product-validation-errors');
            return;
        }

        const attributeRows = document.getElementById('attributesContainer').getElementsByClassName('attribute-row');
        for (let row of attributeRows) {
            const key = row.querySelector('input:nth-child(1)').value;
            const value = row.querySelector('input:nth-child(2)').value;
            if (key && value) {
                productData.attributes[key] = value;
            }
        }

        const imageFile = document.getElementById('productImage').files[0];
        if (!imageFile && !productId) {
            displayValidationErrors(['Пожалуйста, загрузите изображение товара.'], 'product-validation-errors');
            return;
        } else if (!imageFile && productId) {
            saveProduct(productId, productData);
        } else {
            const reader = new FileReader();
            reader.onload = function (e) {
                productData.imageUrl = e.target.result.split(',')[1];
                saveProduct(productId, productData);
            };
            reader.readAsDataURL(imageFile);
        }
    });

    // Event handler for editing the product
    document.getElementById('products-list').addEventListener('click', async function (event) {
        if (event.target.classList.contains('editButton')) {
            const productId = event.target.dataset.id;
            const response = await fetch(`/main/product/${productId}`);
            const product = await response.json();

            document.getElementById('productId').value = product.id;
            document.getElementById('productName').value = product.name;
            document.getElementById('productCategory').value = product.category;
            document.getElementById('productDescription').value = product.description;
            document.getElementById('productPrice').value = product.price;
            document.getElementById('productQuantity').value = product.quantity;

            if (!product.category) {
                displayValidationErrors(['Пожалуйста, выберите категорию товара.'], 'product-validation-errors');
                return;
            }

            const attributesContainer = document.getElementById('attributesContainer');
            attributesContainer.innerHTML = '';

            if (typeof product.attributes === 'object' && product.attributes !== null) {
                Object.entries(product.attributes).forEach(([key, value]) => {
                    const attributeRow = document.createElement('div');
                    attributeRow.classList.add('attribute-row', 'd-flex', 'mb-2');
                    attributeRow.innerHTML = `
                    <input type="text" class="form-control mr-2" placeholder="Ключ" value="${key}" />
                    <input type="text" class="form-control mr-2" placeholder="Значение" value="${value}" />
                    <button type="button" class="btn btn-danger removeAttributeButton">Удалить</button>
                `;
                    attributesContainer.appendChild(attributeRow);
                });
            } else {
                console.warn('Attributes is not a valid object:', product.attributes);
            }
            openModal();
        }

        // Event handler for deleting the product
        if (event.target.classList.contains('deleteButton')) {
            const productId = event.target.dataset.id;
            if (confirm('Вы уверены, что хотите удалить этот товар?')) {
                await fetchWithAuth(`/employee/product/delete/${productId}`, {
                    method: 'DELETE'
                });
                fetchProducts();
            }
        }
    });

    // Event handler for searching products
    document.getElementById('searchButton').addEventListener('click', async function () {
        const quantity = parseInt(document.getElementById('product-quantity').value);
        if (quantity) {
            fetchProducts(1, quantity + 1);
        } else {
            fetchProducts();
        }
    });

    // Function for adding a new attribute row
    document.getElementById('addAttributeButton').addEventListener('click', function () {
        const attributeRow = document.createElement('div');
        attributeRow.classList.add('attribute-row', 'd-flex', 'mb-2');
        attributeRow.innerHTML = `
            <input type="text" class="form-control mr-2" placeholder="Ключ" />
            <input type="text" class="form-control mr-2" placeholder="Значение" />
            <button type="button" class="btn btn-danger removeAttributeButton">Удалить</button>
        `;
        document.getElementById('attributesContainer').appendChild(attributeRow);
    });

    // Event handler for removing an attribute
    document.getElementById('attributesContainer').addEventListener('click', function (event) {
        if (event.target.classList.contains('removeAttributeButton')) {
            const attributeRow = event.target.closest('.attribute-row');
            document.getElementById('attributesContainer').removeChild(attributeRow);
        }
    });
});


// Function for saving or updating the product
async function saveProduct(productId, productData) {
    const response = productId
        ? await fetchWithAuth(`/employee/product/update/${productId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(productData),
        })
        : await fetchWithAuth('/employee/product/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(productData),
        });

    if (response.ok) {
        closeModal();
        fetchProducts();
    } else {
        const result = await response.json();
        if (result) {
            displayValidationErrors(result, 'product-validation-errors');
        } else {
            document.getElementById('product-validation-errors').style.display = 'none';
        }
    }
}

// Displaying orders
function displayOrders(orders) {
    const ordersList = document.getElementById("orders-list");
    ordersList.innerHTML = "";
    orders.forEach(order => {
        const row = document.createElement("tr");
        row.innerHTML = `
                <td>${order.id}</td>
                <td>
                    <select onchange="updateOrderStatus(${order.id}, this.value)">
                        ${Object.keys(OrderStatus).map(status => `
                            <option value="${status}" ${order.orderStatus === status ? 'selected' : ''}>${OrderStatus[status]}</option>
                        `).join('')}
                    </select>
                </td>
                <td>${new Date(order.orderDate).toLocaleString()}</td>
                <td><button onclick="showOrderDetails(${order.id})" class="btn btn-info btn-sm">Детали</button></td>
                <td><button onclick="deleteOrder(${order.id})" class="btn btn-danger btn-sm">Удалить</button></td>
            `;
        ordersList.appendChild(row);
    });
}

// Loading orders on page load or status change
function loadOrders(status = "ALL", page = 1) {
    let url = `/employee/orders?page=${page - 1}&size=${itemsPerPage}`;
    if (status !== "ALL") {
        url = `/employee/orders-by-status?status=${status}&page=${page - 1}&size=${itemsPerPage}`;
    }

    fetchWithAuth(url,)
        .then(response => response.json())
        .then(data => {
            displayOrders(data.content);
            setupPagination(data.page.totalPages, page, (newPage) => loadOrders(status, newPage), 'pagination');
        })
        .catch(error => console.error("Ошибка загрузки заказов:", error));
}

function showTab(tabId) {
    document.querySelectorAll(".tab-content").forEach(tab => {
        tab.style.display = "none";
    });

    document.getElementById(tabId).style.display = "block";

    document.querySelectorAll(".list-group-item").forEach(button => {
        button.classList.remove("active");
    });
    document.getElementById(tabId + "-tab").classList.add("active");
}

function searchOrderById(orderId) {
    fetchWithAuth(`/employee/order/get-single/${orderId}`,)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status}`);
            }
            return response.json();
        })
        .then(order => {
            if (order && order.id) {
                displayOrders([order]);
            } else {
                console.error("Заказ не найден");
                alert("Заказ не найден");
            }
        })
        .catch(error => {
            console.error("Ошибка поиска заказа:", error);
            alert("Ошибка поиска заказа: Заказ не найден");
        });
}

// Deleting an order
function deleteOrder(orderId) {
    fetchWithAuth(`/employee/order/delete/${orderId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.ok) {
                loadOrders(document.getElementById("order-status").value);
            }
        })
        .catch(error => console.error("Ошибка удаления заказа:", error));
}

// Updating the order status
function updateOrderStatus(orderId, newStatus) {
    fetchWithAuth(`/employee/order/update/${orderId}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({orderStatus: newStatus})
    })
        .then(response => {
            if (response.ok) {
                loadOrders(document.getElementById("order-status").value);
            }
        })
        .catch(error => console.error("Ошибка обновления статуса:", error));
}

async function showOrderDetails(orderId) {
    try {
        const response = await fetchWithAuth(`/employee/order/get-single/${orderId}`, {});

        if (!response.ok) {
            throw new Error("Ошибка загрузки деталей заказа");
        }

        const order = await response.json();
        const products = await Promise.all(order.items.map(item => getProductById(item.productId)));

        const totalPrice = products.reduce((sum, product, index) => sum + (product.price * order.items[index].quantity), 0);

        const orderDetailsModal = document.createElement("div");
        orderDetailsModal.className = "modal";
        orderDetailsModal.id = "orderDetailsModal";
        orderDetailsModal.style.display = "block";
        orderDetailsModal.innerHTML = `
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Детали заказа #${order.id}</h5>
                        <button type="button" class="btn-close" onclick="closeOrderDetailsModal()"></button>
                    </div>
                    <div class="modal-body">
                        <p><strong>Получатель:</strong> ${order.clientFirstName} ${order.clientLastName}</p>
                        <p><strong>Адрес доставки:</strong> ${order.deliveryAddress}</p>
                        <p><strong>Заметки:</strong> ${order.notes || "Нет заметок"}</p>
                        <p><strong>Общая стоимость:</strong> ${totalPrice.toFixed(2)} ₴</p>
                        <h6>Товары:</h6>
                        <ul>
                            ${products.map((product, index) => `
                                <li>
                                    <img src="${product.imageUrl}" alt="${product.name}" style="width: 50px; height: 50px; margin-right: 10px;">
                                    ${product.name} - ${order.items[index].quantity} шт., ${product.price.toFixed(2)} ₴
                                </li>
                            `).join('')}
                        </ul>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" onclick="closeOrderDetailsModal()">Закрыть</button>
                    </div>
                </div>
            </div>
        `;

        document.body.appendChild(orderDetailsModal);

        window.closeOrderDetailsModal = function () {
            orderDetailsModal.style.display = "none";
            orderDetailsModal.remove();
        };
    } catch (error) {
        console.error("Ошибка при загрузке деталей заказа:", error);
    }
}

async function getProductById(productId) {
    const response = await fetch(`/main/product/${productId}`);
    if (!response.ok) {
        console.error(`Ошибка загрузки продукта с ID ${productId}`);
        return {name: 'Неизвестный товар'};
    }
    return await response.json();
}

// Universal function for setting up pagination
function setupPagination(totalPages, currentPage, loadFunction, paginationElementId) {
    const pagination = document.getElementById(paginationElementId);
    pagination.innerHTML = "";

    if (totalPages <= 1) {
        pagination.parentElement.style.display = "none";
    } else {
        pagination.parentElement.style.display = "block";

        for (let i = 1; i <= totalPages; i++) {
            const pageItem = document.createElement("li");
            pageItem.className = `page-item ${i === currentPage ? 'active' : ''}`;

            pageItem.innerHTML = `<a class="page-link" href="#">${i}</a>`;
            pageItem.addEventListener('click', (e) => {
                e.preventDefault();
                loadFunction(i);
            });

            pagination.appendChild(pageItem);
        }
    }
}

// Function for displaying products
function displayProducts(products) {
    document.getElementById('products-list').innerHTML = '';

    products.forEach(product => {
        const row = document.createElement('tr');
        row.innerHTML = `
                <td>${product.id}</td>
                <td><img src="${product.imageUrl}" alt="${product.name}" width="50" /></td>
                <td>${product.name}</td>
                <td>${product.quantity}</td>
                <td>
                    <button class="btn btn-warning editButton" data-id="${product.id}">Редактировать</button>
                    <button class="btn btn-danger deleteButton" data-id="${product.id}">Удалить</button>
                </td>
            `;
        document.getElementById('products-list').appendChild(row);
    });
}

// Fetching products list (e.g., on page load)
async function fetchProducts(page = 1, quantity = null) {
    let data;
    try {
        if (quantity === null) {
            const response = await fetch(`/main/products?page=${page - 1}&size=${itemsPerPage}`);
            data = await response.json();
        } else {
            const response = await fetch(`/employee/products-by-quantity?quantity=${quantity}`);
            data = await response.json();
        }
        displayProducts(data.content);
        setupPagination(data.page.totalPages, page, fetchProducts, 'products-pagination');
    } catch (error) {
        console.error("Ошибка загрузки товаров:", error);
    }
}

function openModal() {
    document.getElementById('productModal').style.display = 'block';
    document.getElementById('modalBackground').style.display = 'block';
}

function closeModal() {
    document.getElementById('productModal').style.display = 'none';
    document.getElementById('modalBackground').style.display = 'none';
}




