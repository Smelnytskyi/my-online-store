// Глобальная переменная для хранения списка сотрудников
let employees = [];
let selectedEmployees = [];
let currentPage = 0; // Добавляем переменную для текущей страницы

// Загрузка данных при старте страницы
window.onload = function() {
    loadEmployees();
};

// Функция показа модального окна добавления сотрудника
function showAddEmployeeModal() {
    document.getElementById('addEmployeeModal').style.display = 'flex';
}

// Функция закрытия модального окна добавления сотрудника
function closeAddEmployeeModal() {
    document.getElementById('addEmployeeModal').style.display = 'none';
}

function showEmployeesSection() {
    document.getElementById('employees-section').style.display = 'block';
}

// Функция загрузки сотрудников с сервера
async function loadEmployees(page = 0) {
    try {
        const response = await fetchWithAuth(`/admin/employees?page=${page}&size=20`, {
            method: 'GET',
        });

        if (response.ok) {
            const data = await response.json();
            employees = data.content;  // Сохраняем сотрудников в глобальную переменную
            displayEmployees(employees);
            setupPagination(data.page.totalPages, page);
            currentPage = page;
        } else {
            console.error('Ошибка загрузки сотрудников');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

// Функция для загрузки определенной страницы
function loadPage(page) {
    loadEmployees(page); // Загружаем сотрудников для указанной страницы
}

// Функция отображения сотрудников
function displayEmployees(employees) {
    const employeesList = document.getElementById('employees-list');
    employeesList.innerHTML = '';

    for (const employee of employees) {
        const employeeElement = document.createElement('div');
        employeeElement.className = 'employee card mb-3';
        employeeElement.innerHTML = `
            <div class="employee-summary card-header d-flex justify-content-between align-items-center">
                <input type="checkbox" class="me-2" id="employee-checkbox-${employee.id}" onclick="toggleEmployeeSelection(${employee.id})">
                <span>ID: ${employee.id}</span>
                <span>${employee.firstName} ${employee.lastName}</span>
                <span>${employee.phone}</span>
                <button class="btn btn-warning btn-sm" onclick="showEditEmployeeModal(${employee.id})">Редактировать</button>
                <button class="btn btn-danger btn-sm" onclick="deleteEmployee(${employee.id})">Удалить</button>
            </div>
        `;
        employeesList.appendChild(employeeElement);
    }
}

async function addEmployee() {
    const newEmployee = {
        employee: {
            firstName: document.getElementById('employee-first-name').value,
            lastName: document.getElementById('employee-last-name').value,
            phone: document.getElementById('employee-phone').value
        },
        user: {
            email: document.getElementById('employee-email').value,
            googleId: null // Или любое другое значение по умолчанию, если не используется
        },
        password: document.getElementById('employee-password').value
    };

    try {
        const response = await fetchWithAuth('/admin/employee/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(newEmployee)
        });

        if (response.ok) {
            alert('Сотрудник добавлен');
            loadEmployees(); // Обновляем список сотрудников
            closeAddEmployeeModal(); // Закрыть модальное окно после успешного добавления
        } else {
            const errorData = await response.json();
            if (response.status === 400 && errorData.message === 'Email already exists') {
                alert('Ошибка: Email уже существует.');
            } else {
                displayValidationErrors(errorData); // Отображаем другие ошибки валидации
            }
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

// Функция показа модального окна редактирования сотрудника
function showEditEmployeeModal(employeeId) {
    const employee = employees.find(emp => emp.id === employeeId);
    document.getElementById('edit-employee-first-name').value = employee.firstName;
    document.getElementById('edit-employee-last-name').value = employee.lastName;
    document.getElementById('edit-employee-phone').value = employee.phone;
    document.getElementById('edit-employee-id').value = employee.id;

    document.getElementById('editEmployeeModal').style.display = 'flex';

    document.getElementById('edit-employee-save-button').onclick = function() {
        updateEmployee(employeeId);
    };
}

// Функция закрытия модального окна редактирования сотрудника
function closeEditEmployeeModal() {
    document.getElementById('editEmployeeModal').style.display = 'none';
}

// Функция обновления сотрудника
async function updateEmployee(employeeId) {
    const updatedEmployee = {
        firstName: document.getElementById('edit-employee-first-name').value,
        lastName: document.getElementById('edit-employee-last-name').value,
        phone: document.getElementById('edit-employee-phone').value,
    };

    try {
        const response = await fetchWithAuth(`/admin/employee/update/${employeeId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedEmployee)
        });

        if (response.ok) {
            alert('Сотрудник обновлен');
            loadEmployees(); // Обновляем список сотрудников
            closeEditEmployeeModal()
        } else{
            const errorData = await response.json();
            displayValidationErrors(errorData); // Отображаем другие ошибки валидации
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

// Функция удаления сотрудника
async function deleteEmployee(employeeId) {
    if (confirm('Вы уверены, что хотите удалить этого сотрудника?')) {
        try {
            const response = await fetchWithAuth(`/admin/user/delete/${employeeId}`, {
                method: 'DELETE',
            });

            if (response.ok) {
                alert('Сотрудник удален');
                loadEmployees(); // Обновляем список сотрудников
            } else {
                console.error('Ошибка при удалении сотрудника');
            }
        } catch (error) {
            console.error('Ошибка:', error);
        }
    }
}

// Функция для переключения состояния выбора сотрудника
function toggleEmployeeSelection(employeeId) {
    const checkbox = document.getElementById(`employee-checkbox-${employeeId}`);
    if (checkbox.checked) {
        // Добавляем сотрудника в массив выбранных
        selectedEmployees.push(employeeId);
    } else {
        // Удаляем сотрудника из массива выбранных
        selectedEmployees = selectedEmployees.filter(id => id !== employeeId);
    }
}

// Функция для удаления выбранных сотрудников
async function deleteSelectedEmployees() {
    if (selectedEmployees.length === 0) {
        alert('Пожалуйста, выберите сотрудников для удаления.');
        return;
    }

    const confirmation = confirm('Вы уверены, что хотите удалить выбранных сотрудников?');
    if (!confirmation) return;

    try {
        const response = await fetchWithAuth('/admin/users/delete', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(selectedEmployees)
        });

        if (response.ok) {
            alert('Сотрудники успешно удалены.');
            loadEmployees(); // Обновляем список сотрудников
            selectedEmployees = []; // Очищаем массив выбранных сотрудников
        } else {
            console.error('Ошибка при удалении сотрудников');
            alert('Не удалось удалить сотрудников. Пожалуйста, попробуйте позже.');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

// Функция для отображения ошибок валидации
function displayValidationErrors(errors) {
    let errorMessages = '';

    // Предполагаем, что ошибки приходят в формате { field: 'error message' }
    for (const field in errors) {
        errorMessages += `${field}: ${errors[field]} \n`;
    }

    alert(`Ошибки валидации:\n${errorMessages}`);
}

// Функция для настройки пагинации
function setupPagination(totalPages, currentPage) {
    const paginationContainer = document.getElementById('pagination-container'); // Измените ID на ваш актуальный
    paginationContainer.innerHTML = '';

    // Скрываем контейнер, если страниц меньше или равно 1
    if (totalPages <= 1) {
        paginationContainer.style.display = 'none';
        return;
    }

    paginationContainer.style.display = 'flex'; // Показываем контейнер

    // Добавляем кнопки пагинации
    for (let i = 0; i < totalPages; i++) {
        const pageItem = document.createElement('li');
        pageItem.classList.add('page-item');

        // Если это текущая страница, добавляем класс 'active'
        if (i === currentPage) {
            pageItem.classList.add('active');
        }

        pageItem.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`; // Пагинация с 1 до totalPages

        // Добавляем обработчик события для клика по кнопке страницы
        pageItem.addEventListener('click', (e) => {
            e.preventDefault();
            loadPage(i); // Замените fetchProducts на loadPage
        });

        paginationContainer.appendChild(pageItem);
    }
}