function displayValidationErrors(errors, containerId) {
    const errorContainer = document.getElementById(containerId);
    errorContainer.innerHTML = '';

    if (Array.isArray(errors) && errors.length > 0) {
        errors.forEach((error) => {
            const errorElement = document.createElement('div');
            errorElement.classList.add('error-message');
            errorElement.innerText = error;
            errorContainer.appendChild(errorElement);
        });
        errorContainer.style.display = 'block';
    } else {
        errorContainer.style.display = 'none';
    }
}