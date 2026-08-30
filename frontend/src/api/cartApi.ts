import type { CartResponse } from '../types/Cart';

const GUEST_CART_TOKEN_KEY = 'guestCartToken';

function createHeaders(includeContentType = false): Record<string, string>
{
    const token = localStorage.getItem('token');
    const guestToken = localStorage.getItem(GUEST_CART_TOKEN_KEY);

    const headers: Record<string, string> = {};

    if (includeContentType)
    {
        headers['Content-Type'] = 'application/json';
    }

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

function storeGuestToken(cart: CartResponse)
{
    const token = localStorage.getItem('token');

    if (!token && cart.guestToken)
    {
        localStorage.setItem(GUEST_CART_TOKEN_KEY, cart.guestToken);
    }
}

export function hasCartIdentity()
{
    return Boolean(localStorage.getItem('token') || localStorage.getItem(GUEST_CART_TOKEN_KEY));
}

export async function getCart(): Promise<CartResponse>
{
    const response = await fetch('/api/cart', {
        headers: createHeaders(),
    });

    if (!response.ok)
    {
        throw new Error('No se pudo cargar el carrito');
    }
    const cart = await response.json() as CartResponse;
    storeGuestToken(cart);
    return cart;
}

export async function addToCart(
    productId: number,
    quantity: number
): Promise<CartResponse>
{
    const response = await fetch('/api/cart/items', {
        method: 'POST',
        headers: createHeaders(true),
        body: JSON.stringify({
            productId,
            quantity,
        }),
    });

    if (!response.ok)
    {
        throw new Error(
            'No se pudo agregar la carta al carrito'
        );
    }
    const cart = await response.json() as CartResponse;
    storeGuestToken(cart);
    return cart;
}

export async function updateCartItem(
    productId: number,
    quantity: number
): Promise<CartResponse>
{
    const response = await fetch(
        `/api/cart/items/${productId}`,
        {
            method: 'PATCH',
            headers: createHeaders(true),
            body: JSON.stringify({
                quantity,
            }),
        }
    );

    if (!response.ok)
    {
        throw new Error('No se pudo actualizar la cantidad');
    }
    const cart = await response.json() as CartResponse;
    storeGuestToken(cart);
    return cart;
}

export async function removeCartItem(productId: number): Promise<CartResponse>
{
    const response = await fetch(
        `/api/cart/items/${productId}`,
        {
            method: 'DELETE',
            headers: createHeaders(),
        }
    );

    if (!response.ok)
    {
        throw new Error('No se pudo eliminar la carta');
    }
    const cart = await response.json() as CartResponse;
    storeGuestToken(cart);
    return cart;
}

export async function clearCart(): Promise<CartResponse>
{
    const response = await fetch('/api/cart/items', {
        method: 'DELETE',
        headers: createHeaders(),
    });

    if (!response.ok)
    {
        throw new Error('No se pudo vaciar el carrito');
    }

    const cart = await response.json() as CartResponse;
    storeGuestToken(cart);
    return cart;
}