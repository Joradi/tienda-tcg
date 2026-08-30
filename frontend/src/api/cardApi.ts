export interface CardSearchResult {
    id: number;
    name: string;
    number: string;
    imageUrl: string;
    setName: string;
    setPrintedTotal: number;
}

interface CardSearchApiResponse {
    id: number;
    name: string;
    number: string;
    imageUrl: string;
    setName: string;
    printedTotal: number;
}

function formatCardNumber(number: string): string
{
    if(!/^\d+$/.test(number))
    {
        return number;
    }

    return number.padStart(
        3,
        '0'
    );
}

function formatPrintedTotal(printedTotal: number): string
{
    return String(printedTotal).padStart(
        3,
        '0'
    );
}

export function getCollectorNumber(card: CardSearchResult): string
{
    return `${card.number}/${formatPrintedTotal(card.setPrintedTotal)}`;
}

export async function searchCards(query: string): Promise<CardSearchResult[]>
{
    const value = query.trim();

    if(value === '')
    {
        return [];
    }

    const response = await fetch(`/api/cards/search?query=${encodeURIComponent(value)}`
    );

    if(!response.ok)
    {
        throw new Error('No se pudieron buscar las cartas');
    }

    const cards: CardSearchApiResponse[] =
        await response.json();

    return cards.map(card => ({
        id: card.id,
        name: card.name,
        number: formatCardNumber(
            card.number
        ),
        imageUrl: card.imageUrl,
        setName: card.setName,
        setPrintedTotal: card.printedTotal,
    }));
}