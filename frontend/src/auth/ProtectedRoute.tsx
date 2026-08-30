import type {ReactNode} from "react";
import {getStoredUser} from "./jwt.ts";
import { Navigate } from 'react-router-dom';

interface ProtectedRouteProps{
    children: ReactNode;
    requiredRole?: string;
}

function ProtectedRoute({children, requiredRole}: ProtectedRouteProps)
{
    const user = getStoredUser();

    if(!user)
    {
        return <Navigate to="/login" replace />;
    }

    if(requiredRole && user.role !== requiredRole)
    {
        return <Navigate to="/" replace />;
    }

    return children;
}

export default ProtectedRoute;