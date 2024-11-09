let currentPage = 0;
const pageSize = 20;

window.onload = function () {
    loadProfile();
    checkPasswordExistence();
};

function showProfileSection() {
    document.getElementById('profile-section').style.display = 'block';
    document.getElementById('orders-section').style.display = 'none';
    document.getElementById('profile-button').classList.add('active');
    document.getElementById('orders-button').classList.remove('active');
    loadProfile();
    checkPasswordExistence();
}

function showOrdersSection() {
    document.getElementById('profile-section').style.display = 'none';
    document.getElementById('orders-section').style.display = 'block';
    document.getElementById('orders-button').classList.add('active');
    document.getElementById('profile-button').classList.remove('active');
    loadOrders();
}

function toggleEditSection(sectionId) {
    const section = document.getElementById(sectionId);
    section.style.display = section.style.display === "none" ? "block" : "none";
}

function toggleOrderDetails(orderId) {
    const order = document.getElementById(orderId);
    order.style.display = order.style.display === "none" ? "block" : "none";
}

async function loadProfile() {
    try {
        const response = await fetchWithAuth('/client/profile', {
            method: 'GET',
        });
        if (response.ok) {
            const data = await response.json();
            displayProfileData(data);
        } else {
            console.error('Ошибка загрузки профиля');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

function displayProfileData(data) {
    document.getElementById('first-name').innerText = data.firstName;
    document.getElementById('last-name').innerText = data.lastName;
    document.getElementById('client-phone').innerText = data.phone;
    document.getElementById('client-address').innerText = data.address;
}

async function updateProfile() {
    const updatedData = {
        firstName: document.getElementById('edit-first-name').value || null,
        lastName: document.getElementById('edit-last-name').value || null,
        phone: document.getElementById('edit-phone').value || null,
        address: document.getElementById('edit-address').value || null
    };

    try {
        const response = await fetchWithAuth('/client/profile/update', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedData)
        });

        if (response.ok) {
            alert('Данные обновлены');
            await loadProfile();
        } else {
            const errorData = await response.json();
            displayValidationErrors(errorData);
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

async function checkPasswordExistence() {
    try {
        const response = await fetchWithAuth('/client/profile/has-password', {
            method: 'GET',
        });

        if (response.ok) {
            const hasPassword = await response.json();
            document.getElementById(hasPassword ? 'change-password-section' : 'set-password-section').style.display = 'block';
        } else {
            console.error('Ошибка проверки пароля');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

async function changePassword() {
    const requestBody = {
        oldPassword: document.getElementById('old-password').value,
        newPassword: document.getElementById('change-new-password').value
    };

    document.getElementById('password-error-messages').innerText = '';

    try {
        const response = await fetchWithAuth('/client/profile/change-password', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });

        if (response.ok) {
            alert('Пароль изменен');
        } else {
            document.getElementById('password-error-messages').innerText = await response.text();
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

async function setPassword() {
    const rawPassword = document.getElementById('set-new-password').value;

    try {
        const response = await fetchWithAuth('/client/profile/set-password', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(rawPassword)
        });

        if (response.ok) {
            alert('Пароль установлен');
        } else {
            console.error('Ошибка установки пароля');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

function displayValidationErrors(errors) {
    const errorContainer = document.getElementById('error-messages');
    errorContainer.innerHTML = '';

    for (const field in errors) {
        const message = errors[field];
        const errorElement = document.createElement('div');
        errorElement.classList.add('error-message');
        errorElement.innerText = `${field}: ${message}`;
        errorContainer.appendChild(errorElement);
    }
}

async function loadOrders(page = 0) {
    try {
        const response = await fetchWithAuth(`/client/orders?page=${page}&size=${pageSize}`, {
            method: 'GET',
        });

        if (response.ok) {
            const data = await response.json();
            await displayOrders(data.content);
            setupPagination(data.page.totalPages);
        } else {
            console.error('Ошибка загрузки заказов');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

async function displayOrders(orders) {
    const ordersList = document.getElementById('orders-list');
    ordersList.innerHTML = '';

    for (const order of orders) {
        const products = await Promise.all(order.items.map(item => getProductById(item.productId)));
        const totalPrice = products.reduce((sum, product, index) => sum + (product.price * order.items[index].quantity), 0);

        const orderElement = document.createElement('div');
        orderElement.className = 'order card mb-3';
        orderElement.innerHTML = `
            <div class="order-summary card-header d-flex justify-content-between align-items-center" onclick="toggleOrderDetails('order-${order.id}')">
                <span>№ ${order.id}</span>
                <span>${formatDate(order.orderDate)}</span>
                <span>${order.orderStatus}</span>
                <span>Итого: ${totalPrice.toFixed(2)} ₴</span>
            </div>
            <div class="order-details card-body" id="order-${order.id}" style="display: none;">
                <p><strong>Получатель:</strong> ${order.clientFirstName} ${order.clientLastName}</p>
                <p><strong>Адрес доставки:</strong> ${order.deliveryAddress}</p>
                <p><strong>Заметки:</strong> ${order.notes || 'Нет'}</p>
                <h4>Товары в заказе:</h4>
                <ul class="list-unstyled">
                    ${products.map((product, index) => `
                        <li class="d-flex align-items-center mb-2">
                            <img src="${product.imageUrl}" alt="${product.name}" class="me-2" style="width: 50px;">
                            <span>${product.name}</span> <span>(кол-во: ${order.items[index].quantity}, цена: ${product.price.toFixed(2)} ₴)</span>
                        </li>
                    `).join('')}
                </ul>
                <button class="btn btn-danger" onclick="cancelOrder(${order.id})" ${order.orderStatus !== 'PENDING' && order.orderStatus !== 'CONFIRMED' ? 'style="display:none;"' : ''}>
                    Отменить
                </button>
            </div>
        `;
        ordersList.appendChild(orderElement);
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

function formatDate(date) {
    const options = {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    };
    return new Date(date).toLocaleString('ru-RU', options);
}

function setupPagination(totalPages) {
    const paginationContainer = document.getElementById('pagination');
    paginationContainer.innerHTML = '';

    if (totalPages > 1) {
        for (let i = 0; i < totalPages; i++) {
            const pageButton = document.createElement('button');
            pageButton.className = `btn btn-outline-primary mx-1 ${i === currentPage ? 'active' : ''}`;
            pageButton.innerText = i + 1;
            pageButton.onclick = () => {
                currentPage = i;
                loadOrders(i);
                setupPagination(totalPages);
            };

            paginationContainer.appendChild(pageButton);
        }
        paginationContainer.style.display = 'flex';
    } else {
        paginationContainer.style.display = 'none';
    }
}


async function cancelOrder(orderId) {
    try {
        const response = await fetchWithAuth(`/client/order/cancel-order/${orderId}`, {
            method: 'PATCH',
        });

        if (response.ok) {
            alert('Заказ отменён');
            await loadOrders(currentPage);
        } else {
            console.error('Ошибка отмены заказа');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}