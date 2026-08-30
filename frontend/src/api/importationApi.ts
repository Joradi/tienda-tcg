import type {
    ImportationAnalysis,
    ImportationCreateRequest,
    ImportationResponse,
    ImportScenarioComparison,
    ImportScenarioComparisonRequest
} from '../types/Importation';

function createHeader(): Record<string, string>
{
    const token = localStorage.getItem('token');

    const headers: Record<string, string> = { 'Content-Type': 'application/json' };

    if(token)
    {
        headers.Authorization = `Bearer ${token}`;
    }

    return headers;
}

async function getErrorMessage(response: Response, defaultMessage: string): Promise<string>
{
    try{
        const error = await response.json();

        if(error.message)
        {
            return error.message;
        }
    }
    catch{}

    return defaultMessage;
}

export async function getImportations(): Promise<ImportationResponse[]>
{
    const response = await fetch('/api/importations',
        {
            method: 'GET',
            headers: createHeader(),
        });

    if(!response.ok)
    {
        throw new Error(await getErrorMessage(
            response, 'No se pudieron cargar las importaciones'
        ));
    }

    return response.json();
}

export async function getImportation(importationId: number): Promise<ImportationResponse>
{
    const response = await fetch(`/api/importations/${importationId}`,
        {
            method: 'GET',
            headers: createHeader(),
        });

    if(!response.ok)
    {
        throw new Error(await getErrorMessage(
            response, 'No se pudo cargar la importación'
        ));
    }

    return response.json();
}

export async function createImportation(request: ImportationCreateRequest): Promise<ImportationResponse>
{
    const response = await fetch('/api/importations',
        {
            method: 'POST',
            headers: createHeader(),
            body: JSON.stringify(request),
        });

    if(!response.ok)
    {
        throw new Error(await getErrorMessage(
            response, 'No se pudo crear la importación'
        ));
    }

    return response.json();
}

export async function getImportationAnalysis(importationId: number): Promise<ImportationAnalysis>
{
    const response = await fetch(`/api/importations/${importationId}/analysis`,
        {
            method: 'GET',
            headers: createHeader(),
        });

    if(!response.ok)
    {
        throw new Error(await getErrorMessage(
            response, 'No se pudo calcular el análisis de la importación'
        ));
    }

    return response.json();
}

export async function compareImportationScenarios(request: ImportScenarioComparisonRequest): Promise<ImportScenarioComparison>
{
    const response = await fetch('/api/importations/scenarios/compare',
        {
            method: 'POST',
            headers: createHeader(),
            body: JSON.stringify(request),
        });

    if(!response.ok)
    {
        throw new Error(await getErrorMessage(
            response, 'No se pudieron comparar los escenarios'
        ));
    }

    return response.json();
}