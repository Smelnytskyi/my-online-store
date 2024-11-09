async function checkRole(token){
    if (token){
        const roleResponse = await fetch('/auth/role', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        const roleData = await roleResponse.json();
        const role = roleData.role;
        return !(role === 'ADMIN' || role === 'EMPLOYEE');
    }
    return true;
}