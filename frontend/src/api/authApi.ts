export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    token: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
}

export interface RegisterResponse {
    id: number;
    email: string;
    role: string;
}

async function getErrorMessage(response: Response, defaultMessage: string): Promise<string>
{
    try
    {
        const error = await response.json();

        if (error.message)
        {
            return error.message;
        }
    }
    catch
    {}

    return defaultMessage;
}

export async function login(request: LoginRequest): Promise<LoginResponse>
{
    const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(request),
            });

    if (!response.ok)
    {
        throw new Error('Email o contraseña incorrectos');
    }

    return response.json();
}

export async function register(request: RegisterRequest): Promise<RegisterResponse>
{
    const response = await fetch('/api/users/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(request),
            });

    if (!response.ok)
    {
        throw new Error(await getErrorMessage(response, 'No se pudo crear la cuenta'));
    }

    return response.json();
}