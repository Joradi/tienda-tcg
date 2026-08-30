import type { Product } from '../types/Product';

interface ProductCreateRequest  {
    cardId: number;
    language: string;
    variant: string;
    condition: string;
    stock:number;
    price: number;
}

interface ProductUpdateRequest {
    stock?: number;
    price?: number;
}

function createHeaders(): Record<string, string>
{
    const token = localStorage.getItem('token');

    const headers: Record<string, string> = {'Content-Type': 'application/json',};

    if (token)
    {
        headers.Authorization = `Bearer ${token}`;
    }

    return headers;
}

export async function getAllProducts(): Promise<Product[]>
{
    const response = await fetch('/api/products', {
            method: 'GET',
            headers: createHeaders(),
        }
    );

    if (!response.ok)
    {
        throw new Error('No se pudo cargar el inventario');
    }

    return response.json();
}

export async function createProduct(request: ProductCreateRequest): Promise<Product>
{
    const response = await fetch('/api/products', {
            method: 'POST',
            headers: createHeaders(),
            body: JSON.stringify(request),
        }
    );

    if (!response.ok)
    {
        let message = 'No se pudo crear el producto';

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

    return response.json();
}

export async function updateProduct(productId: number, request: ProductUpdateRequest): Promise<Product>
{
    const response = await fetch(`/api/products/${productId}`, {
            method: 'PATCH',
            headers: createHeaders(),
            body: JSON.stringify(request),
        }
    );

    if (!response.ok)
    {
        let message = 'No se pudo actualizar el producto';

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

    return response.json();
}