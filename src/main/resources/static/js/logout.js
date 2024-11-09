async function logout() {
    try {
        const response = await fetch('/auth/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            localStorage.removeItem('token');
            sessionStorage.clear();
            window.location.href = '/';
        } else {
            console.error('Ошибка выхода');
        }
    } catch (error) {
        console.error('Ошибка при запросе выхода:', error);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', logout);
    }
});