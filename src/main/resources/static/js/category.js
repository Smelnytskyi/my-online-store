const urlParams = new URLSearchParams(window.location.search);
const category = urlParams.get('category'); // Получаем выбранную категорию из URL
let selectedFilters = []; // Для хранения выбранных фильтров

document.addEventListener('DOMContentLoaded', () => {
    if (category) {
        document.getElementById('category-title').innerText = category; // Устанавливаем заголовок категории
        fetchProductsByCategory(category); // Загружаем товары выбранной категории
        fetchAttributesByCategory(category); // Загружаем фильтры для выбранной категории
    }

    updateFilterTags();
});

// Функция для загрузки товаров выбранной категории
function fetchProductsByCategory(category, page = 1, size = 20, sort = currentSort) {
    fetch(`/main/products-by-category?category=${category}&page=${page - 1}&size=${size}&sort=${sort}`)
        .then(response => response.json())
        .then(data => {
            if (data.content) { // Убедитесь, что контент существует
                renderProducts(data.content); // Вызов функции для отображения товаров
                setupPagination(data.page.totalPages, page); // Пагинация
            } else {
                console.error("No content found in response data");
            }
        })
        .catch(error => console.error('Ошибка при загрузке товаров:', error));
}

// Функция для загрузки атрибутов фильтров по категории
function fetchAttributesByCategory(category) {
    fetch(`/main/get-product-attributes?category=${category}`)
        .then(response => response.json())
        .then(data => renderFilters(data)) // Рендер фильтров
        .catch(error => console.error('Ошибка при загрузке атрибутов:', error));
}

// Функция для отображения доступных фильтров (создает чекбоксы для каждого атрибута)
function renderFilters(attributes) {
    const filterSidebar = document.querySelector('.filter-sidebar');
    filterSidebar.innerHTML = ''; // Очищаем перед отрисовкой новых фильтров

    // Сначала добавляем фильтр по цене
    const priceFilterSection = document.createElement('div');
    priceFilterSection.classList.add('filter-section');
    priceFilterSection.innerHTML = `
        <h3>Цена</h3>
        <div class="price-filter">
            <label for="min-price">Минимальная цена:</label>
            <div class="input-group">
                <span class="input-group-text"><i class="bi bi-cash"></i></span>
                <input type="number" id="min-price" placeholder="0" />
            </div>
            <label for="max-price">Максимальная цена:</label>
            <div class="input-group">
                <span class="input-group-text"><i class="bi bi-cash"></i></span>
                <input type="number" id="max-price" placeholder="10000" />
            </div>
            <button id="apply-price-filters" class="btn btn-primary">Применить</button>
        </div>
    `;
    filterSidebar.appendChild(priceFilterSection);

    document.getElementById('apply-price-filters').addEventListener('click', () => {
        const minPrice = parseFloat(document.getElementById('min-price').value);
        const maxPrice = parseFloat(document.getElementById('max-price').value);

        // Проверка корректности введенных данных
        if (!isNaN(minPrice) && !isNaN(maxPrice) && minPrice <= maxPrice) {
            applyPriceFilter(category, minPrice, maxPrice);
        } else {
            alert('Пожалуйста, введите корректные значения цен.');
        }
    });

    // Добавляем фильтры по атрибутам
    for (let [attribute, values] of Object.entries(attributes)) {
        const filterSection = document.createElement('div');
        filterSection.classList.add('filter-section');
        filterSection.innerHTML = `<h3>${attribute}</h3>`;

        values.forEach(value => {
            const checkbox = document.createElement('label');
            checkbox.innerHTML = `<input type="checkbox" value="${value}"> ${value}`;
            checkbox.querySelector('input').addEventListener('change', function() {
                toggleFilter(attribute, value); // Используем toggleFilter
            });
            filterSection.appendChild(checkbox);
        });
        filterSidebar.appendChild(filterSection);
    }
}

// Функция для применения фильтров по цене
function applyPriceFilter(category, min, max, page = 1) {

    // Проверка на наличие выбранных атрибутов
    const selectedAttributes = {};
    document.querySelectorAll('.filter-sidebar input[type="checkbox"]:checked').forEach(checkbox => {
        const attribute = checkbox.closest('.filter-section').querySelector('h3').innerText;
        if (!selectedAttributes[attribute]) selectedAttributes[attribute] = [];
        selectedAttributes[attribute].push(checkbox.value);
    });

    fetch(`/main/search-by-attributes?category=${category}&minPrice=${min}&maxPrice=${max}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(selectedAttributes)
    })
        .then(response => response.json())
        .then(data => {
            renderProducts(data.content); // Обновляем отображение товаров
            setupPagination(data.page.totalPages, page);
        })
        .catch(error => console.error('Ошибка при фильтрации товаров по цене:', error));
}

// Функция для отображения тегов фильтров
function updateFilterTags() {
    const filterTagsContainer = document.getElementById('filter-tags-container');
    filterTagsContainer.innerHTML = ''; // Очищаем перед отрисовкой новых тегов

    // Добавляем тег "Сброс", если есть выбранные фильтры
    if (selectedFilters.length > 0) {
        const resetTag = document.createElement('span');
        resetTag.classList.add('filter-tag');
        resetTag.innerHTML = 'Сброс <button class="remove-tag" id="reset-tag">x</button>';
        filterTagsContainer.appendChild(resetTag);

        // Обработчик для тега "Сброс"
        resetTag.querySelector('#reset-tag').addEventListener('click', () => {
            resetFilters(); // Сброс всех фильтров
        });
    }

    selectedFilters.forEach(filter => {
        const tag = document.createElement('span');
        tag.classList.add('filter-tag');
        tag.innerHTML = `${filter} <button class="remove-tag" data-filter="${filter}">x</button>`;
        filterTagsContainer.appendChild(tag);
    });

    // Добавляем обработчики для удаления тегов
    document.querySelectorAll('.remove-tag').forEach(button => {
        button.addEventListener('click', (e) => {
            const filterToRemove = e.target.getAttribute('data-filter');
            removeFilter(filterToRemove); // Удаляем фильтр и обновляем страницу
        });
    });
}

// Функция для добавления или удаления фильтра
function toggleFilter(attribute, value) {
    const filterIndex = selectedFilters.findIndex(filter => filter === value);
    if (filterIndex === -1) {
        selectedFilters.push(value); // Добавляем фильтр, если его нет в списке
    } else {
        selectedFilters.splice(filterIndex, 1); // Удаляем фильтр, если он уже есть
    }
    updateFilterTags(); // Обновляем отображение тегов
    applyFilters(category); // Применяем фильтры к товарам
}

// Обновляем фильтры при выборе чекбоксов
function applyFilters(category, page = 1, size = 20, sort = currentSort) {
    const filters = {};

    document.querySelectorAll('.filter-sidebar input[type="checkbox"]:checked').forEach(checkbox => {
        const attribute = checkbox.closest('.filter-section').querySelector('h3').innerText;
        if (!filters[attribute]) filters[attribute] = [];
        filters[attribute].push(checkbox.value);
    });

    fetch(`/main/search-by-attributes?category=${category}&page=${page - 1}&size=${size}&sort=${sort}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(filters)
    })
        .then(response => response.json())
        .then(data => {
            renderProducts(data.content); // Обновляем отображение товаров
            setupPagination(data.page.totalPages, page);
        })
        .catch(error => console.error('Ошибка при фильтрации товаров:', error));
}

// Функция для удаления фильтра и обновления чекбокса
function removeFilter(value) {
    // Удаляем фильтр из selectedFilters
    selectedFilters = selectedFilters.filter(filter => filter !== value);

    // Обновляем чекбокс
    document.querySelectorAll('.filter-sidebar input[type="checkbox"]').forEach(checkbox => {
        if (checkbox.value === value) {
            checkbox.checked = false;
        }
    });

    updateFilterTags(); // Обновляем отображение тегов
    applyFilters(category); // Применяем обновленные фильтры к товарам
}

// Устанавливаем обработчик на все чекбоксы
document.querySelectorAll('.filter-sidebar input[type="checkbox"]').forEach(checkbox => {
    checkbox.addEventListener('change', function() {
        const attribute = this.closest('.filter-section').querySelector('h3').innerText;
        toggleFilter(attribute, this.value);
    });
});

// Функция для сброса всех фильтров
function resetFilters() {
    // Сбрасываем выбранные фильтры
    selectedFilters = [];
    document.querySelectorAll('.filter-sidebar input[type="checkbox"]').forEach(checkbox => {
        checkbox.checked = false; // Сбрасываем чекбоксы
    });
    // Сбрасываем поля стоимости
    document.getElementById('min-price').value = '';
    document.getElementById('max-price').value = '';
    updateFilterTags(); // Обновляем отображение тегов
    fetchProductsByCategory(category); // Перезагружаем товары без фильтров
}