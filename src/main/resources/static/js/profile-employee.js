document.addEventListener("DOMContentLoaded", function () {
    loadEmployeeProfile();

    // Загрузка и отображение информации работника
    function loadEmployeeProfile() {
        fetch('/client/profile', {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        })
            .then(response => response.json())
            .then(data => {
                document.getElementById("employee-firstName").textContent = data.firstName;
                document.getElementById("employee-lastName").textContent = data.lastName;
                document.getElementById("employee-phone").textContent = data.phone;
            })
            .catch(error => console.error("Ошибка загрузки профиля:", error));
    }

    document.getElementById("edit-personal-info").addEventListener("click", function () {
        document.getElementById("edit-info-form").style.display = "block";
    });


    document.getElementById("save-info").addEventListener("click", function () {
        const updatedData = {
            firstName: document.getElementById("input-firstName").value || null,
            lastName: document.getElementById("input-lastName").value || null,
            phone: document.getElementById("input-phone").value || null
        };

        fetch('/client/profile/update', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify( updatedData )
        })
            .then(response => {
                if (response.ok) {
                    // Если ответ успешен, выполняем действия сразу
                    loadEmployeeProfile();
                    toggleEditSection('edit-info-form');
                    return response.json().catch(() => ({})); // Обрабатываем JSON, даже если он пустой
                } else {
                    // Если статус не успешный, парсим ошибочный ответ
                    return response.json().then(result => {
                        throw new Error(result.errors ? result.errors.join(", ") : "Неизвестная ошибка");
                    });
                }
            })
            .catch(error => {
                // Обработка ошибок
                document.getElementById("error-messages").textContent = error.message;
                console.error("Ошибка:", error);
            });
    });

    document.getElementById("cancel-edit").addEventListener("click", function () {
        toggleEditSection('edit-info-form');
    });

    // Обновление пароля
    document.getElementById("update-password").addEventListener("click", function () {
        const oldPassword = document.getElementById("old-password").value;
        const newPassword = document.getElementById("new-password").value;

        fetch('/client/profile/change-password', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify({ oldPassword, newPassword })
        })
            .then(response => {
                if (response.ok) {
                    alert("Пароль успешно обновлен");
                } else {
                    document.getElementById("password-error-messages").textContent = "Неверный пароль";
                }
            })
            .catch(error => console.error("Ошибка:", error));
    });

    function toggleEditSection(sectionId) {
        const section = document.getElementById(sectionId);
        const isVisible = section.style.display === "block";
        section.style.display = isVisible ? "none" : "block";
        document.getElementById("personal-info").style.display = isVisible ? "block" : "none";
    }
});

