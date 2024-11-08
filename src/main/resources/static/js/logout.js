async function logout() {
    try {
        // Отправляем запрос на сервер для выхода
        const response = await fetch('/auth/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            // Удаляем токен из localStorage
            localStorage.removeItem('token');

            // Очистка других данных сессии, если нужно
            sessionStorage.clear();

            // Перенаправление на страницу входа или главную
            window.location.href = '/'; // Или на главную страницу
        } else {
            console.error('Ошибка выхода');
        }
    } catch (error) {
        console.error('Ошибка при запросе выхода:', error);
    }
}

// Добавляем слушатель события на кнопку выхода
document.addEventListener('DOMContentLoaded', () => {
    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', logout);
    }
});