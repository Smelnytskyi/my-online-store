const urlParams = new URLSearchParams(window.location.search);
const productId = urlParams.get('id');

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


document.addEventListener("DOMContentLoaded", async () => {
    loadTopPanel();

    if (!productId) {
        console.error("Product ID не найден в URL");
        return;
    }

    try {
        // Запрос данных продукта с сервера
        const response = await fetch(`/main/product/${productId}`);
        const product = await response.json();

        // Отображение данных продукта на странице
        document.getElementById('product-title').textContent = product.name;
        document.getElementById('product-id').textContent = `ID: ${product.id}`;
        document.getElementById('product-price').textContent = `Цена: ${product.price} грн`;
        document.getElementById('stock-status').textContent = product.quantity > 0 ? "В наличии" : "Нет в наличии";
        document.getElementById('product-image').src = product.imageUrl;

        // Отображение описания товара
        const descriptionElement = document.getElementById('description-text');
        descriptionElement.textContent = product.description || "Описание товара отсутствует.";

        // Отображение характеристик
        const specsList = document.getElementById('specifications-list');
        if (Object.keys(product.attributes).length === 0) {
            specsList.innerHTML = "<li class='list-group-item'>Характеристики не указаны</li>";
        } else {
            for (const [key, value] of Object.entries(product.attributes)) {
                const li = document.createElement('li');
                li.classList.add('list-group-item');
                li.innerHTML = `<strong>${key}:</strong> ${value}`;
                specsList.appendChild(li);
            }
        }
    } catch (error) {
        console.error("Ошибка загрузки данных продукта:", error);
    }
});

document.getElementById('buy-button').addEventListener('click', async () => {
    const cartItem = {
        productId: parseInt(urlParams.get('id')),
        quantity: 1
    };
    const token = localStorage.getItem('token');

    try {
        await fetch('/main/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { 'Authorization': `Bearer ${token}` } : {})
            },
            body: JSON.stringify(cartItem)
        });
        loadCartCount();
    } catch (error) {
        console.error("Ошибка при добавлении в корзину:", error);
    }
});
