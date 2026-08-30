import {
    BrowserRouter,
    Link,
    Navigate,
    Route,
    Routes,
    useNavigate
} from 'react-router-dom';

import {
    useCallback,
    useEffect,
    useState
} from 'react';

import CatalogPage from './pages/CatalogPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import OrdersPage from './pages/OrdersPage';
import AdminPage from './pages/AdminPage';
import ImportationsPage from './pages/ImportationsPage';
import CheckoutPage from './pages/CheckoutPage';

import CartDrawer from './components/CartDrawer';

import {
    getStoredUser
} from './auth/jwt';

import ProtectedRoute
    from './auth/ProtectedRoute';

import {
    getCart,
    hasCartIdentity
} from './api/cartApi';

import type {
    CartResponse
} from './types/Cart';

function AppContent()
{
    const navigate =
        useNavigate();

    const user =
        getStoredUser();

    const [cartOpen, setCartOpen] =
        useState(false);

    const [cart, setCart] =
        useState<CartResponse | null>(null);

    const handleCartChange =
        useCallback(
            (updatedCart: CartResponse) =>
            {
                setCart(updatedCart);
            },
            []
        );

    useEffect(() =>
    {
        if (!hasCartIdentity())
        {
            setCart(null);
            return;
        }

        async function loadCart()
        {
            try
            {
                const response =
                    await getCart();

                setCart(response);
            }
            catch
            {
                setCart(null);
            }
        }

        loadCart();
    }, [user?.sub]);

    function handleCartUpdated(
        updatedCart: CartResponse
    )
    {
        setCart(updatedCart);
        setCartOpen(true);
    }

    function handleCheckoutCompleted()
    {
        setCart(null);
        setCartOpen(false);
    }

    function handleLogout()
    {
        localStorage.removeItem('token');

        setCart(null);
        setCartOpen(false);

        navigate('/login');

        window.location.reload();
    }

    const cartItemCount =
        cart?.items.reduce(
            (total, item) =>
                total + item.quantity,
            0
        ) ?? 0;

    return (
        <>
            <nav>

                <div className="nav-left">

                    <Link to="/">
                        Catálogo
                    </Link>

                    {user && (
                        <Link to="/orders">
                            Órdenes
                        </Link>
                    )}

                    {user?.role === 'ROLE_ADMIN' && (
                        <>
                            <Link to="/admin">
                                Admin
                            </Link>

                            <Link to="/admin/importations">
                                Importaciones
                            </Link>
                        </>
                    )}

                </div>

                <div className="nav-right">

                    <button
                        type="button"
                        className="nav-cart-button"
                        onClick={() =>
                            setCartOpen(true)
                        }
                    >
                        🛒 Carrito ({cartItemCount})
                    </button>

                    {!user ? (
                        <Link to="/login">
                            Login
                        </Link>
                    ) : (
                        <>
                            <span>
                                {user.sub}
                            </span>

                            <button
                                type="button"
                                onClick={handleLogout}
                            >
                                Cerrar sesión
                            </button>
                        </>
                    )}

                </div>

            </nav>

            <Routes>

                <Route
                    path="/"
                    element={
                        <CatalogPage
                            onCartUpdated={
                                handleCartUpdated
                            }
                        />
                    }
                />

                <Route
                    path="/cart"
                    element={
                        <Navigate
                            to="/"
                            replace
                        />
                    }
                />

                <Route
                    path="/checkout"
                    element={
                        <CheckoutPage
                            onCheckoutCompleted={
                                handleCheckoutCompleted
                            }
                        />
                    }
                />

                <Route
                    path="/login"
                    element={
                        <LoginPage />
                    }
                />

                <Route
                    path="/register"
                    element={
                        <RegisterPage />
                    }
                />

                <Route
                    path="/orders"
                    element={
                        <ProtectedRoute>
                            <OrdersPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin"
                    element={
                        <ProtectedRoute
                            requiredRole="ROLE_ADMIN"
                        >
                            <AdminPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/importations"
                    element={
                        <ProtectedRoute
                            requiredRole="ROLE_ADMIN"
                        >
                            <ImportationsPage />
                        </ProtectedRoute>
                    }
                />

            </Routes>

            <CartDrawer
                open={cartOpen}
                cart={cart}
                onClose={() =>
                    setCartOpen(false)
                }
                onCartChange={
                    handleCartChange
                }
            />

        </>
    );
}

function App()
{
    return (
        <BrowserRouter>
            <AppContent />
        </BrowserRouter>
    );
}

export default App;