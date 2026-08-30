import { useEffect } from 'react';
import type { Product } from '../types/Product';

interface ProductDetailModalProps {
    product: Product | null;
    adding: boolean;
    added: boolean;
    onClose: () => void;
    onAddToCart: (product: Product) => Promise<boolean>;
}

function ProductDetailModal({
                                product,
                                adding,
                                added,
                                onClose,
                                onAddToCart
                            }: ProductDetailModalProps) {
    useEffect(() => {
        if (!product)
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

        document.addEventListener('keydown', handleKeyDown);
        document.body.style.overflow = 'hidden';

        return () => {
            document.removeEventListener('keydown', handleKeyDown);
            document.body.style.overflow = '';
        };
    }, [product, onClose]);

    if (!product)
    {
        return null;
    }

    const currentProduct = product;

    function formatPrice(price: number)
    {
        return new Intl.NumberFormat('es-CL', {
            style: 'currency',
            currency: 'CLP',
            maximumFractionDigits: 0,
        }).format(price);
    }

    function formatCardNumber(
        number: string,
        printedTotal: number
    )
    {
        const total = String(printedTotal);

        const digits = Math.max(
            3,
            total.length
        );

        return `${number.padStart(digits, '0')}/${total.padStart(digits, '0')}`;
    }

    function formatValue(value: string)
    {
        return value
            .toLowerCase()
            .replaceAll('_', ' ')
            .replace(/\b\w/g, (letter) =>
                letter.toUpperCase()
            );
    }

    async function handleAdd()
    {
        const success =
            await onAddToCart(currentProduct);

        if (success)
        {
            onClose();
        }
    }

    return (
        <div
            className="product-detail-overlay"
            onClick={onClose}
        >
            <section
                className="product-detail-modal"
                onClick={(event) =>
                    event.stopPropagation()
                }
            >
                <button
                    type="button"
                    className="product-detail-close"
                    onClick={onClose}
                    aria-label="Cerrar detalle"
                >
                    ×
                </button>

                <div className="product-detail-image">
                    <img
                        src={currentProduct.imageUrl}
                        alt={currentProduct.cardName}
                    />
                </div>

                <div className="product-detail-info">
                    <p className="product-detail-eyebrow">
                        {currentProduct.setName}
                    </p>

                    <h2>
                        {currentProduct.cardName}
                    </h2>

                    <div className="product-detail-tags">
                        <span>
                            {formatValue(
                                currentProduct.language
                            )}
                        </span>

                        <span>
                            {formatValue(
                                currentProduct.variant
                            )}
                        </span>

                        <span>
                            {formatValue(
                                currentProduct.condition
                            )}
                        </span>
                    </div>

                    <div className="product-detail-data">
                        <div>
                            <span>Número</span>

                            <strong>
                                {formatCardNumber(
                                    currentProduct.cardNumber,
                                    currentProduct.setPrintedTotal
                                )}
                            </strong>
                        </div>

                        {currentProduct.rarity && (
                            <div>
                                <span>Rareza</span>

                                <strong>
                                    {currentProduct.rarity}
                                </strong>
                            </div>
                        )}

                        {currentProduct.illustrator && (
                            <div>
                                <span>Ilustrador</span>

                                <strong>
                                    {currentProduct.illustrator}
                                </strong>
                            </div>
                        )}

                        {currentProduct.superType && (
                            <div>
                                <span>Tipo</span>

                                <strong>
                                    {currentProduct.superType}
                                </strong>
                            </div>
                        )}

                        {currentProduct.subTypes.length > 0 && (
                            <div>
                                <span>Subtipo</span>

                                <strong>
                                    {currentProduct.subTypes.join(', ')}
                                </strong>
                            </div>
                        )}
                    </div>

                    <div className="product-detail-price">
                        <span>Precio</span>

                        <strong>
                            {formatPrice(
                                currentProduct.price
                            )}
                        </strong>
                    </div>

                    <button
                        type="button"
                        className="product-detail-cart-button"
                        disabled={adding}
                        onClick={handleAdd}
                    >
                        {adding
                            ? 'Agregando...'
                            : added
                                ? '✓ Agregada'
                                : '🛒 Agregar al carrito'}
                    </button>
                </div>
            </section>
        </div>
    );
}

export default ProductDetailModal;