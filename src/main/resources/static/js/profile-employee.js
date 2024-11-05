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

    // Загрузка и отображение информации работника
    function loadEmployeeProfile() {
        fetch('/client/profile', {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        })
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

        fetch('/client/profile/update', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify( updatedData )
        })
            .then(response => {
                if (response.ok) {
                    // Если ответ успешен, выполняем действия сразу
                    loadEmployeeProfile();
                    toggleEditSection('edit-info-form');
                    return response.json().catch(() => ({})); // Обрабатываем JSON, даже если он пустой
                } else {
                    // Если статус не успешный, парсим ошибочный ответ
                    return response.json().then(result => {
                        throw new Error(result.errors ? result.errors.join(", ") : "Неизвестная ошибка");
                    });
                }
            })
            .catch(error => {
                // Обработка ошибок
                document.getElementById("error-messages").textContent = error.message;
                console.error("Ошибка:", error);
            });
    });

    document.getElementById("cancel-edit").addEventListener("click", function () {
        toggleEditSection('edit-info-form');
    });

    // Обновление пароля
    document.getElementById("update-password").addEventListener("click", function () {
        const oldPassword = document.getElementById("old-password").value;
        const newPassword = document.getElementById("new-password").value;

        fetch('/client/profile/change-password', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify({ oldPassword, newPassword })
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

    // Фильтрация по статусу
    document.getElementById("order-status").addEventListener("change", function () {
        loadOrders(this.value);
    });

    // Обработчик события для кнопки поиска
    document.getElementById("search-button").addEventListener("click", function () {
        const orderId = document.getElementById("order-search").value;
        if (orderId) {
            searchOrderById(orderId);
        } else {
            loadOrders(); // Если поле пустое, загружаем все заказы
        }
    });

    // Обработчик события для нажатия Enter
    document.getElementById("order-search").addEventListener("keyup", function (e) {
        if (e.key === "Enter" && this.value) {
            searchOrderById(this.value);
        }
    });
});

// Отображение заказов
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

// Загрузка заказов при загрузке страницы или изменении фильтра
function loadOrders(status = "ALL") {
    let url = '/employee/orders';
    if (status !== "ALL") {
        url = `/employee/orders-by-status?status=${status}`;
    }

    fetch(url, {
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        }
    })
        .then(response => response.json())
        .then(data => {
            displayOrders(data.content); // Метод для отображения заказов
        })
        .catch(error => console.error("Ошибка загрузки заказов:", error));
}

function showTab(tabId) {
    // Скрываем все вкладки
    document.querySelectorAll(".tab-content").forEach(tab => {
        tab.style.display = "none";
    });

    // Отображаем только выбранную вкладку
    document.getElementById(tabId).style.display = "block";

    // Динамическая подсветка вкладок
    document.querySelectorAll(".list-group-item").forEach(button => {
        button.classList.remove("active");
    });
    document.getElementById(tabId + "-tab").classList.add("active");
}

function searchOrderById(orderId) {
    fetch(`/employee/order/get-single/${orderId}`, {
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status}`);
            }
            return response.json();
        })
        .then(order => {
            if (order && order.id) {
                displayOrders([order]); // Показываем только найденный заказ
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

// Удаление заказа
function deleteOrder(orderId) {
    fetch(`/employee/order/delete/${orderId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        }
    })
        .then(response => {
            if (response.ok) {
                loadOrders(document.getElementById("order-status").value);
            }
        })
        .catch(error => console.error("Ошибка удаления заказа:", error));
}

// Обновление статуса заказа
function updateOrderStatus(orderId, newStatus) {
    fetch(`/employee/order/update/${orderId}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify({ orderStatus: newStatus })
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
        const response = await fetch(`/employee/order/get-single/${orderId}`, {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        });

        if (!response.ok) {
            throw new Error("Ошибка загрузки деталей заказа");
        }

        const order = await response.json();
        const products = await Promise.all(order.items.map(item => getProductById(item.productId)));

        // Подсчет общей стоимости заказа
        const totalPrice = products.reduce((sum, product, index) => sum + (product.price * order.items[index].quantity), 0);

        // Создание модального окна
        const orderDetailsModal = document.createElement("div");
        orderDetailsModal.className = "modal";
        orderDetailsModal.id = "orderDetailsModal";
        orderDetailsModal.style.display = "block"; // Показываем модальное окно
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

        // Закрытие модального окна
        window.closeOrderDetailsModal = function() {
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
        return { name: 'Неизвестный товар' }; // Возвращаем заглушку, если продукт не найден
    }
    return await response.json();
}

