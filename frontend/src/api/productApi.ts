import type { Product } from '../types/Product';

export async function getProducts(): Promise<Product[]>
{
    const response = await fetch('/api/products/available');

    if (!response.ok)
    {
        throw new Error('No se pudo cargar el catálogo');
    }

    return response.json();
}