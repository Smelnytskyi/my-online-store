let employees = [];
let selectedEmployees = [];
let currentPage = 0;

// Data loading when the page starts
window.onload = function () {
    loadEmployees();
};

// Function to show the modal window for adding an employee
function showAddEmployeeModal() {
    document.getElementById('addEmployeeModal').style.display = 'flex';
}

// Function to close the modal window for adding an employee
function closeAddEmployeeModal() {
    document.getElementById('addEmployeeModal').style.display = 'none';
}

function showEmployeesSection() {
    document.getElementById('employees-section').style.display = 'block';
}

// Function to display the employees section
async function loadEmployees(page = 0) {
    try {
        const response = await fetchWithAuth(`/admin/employees?page=${page}&size=20`, {
            method: 'GET',
        });

        if (response.ok) {
            const data = await response.json();
            employees = data.content;
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

// Function to load a page of employees
function loadPage(page) {
    loadEmployees(page);
}

// Function to display employees on the page
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
            googleId: null
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
            loadEmployees();
            closeAddEmployeeModal();
        } else {
            const errorData = await response.json();
            if (response.status === 400 && errorData.message === 'Email already exists') {
                alert('Ошибка: Email уже существует.');
            } else {
                displayValidationErrors(errorData);
            }
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

// Function to show the modal window for editing an employee
function showEditEmployeeModal(employeeId) {
    const employee = employees.find(emp => emp.id === employeeId);
    document.getElementById('edit-employee-first-name').value = employee.firstName;
    document.getElementById('edit-employee-last-name').value = employee.lastName;
    document.getElementById('edit-employee-phone').value = employee.phone;
    document.getElementById('edit-employee-id').value = employee.id;

    document.getElementById('editEmployeeModal').style.display = 'flex';

    document.getElementById('edit-employee-save-button').onclick = function () {
        updateEmployee(employeeId);
    };
}

// Function to close the modal window for editing an employee
function closeEditEmployeeModal() {
    document.getElementById('editEmployeeModal').style.display = 'none';
}

// Function to update an employee's information
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
            loadEmployees();
            closeEditEmployeeModal()
        } else {
            const errorData = await response.json();
            displayValidationErrors(errorData);
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

// Function to delete an employee
async function deleteEmployee(employeeId) {
    if (confirm('Вы уверены, что хотите удалить этого сотрудника?')) {
        try {
            const response = await fetchWithAuth(`/admin/user/delete/${employeeId}`, {
                method: 'DELETE',
            });

            if (response.ok) {
                alert('Сотрудник удален');
                loadEmployees();
            } else {
                console.error('Ошибка при удалении сотрудника');
            }
        } catch (error) {
            console.error('Ошибка:', error);
        }
    }
}

// Function to toggle the selection of an employee
function toggleEmployeeSelection(employeeId) {
    const checkbox = document.getElementById(`employee-checkbox-${employeeId}`);
    if (checkbox.checked) {
        selectedEmployees.push(employeeId);
    } else {
        selectedEmployees = selectedEmployees.filter(id => id !== employeeId);
    }
}

// Function to delete selected employees
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
            loadEmployees();
            selectedEmployees = [];
        } else {
            console.error('Ошибка при удалении сотрудников');
            alert('Не удалось удалить сотрудников. Пожалуйста, попробуйте позже.');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

// Function to display validation error messages
function displayValidationErrors(errors) {
    let errorMessages = '';

    for (const field in errors) {
        errorMessages += `${field}: ${errors[field]} \n`;
    }
    alert(`Ошибки валидации:\n${errorMessages}`);
}

// Function to set up pagination controls
function setupPagination(totalPages, currentPage) {
    const paginationContainer = document.getElementById('pagination-container');
    paginationContainer.innerHTML = '';

    if (totalPages <= 1) {
        paginationContainer.style.display = 'none';
        return;
    }

    paginationContainer.style.display = 'flex';

    for (let i = 0; i < totalPages; i++) {
        const pageItem = document.createElement('li');
        pageItem.classList.add('page-item');

        if (i === currentPage) {
            pageItem.classList.add('active');
        }

        pageItem.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`;

        pageItem.addEventListener('click', (e) => {
            e.preventDefault();
            loadPage(i);
        });

        paginationContainer.appendChild(pageItem);
    }
}