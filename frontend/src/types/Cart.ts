export interface CartItem {
    productId: number;
    cardName: string;
    imageUrl: string;
    language: string;
    variant: string;
    condition: string;
    unitPrice: number;
    quantity: number;
    subtotal: number;
}

export interface CartResponse {
    cartId: number;
    guestToken: string | null;
    items: CartItem[];
    netAmount: number;
    taxAmount: number;
    total: number;
}