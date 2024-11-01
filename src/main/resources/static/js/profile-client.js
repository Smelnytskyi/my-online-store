let currentPage = 0; // Текущая страница
const pageSize = 20; // Количество заказов на странице

// Загрузка профиля при старте страницы
window.onload = function() {
    loadProfile();
    checkPasswordExistence();
};

function showProfileSection() {
    document.getElementById("profile-section").style.display = "block";
    document.getElementById("orders-section").style.display = "none";
    loadProfile();
    checkPasswordExistence();
}

function showOrdersSection() {
    document.getElementById("profile-section").style.display = "none";
    document.getElementById("orders-section").style.display = "block";
    loadOrders(); // Загрузить заказы при открытии секции
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
        const response = await fetch('/client/profile', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
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
    document.getElementById('phone').innerText = data.phone;
    document.getElementById('address').innerText = data.address;
}

async function updateProfile() {
    const updatedData = {
        firstName: document.getElementById('edit-first-name').value || null,
        lastName: document.getElementById('edit-last-name').value || null,
        phone: document.getElementById('edit-phone').value || null,
        address: document.getElementById('edit-address').value || null
    };

    try {
        const response = await fetch('/client/profile/update', {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedData)
        });

        if (response.ok) {
            alert('Данные обновлены');
            loadProfile();
        } else {
            const errorData = await response.json();
            displayValidationErrors(errorData); // Обработка ошибок валидации
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

async function checkPasswordExistence() {
    try {
        const response = await fetch('/client/profile/has-password', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
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
        const response = await fetch('/client/profile/change-password', {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });

        if (response.ok) {
            alert('Пароль изменен');
        } else {
            document.getElementById('password-error-messages').innerText =  await response.text();
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

async function setPassword() {
    const rawPassword = document.getElementById('set-new-password').value;

    try {
        const response = await fetch('/client/profile/set-password', {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
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
    // Удаляем предыдущие сообщения об ошибках
    const errorContainer = document.getElementById('error-messages');
    errorContainer.innerHTML = ''; // Очищаем контейнер

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
        const response = await fetch(`/client/orders?page=${page}&size=${pageSize}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (response.ok) {
            const data = await response.json();
            await displayOrders(data.content); // Отобразить заказы
            setupPagination(data.totalPages); // Настроить пагинацию
        } else {
            console.error('Ошибка загрузки заказов');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

async function displayOrders(orders) {
    const ordersList = document.getElementById('orders-list');
    ordersList.innerHTML = ''; // Очищаем предыдущий список

    for (const order of orders) {
        const products = await Promise.all(order.items.map(item => getProductById(item.productId)));
        const totalPrice = products.reduce((sum, product, index) => sum + (product.price * order.items[index].quantity), 0);

        const orderElement = document.createElement('div');
        orderElement.className = 'order';
        orderElement.innerHTML = `
            <div class="order-summary" onclick="toggleOrderDetails('order-${order.id}')">
                <p>№ ${order.id}</p>
                <p>${formatDate(order.orderDate)}</p>
                <p>${order.orderStatus}</p>
                <p>Итого: ${totalPrice.toFixed(2)} ₴</p>
            </div>
            <div class="order-details" id="order-${order.id}" style="display: none;">
                <p>Получатель: ${order.clientFirstName} ${order.clientLastName}</p> <!-- Имя и фамилия клиента -->
                <p>Адрес доставки: ${order.deliveryAddress}</p>
                <p>Заметки: ${order.notes || 'Нет'}</p>
                <h4>Товары в заказе:</h4>
                <ul>
                    ${products.map((product, index) => `
                        <li>
                            <img src="${product.imageUrl}" alt="${product.name}" style="width: 50px; height: auto;"> 
                            ${product.name} (кол-во: ${order.items[index].quantity}, цена: ${product.price.toFixed(2)} ₴)
                        </li>
                    `).join('')}
                </ul>
                <button onclick="cancelOrder(${order.id})" ${order.orderStatus !== 'PENDING' && order.orderStatus !== 'CONFIRMED' ? 'style="display:none;"' : ''}>
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
        return { name: 'Неизвестный товар' }; // Возвращаем заглушку, если продукт не найден
    }
    return await response.json();
}

// Функция для форматирования даты
function formatDate(date) {
    const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false };
    return new Date(date).toLocaleString('ru-RU', options); // Форматирование даты в русском формате
}

function setupPagination(totalPages) {
    const paginationContainer = document.getElementById('pagination');
    paginationContainer.innerHTML = ''; // Очищаем предыдущую пагинацию

    for (let i = 0; i < totalPages; i++) {
        const pageButton = document.createElement('button');
        pageButton.innerText = i + 1;
        pageButton.onclick = () => loadOrders(i);
        paginationContainer.appendChild(pageButton);
    }
}

async function cancelOrder(orderId) {
    try {
        const response = await fetch(`/client/order/cancel-order/${orderId}`, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (response.ok) {
            alert('Заказ отменён');
            loadOrders(currentPage); // Перезагружаем заказы
        } else {
            console.error('Ошибка отмены заказа');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}