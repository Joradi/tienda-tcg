export interface CheckoutRequest {
    customerName: string;
    customerEmail: string;
    shippingAddress: string;
}

export interface OrderItem {
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

export interface OrderResponse {
    orderId: number;
    customerEmail: string;
    customerName: string;
    shippingAddress: string;
    status: string;
    netAmount: number;
    taxAmount: number;
    total: number;
    createdAt: string;
    items: OrderItem[];
}