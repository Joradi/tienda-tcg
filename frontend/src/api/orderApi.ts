import type { OrderResponse } from '../types/Checkout';

function createHeaders(): Record<string, string>
{
    const token =
        localStorage.getItem('token');

    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
    };

    if (token)
    {
        headers.Authorization = `Bearer ${token}`;
    }

    return headers;
}

export async function getOrders(): Promise<OrderResponse[]>
{
    const response = await fetch('/api/orders', {
            method: 'GET',
            headers: createHeaders(),
        });

    if (!response.ok)
    {
        throw new Error('No se pudieron cargar las órdenes');
    }

    return response.json();
}

export async function getOrder(orderId: number): Promise<OrderResponse>
{
    const response = await fetch(`/api/orders/${orderId}`, {
            method: 'GET',
            headers: createHeaders(),
        }
    );

    if (!response.ok)
    {
        throw new Error('No se pudo cargar la orden');
    }

    return response.json();
}