// Глобальная переменная для хранения списка сотрудников
let employees = [];

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
        const response = await fetch(`/admin/employees?page=${page}&size=20`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (response.ok) {
            const data = await response.json();
            employees = data.content;  // Сохраняем сотрудников в глобальную переменную
            displayEmployees(employees);
            setupPagination(data.totalPages);
        } else {
            console.error('Ошибка загрузки сотрудников');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
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

// Функция добавления нового сотрудника
async function addEmployee() {
    const newEmployee = {
        firstName: document.getElementById('employee-first-name').value,
        lastName: document.getElementById('employee-last-name').value,
        phone: document.getElementById('employee-phone').value,
        email: document.getElementById('employee-email').value,
        password: document.getElementById('employee-password').value
    };

    try {
        const response = await fetch('/admin/employees', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(newEmployee)
        });

        if (response.ok) {
            alert('Сотрудник добавлен');
            loadEmployees(); // Обновляем список сотрудников
        } else {
            console.error('Ошибка при добавлении сотрудника');
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
    document.getElementById('edit-employee-email').value = employee.email;
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
        email: document.getElementById('edit-employee-email').value,
    };

    try {
        const response = await fetch(`/admin/employees/${employeeId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedEmployee)
        });

        if (response.ok) {
            alert('Сотрудник обновлен');
            loadEmployees(); // Обновляем список сотрудников
        } else {
            console.error('Ошибка при обновлении сотрудника');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

// Функция удаления сотрудника
async function deleteEmployee(employeeId) {
    if (confirm('Вы уверены, что хотите удалить этого сотрудника?')) {
        try {
            const response = await fetch(`/admin/employees/${employeeId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                }
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

// Дополнительные функции для удаления выбранных сотрудников и переключения состояния чекбоксов
function toggleEmployeeSelection(employeeId) {
    const checkbox = document.getElementById(`employee-checkbox-${employeeId}`);
    // Здесь можно реализовать логику для управления состоянием выбранных сотрудников
}

function deleteSelectedEmployees() {
    const selectedIds = employees.filter(emp => document.getElementById(`employee-checkbox-${emp.id}`).checked).map(emp => emp.id);
    // Здесь можно реализовать логику для удаления выбранных сотрудников
}

