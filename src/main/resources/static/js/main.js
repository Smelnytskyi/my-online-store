let currentSort = 'name,asc';

async function loadTopPanel() {
    try {
        const response = await fetch('top-panel.html');
        if (!response.ok) throw new Error("Failed to load top panel");
        const html = await response.text();
        document.getElementById('top-panel').innerHTML = html;
    } catch (error) {
        console.error("Error loading top panel:", error);
    }
}

async function fetchProducts(page = 1, size = 20, sort = currentSort) {
    console.log(`Fetching products - Page: ${page}, Size: ${size}, Sort: ${sort}`);
    try {
        const response = await fetch(`/main/products?page=${page - 1}&size=${size}&sort=${sort}`);
        if (!response.ok) throw new Error("Failed to fetch products");

        const data = await response.json();
        renderProducts(data.content);
        setupPagination(data.page.totalPages, page);
    } catch (error) {
        console.error(error);
    }
}

function renderProducts(products) {
    const productsContainer = document.querySelector('.products-container .row');
    const paginationContainer = document.querySelector('.pagination');
    productsContainer.innerHTML = '';

    if (products.length === 0) {
        productsContainer.innerHTML = '<p class="text-center mt-4">Товар не найден</p>';
        paginationContainer.style.display = 'none';
        return;
    }

    paginationContainer.style.display = 'flex';
    products.forEach(product => {
        const productCard = `
            <div class="col-md-4 col-lg-3 mb-4">
                <div class="card">
                    <img src="${product.imageUrl}" class="card-img-top" alt="${product.name}">
                    <div class="card-body">
                        <h5 class="card-title">${product.name}</h5>
                        <p class="card-text">Цена: ${product.price} грн</p>
                        <p class="card-text">Количество: ${product.quantity}</p>
                        <a href="product.html?id=${product.id}" class="btn btn-primary">Подробнее</a>
                        <a href="#" class="btn btn-outline-primary add-to-cart-btn" data-product-id="${product.id}">В корзину</a>
                    </div>
                </div>
            </div>
        `;
        productsContainer.insertAdjacentHTML('beforeend', productCard);
    });
    loadCartCount();
}

function setupPagination(totalPages, currentPage) {
    const paginationContainer = document.querySelector('.pagination');
    paginationContainer.innerHTML = '';

    if (totalPages <= 1) {
        paginationContainer.style.display = 'none';
        return;
    }

    paginationContainer.style.display = 'flex';

    for (let i = 1; i <= totalPages; i++) {
        const pageItem = document.createElement('li');
        pageItem.classList.add('page-item');
        if (i === currentPage) {
            pageItem.classList.add('active');
        }

        pageItem.innerHTML = `<a class="page-link" href="#">${i}</a>`;

        pageItem.addEventListener('click', (e) => {
            e.preventDefault();
            fetchProducts(i);
        });

        paginationContainer.appendChild(pageItem);
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await loadTopPanel();

    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('search');
    if (searchQuery) {
        const searchBar = document.querySelector('.search-bar');
        if (searchBar) {
            searchBar.value = searchQuery;
            searchProductByName(searchQuery);
        }
    }else {
        // Вызываем fetchProducts только если нет параметра category
        const category = urlParams.get('category');
        if (!category) {
            fetchProducts(); // Загружаем все товары
        }
    }

    //Сортировка в зависимости от страницы
    const currentPage = document.body.getAttribute('data-page');
    if(currentPage === 'category'){
        document.getElementById('sortSelect').addEventListener('change', function() {
            currentSort = this.value;
            if (selectedFilters.length > 0) {
                applyFilters(category, 1, 20, currentSort); // Применение сортировки к фильтрованным товарам
            } else {
                fetchProductsByCategory(category, 1, 20, currentSort); // Сортировка всех товаров категории
            }
        });
    }else if(currentPage === 'main'){
        document.getElementById('sortSelect').addEventListener('change', function() {
            currentSort = this.value;
            fetchProducts(1, 20, currentSort);
        });
    }
});

document.addEventListener('click', (event) => {
    if (event.target.classList.contains('add-to-cart-btn')) {
        event.preventDefault();
        const productId = event.target.getAttribute('data-product-id');
        addToCart(productId);
    }
});

async function addToCart(productId) {
    try {
        const token = localStorage.getItem('token');

        // Получаем роль пользователя
        const roleResponse = await fetch('/auth/role', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        const roleData = await roleResponse.json();
        const role = roleData.role;

        // Если роль — админ или работник, не позволяем добавить товар в корзину
        if (role === 'ADMIN' || role === 'EMPLOYEE') {
            alert('Администратор и работник не могут добавлять товары в корзину. Пожалуйста, авторизируйтесь под клиентом.');
            return;  // Завершаем выполнение функции
        }

        const response = await fetch('/main/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { 'Authorization': `Bearer ${token}` } : {})
            },
            body: JSON.stringify({ productId: productId, quantity: 1 })
        });

        if (!response.ok) throw new Error("Failed to add product to cart");
        const data = await response.json().catch(() => ({}));
        console.log(`Товар с ID ${productId} добавлен в корзину`, data);
        loadCartCount();
    } catch (error) {
        console.error("Error adding product to cart:", error);
    }
}

const scrollToTopBtn = document.getElementById("scrollToTopBtn");
window.addEventListener("scroll", () => {
    if (window.scrollY > 300) {
        scrollToTopBtn.style.display = "block";
    } else {
        scrollToTopBtn.style.display = "none";
    }
});

scrollToTopBtn.addEventListener("click", () => {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
});

document.querySelectorAll('.categories-container .list-group-item').forEach(item => {
    item.addEventListener('click', function(event) {
        event.preventDefault();
        const category = this.getAttribute('data-category');
        window.location.href = `category.html?category=${category}`;
    });
});

