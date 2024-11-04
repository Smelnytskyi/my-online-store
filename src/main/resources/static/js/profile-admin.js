// Загрузка данных при старте страницы
window.onload = function() {
    loadEmployees();
};

function showEmployeesSection() {
    document.getElementById('employees-section').style.display = 'block';
}

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
            displayEmployees(data.content);
            setupPagination(data.totalPages);
        } else {
            console.error('Ошибка загрузки сотрудников');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

function displayEmployees(employees) {
    const employeesList = document.getElementById('employees-list');
    employeesList.innerHTML = '';

    for (const employee of employees) {
        const employeeElement = document.createElement('div');
        employeeElement.className = 'employee card mb-3';
        employeeElement.innerHTML = `
            <div class="employee-summary card-header d-flex justify-content-between align-items-center" onclick="toggleEmployeeDetails(${employee.id})">
                <input type="checkbox" class="me-2" onclick="selectEmployee(${employee.id})">
                <span>ID: ${employee.id}</span>
                <span>${employee.firstName} ${employee.lastName}</span>
                <span>${employee.phone}</span>
            </div>
            <div class="employee-details card-body" id="employee-${employee.id}" style="display: none;">
                <button class="btn btn-warning me-2" onclick="showEditEmployeeModal(${employee.id})">Редактировать</button>
                <button class="btn btn-danger" onclick="deleteEmployee(${employee.id})">Удалить</button>
            </div>
        `;
        employeesList.appendChild(employeeElement);
    }
}

function toggleEmployeeDetails(employeeId) {
    const details = document.getElementById(`employee-${employeeId}`);
    details.style.display = details.style.display === "none" ? "block" : "none";
}

function showAddEmployeeModal() {
    const modal = new bootstrap.Modal(document.getElementById('addEmployeeModal'));
    modal.show();
}

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
            loadEmployees();
        } else {
            console.error('Ошибка при добавлении сотрудника');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

function deleteSelectedEmployees() {
    const selectedEmployeeIds = getSelectedEmployeeIds();
    // Добавить логику удаления выбранных сотрудников
}

// Дополнительные вспомогательные функции: редактирование, удаление и др.
