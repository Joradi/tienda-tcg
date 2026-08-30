import {
    useEffect,
    useState
} from 'react';

import { getOrders } from '../api/orderApi';
import type { OrderResponse } from '../types/Checkout';

function OrdersPage()
{
    const [orders, setOrders] =
        useState<OrderResponse[]>([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState('');

    useEffect(() => {
        async function loadOrders()
        {
            try
            {
                setError('');

                const response =
                    await getOrders();

                const sortedOrders =
                    [...response].sort(
                        (a, b) =>
                            new Date(b.createdAt).getTime() -
                            new Date(a.createdAt).getTime()
                    );

                setOrders(sortedOrders);
            }
            catch (loadError)
            {
                if (loadError instanceof Error)
                {
                    setError(loadError.message);
                }
                else
                {
                    setError(
                        'No se pudieron cargar las órdenes'
                    );
                }
            }
            finally
            {
                setLoading(false);
            }
        }

        loadOrders();
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

    function formatDate(date: string)
    {
        return new Intl.DateTimeFormat(
            'es-CL',
            {
                dateStyle: 'medium',
                timeStyle: 'short',
            }
        ).format(new Date(date));
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

    if (loading)
    {
        return (
            <main className="orders-page">
                <p className="catalog-message">
                    Cargando órdenes...
                </p>
            </main>
        );
    }

    if (error)
    {
        return (
            <main className="orders-page">
                <p className="catalog-message">
                    {error}
                </p>
            </main>
        );
    }

    if (orders.length === 0)
    {
        return (
            <main className="orders-page">
                <header className="orders-header">
                    <p className="catalog-eyebrow">
                        MIS COMPRAS
                    </p>

                    <h1>
                        Órdenes
                    </h1>
                </header>

                <div className="orders-empty">
                    <h2>
                        Todavía no tienes órdenes
                    </h2>

                    <p>
                        Las compras que realices
                        aparecerán aquí.
                    </p>
                </div>
            </main>
        );
    }

    return (
        <main className="orders-page">
            <header className="orders-header">
                <p className="catalog-eyebrow">
                    MIS COMPRAS
                </p>

                <h1>
                    Órdenes
                </h1>

                <p>
                    Historial de tus compras realizadas.
                </p>
            </header>

            <div className="orders-list">
                {orders.map((order) => (
                    <article
                        className="order-card"
                        key={order.orderId}
                    >
                        <header className="order-card-header">
                            <div>
                                <span className="order-label">
                                    Orden
                                </span>

                                <h2>
                                    #{order.orderId}
                                </h2>
                            </div>

                            <div className="order-card-meta">
                                <span className="order-date">
                                    {formatDate(
                                        order.createdAt
                                    )}
                                </span>

                                <span className="order-status">
                                    {formatLabel(
                                        order.status
                                    )}
                                </span>
                            </div>
                        </header>

                        <div className="order-items">
                            {order.items.map((item) => (
                                <div
                                    className="order-item"
                                    key={item.productId}
                                >
                                    <img
                                        src={item.imageUrl}
                                        alt={item.cardName}
                                    />

                                    <div className="order-item-info">
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

                                    <strong className="order-item-price">
                                        {formatPrice(
                                            item.subtotal
                                        )}
                                    </strong>
                                </div>
                            ))}
                        </div>

                        <footer className="order-card-footer">
                            <div className="order-shipping">
                                <span>
                                    Envío
                                </span>

                                <strong>
                                    {order.shippingAddress}
                                </strong>
                            </div>

                            <div className="order-totals">
                                <div>
                                    <span>
                                        Neto
                                    </span>

                                    <strong>
                                        {formatPrice(
                                            order.netAmount
                                        )}
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        IVA
                                    </span>

                                    <strong>
                                        {formatPrice(
                                            order.taxAmount
                                        )}
                                    </strong>
                                </div>

                                <div className="order-total">
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
                        </footer>
                    </article>
                ))}
            </div>
        </main>
    );
}

export default OrdersPage;