import {
    type FormEvent,
    useEffect,
    useState
} from 'react';
import { Link } from 'react-router-dom';

import {
    getCart,
    hasCartIdentity
} from '../api/cartApi';

import { checkout } from '../api/checkoutApi';

import type { CartResponse } from '../types/Cart';
import type { OrderResponse } from '../types/Checkout';

interface CheckoutPageProps {
    onCheckoutCompleted: () => void;
}

function CheckoutPage({
                          onCheckoutCompleted
                      }: CheckoutPageProps) {
    const [cart, setCart] =
        useState<CartResponse | null>(null);

    const [customerName, setCustomerName] =
        useState('');

    const [customerEmail, setCustomerEmail] =
        useState('');

    const [shippingAddress, setShippingAddress] =
        useState('');

    const [loading, setLoading] =
        useState(true);

    const [submitting, setSubmitting] =
        useState(false);

    const [error, setError] =
        useState('');

    const [order, setOrder] =
        useState<OrderResponse | null>(null);

    useEffect(() => {
        async function loadCart()
        {
            if (!hasCartIdentity())
            {
                setLoading(false);
                return;
            }

            try
            {
                const response = await getCart();
                setCart(response);
            }
            catch
            {
                setError(
                    'No pudimos cargar tu carrito'
                );
            }
            finally
            {
                setLoading(false);
            }
        }

        loadCart();
    }, []);

    function formatPrice(price: number)
    {
        return new Intl.NumberFormat(
            'es-CL',
            {
                style: 'currency',
                currency: 'CLP',
                maximumFractionDigits: 0,
            }
        ).format(price);
    }

    function formatLabel(value: string)
    {
        return value
            .toLowerCase()
            .replaceAll('_', ' ')
            .replace(
                /\b\w/g,
                (letter) =>
                    letter.toUpperCase()
            );
    }

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>
    )
    {
        event.preventDefault();

        if (
            !customerName.trim() ||
            !customerEmail.trim() ||
            !shippingAddress.trim()
        )
        {
            setError(
                'Completa todos los datos'
            );

            return;
        }

        setError('');
        setSubmitting(true);

        try
        {
            const response = await checkout({
                customerName:
                    customerName.trim(),

                customerEmail:
                    customerEmail.trim(),

                shippingAddress:
                    shippingAddress.trim(),
            });

            setOrder(response);
            setCart(null);

            onCheckoutCompleted();
        }
        catch (checkoutError)
        {
            if (checkoutError instanceof Error)
            {
                setError(
                    checkoutError.message
                );
            }
            else
            {
                setError(
                    'No se pudo completar la compra'
                );
            }
        }
        finally
        {
            setSubmitting(false);
        }
    }

    if (loading)
    {
        return (
            <p className="catalog-message">
                Cargando checkout...
            </p>
        );
    }

    if (order)
    {
        return (
            <section className="checkout-page">
                <div className="checkout-success">
                    <div className="checkout-success-icon">
                        ✓
                    </div>

                    <p className="catalog-eyebrow">
                        COMPRA COMPLETADA
                    </p>

                    <h1>
                        Pedido confirmado
                    </h1>

                    <p>
                        Tu pedido fue registrado
                        correctamente.
                    </p>

                    <div className="checkout-order-info">
                        <div>
                            <span>
                                Orden
                            </span>

                            <strong>
                                #{order.orderId}
                            </strong>
                        </div>

                        <div>
                            <span>
                                Estado
                            </span>

                            <strong>
                                {formatLabel(
                                    order.status
                                )}
                            </strong>
                        </div>

                        <div>
                            <span>
                                Cliente
                            </span>

                            <strong>
                                {order.customerName}
                            </strong>
                        </div>

                        <div>
                            <span>
                                Email
                            </span>

                            <strong>
                                {order.customerEmail}
                            </strong>
                        </div>

                        <div>
                            <span>
                                Dirección
                            </span>

                            <strong>
                                {order.shippingAddress}
                            </strong>
                        </div>

                        <div>
                            <span>
                                Total
                            </span>

                            <strong>
                                {formatPrice(
                                    order.total
                                )}
                            </strong>
                        </div>
                    </div>

                    <Link
                        to="/"
                        className="checkout-return-button"
                    >
                        Volver al catálogo
                    </Link>
                </div>
            </section>
        );
    }

    if (
        !cart ||
        cart.items.length === 0
    )
    {
        return (
            <section className="checkout-page">
                <div className="checkout-empty">
                    <h1>
                        Tu carrito está vacío
                    </h1>

                    <p>
                        Agrega cartas antes de
                        continuar con la compra.
                    </p>

                    <Link
                        to="/"
                        className="checkout-return-button"
                    >
                        Volver al catálogo
                    </Link>
                </div>
            </section>
        );
    }

    return (
        <section className="checkout-page">
            <div className="checkout-header">
                <p className="catalog-eyebrow">
                    FINALIZAR COMPRA
                </p>

                <h1>
                    Checkout
                </h1>

                <p>
                    Revisa tu pedido e ingresa
                    los datos de envío.
                </p>
            </div>

            <div className="checkout-layout">
                <form
                    className="checkout-form"
                    onSubmit={handleSubmit}
                >
                    <h2>
                        Datos de envío
                    </h2>

                    <label>
                        Nombre

                        <input
                            type="text"
                            value={customerName}
                            onChange={(event) =>
                                setCustomerName(
                                    event.target.value
                                )
                            }
                            placeholder="Nombre completo"
                            required
                        />
                    </label>

                    <label>
                        Email

                        <input
                            type="email"
                            value={customerEmail}
                            onChange={(event) =>
                                setCustomerEmail(
                                    event.target.value
                                )
                            }
                            placeholder="correo@ejemplo.cl"
                            required
                        />
                    </label>

                    <label>
                        Dirección de envío

                        <textarea
                            value={shippingAddress}
                            onChange={(event) =>
                                setShippingAddress(
                                    event.target.value
                                )
                            }
                            placeholder="Calle, número, comuna, ciudad"
                            required
                        />
                    </label>

                    {error && (
                        <p className="cart-feedback cart-feedback-error">
                            {error}
                        </p>
                    )}

                    <button
                        type="submit"
                        className="checkout-submit-button"
                        disabled={submitting}
                    >
                        {submitting
                            ? 'Procesando compra...'
                            : `Confirmar compra · ${formatPrice(cart.total)}`}
                    </button>
                </form>

                <aside className="checkout-summary">
                    <h2>
                        Tu pedido
                    </h2>

                    <div className="checkout-items">
                        {cart.items.map((item) => (
                            <article
                                className="checkout-item"
                                key={item.productId}
                            >
                                <img
                                    src={item.imageUrl}
                                    alt={item.cardName}
                                />

                                <div className="checkout-item-info">
                                    <strong>
                                        {item.cardName}
                                    </strong>

                                    <span>
                                        {formatLabel(
                                            item.language
                                        )}
                                        {' · '}
                                        {formatLabel(
                                            item.variant
                                        )}
                                        {' · '}
                                        {formatLabel(
                                            item.condition
                                        )}
                                    </span>

                                    <span>
                                        Cantidad:{' '}
                                        {item.quantity}
                                    </span>
                                </div>

                                <strong>
                                    {formatPrice(
                                        item.subtotal
                                    )}
                                </strong>
                            </article>
                        ))}
                    </div>

                    <div className="checkout-totals">
                        <div>
                            <span>
                                Neto
                            </span>

                            <strong>
                                {formatPrice(
                                    cart.netAmount
                                )}
                            </strong>
                        </div>

                        <div>
                            <span>
                                IVA
                            </span>

                            <strong>
                                {formatPrice(
                                    cart.taxAmount
                                )}
                            </strong>
                        </div>

                        <div className="checkout-total">
                            <span>
                                Total
                            </span>

                            <strong>
                                {formatPrice(
                                    cart.total
                                )}
                            </strong>
                        </div>
                    </div>
                </aside>
            </div>
        </section>
    );
}

export default CheckoutPage;