// Sample JSON data (In real application, this would come from backend API)
const productData = {
    name: "MSI PCI-Ex GeForce RTX 4060",
    id: "384220803",
    price: "58 259 ₽",
    stock: "In stock",
    imageUrl: "path/to/image.png", // Replace with actual URL
    specifications: {
        "Графический чип": "GeForce RTX 4060",
        "Частота памяти": "17000 MHz",
        "Частота ядра": "2505 MHz",
        "Объем памяти": "8 GB",
        "Система охлаждения": "TORX FAN 4.0",
        // Add more specifications as needed
    }
};

// Populate HTML elements with data
document.addEventListener("DOMContentLoaded", () => {
    // Set title, ID, and image
    document.getElementById("product-title").innerText = productData.name;
    document.getElementById("product-id").innerText = `ID: ${productData.id}`;
    document.getElementById("product-image").src = productData.imageUrl;
    document.getElementById("product-price").innerText = `Цена: ${productData.price}`;
    document.getElementById("stock-status").innerText = productData.stock;

    // Populate specifications
    const specsList = document.getElementById("specifications-list");
    for (const [key, value] of Object.entries(productData.specifications)) {
        const listItem = document.createElement("li");
        listItem.innerHTML = `<strong>${key}</strong>: ${value}`;
        specsList.appendChild(listItem);
    }
});
