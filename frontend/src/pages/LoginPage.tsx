import {
    useState
} from 'react';

import type {
    FormEvent
} from 'react';

import {
    Link,
    useLocation,
    useNavigate
} from 'react-router-dom';

import {
    login
} from '../api/authApi';

interface LoginLocationState {
    registered?: boolean;
}

function LoginPage()
{
    const navigate =
        useNavigate();

    const location =
        useLocation();

    const locationState =
        location.state as LoginLocationState | null;

    const [email, setEmail] =
        useState('');

    const [password, setPassword] =
        useState('');

    const [error, setError] =
        useState('');

    const [loading, setLoading] =
        useState(false);

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>
    )
    {
        event.preventDefault();

        if (loading)
        {
            return;
        }

        const normalizedEmail =
            email.trim();

        if (!normalizedEmail)
        {
            setError(
                'Debes ingresar tu email'
            );

            return;
        }

        if (!password)
        {
            setError(
                'Debes ingresar tu contraseña'
            );

            return;
        }

        setError('');
        setLoading(true);

        try
        {
            const response =
                await login({
                    email:
                    normalizedEmail,
                    password,
                });

            localStorage.setItem(
                'token',
                response.token
            );

            navigate(
                '/',
                {
                    replace: true
                }
            );

            window.location.reload();
        }
        catch (loginError)
        {
            setError(
                loginError instanceof Error
                    ? loginError.message
                    : 'Email o contraseña incorrectos'
            );
        }
        finally
        {
            setLoading(false);
        }
    }

    return (
        <main className="auth-page">

            <div className="auth-shell">

                <section className="auth-brand">

                    <p className="catalog-eyebrow">
                        TCG PREMIUM
                    </p>

                    <h1>
                        Cartas que quieres.
                        <span>
                            {' '}Colección que crece.
                        </span>
                    </h1>

                    <p>
                        Accede a tu cuenta para comprar cartas,
                        gestionar tu carrito y revisar tus pedidos.
                    </p>

                    <div className="auth-brand-features">

                        <div>
                            <strong>
                                Catálogo
                            </strong>

                            <span>
                                Explora cartas disponibles
                                y sus distintas versiones.
                            </span>
                        </div>

                        <div>
                            <strong>
                                Carrito
                            </strong>

                            <span>
                                Mantén tus productos antes
                                de completar una compra.
                            </span>
                        </div>

                        <div>
                            <strong>
                                Pedidos
                            </strong>

                            <span>
                                Revisa tus compras
                                y su información.
                            </span>
                        </div>

                    </div>

                </section>

                <section className="auth-card">

                    <header className="auth-card-header">

                        <span>
                            BIENVENIDO
                        </span>

                        <h2>
                            Iniciar sesión
                        </h2>

                        <p>
                            Ingresa tus credenciales para
                            acceder a tu cuenta.
                        </p>

                    </header>

                    {locationState?.registered && (
                        <p className="auth-success">
                            Cuenta creada correctamente.
                            Ya puedes iniciar sesión.
                        </p>
                    )}

                    <form
                        className="auth-form"
                        onSubmit={handleSubmit}
                    >

                        <label>
                            Email

                            <input
                                type="email"
                                autoComplete="email"
                                placeholder="nombre@email.com"
                                required
                                value={email}
                                disabled={loading}
                                onChange={(event) =>
                                    setEmail(
                                        event.target.value
                                    )
                                }
                            />
                        </label>

                        <label>
                            Contraseña

                            <input
                                type="password"
                                autoComplete="current-password"
                                placeholder="Tu contraseña"
                                required
                                value={password}
                                disabled={loading}
                                onChange={(event) =>
                                    setPassword(
                                        event.target.value
                                    )
                                }
                            />
                        </label>

                        {error && (
                            <p className="auth-error">
                                {error}
                            </p>
                        )}

                        <button
                            type="submit"
                            className="auth-submit-button"
                            disabled={loading}
                        >
                            {loading
                                ? 'Ingresando...'
                                : 'Ingresar'}
                        </button>

                    </form>

                    <footer className="auth-card-footer">

                        <span>
                            ¿No tienes una cuenta?
                        </span>

                        <Link to="/register">
                            Crear cuenta
                        </Link>

                    </footer>

                </section>

            </div>

        </main>
    );
}

export default LoginPage;