let cartCount = 0;

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

        // Обработчики для кнопок регистрации и Google авторизации
        document.getElementById('registerBtn').addEventListener('click', () => {
            window.location.href = '/registration'; // Перенаправление на страницу регистрации
        });

        document.getElementById('googleAuthBtn').addEventListener('click', () => {
            window.location.href = '/auth/google'; // Путь для авторизации через Google
        });
    } catch (error) {
        console.error("Error loading top panel:", error);
    }
}

// Получаем количество товаров в корзине
async function loadCartCount() {
    try {
        const response = await fetch('/main/cart/count');
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

    try {
        await fetch('/main/cart/update', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
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
    try {
        await fetch(`/main/cart/remove/${productId}`, { method: 'DELETE' });
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

function openAuthModal() {
    const token = localStorage.getItem('token');
    if (token) {
        // Если токен есть, перенаправляем на страницу профиля
        window.location.href = '/profile';
    } else {
        // Если токена нет, открываем модальное окно авторизации
        document.getElementById('authModal').style.display = 'flex';
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
            document.getElementById('authModal').style.display = 'none'; // Закрываем окно авторизации
            // Здесь можно обновить состояние пользователя
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

loadTopPanel();
