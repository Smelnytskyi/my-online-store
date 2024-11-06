let cartCount = 0;
let topPanelLoaded = 0;

// Загружаем верхнюю панель и количество товаров в корзине
async function loadTopPanel() {

    try {
        const response = await fetch('top-panel.html');
        if (!response.ok) throw new Error("Failed to load top panel");
        const html = await response.text();
        document.getElementById('top-panel').innerHTML = html;

        await loadCartCount();

        // Устанавливаем обработчики событий
        document.getElementById('cartBtn').addEventListener('click', openCart);
        document.getElementById('closeCartBtn').addEventListener('click', () => {
            document.getElementById('cartModal').style.display = 'none';
        });

        // Обработчик для иконки человека
        document.getElementById('personBtn').addEventListener('click', openAuthModal);

        // Проверка авторизации
        const token = localStorage.getItem('token');
        const personIcon = document.querySelector('.person-icon');
        if (token) {
            personIcon.classList.add('person-icon-auth'); // Добавляем зелёный класс
        } else {
            personIcon.classList.remove('person-icon-auth'); // Убираем зелёный класс
        }

        //Устанавливаем обработчик событий для поиска
        document.querySelector('form[role="search"]').addEventListener('submit', async (event) => {
            event.preventDefault();
            const searchInput = document.querySelector('.search-bar').value.trim();
            if (searchInput) {
                await searchProductByName(searchInput);
            }
        });

        // Обработка закрытия авторизации
        document.getElementById('closeAuthBtn').addEventListener('click', () => {
            document.getElementById('authModal').style.display = 'none';
        });

        // Открытие модального окна при нажатии на кнопку регистрации
        document.getElementById('registerBtn').addEventListener('click', () => {
            const modal = document.getElementById('registrationModal');
            modal.style.display = 'flex';  // Показываем модальное окно
        });

        // Закрытие модального окна
        document.getElementById('closeRegisterBtn').addEventListener('click', () => {
            const modal = document.getElementById('registrationModal');
            modal.style.display = 'none';  // Скрываем модальное окно
        });

        // Обработчик отправки формы
        document.getElementById('registrationForm').addEventListener('submit', async (e) => {
            e.preventDefault();  // Предотвращаем обычное отправление формы

            // Сбор данных из формы
            const formData = new FormData(e.target);
            const data = {
                firstName: formData.get('firstName'),
                lastName: formData.get('lastName'),
                phone: formData.get('phone'),
                address: formData.get('address'),
                email: formData.get('email'),
                password: formData.get('password'),
            };

            try {
                // Отправляем данные на сервер
                const response = await fetch('/client/registration', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        clientDTO: {
                            firstName: data.firstName,
                            lastName: data.lastName,
                            phone: data.phone,
                            address: data.address,
                        },
                        userDTO: {
                            email: data.email,
                        },
                        password: data.password,
                    }),
                });

                if (response.ok) {
                    alert('Регистрация прошла успешно!');
                    // Закрываем модальное окно
                    const modal = document.getElementById('registrationModal');
                    modal.style.display = 'none';
                } else {
                    // Получаем ошибки с бэкенда
                    const errors = await response.json();
                    displayValidationErrors(errors, 'validationErrors');
                }
            } catch (error) {
                console.error('Ошибка при регистрации:', error);
                alert('Произошла ошибка. Попробуйте позже.');
            }
        });

        // Обработчик для кнопоки Google авторизации
        document.getElementById('googleAuthBtn').addEventListener('click', () => {
            window.location.href = '/auth/google'; // Путь для авторизации через Google
        });

        // Обработчик для кнопоки заказа
        document.getElementById('checkoutBtn').addEventListener('click', async () => {
            const token = localStorage.getItem('token');
            const authWarning = document.getElementById('authWarning');

            if (token) {

                const isUserAllowed = await checkRole(token);
                // Пользователь авторизован, скрываем предупреждение (если оно было показано ранее)
                if (isUserAllowed){
                    authWarning.style.display = 'none';
                    openOrderModal();
                }else {
                    authWarning.style.display = 'inline';
                }
            } else {
                // Показываем предупреждение о необходимости авторизации
                authWarning.style.display = 'inline';
            }
        });

        document.getElementById('closeOrderModal').addEventListener('click', closeOrderModal);

        document.getElementById('confirmOrderBtn').addEventListener('click', async () => {
            const token = localStorage.getItem('token');
            const notes = document.getElementById('orderNotes').value; // Предполагается, что поле для заметок имеет id="notes"

            if (!token) {
                console.error('Пользователь не авторизован');
                return;
            }

            try {
                const response = await fetch('main/order/create', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(notes)
                });

                if (response.status === 402) {  // PAYMENT_REQUIRED
                    alert('Пожалуйста, введите адрес в вашем профиле');
                } else if (response.status === 400){
                    alert('Количество товара на складе меньше чем в заказе');
                } else if (response.ok) {
                    alert('Заказ успешно подтвержден');
                    closeOrderModal();
                    document.getElementById('cartModal').style.display = 'none';
                    location.reload();
                } else {
                    console.error('Ошибка при подтверждении заказа');
                }
            } catch (error) {
                console.error('Ошибка при отправке запроса:', error);
            }
        });

    } catch (error) {
        console.error("Error loading top panel:", error);
    }

    topPanelLoaded += 1;
    console.log(topPanelLoaded);
}

// Получаем количество товаров в корзине
async function loadCartCount() {
    try {
        const token = localStorage.getItem('token');


        const response = await fetch('/main/cart/count',{
            method: 'GET',
            headers: {
                ...(token ? { 'Authorization': `Bearer ${token}` } : {})
            },
        });
        if (!response.ok) throw new Error("Failed to fetch cart count");

        const data = await response.json();
        cartCount = data.count;
        document.getElementById('cartCount').textContent = cartCount;
    } catch (error) {
        console.error("Error loading cart count:", error);
    }
}

// Открытие корзины и получение информации о товарах
async function openCart() {
    try {
        const token = localStorage.getItem('token');

        const response = await fetch('/main/cart', {
            headers: {
                // Если токен существует, добавляем его в заголовок Authorization
                ...(token ? { 'Authorization': `Bearer ${token}` } : {})
            }
        });

        if (!response.ok) throw new Error("Failed to fetch cart items"); // Проверка на ошибки

        const cartItems = await response.json();
        const detailedItems = await Promise.all(cartItems.map(async (item) => {
            const productResponse = await fetch(`/main/product/${item.productId}`);
            const productData = await productResponse.json();
            return { ...productData, quantity: item.quantity };
        }));
        updateCartItems(detailedItems);
        document.getElementById('cartModal').style.display = 'flex';
    } catch (error) {
        console.error("Ошибка при загрузке корзины:", error);
    }
}

// Обновляем список товаров в корзине
function updateCartItems(cartItems) {
    const cartItemsContainer = document.getElementById('cartItemsContainer');
    cartItemsContainer.innerHTML = '';

    if (cartItems.length === 0) {
        cartItemsContainer.innerHTML = '<p>Корзина пуста</p>';
        document.getElementById('totalAmount').textContent = '0 ₴';
        return;
    }

    cartItems.sort((a, b) => a.name.localeCompare(b.name));

    cartItems.forEach(item => {
        const itemElement = document.createElement('div');
        itemElement.classList.add('cart-item', 'd-flex', 'justify-content-between', 'align-items-center', 'mb-2');
        itemElement.innerHTML = `
            <img src="${item.imageUrl}" alt="${item.name}" class="cart-item-image" style="width: 50px; height: 50px;">
            <div>
                <p class="m-0">${item.name}</p>
                <small>Цена: ${item.price} ₴ x ${item.quantity}</small>
            </div>
            <div>
                <button class="btn btn-outline-secondary btn-sm" onclick="updateQuantity(${item.id}, ${item.quantity - 1})">-</button>
                <input type="text" value="${item.quantity}" style="width: 40px; text-align: center;" onchange="updateQuantity(${item.id}, this.value)">
                <button class="btn btn-outline-secondary btn-sm" onclick="updateQuantity(${item.id}, ${item.quantity + 1})">+</button>
            </div>
            <span>${(item.price * item.quantity).toFixed(2)} ₴</span>
            <button class="btn btn-outline-danger btn-sm remove-from-cart-btn" onclick="removeFromCart(${item.id})">Удалить</button>
        `;
        cartItemsContainer.appendChild(itemElement);
    });

    const totalAmount = cartItems.reduce((total, item) => total + item.price * item.quantity, 0);
    document.getElementById('totalAmount').textContent = `${totalAmount.toFixed(2)} ₴`;
}

// Функция для обновления количества товара
async function updateQuantity(productId, quantity) {
    if (quantity < 1) return; // Количество не может быть меньше 1
    const token = localStorage.getItem('token');

    try {
        await fetch('/main/cart/update', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { 'Authorization': `Bearer ${token}` } : {})
            },
            body: JSON.stringify({ productId, quantity })
        });
        await openCart(); // Обновляем корзину после изменения количества
        await loadCartCount(); // Обновляем количество на иконке
    } catch (error) {
        console.error("Error updating product quantity:", error);
    }
}

// Удаление товара из корзины
async function removeFromCart(productId) {
    const token = localStorage.getItem('token');

    try {
        await fetch(`/main/cart/remove/${productId}`, {
            method: 'DELETE' ,
            headers: {
                ...(token ? { 'Authorization': `Bearer ${token}` } : {})
            }
        });
        await openCart();
        await loadCartCount();
    } catch (error) {
        console.error("Error removing product from cart:", error);
    }
}

async function searchProductByName(name) {
    // Проверяем, находится ли пользователь на главной странице
    if (!window.location.pathname.endsWith('/index.html')) {
        // Если нет, перенаправляем его на главную страницу с параметром поиска
        window.location.href = `/index.html?search=${encodeURIComponent(name)}`;
        return;
    }

    try {
        const response = await fetch(`/main/search?name=${encodeURIComponent(name)}&page=0&size=20`);
        if (response.status === 404) {
            showNoResultsMessage();
            return;
        }
        if (!response.ok) throw new Error("Failed to fetch search results");

        const data = await response.json();
        window.renderProducts(data.content); // Переиспользуйте функцию отрисовки из main.js
        window.setupPagination(data.page.totalPages, 1); // Также переиспользуйте функцию для пагинации
    } catch (error) {
        console.error("Error during search: ", error);
    }
}

function showNoResultsMessage() {
    const productsContainer = document.querySelector('.products-container .row');
    const paginationContainer = document.querySelector('.pagination');
    productsContainer.innerHTML = '<p class="text-center mt-4">Товар не найден</p>';
    paginationContainer.style.display = 'none';
}

async function openAuthModal() {
    const token = localStorage.getItem('token');
    if (token) {
        try {
            const role = await getUserRole(token);
            redirectToProfile(role);
        } catch (error) {
            console.error("Ошибка получения роли пользователя:", error);
            showError("Ошибка при определении профиля");
        }
    } else {
        // Если токена нет, открываем модальное окно авторизации
        document.getElementById('authModal').style.display = 'flex';
    }
}

async function getUserRole(token) {
    try {
        const response = await fetch('/auth/role', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error("Failed to fetch user role");

        const data = await response.json();
        return data.role; // Предполагаем, что роль хранится в поле "role"
    } catch (error) {
        console.error("Ошибка при получении роли пользователя:", error);
        throw error; // Чтобы обработать ошибку выше
    }
}

function redirectToProfile(role) {
    if (role === 'CLIENT') {
        window.location.href = 'profile-client.html';
    } else if (role === 'ADMIN') {
        window.location.href = 'profile-admin.html';
    } else if (role === 'EMPLOYEE') {
        window.location.href = 'profile-employee.html';
    } else {
        console.error("Неизвестная роль:", role);
        showError("Неизвестный профиль пользователя");
    }
}

async function submitLoginForm() {
    // Останавливаем отправку формы
    event.preventDefault();

    // Получаем введённые значения
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    try {
        // Отправляем запрос на сервер
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            document.getElementById('authModal').style.display = 'none';
            const role = await getUserRole(data.token);
            redirectToProfile(role);
        } else {
            const errorMessage = await response.text(); // Получаем текст ошибки
            showError(errorMessage); // Показываем сообщение об ошибке
        }
    } catch (error) {
        console.error("Ошибка при авторизации:", error);
        showError("Ошибка соединения с сервером");
    }
}

function showError(message) {
    const errorContainer = document.getElementById('errorContainer'); // Элемент для отображения ошибок
    errorContainer.textContent = message;
    errorContainer.style.display = 'block'; // Показываем сообщение об ошибке
}

async function openOrderModal() {
    // Получение данных профиля пользователя
    const profileResponse = await fetch('/client/profile', {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    });
    const profileData = await profileResponse.json();
    document.getElementById('clientName').value = `${profileData.lastName} ${profileData.firstName}`;
    document.getElementById('clientAddress').value = profileData.address;
    document.getElementById('clientPhone').value = profileData.phone;

    // Получение данных корзины
    const cartResponse = await fetch('/main/cart', {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    });
    const cartItems = await cartResponse.json();

    let totalAmount = 0;
    const orderItemsContainer = document.getElementById('orderItemsContainer');
    orderItemsContainer.innerHTML = ''; // Очистка предыдущих данных

    // Загрузка информации о каждом товаре
    for (const item of cartItems) {
        const productResponse = await fetch(`/main/product/${item.productId}`);
        const productData = await productResponse.json();

        const itemTotal = productData.price * item.quantity;
        totalAmount += itemTotal;

        // Создание элемента для товара
        const orderItem = document.createElement('div');
        orderItem.classList.add('order-item');
        orderItem.innerHTML = `
            <img src="${productData.imageUrl}" alt="${productData.name}">
            <div>
                <p>${productData.name}</p>
                <p>${productData.price} ₴ × ${item.quantity} ед.</p>
            </div>
            <div><strong>${itemTotal} ₴</strong></div>
        `;
        orderItemsContainer.appendChild(orderItem);
    }

    // Установка итоговой суммы
    document.getElementById('totalAmountOrder').innerText = `${totalAmount} ₴`;
    document.getElementById('totalPayment').innerText = `${totalAmount} ₴`;

    // Показ модального окна
    document.getElementById('orderModal').style.display = 'flex';
}

function closeOrderModal() {
    document.getElementById('orderModal').style.display = 'none';
}

loadTopPanel();

