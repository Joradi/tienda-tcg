import {
    useEffect,
    useMemo,
    useState
} from 'react';

import {
    getAllProducts,
    updateProduct
} from '../api/productAdminApi';

import ProductCreateModal
    from '../components/ProductCreateModal';

import type {
    Product
} from '../types/Product';

function AdminPage()
{
    const [products, setProducts] =
        useState<Product[]>([]);

    const [search, setSearch] =
        useState('');

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState('');

    const [success, setSuccess] =
        useState('');

    const [editingProductId, setEditingProductId] =
        useState<number | null>(null);

    const [stockValue, setStockValue] =
        useState('');

    const [priceValue, setPriceValue] =
        useState('');

    const [saving, setSaving] =
        useState(false);

    const [createModalOpen, setCreateModalOpen] =
        useState(false);

    useEffect(() =>
    {
        async function loadProducts()
        {
            try
            {
                setError('');

                const response =
                    await getAllProducts();

                setProducts(response);
            }
            catch (loadError)
            {
                if (loadError instanceof Error)
                {
                    setError(
                        loadError.message
                    );
                }
                else
                {
                    setError(
                        'No se pudo cargar el inventario'
                    );
                }
            }
            finally
            {
                setLoading(false);
            }
        }

        loadProducts();
    }, []);

    const filteredProducts =
        useMemo(() =>
        {
            const normalizedSearch =
                search
                    .trim()
                    .toLowerCase();

            if (!normalizedSearch)
            {
                return products;
            }

            return products.filter((product) =>
            {
                const collectorNumber =
                    `${product.cardNumber}/${product.setPrintedTotal}`;

                return (
                    product.id
                        .toString()
                        .includes(normalizedSearch) ||
                    product.cardName
                        .toLowerCase()
                        .includes(normalizedSearch) ||
                    product.cardNumber
                        .toLowerCase()
                        .includes(normalizedSearch) ||
                    collectorNumber
                        .toLowerCase()
                        .includes(normalizedSearch) ||
                    product.setName
                        .toLowerCase()
                        .includes(normalizedSearch) ||
                    product.language
                        .toLowerCase()
                        .includes(normalizedSearch) ||
                    product.variant
                        .toLowerCase()
                        .includes(normalizedSearch) ||
                    product.condition
                        .toLowerCase()
                        .includes(normalizedSearch)
                );
            });
        }, [
            products,
            search
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

    function formatCollectorNumber(
        product: Product
    )
    {
        const rawNumber =
            product.cardNumber;

        const formattedNumber =
            /^\d+$/.test(rawNumber)
                ? rawNumber.padStart(
                    product.setPrintedTotal
                        .toString()
                        .length,
                    '0'
                )
                : rawNumber;

        return `${formattedNumber}/${product.setPrintedTotal}`;
    }

    function startEditing(
        product: Product
    )
    {
        setEditingProductId(
            product.id
        );

        setStockValue(
            product.stock.toString()
        );

        setPriceValue(
            product.price.toString()
        );

        setError('');
        setSuccess('');
    }

    function cancelEditing()
    {
        setEditingProductId(null);
        setStockValue('');
        setPriceValue('');
        setError('');
    }

    async function handleSave(
        product: Product
    )
    {
        if (saving)
        {
            return;
        }

        const stock =
            Number(stockValue);

        const price =
            Number(priceValue);

        if (
            !Number.isInteger(stock) ||
            stock < 0
        )
        {
            setError(
                'El stock debe ser un número entero mayor o igual a 0'
            );

            return;
        }

        if (
            !Number.isInteger(price) ||
            price <= 0
        )
        {
            setError(
                'El precio debe ser un número entero mayor a 0'
            );

            return;
        }

        setSaving(true);
        setError('');
        setSuccess('');

        try
        {
            const updatedProduct =
                await updateProduct(
                    product.id,
                    {
                        stock,
                        price,
                    }
                );

            setProducts(
                (currentProducts) =>
                    currentProducts.map(
                        (currentProduct) =>
                            currentProduct.id ===
                            updatedProduct.id
                                ? updatedProduct
                                : currentProduct
                    )
            );

            setEditingProductId(null);
            setStockValue('');
            setPriceValue('');

            setSuccess(
                `${updatedProduct.cardName} actualizado correctamente`
            );
        }
        catch (saveError)
        {
            if (saveError instanceof Error)
            {
                setError(
                    saveError.message
                );
            }
            else
            {
                setError(
                    'No se pudo actualizar el producto'
                );
            }
        }
        finally
        {
            setSaving(false);
        }
    }

    function openCreateModal()
    {
        setError('');
        setSuccess('');
        setCreateModalOpen(true);
    }

    function handleProductCreated(
        product: Product
    )
    {
        setProducts(
            (currentProducts) => [
                product,
                ...currentProducts
            ]
        );

        setCreateModalOpen(false);
        setError('');

        setSuccess(
            `${product.cardName} agregado al catálogo correctamente`
        );
    }

    if (loading)
    {
        return (
            <main className="admin-page">
                <p className="catalog-message">
                    Cargando inventario...
                </p>
            </main>
        );
    }

    return (
        <main className="admin-page">

            <header className="admin-header">

                <div>
                    <p className="catalog-eyebrow">
                        BACKOFFICE
                    </p>

                    <h1>
                        Inventario
                    </h1>

                    <p>
                        Administra stock y precios
                        de los productos.
                    </p>
                </div>

                <div className="admin-summary">

                    <button
                        type="button"
                        className="admin-save-button"
                        disabled={
                            editingProductId !== null ||
                            saving
                        }
                        onClick={openCreateModal}
                    >
                        + Agregar carta
                    </button>

                    <div>
                        <span>
                            Productos
                        </span>

                        <strong>
                            {products.length}
                        </strong>
                    </div>

                    <div>
                        <span>
                            Sin stock
                        </span>

                        <strong>
                            {
                                products.filter(
                                    (product) =>
                                        product.stock === 0
                                ).length
                            }
                        </strong>
                    </div>

                </div>

            </header>

            <section className="admin-toolbar">

                <label className="admin-search">
                    Buscar producto

                    <input
                        type="search"
                        value={search}
                        onChange={(event) =>
                            setSearch(
                                event.target.value
                            )
                        }
                        placeholder="Carta, set, número, idioma..."
                    />
                </label>

                <span>
                    {filteredProducts.length}
                    {' '}
                    {filteredProducts.length === 1
                        ? 'resultado'
                        : 'resultados'}
                </span>

            </section>

            {error && (
                <p className="cart-feedback cart-feedback-error">
                    {error}
                </p>
            )}

            {success && (
                <p className="admin-success">
                    {success}
                </p>
            )}

            {filteredProducts.length === 0 ? (

                <div className="admin-empty">

                    <h2>
                        No encontramos productos
                    </h2>

                    <p>
                        Prueba con otra búsqueda.
                    </p>

                </div>

            ) : (

                <div className="admin-table-container">

                    <table className="admin-table">

                        <thead>
                        <tr>
                            <th>
                                Producto
                            </th>

                            <th>
                                Detalle
                            </th>

                            <th>
                                Stock
                            </th>

                            <th>
                                Precio
                            </th>

                            <th>
                                Acción
                            </th>
                        </tr>
                        </thead>

                        <tbody>

                        {filteredProducts.map(
                            (product) =>
                            {
                                const editing =
                                    editingProductId ===
                                    product.id;

                                return (
                                    <tr key={product.id}>

                                        <td>
                                            <div className="admin-product">

                                                <img
                                                    src={
                                                        product.imageUrl
                                                    }
                                                    alt={
                                                        product.cardName
                                                    }
                                                />

                                                <div>

                                                    <strong>
                                                        {
                                                            product.cardName
                                                        }
                                                    </strong>

                                                    <span>
                                                        {
                                                            product.setName
                                                        }
                                                        {' · '}
                                                        {
                                                            formatCollectorNumber(
                                                                product
                                                            )
                                                        }
                                                    </span>

                                                    <span>
                                                        Producto #
                                                        {
                                                            product.id
                                                        }
                                                    </span>

                                                </div>

                                            </div>
                                        </td>

                                        <td>
                                            <div className="admin-product-tags">

                                                <span>
                                                    {
                                                        formatLabel(
                                                            product.language
                                                        )
                                                    }
                                                </span>

                                                <span>
                                                    {
                                                        formatLabel(
                                                            product.variant
                                                        )
                                                    }
                                                </span>

                                                <span>
                                                    {
                                                        formatLabel(
                                                            product.condition
                                                        )
                                                    }
                                                </span>

                                            </div>
                                        </td>

                                        <td>

                                            {editing ? (

                                                <input
                                                    className="admin-number-input"
                                                    type="number"
                                                    min="0"
                                                    step="1"
                                                    value={
                                                        stockValue
                                                    }
                                                    onChange={(
                                                        event
                                                    ) =>
                                                        setStockValue(
                                                            event.target.value
                                                        )
                                                    }
                                                />

                                            ) : (

                                                <span
                                                    className={
                                                        product.stock === 0
                                                            ? 'admin-stock admin-stock-empty'
                                                            : 'admin-stock admin-stock-available'
                                                    }
                                                >
                                                    {
                                                        product.stock
                                                    }
                                                </span>

                                            )}

                                        </td>

                                        <td>

                                            {editing ? (

                                                <input
                                                    className="admin-number-input admin-price-input"
                                                    type="number"
                                                    min="1"
                                                    step="1"
                                                    value={
                                                        priceValue
                                                    }
                                                    onChange={(
                                                        event
                                                    ) =>
                                                        setPriceValue(
                                                            event.target.value
                                                        )
                                                    }
                                                />

                                            ) : (

                                                <strong className="admin-price">
                                                    {
                                                        formatPrice(
                                                            product.price
                                                        )
                                                    }
                                                </strong>

                                            )}

                                        </td>

                                        <td>

                                            {editing ? (

                                                <div className="admin-actions">

                                                    <button
                                                        type="button"
                                                        className="admin-save-button"
                                                        disabled={
                                                            saving
                                                        }
                                                        onClick={() =>
                                                            void handleSave(
                                                                product
                                                            )
                                                        }
                                                    >
                                                        {saving
                                                            ? 'Guardando...'
                                                            : 'Guardar'}
                                                    </button>

                                                    <button
                                                        type="button"
                                                        className="admin-cancel-button"
                                                        disabled={
                                                            saving
                                                        }
                                                        onClick={
                                                            cancelEditing
                                                        }
                                                    >
                                                        Cancelar
                                                    </button>

                                                </div>

                                            ) : (

                                                <button
                                                    type="button"
                                                    className="admin-edit-button"
                                                    disabled={
                                                        editingProductId !==
                                                        null
                                                    }
                                                    onClick={() =>
                                                        startEditing(
                                                            product
                                                        )
                                                    }
                                                >
                                                    Editar
                                                </button>

                                            )}

                                        </td>

                                    </tr>
                                );
                            }
                        )}

                        </tbody>

                    </table>

                </div>

            )}

            <ProductCreateModal
                open={createModalOpen}
                onClose={() =>
                    setCreateModalOpen(false)
                }
                onCreated={
                    handleProductCreated
                }
            />

        </main>
    );
}

export default AdminPage;