export interface Product {
    id: number;
    cardId: number;
    cardName: string;
    imageUrl: string;

    cardNumber: string;
    setName: string;
    setPrintedTotal: number;
    illustrator: string | null;
    rarity: string | null;
    superType: string | null;
    subTypes: string[];

    language: string;
    variant: string;
    condition: string;

    stock: number;
    price: number;
    lastPriceReview: string;
}