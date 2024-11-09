const urlParams = new URLSearchParams(window.location.search);
const productId = urlParams.get('id');

document.addEventListener("DOMContentLoaded", async () => {

    if (!productId) {
        console.error("Product ID не найден в URL");
        return;
    }

    try {
        const response = await fetch(`/main/product/${productId}`);
        const product = await response.json();

        document.getElementById('product-title').textContent = product.name;
        document.getElementById('product-id').textContent = `ID: ${product.id}`;
        document.getElementById('product-price').textContent = `Цена: ${product.price} грн`;
        document.getElementById('stock-status').textContent = product.quantity > 0 ? "В наличии" : "Нет в наличии";
        document.getElementById('product-image').src = product.imageUrl;

        const descriptionElement = document.getElementById('description-text');
        descriptionElement.textContent = product.description || "Описание товара отсутствует.";

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
    if (await checkRole(token)) {
        try {
            await fetch('/main/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(token ? {'Authorization': `Bearer ${token}`} : {})
                },
                body: JSON.stringify(cartItem)
            });
            await loadCartCount();
        } catch (error) {
            console.error("Ошибка при добавлении в корзину:", error);
        }
    } else {
        alert('Администратор и работник не могут добавлять товары в корзину. Пожалуйста, авторизируйтесь под клиентом.');
    }
});
