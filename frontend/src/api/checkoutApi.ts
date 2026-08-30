import type {
    CheckoutRequest,
    OrderResponse
} from '../types/Checkout';

const GUEST_CART_TOKEN_KEY = 'guestCartToken';

function createHeaders(): Record<string, string>
{
    const token = localStorage.getItem('token');

    const guestToken = localStorage.getItem(GUEST_CART_TOKEN_KEY);

    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
    };

    if (token)
    {
        headers.Authorization = `Bearer ${token}`;
    }
    else if (guestToken)
    {
        headers['X-Guest-Cart-Token'] = guestToken;
    }

    return headers;
}

export async function checkout(request: CheckoutRequest): Promise<OrderResponse>
{
    const response = await fetch('/api/checkout', {
            method: 'POST',
            headers: createHeaders(),
            body: JSON.stringify(request),
        }
    );

    if (!response.ok)
    {
        let message = 'No se pudo completar la compra';

        try
        {
            const error = await response.json();

            if (error.message)
            {
                message = error.message;
            }
        }
        catch
        {}

        throw new Error(message);
    }

    const order: OrderResponse = await response.json();

    localStorage.removeItem(GUEST_CART_TOKEN_KEY);

    return order;
}