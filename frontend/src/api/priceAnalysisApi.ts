import type {
    PriceAnalysisRequest,
    SaleScenarioAnalysis
} from '../types/Importation';

function createHeader(): Record<string, string>
{
    const token = localStorage.getItem('token');

    const headers: Record<string, string> = {
        'Content-Type': 'application/json'
    };

    if(token)
    {
        headers.Authorization = `Bearer ${token}`;
    }

    return headers;
}

async function getErrorMessage(response: Response, defaultMessage: string): Promise<string>
{
    try
    {
        const error = await response.json();

        if(error.message)
        {
            return error.message;
        }
    }
    catch {}

    return defaultMessage;
}

export async function analyzePrice(request: PriceAnalysisRequest): Promise<SaleScenarioAnalysis[]>
{
    const response = await fetch('/api/importations/price-analysis', {
            method: 'POST',
            headers: createHeader(),
            body: JSON.stringify(request),
        });

    if(!response.ok)
    {
        throw new Error(await getErrorMessage(
            response, 'No se pudo calcular el análisis de precio'));
    }

    return response.json();
}