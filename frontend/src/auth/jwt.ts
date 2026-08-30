export interface JwtPayload {
    sub: string;
    role: string;
    exp: number;
}

export function decodeJwt(token: string): JwtPayload | null
{
    try{
        const payload = token.split('.')[1];

        if(!payload)
        {
            return null;
        }

        const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');

        const decoded = atob(normalized);

        return JSON.parse(decoded) as JwtPayload;
    } catch
    {
        return null;
    }
}

export function getStoredUser(): JwtPayload | null
{
    const token = localStorage.getItem('token');

    if(!token)
    {
        return null;
    }

    const payload = decodeJwt(token);

    if(!payload)
    {
        return null;
    }

    if(payload.exp * 1000 <= Date.now())
    {
        localStorage.removeItem('token');
        return null;
    }

    return payload;
}