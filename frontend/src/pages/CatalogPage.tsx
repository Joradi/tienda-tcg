import { useEffect, useMemo, useState } from 'react';
import { getProducts } from '../api/productApi';
import { addToCart } from '../api/cartApi';
import ProductDetailModal from '../components/ProductDetailModal';
import type { CartResponse } from '../types/Cart';
import type { Product } from '../types/Product';

interface CatalogPageProps {
    onCartUpdated: (cart: CartResponse) => void;
}

function CatalogPage({onCartUpdated}: CatalogPageProps) {
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [search, setSearch] = useState('');
    const [language, setLanguage] = useState('');
    const [variant, setVariant] = useState('');
    const [condition, setCondition] = useState('');

    const [addingProductId, setAddingProductId] =
        useState<number | null>(null);

    const [addedProductId, setAddedProductId] =
        useState<number | null>(null);

    const [selectedProduct, setSelectedProduct] =
        useState<Product | null>(null);

    const [cartError, setCartError] = useState('');

    useEffect(() => {
        async function loadProducts()
        {
            try
            {
                const data = await getProducts();
                setProducts(data);
            }
            catch
            {
                setError('No pudimos cargar el catálogo');
            }
            finally
            {
                setLoading(false);
            }
        }

        loadProducts();
    }, []);

    const languages = useMemo(
        () => [
            ...new Set(
                products.map((product) => product.language)
            )
        ],
        [products]
    );

    const variants = useMemo(
        () => [
            ...new Set(
                products.map((product) => product.variant)
            )
        ],
        [products]
    );

    const conditions = useMemo(
        () => [
            ...new Set(
                products.map((product) => product.condition)
            )
        ],
        [products]
    );

    const filteredProducts = useMemo(() => {
        const normalizedSearch =
            search.trim().toLowerCase();

        return products.filter((product) => {
            const matchesSearch =
                normalizedSearch === '' ||
                product.cardName
                    .toLowerCase()
                    .includes(normalizedSearch) ||
                product.cardNumber
                    .toLowerCase()
                    .includes(normalizedSearch) ||
                product.setName
                    .toLowerCase()
                    .includes(normalizedSearch) ||
                product.illustrator
                    ?.toLowerCase()
                    .includes(normalizedSearch) ||
                product.rarity
                    ?.toLowerCase()
                    .includes(normalizedSearch) ||
                product.superType
                    ?.toLowerCase()
                    .includes(normalizedSearch) ||
                product.subTypes.some((subType) =>
                    subType
                        .toLowerCase()
                        .includes(normalizedSearch)
                );

            const matchesLanguage =
                language === '' ||
                product.language === language;

            const matchesVariant =
                variant === '' ||
                product.variant === variant;

            const matchesCondition =
                condition === '' ||
                product.condition === condition;

            return (
                matchesSearch &&
                matchesLanguage &&
                matchesVariant &&
                matchesCondition
            );
        });
    }, [
        products,
        search,
        language,
        variant,
        condition
    ]);

    function formatPrice(price: number)
    {
        return new Intl.NumberFormat('es-CL', {
            style: 'currency',
            currency: 'CLP',
            maximumFractionDigits: 0,
        }).format(price);
    }

    function formatCardNumber(
        cardNumber: string,
        printedTotal: number
    )
    {
        const total = String(printedTotal);

        const digits = Math.max(
            3,
            total.length
        );

        return (
            `${cardNumber.padStart(digits, '0')}/` +
            `${total.padStart(digits, '0')}`
        );
    }

    function formatLabel(value: string)
    {
        return value
            .toLowerCase()
            .replaceAll('_', ' ')
            .replace(/\b\w/g, (letter) =>
                letter.toUpperCase()
            );
    }

    function clearFilters()
    {
        setSearch('');
        setLanguage('');
        setVariant('');
        setCondition('');
    }

    async function handleAddToCart(
        product: Product
    ): Promise<boolean>
    {
        if (addingProductId !== null)
        {
            return false;
        }

        setCartError('');
        setAddedProductId(null);
        setAddingProductId(product.id);

        try
        {
            const cart = await addToCart(
                product.id,
                1
            );

            onCartUpdated(cart);
            setAddedProductId(product.id);

            window.setTimeout(() => {
                setAddedProductId(null);
            }, 1800);

            return true;
        }
        catch
        {
            setCartError(
                `No pudimos agregar ${product.cardName} al carrito`
            );

            return false;
        }
        finally
        {
            setAddingProductId(null);
        }
    }

    if (loading)
    {
        return (
            <p className="catalog-message">
                Cargando catálogo...
            </p>
        );
    }

    if (error)
    {
        return (
            <p className="catalog-message catalog-error">
                {error}
            </p>
        );
    }

    return (
        <>
            <section className="catalog-page">
                <section className="catalog-hero">
                    <div className="hero-content">
                        <p className="catalog-eyebrow">
                            GOSUTO AKU · POKÉMON TCG
                        </p>

                        <h1>
                            Encuentra tu próxima
                            <span> carta.</span>
                        </h1>

                        <p className="hero-description">
                            Singles Pokémon para coleccionistas y jugadores.
                            Busca entre nuestras cartas disponibles y encuentra
                            la versión que necesitas.
                        </p>

                        <div className="hero-search">
                            <input
                                type="search"
                                placeholder="Busca por nombre, set, número o artista..."
                                value={search}
                                onChange={(event) =>
                                    setSearch(
                                        event.target.value
                                    )
                                }
                            />
                        </div>
                    </div>

                    <div className="hero-ghost">
                        <div className="ghost-glow"></div>

                        <span>悪霊</span>

                        <strong>
                            GOSUTO AKU
                        </strong>
                    </div>
                </section>

                <section className="catalog-controls">
                    <div className="filter-heading">
                        <h2>
                            Catálogo
                        </h2>

                        <button
                            type="button"
                            className="clear-filters"
                            onClick={clearFilters}
                        >
                            Limpiar filtros
                        </button>
                    </div>

                    <div className="filters">
                        <label>
                            Idioma

                            <select
                                value={language}
                                onChange={(event) =>
                                    setLanguage(
                                        event.target.value
                                    )
                                }
                            >
                                <option value="">
                                    Todos
                                </option>

                                {languages.map((value) => (
                                    <option
                                        key={value}
                                        value={value}
                                    >
                                        {formatLabel(value)}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label>
                            Variante

                            <select
                                value={variant}
                                onChange={(event) =>
                                    setVariant(
                                        event.target.value
                                    )
                                }
                            >
                                <option value="">
                                    Todas
                                </option>

                                {variants.map((value) => (
                                    <option
                                        key={value}
                                        value={value}
                                    >
                                        {formatLabel(value)}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label>
                            Condición

                            <select
                                value={condition}
                                onChange={(event) =>
                                    setCondition(
                                        event.target.value
                                    )
                                }
                            >
                                <option value="">
                                    Todas
                                </option>

                                {conditions.map((value) => (
                                    <option
                                        key={value}
                                        value={value}
                                    >
                                        {formatLabel(value)}
                                    </option>
                                ))}
                            </select>
                        </label>
                    </div>
                </section>

                <div className="catalog-toolbar">
                    <h2>
                        Cartas disponibles
                    </h2>

                    <span>
                        {filteredProducts.length}
                        {' '}
                        {filteredProducts.length === 1
                            ? 'carta'
                            : 'cartas'}
                    </span>
                </div>

                {cartError && (
                    <p className="cart-feedback cart-feedback-error">
                        {cartError}
                    </p>
                )}

                {filteredProducts.length === 0 ? (
                    <div className="empty-catalog">
                        <h3>
                            No encontramos cartas
                        </h3>

                        <p>
                            Prueba con otro nombre, número,
                            set, artista o cambia los filtros.
                        </p>

                        <button
                            type="button"
                            onClick={clearFilters}
                        >
                            Limpiar búsqueda
                        </button>
                    </div>
                ) : (
                    <div className="product-grid">
                        {filteredProducts.map((product) => {
                            const adding =
                                addingProductId === product.id;

                            const added =
                                addedProductId === product.id;

                            return (
                                <article
                                    className="product-card"
                                    key={product.id}
                                    onClick={() =>
                                        setSelectedProduct(
                                            product
                                        )
                                    }
                                >
                                    <div className="product-image-container">
                                        <img
                                            src={product.imageUrl}
                                            alt={product.cardName}
                                            className="product-image"
                                        />
                                    </div>

                                    <div className="product-info">
                                        <h3>
                                            {product.cardName}
                                        </h3>

                                        <div className="product-tags">
                                            <span>
                                                {formatLabel(
                                                    product.language
                                                )}
                                            </span>

                                            <span>
                                                {formatLabel(
                                                    product.variant
                                                )}
                                            </span>

                                            <span>
                                                {formatLabel(
                                                    product.condition
                                                )}
                                            </span>
                                        </div>

                                        <div className="product-footer">
                                            <strong>
                                                {formatPrice(
                                                    product.price
                                                )}
                                            </strong>

                                            <span>
                                                {formatCardNumber(
                                                    product.cardNumber,
                                                    product.setPrintedTotal
                                                )}
                                            </span>
                                        </div>

                                        <button
                                            type="button"
                                            className="add-cart-button"
                                            disabled={
                                                addingProductId !== null
                                            }
                                            onClick={(event) => {
                                                event.stopPropagation();

                                                void handleAddToCart(
                                                    product
                                                );
                                            }}
                                        >
                                            {adding
                                                ? 'Agregando...'
                                                : added
                                                    ? '✓ Agregada'
                                                    : '🛒 Agregar al carrito'}
                                        </button>
                                    </div>
                                </article>
                            );
                        })}
                    </div>
                )}
            </section>

            <ProductDetailModal
                product={selectedProduct}
                adding={
                    selectedProduct !== null &&
                    addingProductId === selectedProduct.id
                }
                added={
                    selectedProduct !== null &&
                    addedProductId === selectedProduct.id
                }
                onClose={() =>
                    setSelectedProduct(null)
                }
                onAddToCart={handleAddToCart}
            />
        </>
    );
}

export default CatalogPage;