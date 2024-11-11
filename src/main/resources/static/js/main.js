let currentSort = 'name,asc';

// Get token from URL
const token = getUrlParameter('token');
if (token) {
    localStorage.setItem('token', token);
    window.history.replaceState({}, document.title, "/index.html");
    window.location.href = `profile-client.html`;
}

async function fetchProducts(page = 1, size = 24, sort = currentSort) {
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
            <div class="col-md-2 col-lg-2 mb-4">
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

function setupPagination(totalPages, currentPage, category = null) {
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
            if (category) {
                fetchProductsByCategory(category, i, 24);
            } else {
                fetchProducts(i);
            }
        });

        paginationContainer.appendChild(pageItem);
    }
}

document.addEventListener('DOMContentLoaded', async () => {

    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('search');
    if (searchQuery) {
        const searchBar = document.querySelector('.search-bar');
        if (searchBar) {
            searchBar.value = searchQuery;
            searchProductByName(searchQuery);
        }
    } else {
        const category = urlParams.get('category');
        if (!category) {
            fetchProducts();
        }
    }

    const currentPage = document.body.getAttribute('data-page');
    if (currentPage === 'category') {
        document.getElementById('sortSelect').addEventListener('change', function () {
            currentSort = this.value;
            if (selectedFilters.length > 0) {
                applyFilters(category, 1, 24, currentSort);
            } else {
                fetchProductsByCategory(category, 1, 24, currentSort);
            }
        });
    } else if (currentPage === 'main') {
        document.getElementById('sortSelect').addEventListener('change', function () {
            currentSort = this.value;
            fetchProducts(1, 24, currentSort);
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
        if (await checkRole(token)) {
            const response = await fetch('/main/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(token ? {'Authorization': `Bearer ${token}`} : {})
                },
                body: JSON.stringify({productId: productId, quantity: 1})
            });

            if (!response.ok) throw new Error("Failed to add product to cart");
            const data = await response.json().catch(() => ({}));
            await loadCartCount();
        } else {
            alert('Администратор и работник не могут добавлять товары в корзину. Пожалуйста, авторизируйтесь под клиентом.');
        }
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
    item.addEventListener('click', function (event) {
        event.preventDefault();
        const category = this.getAttribute('data-category');
        window.location.href = `category.html?category=${category}`;
    });
});

// Function to extract URL parameters
function getUrlParameter(name) {
    name = name.replace(/[\[\]]/g, '\\$&');
    const regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
    const results = regex.exec(window.location.href);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}