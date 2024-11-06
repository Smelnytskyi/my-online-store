function displayValidationErrors(errors, containerId) {
    // Получаем контейнер для ошибок
    const errorContainer = document.getElementById(containerId);
    errorContainer.innerHTML = ''; // Очищаем контейнер

    // Проверяем, что ошибки это массив
    if (Array.isArray(errors) && errors.length > 0) {
        // Перебираем каждую ошибку из массива
        errors.forEach((error) => {
            const errorElement = document.createElement('div');
            errorElement.classList.add('error-message');
            errorElement.innerText = error;  // Каждую ошибку отображаем как текст
            errorContainer.appendChild(errorElement);
        });
        errorContainer.style.display = 'block'; // Показываем ошибки
    } else {
        errorContainer.style.display = 'none'; // Если ошибок нет, скрываем контейнер
    }
}