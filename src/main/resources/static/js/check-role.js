async function checkRole(token){
    if (token){
        // Получаем роль пользователя
        const roleResponse = await fetch('/auth/role', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        const roleData = await roleResponse.json();
        const role = roleData.role;

        console.log(role);

        // Если роль — админ или работник, не позволяем добавить товар в корзину
        return !(role === 'ADMIN' || role === 'EMPLOYEE');
    }
    return true;
}