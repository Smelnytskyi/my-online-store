let currentSort = 'name,asc';

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
    productsContainer.innerHTML = '';

    products.forEach(product => {
        const productCard = `
            <div class="col-md-4 col-lg-3 mb-4">
                <div class="card">
                    <img src="${product.imageUrl}" class="card-img-top" alt="${product.name}">
                    <div class="card-body">
                        <h5 class="card-title">${product.name}</h5>
                        <p class="card-text">Цена: ${product.price} грн</p>
                        <p class="card-text">Количество: ${product.quantity}</p>
                        <a href="#" class="btn btn-primary">Подробнее</a>
                        <a href="#" class="btn btn-outline-primary add-to-cart-btn">В корзину</a>
                    </div>
                </div>
            </div>
        `;
        productsContainer.innerHTML += productCard;
    });
    updateCartButtons();
}

function updateCartButtons(){
    let cartCount = 0;
    document.querySelectorAll('.add-to-cart-btn').forEach(btn => {
        btn.addEventListener('click', () =>{
            cartCount++;
            document.getElementById('cartCount').textContent = cartCount;
            document.getElementById('cartCount').style.color = "green";
        });
    });
}

function setupPagination(totalPages, currentPage) {
    const paginationContainer = document.querySelector('.pagination');
    paginationContainer.innerHTML = '';

    for (let i = 1; i <= totalPages; i++) {
        const pageItem = document.createElement('li');
        pageItem.classList.add('page-item');
        if(i === currentPage){
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

document.getElementById('sortSelect').addEventListener('change', function() {
    currentSort = this.value;
    fetchProducts(1, 20, currentSort);
});

document.addEventListener('DOMContentLoaded', () => {
    fetchProducts();
});

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

