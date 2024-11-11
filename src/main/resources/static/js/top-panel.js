let cartCount = 0;
let topPanelLoaded = 0;

// Loading the top panel and cart item count
async function loadTopPanel() {

    try {
        const response = await fetch('top-panel.html');
        if (!response.ok) throw new Error("Failed to load top panel");
        const html = await response.text();
        document.getElementById('top-panel').innerHTML = html;

        await loadCartCount();

        document.getElementById('cartBtn').addEventListener('click', openCart);
        document.getElementById('closeCartBtn').addEventListener('click', () => {
            document.getElementById('cartModal').style.display = 'none';
        });

        // Handler for person icon
        document.getElementById('personBtn').addEventListener('click', openAuthModal);

        // Checking authorization
        const token = localStorage.getItem('token');
        const personIcon = document.querySelector('.person-icon');
        if (token) {
            personIcon.classList.add('person-icon-auth');
        } else {
            personIcon.classList.remove('person-icon-auth');
        }

        // Setting up the search event handler
        document.querySelector('form[role="search"]').addEventListener('submit', async (event) => {
            event.preventDefault();
            const searchInput = document.querySelector('.search-bar').value.trim();
            if (searchInput) {
                await searchProductByName(searchInput);
            }
        });

        // Handling the authorization modal close button
        document.getElementById('closeAuthBtn').addEventListener('click', () => {
            document.getElementById('authModal').style.display = 'none';
        });

        // Opening the registration modal when clicking the register button
        document.getElementById('registerBtn').addEventListener('click', () => {
            const modal = document.getElementById('registrationModal');
            modal.style.display = 'flex';
        });

        // Closing the registration modal
        document.getElementById('closeRegisterBtn').addEventListener('click', () => {
            const modal = document.getElementById('registrationModal');
            modal.style.display = 'none';
        });

        // Form submission handler
        document.getElementById('registrationForm').addEventListener('submit', async (e) => {
            e.preventDefault();

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
                const response = await fetch('/main/registration', {
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
                    const modal = document.getElementById('registrationModal');
                    modal.style.display = 'none';
                } else {
                    const errors = await response.json();
                    displayValidationErrors(errors, 'validationErrors');
                }
            } catch (error) {
                console.error('Ошибка при регистрации:', error);
                alert('Произошла ошибка. Попробуйте позже.');
            }
        });

        // Handler for Google authorization button
        document.getElementById('googleAuthBtn').addEventListener('click', () => {
            window.location.href = '/oauth2/authorization/google';
        });

        // Handler for checkout button
        document.getElementById('checkoutBtn').addEventListener('click', async () => {
            const token = localStorage.getItem('token');
            const authWarning = document.getElementById('authWarning');

            if (token) {

                const isUserAllowed = await checkRole(token);
                if (isUserAllowed) {
                    authWarning.style.display = 'none';
                    openOrderModal();
                } else {
                    authWarning.style.display = 'inline';
                }
            } else {
                authWarning.style.display = 'inline';
            }
        });

        document.getElementById('closeOrderModal').addEventListener('click', closeOrderModal);

        document.getElementById('confirmOrderBtn').addEventListener('click', async () => {
            const token = localStorage.getItem('token');
            const notes = document.getElementById('orderNotes').value;

            if (!token) {
                console.error('Пользователь не авторизован');
                return;
            }

            try {
                const response = await fetchWithAuth('main/order/create', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(notes)
                });

                if (response.status === 402) {
                    alert('Пожалуйста, введите адрес и/или номер телефона в вашем профиле');
                } else if (response.status === 400) {
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
}

// Getting the number of items in the cart
async function loadCartCount() {
    try {
        const token = localStorage.getItem('token');


        const response = await fetchWithAuth('/main/cart/count', {
            method: 'GET',
        });
        if (!response.ok) throw new Error("Failed to fetch cart count");

        const data = await response.json();
        cartCount = data.count;
        document.getElementById('cartCount').textContent = cartCount;
    } catch (error) {
        console.error("Error loading cart count:", error);
    }
}

// Opening the cart and fetching item details
async function openCart() {
    try {
        const token = localStorage.getItem('token');

        const response = await fetchWithAuth('/main/cart', {});

        if (!response.ok) throw new Error("Failed to fetch cart items");

        const cartItems = await response.json();
        const detailedItems = await Promise.all(cartItems.map(async (item) => {
            const productResponse = await fetch(`/main/product/${item.productId}`);
            const productData = await productResponse.json();
            return {...productData, quantity: item.quantity};
        }));
        updateCartItems(detailedItems);
        document.getElementById('cartModal').style.display = 'flex';
    } catch (error) {
        console.error("Ошибка при загрузке корзины:", error);
    }
}

// Updating the cart item list
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

// Function for updating product quantity
async function updateQuantity(productId, quantity) {
    if (quantity < 1) return;
    const token = localStorage.getItem('token');

    try {
        await fetchWithAuth('/main/cart/update', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({productId, quantity})
        });
        await openCart();
        await loadCartCount();
    } catch (error) {
        console.error("Error updating product quantity:", error);
    }
}

// Removing an item from the cart
async function removeFromCart(productId) {
    const token = localStorage.getItem('token');

    try {
        await fetchWithAuth(`/main/cart/remove/${productId}`, {
            method: 'DELETE',
        });
        await openCart();
        await loadCartCount();
    } catch (error) {
        console.error("Error removing product from cart:", error);
    }
}

async function searchProductByName(name) {
    if (!window.location.pathname.endsWith('/index.html')) {
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
        window.renderProducts(data.content);
        window.setupPagination(data.page.totalPages, 1);
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
            showError("Срок авторизации истек, пожалуйста авторизируйтесь повторно");
        }
    } else {
        document.getElementById('authModal').style.display = 'flex';
    }
}

async function getUserRole(token) {
    try {
        const response = await fetchWithAuth('/auth/role', {});

        if (!response.ok) throw new Error("Failed to fetch user role");

        const data = await response.json();
        return data.role;
    } catch (error) {
        console.error("Ошибка при получении роли пользователя:", error);
        throw error;
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
    event.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({email, password})
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            document.getElementById('authModal').style.display = 'none';
            const redirectUrl = data.redirectUrl;
            if (redirectUrl) {
                window.location.href = redirectUrl;
            } else {
                console.error("Redirect URL is missing!");
            }
        } else {
            const errorMessage = await response.text();
            showError(errorMessage);
        }
    } catch (error) {
        console.error("Ошибка при авторизации:", error);
        showError("Ошибка соединения с сервером");
    }
}

function showError(message) {
    const errorContainer = document.getElementById('errorContainer');
    errorContainer.textContent = message;
    errorContainer.style.display = 'block';
}

async function openOrderModal() {
    const profileResponse = await fetchWithAuth('/client/profile', {});
    const profileData = await profileResponse.json();
    document.getElementById('clientName').value = `${profileData.lastName} ${profileData.firstName}`;
    document.getElementById('clientAddress').value = profileData.address;
    document.getElementById('clientPhone').value = profileData.phone;

    const cartResponse = await fetchWithAuth('/main/cart', {});
    const cartItems = await cartResponse.json();

    let totalAmount = 0;
    const orderItemsContainer = document.getElementById('orderItemsContainer');
    orderItemsContainer.innerHTML = '';

    for (const item of cartItems) {
        const productResponse = await fetch(`/main/product/${item.productId}`);
        const productData = await productResponse.json();

        const itemTotal = productData.price * item.quantity;
        totalAmount += itemTotal;

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

    document.getElementById('totalAmountOrder').innerText = `${totalAmount} ₴`;
    document.getElementById('totalPayment').innerText = `${totalAmount} ₴`;

    document.getElementById('orderModal').style.display = 'flex';
}

function closeOrderModal() {
    document.getElementById('orderModal').style.display = 'none';
}

// Wrapper for requests with 401 handling
async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('token');

    options.headers = {
        ...options.headers,
        ...(token ? {'Authorization': `Bearer ${token}`} : {})
    };

    try {
        const response = await fetch(url, options);

        if (response.status === 401) {
            localStorage.removeItem('token');
            openAuthModal();
        }
        return response;
    } catch (error) {
        console.error('Ошибка сети или сервера:', error);
        throw error;
    }
}

loadTopPanel();

