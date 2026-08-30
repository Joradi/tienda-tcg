import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import {
    clearCart,
    getCart,
    hasCartIdentity,
    removeCartItem,
    updateCartItem
} from '../api/cartApi';

import type { CartResponse } from '../types/Cart';

interface CartDrawerProps {
    open: boolean;
    cart: CartResponse | null;
    onClose: () => void;
    onCartChange: (cart: CartResponse) => void;
}

function CartDrawer({
                        open,
                        cart,
                        onClose,
                        onCartChange
                    }: CartDrawerProps) {
    const navigate = useNavigate();

    const [loading, setLoading] =
        useState(false);

    const [updatingProductId, setUpdatingProductId] =
        useState<number | null>(null);

    const [clearing, setClearing] =
        useState(false);

    const [error, setError] =
        useState('');

    useEffect(() => {
        if (!open)
        {
            return;
        }

        function handleKeyDown(event: KeyboardEvent)
        {
            if (event.key === 'Escape')
            {
                onClose();
            }
        }

        document.addEventListener(
            'keydown',
            handleKeyDown
        );

        document.body.style.overflow =
            'hidden';

        return () => {
            document.removeEventListener(
                'keydown',
                handleKeyDown
            );

            document.body.style.overflow =
                '';
        };
    }, [open, onClose]);

    useEffect(() => {
        if (
            !open ||
            cart !== null ||
            !hasCartIdentity()
        )
        {
            return;
        }

        async function loadCart()
        {
            setLoading(true);
            setError('');

            try
            {
                const response =
                    await getCart();

                onCartChange(response);
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
    }, [
        open,
        cart,
        onCartChange
    ]);

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

    async function handleQuantityChange(
        productId: number,
        quantity: number
    )
    {
        if (
            quantity <= 0 ||
            updatingProductId !== null
        )
        {
            return;
        }

        setError('');
        setUpdatingProductId(productId);

        try
        {
            const response =
                await updateCartItem(
                    productId,
                    quantity
                );

            onCartChange(response);
        }
        catch
        {
            setError(
                'No pudimos actualizar el carrito'
            );
        }
        finally
        {
            setUpdatingProductId(null);
        }
    }

    async function handleRemove(
        productId: number
    )
    {
        if (updatingProductId !== null)
        {
            return;
        }

        setError('');
        setUpdatingProductId(productId);

        try
        {
            const response =
                await removeCartItem(
                    productId
                );

            onCartChange(response);
        }
        catch
        {
            setError(
                'No pudimos eliminar la carta'
            );
        }
        finally
        {
            setUpdatingProductId(null);
        }
    }

    async function handleClear()
    {
        if (
            clearing ||
            !cart ||
            cart.items.length === 0
        )
        {
            return;
        }

        setError('');
        setClearing(true);

        try
        {
            const response =
                await clearCart();

            onCartChange(response);
        }
        catch
        {
            setError(
                'No pudimos vaciar el carrito'
            );
        }
        finally
        {
            setClearing(false);
        }
    }

    function handleCheckout()
    {
        if (
            !cart ||
            cart.items.length === 0
        )
        {
            return;
        }

        onClose();
        navigate('/checkout');
    }

    if (!open)
    {
        return null;
    }

    return (
        <div
            className="cart-drawer-overlay"
            onClick={onClose}
        >
            <aside
                className="cart-drawer"
                onClick={(event) =>
                    event.stopPropagation()
                }
            >
                <header className="cart-drawer-header">
                    <div>
                        <p>
                            TU COMPRA
                        </p>

                        <h2>
                            Carrito
                        </h2>
                    </div>

                    <button
                        type="button"
                        className="cart-close-button"
                        onClick={onClose}
                        aria-label="Cerrar carrito"
                    >
                        ×
                    </button>
                </header>

                {loading ? (
                    <p className="cart-drawer-message">
                        Cargando carrito...
                    </p>
                ) : !cart ||
                cart.items.length === 0 ? (
                    <div className="cart-empty">
                        <span>
                            🛒
                        </span>

                        <h3>
                            Tu carrito está vacío
                        </h3>

                        <p>
                            Agrega cartas desde el catálogo
                            para comenzar.
                        </p>
                    </div>
                ) : (
                    <>
                        <div className="cart-items">
                            {cart.items.map((item) => {
                                const updating =
                                    updatingProductId ===
                                    item.productId;

                                return (
                                    <article
                                        className="cart-item"
                                        key={item.productId}
                                    >
                                        <img
                                            src={item.imageUrl}
                                            alt={item.cardName}
                                        />

                                        <div className="cart-item-info">
                                            <h3>
                                                {item.cardName}
                                            </h3>

                                            <div className="cart-item-tags">
                                                <span>
                                                    {formatLabel(
                                                        item.language
                                                    )}
                                                </span>

                                                <span>
                                                    {formatLabel(
                                                        item.variant
                                                    )}
                                                </span>

                                                <span>
                                                    {formatLabel(
                                                        item.condition
                                                    )}
                                                </span>
                                            </div>

                                            <strong>
                                                {formatPrice(
                                                    item.unitPrice
                                                )}
                                            </strong>

                                            <div className="cart-item-actions">
                                                <div className="quantity-control">
                                                    <button
                                                        type="button"
                                                        disabled={
                                                            updating ||
                                                            item.quantity <= 1
                                                        }
                                                        onClick={() =>
                                                            void handleQuantityChange(
                                                                item.productId,
                                                                item.quantity - 1
                                                            )
                                                        }
                                                    >
                                                        −
                                                    </button>

                                                    <span>
                                                        {item.quantity}
                                                    </span>

                                                    <button
                                                        type="button"
                                                        disabled={updating}
                                                        onClick={() =>
                                                            void handleQuantityChange(
                                                                item.productId,
                                                                item.quantity + 1
                                                            )
                                                        }
                                                    >
                                                        +
                                                    </button>
                                                </div>

                                                <button
                                                    type="button"
                                                    className="remove-cart-item"
                                                    disabled={updating}
                                                    onClick={() =>
                                                        void handleRemove(
                                                            item.productId
                                                        )
                                                    }
                                                >
                                                    Eliminar
                                                </button>
                                            </div>

                                            <span className="cart-item-subtotal">
                                                Subtotal:{' '}
                                                {formatPrice(
                                                    item.subtotal
                                                )}
                                            </span>
                                        </div>
                                    </article>
                                );
                            })}
                        </div>

                        {error && (
                            <p className="cart-drawer-error">
                                {error}
                            </p>
                        )}

                        <footer className="cart-summary">
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

                            <div className="cart-total">
                                <span>
                                    Total
                                </span>

                                <strong>
                                    {formatPrice(
                                        cart.total
                                    )}
                                </strong>
                            </div>

                            <button
                                type="button"
                                className="checkout-button"
                                onClick={handleCheckout}
                            >
                                Ir a pagar
                            </button>

                            <button
                                type="button"
                                className="clear-cart-button"
                                disabled={clearing}
                                onClick={() =>
                                    void handleClear()
                                }
                            >
                                {clearing
                                    ? 'Vaciando...'
                                    : 'Vaciar carrito'}
                            </button>
                        </footer>
                    </>
                )}

                {error &&
                    (!cart ||
                        cart.items.length === 0) && (
                        <p className="cart-drawer-error">
                            {error}
                        </p>
                    )}
            </aside>
        </div>
    );
}

export default CartDrawer;