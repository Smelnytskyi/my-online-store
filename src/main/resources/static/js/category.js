const urlParams = new URLSearchParams(window.location.search);
const category = urlParams.get('category');
let selectedFilters = [];
let minPrice = null;
let maxPrice = null;

document.addEventListener('DOMContentLoaded', () => {
    if (category) {
        document.getElementById('category-title').innerText = category;
        fetchProductsByCategory(category);
        fetchAttributesByCategory(category);
    }

    updateFilterTags();
});

// Function to load products of the selected category
function fetchProductsByCategory(category, page = 1, size = 24, sort = currentSort) {
    fetch(`/main/products-by-category?category=${category}&page=${page - 1}&size=${size}&sort=${sort}`)
        .then(response => response.json())
        .then(data => {
            if (data.content) {
                renderProducts(data.content);
                setupPagination(data.page.totalPages, page, category);
            } else {
                console.error("No content found in response data");
            }
        })
        .catch(error => console.error('Ошибка при загрузке товаров:', error));
}

// Function to load filter attributes by category
function fetchAttributesByCategory(category) {
    fetch(`/main/get-product-attributes?category=${category}`)
        .then(response => response.json())
        .then(data => renderFilters(data))
        .catch(error => console.error('Ошибка при загрузке атрибутов:', error));
}

// Function to render available filters (creates checkboxes for each attribute)
function renderFilters(attributes) {
    const filterSidebar = document.querySelector('.filter-sidebar');
    filterSidebar.innerHTML = '';

    const priceFilterSection = document.createElement('div');
    priceFilterSection.classList.add('filter-section', 'mb-3', 'p-3', 'border', 'rounded');
    priceFilterSection.innerHTML = `
        <h4 class="mb-3">Цена</h4>
        <div class="mb-3">
            <label for="min-price" class="form-label">Минимальная цена:</label>
            <div class="input-group">
                <span class="input-group-text"><i class="bi bi-cash"></i></span>
                <input type="text" id="min-price" placeholder="0" class="form-control" />
            </div>
        </div>
        <div class="mb-3">
            <label for="max-price" class="form-label">Максимальная цена:</label>
            <div class="input-group">
                <span class="input-group-text"><i class="bi bi-cash"></i></span>
                <input type="text" id="max-price" placeholder="100000" class="form-control" />
            </div>
        </div>
        <button id="apply-price-filters" class="btn btn-primary w-100">Применить</button>
    `;
    filterSidebar.appendChild(priceFilterSection);

    document.getElementById('apply-price-filters').addEventListener('click', () => {
        const minPriceInput = document.getElementById('min-price').value;
        const maxPriceInput = document.getElementById('max-price').value;

        const minPrice = minPriceInput ? parseFloat(minPriceInput) : 0;
        const maxPrice = maxPriceInput ? parseFloat(maxPriceInput) : 100000;

        if (minPrice < 0 || maxPrice < 0) {
            alert('Пожалуйста, введите положительные значения цен.');
        } else if (minPrice > maxPrice) {
            alert('Минимальная цена не может быть больше максимальной.');
        } else {
            applyPriceFilter(category, minPrice, maxPrice, 1, currentSort);
        }
    });

    // Add filters for attributes
    for (let [attribute, values] of Object.entries(attributes)) {
        const filterSection = document.createElement('div');
        filterSection.classList.add('filter-section', 'mb-3', 'p-3', 'border', 'rounded');
        filterSection.innerHTML = `<h4>${attribute}</h4>`;

        values.forEach(value => {
            const checkbox = document.createElement('div');
            checkbox.classList.add('form-check');
            checkbox.innerHTML = `
                <input type="checkbox" class="form-check-input" value="${value}" id="${attribute}-${value}">
                <label class="form-check-label" for="${attribute}-${value}">${value}</label>
            `;
            checkbox.querySelector('input').addEventListener('change', function () {
                toggleFilter(attribute, value);
            });
            filterSection.appendChild(checkbox);
        });
        filterSidebar.appendChild(filterSection);
    }
}

// Function to apply price filter
function applyPriceFilter(category, min, max, page = 1, sort = currentSort) {
    minPrice = min;
    maxPrice = max;

    const selectedAttributes = {};
    document.querySelectorAll('.filter-sidebar input[type="checkbox"]:checked').forEach(checkbox => {
        const attribute = checkbox.closest('.filter-section').querySelector('h4').innerText;
        if (!selectedAttributes[attribute]) selectedAttributes[attribute] = [];
        selectedAttributes[attribute].push(checkbox.value);
    });

    fetch(`/main/search-by-attributes?category=${category}&minPrice=${min}&maxPrice=${max}&sort=${sort}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(selectedAttributes)
    })
        .then(response => response.json())
        .then(data => {
            renderProducts(data.content);
            setupPagination(data.page.totalPages, page, category);
        })
        .catch(error => console.error('Ошибка при фильтрации товаров по цене:', error));
}

// Function to display filter tags
function updateFilterTags() {
    const filterTagsContainer = document.getElementById('filter-tags-container');
    filterTagsContainer.innerHTML = '';

    if (selectedFilters.length > 0) {
        const resetTag = document.createElement('span');
        resetTag.classList.add('filter-tag', 'badge', 'bg-danger', 'me-1');
        resetTag.innerHTML = 'Сброс <button class="btn-close btn-sm" id="reset-tag"></button>';
        filterTagsContainer.appendChild(resetTag);

        resetTag.querySelector('#reset-tag').addEventListener('click', () => {
            resetFilters();
        });
    }

    selectedFilters.forEach(filter => {
        const tag = document.createElement('span');
        tag.classList.add('filter-tag', 'badge', 'bg-secondary', 'me-1');
        tag.innerHTML = `${filter} <button class="btn-close btn-sm" data-filter="${filter}"></button>`;
        filterTagsContainer.appendChild(tag);

        tag.querySelector('button').addEventListener('click', () => {
            removeFilter(filter);
        });
    });
}

// Function to add or remove a filter
function toggleFilter(attribute, value) {
    const filterIndex = selectedFilters.findIndex(filter => filter === value);
    if (filterIndex === -1) {
        selectedFilters.push(value);
    } else {
        selectedFilters.splice(filterIndex, 1);
    }
    updateFilterTags();
    applyFilters(category);
}

// Update filters when checkboxes are selected
function applyFilters(category, page = 1, size = 24, sort = currentSort) {
    const filters = {};

    document.querySelectorAll('.filter-sidebar input[type="checkbox"]:checked').forEach(checkbox => {
        const attribute = checkbox.closest('.filter-section').querySelector('h4').innerText;
        if (!filters[attribute]) filters[attribute] = [];
        filters[attribute].push(checkbox.value);
    });

    const url = new URL(`/main/search-by-attributes`, window.location.origin);
    url.searchParams.append('category', category);
    url.searchParams.append('page', page - 1);
    url.searchParams.append('size', size);
    url.searchParams.append('sort', sort);

    if (minPrice !== null) {
        url.searchParams.append('minPrice', minPrice);
    }
    if (maxPrice !== null) {
        url.searchParams.append('maxPrice', maxPrice);
    }

    fetch(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(filters)
    })
        .then(response => response.json())
        .then(data => {
            renderProducts(data.content);
            setupPagination(data.page.totalPages, page, category);
        })
        .catch(error => console.error('Ошибка при фильтрации товаров:', error));
}

// Function to remove a filter and update the checkbox
function removeFilter(value) {
    selectedFilters = selectedFilters.filter(filter => filter !== value);

    document.querySelectorAll('.filter-sidebar input[type="checkbox"]').forEach(checkbox => {
        if (checkbox.value === value) {
            checkbox.checked = false;
        }
    });

    updateFilterTags();
    applyFilters(category);
}

// Set up event handler for all checkboxes
document.querySelectorAll('.filter-sidebar input[type="checkbox"]').forEach(checkbox => {
    checkbox.addEventListener('change', function () {
        const attribute = this.closest('.filter-section').querySelector('h4').innerText;
        toggleFilter(attribute, this.value);
    });
});

// Function to reset all filters
function resetFilters() {
    selectedFilters = [];
    document.querySelectorAll('.filter-sidebar input[type="checkbox"]').forEach(checkbox => {
        checkbox.checked = false;
    });
    document.getElementById('min-price').value = '';
    document.getElementById('max-price').value = '';
    updateFilterTags();
    fetchProductsByCategory(category);
}