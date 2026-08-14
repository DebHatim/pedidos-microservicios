const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export async function fetchProducts() {
    const response = await fetch(`${API_BASE_URL}/api/products`);

    if (!response.ok) {
        throw new Error(`The products could not be loaded (HTTP ${response.status})`);
    }

    return response.json();
}

export async function createOrder(orderDTO) {
    const response = await fetch(`${API_BASE_URL}/api/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(orderDTO)
    });

    if (!response.ok) {
        throw new Error(`The order could not be created (HTTP ${response.status})`);
    }

    return response.json();
}

export async function getOrders() {
    const response = await fetch(`${API_BASE_URL}/api/orders`);

    if (!response.ok) {
        throw new Error('Error loading order history');
    }

    return await response.json();
}

export async function getOrder(id) {
    const response = await fetch(`${API_BASE_URL}/api/orders/${id}`);

    if (!response.ok) {
        throw new Error('Error checking order');
    }

    return await response.json();
}