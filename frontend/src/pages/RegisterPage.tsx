import {
    useState
} from 'react';

import type {
    FormEvent
} from 'react';

import {
    Link,
    useNavigate
} from 'react-router-dom';

import {
    register
} from '../api/authApi';

function RegisterPage()
{
    const navigate =
        useNavigate();

    const [email, setEmail] =
        useState('');

    const [password, setPassword] =
        useState('');

    const [confirmPassword, setConfirmPassword] =
        useState('');

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState('');

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
                'Debes ingresar un email'
            );

            return;
        }

        if (
            password.length < 8 ||
            password.length > 72
        )
        {
            setError(
                'La contraseña debe tener entre 8 y 72 caracteres'
            );

            return;
        }

        if (password !== confirmPassword)
        {
            setError(
                'Las contraseñas no coinciden'
            );

            return;
        }

        setLoading(true);
        setError('');

        try
        {
            await register({
                email:
                normalizedEmail,
                password,
            });

            navigate(
                '/login',
                {
                    replace: true,
                    state: {
                        registered: true
                    }
                }
            );
        }
        catch (registerError)
        {
            if (
                registerError instanceof Error
            )
            {
                setError(
                    registerError.message
                );
            }
            else
            {
                setError(
                    'No se pudo crear la cuenta'
                );
            }
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
                        Tu colección
                        <span>
                            {' '}empieza aquí.
                        </span>
                    </h1>

                    <p>
                        Crea tu cuenta para comprar cartas,
                        administrar tu carrito y revisar
                        tus pedidos.
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
                                Compra
                            </strong>

                            <span>
                                Agrega productos al carrito
                                y completa tus pedidos.
                            </span>
                        </div>

                        <div>
                            <strong>
                                Seguimiento
                            </strong>

                            <span>
                                Revisa tu historial
                                de órdenes.
                            </span>
                        </div>

                    </div>

                </section>

                <section className="auth-card">

                    <header className="auth-card-header">

                        <span>
                            NUEVA CUENTA
                        </span>

                        <h2>
                            Registrarse
                        </h2>

                        <p>
                            Ingresa tus datos para crear
                            una cuenta.
                        </p>

                    </header>

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
                                autoComplete="new-password"
                                placeholder="Mínimo 8 caracteres"
                                minLength={8}
                                maxLength={72}
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

                        <label>
                            Confirmar contraseña

                            <input
                                type="password"
                                autoComplete="new-password"
                                placeholder="Repite tu contraseña"
                                minLength={8}
                                maxLength={72}
                                required
                                value={confirmPassword}
                                disabled={loading}
                                onChange={(event) =>
                                    setConfirmPassword(
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
                                ? 'Creando cuenta...'
                                : 'Crear cuenta'}
                        </button>

                    </form>

                    <footer className="auth-card-footer">

                        <span>
                            ¿Ya tienes una cuenta?
                        </span>

                        <Link to="/login">
                            Iniciar sesión
                        </Link>

                    </footer>

                </section>

            </div>

        </main>
    );
}

export default RegisterPage;