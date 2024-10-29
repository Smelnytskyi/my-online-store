// Fetch and display products based on filters
async function fetchProducts(filters = {}) {
    // Fetch product data from the backend (dummy data for now)
    const products = [
        { id: 1, name: "Товар 1", price: 100, quantity: 7 },
        { id: 2, name: "Товар 2", price: 200, quantity: 5 },
        // Add more products as needed
    ];

    const productGrid = document.querySelector('.product-grid');
    productGrid.innerHTML = ''; // Clear existing products

    products.forEach(product => {
        const productCard = document.createElement('div');
        productCard.className = 'product-card';
        productCard.innerHTML = `
            <img src="product.jpg" alt="${product.name}">
            <h3>${product.name}</h3>
            <p>Цена: ${product.price} грн</p>
            <p>Количество: ${product.quantity}</p>
            <button>Подробнее</button>
            <button>В корзину</button>
        `;
        productGrid.appendChild(productCard);
    });
}

// Filter and Sort Events
document.getElementById('reset-filters').addEventListener('click', () => {
    document.querySelectorAll('.filter-sidebar input[type="checkbox"]').forEach(checkbox => {
        checkbox.checked = false;
    });
    fetchProducts();
});

document.getElementById('back-to-top').addEventListener('click', () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
});

document.getElementById('sort-options').addEventListener('change', (event) => {
    // Add sorting logic based on selected option
    const sortOption = event.target.value;
    console.log('Selected sort:', sortOption);
});

// Initial load
fetchProducts();
