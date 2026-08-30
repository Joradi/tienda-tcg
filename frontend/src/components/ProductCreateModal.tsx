import {
    useEffect,
    useState
} from 'react';

import type {
    FormEvent
} from 'react';

import CardPickerModal
    from './CardPickerModal';

import {
    getCollectorNumber
} from '../api/cardApi';

import type {
    CardSearchResult
} from '../api/cardApi';

import {
    createProduct
} from '../api/productAdminApi';

import {
    analyzePrice
} from '../api/priceAnalysisApi';

import type {
    SaleScenarioAnalysis
} from '../types/Importation';

import type {
    Product
} from '../types/Product';

interface ProductCreateModalProps {
    open: boolean;
    onClose: () => void;
    onCreated: (product: Product) => void;
}

function ProductCreateModal({
                                open,
                                onClose,
                                onCreated
                            }: ProductCreateModalProps)
{
    const [cardPickerOpen, setCardPickerOpen] =
        useState(false);

    const [selectedCard, setSelectedCard] =
        useState<CardSearchResult | null>(null);

    const [language, setLanguage] =
        useState('ENGLISH');

    const [variant, setVariant] =
        useState('NORMAL');

    const [condition, setCondition] =
        useState('NEAR_MINT');

    const [stock, setStock] =
        useState('1');

    const [landedCostUnitClp, setLandedCostUnitClp] =
        useState('');

    const [
        localReferencePriceClp,
        setLocalReferencePriceClp
    ] = useState('');

    const [price, setPrice] =
        useState('');

    const [scenarios, setScenarios] =
        useState<SaleScenarioAnalysis[]>([]);

    const [analyzing, setAnalyzing] =
        useState(false);

    const [creating, setCreating] =
        useState(false);

    const [error, setError] =
        useState('');

    useEffect(() =>
    {
        if (open)
        {
            return;
        }

        setCardPickerOpen(false);
        setSelectedCard(null);
        setLanguage('ENGLISH');
        setVariant('NORMAL');
        setCondition('NEAR_MINT');
        setStock('1');
        setLandedCostUnitClp('');
        setLocalReferencePriceClp('');
        setPrice('');
        setScenarios([]);
        setAnalyzing(false);
        setCreating(false);
        setError('');
    }, [open]);

    useEffect(() =>
    {
        if (!open)
        {
            return;
        }

        function handleKeyDown(
            event: KeyboardEvent
        )
        {
            if (
                event.key === 'Escape' &&
                !cardPickerOpen &&
                !creating
            )
            {
                onClose();
            }
        }

        window.addEventListener(
            'keydown',
            handleKeyDown
        );

        return () =>
        {
            window.removeEventListener(
                'keydown',
                handleKeyDown
            );
        };
    }, [
        open,
        cardPickerOpen,
        creating,
        onClose
    ]);

    function formatPrice(
        value: number
    )
    {
        return new Intl.NumberFormat(
            'es-CL',
            {
                style: 'currency',
                currency: 'CLP',
                maximumFractionDigits: 0,
            }
        ).format(value);
    }

    function formatPercentage(
        value: number
    )
    {
        return new Intl.NumberFormat(
            'es-CL',
            {
                style: 'percent',
                minimumFractionDigits: 1,
                maximumFractionDigits: 1,
            }
        ).format(value);
    }

    function formatStrategy(
        strategy: string
    )
    {
        switch (strategy)
        {
            case 'QUICK':
                return 'Venta rápida';

            case 'NORMAL':
                return 'Venta normal';

            case 'SLOW':
                return 'Venta lenta';

            default:
                return strategy;
        }
    }

    function handleCardSelect(
        card: CardSearchResult
    )
    {
        setSelectedCard(card);
        setError('');
    }

    function handleCostChange(
        value: string
    )
    {
        setLandedCostUnitClp(value);
        setScenarios([]);
    }

    function handleReferenceChange(
        value: string
    )
    {
        setLocalReferencePriceClp(value);
        setScenarios([]);
    }

    async function handleAnalyzePrice()
    {
        if (analyzing)
        {
            return;
        }

        const landedCost =
            Number(landedCostUnitClp);

        const referencePrice =
            Number(localReferencePriceClp);

        if (
            !Number.isInteger(landedCost) ||
            landedCost < 0
        )
        {
            setError(
                'El costo unitario debe ser un número entero mayor o igual a 0'
            );

            return;
        }

        if (
            !Number.isInteger(referencePrice) ||
            referencePrice <= 0
        )
        {
            setError(
                'El precio de referencia local debe ser un número entero mayor a 0'
            );

            return;
        }

        setAnalyzing(true);
        setError('');
        setScenarios([]);

        try
        {
            const response =
                await analyzePrice({
                    landedCostUnitClp:
                    landedCost,
                    localReferencePriceClp:
                    referencePrice,
                });

            setScenarios(response);
        }
        catch (analysisError)
        {
            if (
                analysisError instanceof Error
            )
            {
                setError(
                    analysisError.message
                );
            }
            else
            {
                setError(
                    'No se pudo calcular el análisis de precio'
                );
            }
        }
        finally
        {
            setAnalyzing(false);
        }
    }

    function selectScenario(
        scenario: SaleScenarioAnalysis
    )
    {
        setPrice(
            scenario.salePriceClp.toString()
        );

        setError('');
    }

    async function handleCreate(
        event: FormEvent<HTMLFormElement>
    )
    {
        event.preventDefault();

        if (creating)
        {
            return;
        }

        if (!selectedCard)
        {
            setError(
                'Debes seleccionar una carta'
            );

            return;
        }

        const stockNumber =
            Number(stock);

        const priceNumber =
            Number(price);

        if (
            !Number.isInteger(stockNumber) ||
            stockNumber < 0
        )
        {
            setError(
                'El stock debe ser un número entero mayor o igual a 0'
            );

            return;
        }

        if (
            !Number.isInteger(priceNumber) ||
            priceNumber <= 0
        )
        {
            setError(
                'Debes ingresar o seleccionar un precio de venta válido'
            );

            return;
        }

        setCreating(true);
        setError('');

        try
        {
            const createdProduct =
                await createProduct({
                    cardId:
                    selectedCard.id,
                    language,
                    variant,
                    condition,
                    stock:
                    stockNumber,
                    price:
                    priceNumber,
                });

            onCreated(
                createdProduct
            );
        }
        catch (createError)
        {
            if (
                createError instanceof Error
            )
            {
                setError(
                    createError.message
                );
            }
            else
            {
                setError(
                    'No se pudo crear el producto'
                );
            }
        }
        finally
        {
            setCreating(false);
        }
    }

    if (!open)
    {
        return null;
    }

    return (
        <>
            <div
                className="product-create-backdrop"
                onMouseDown={() =>
                {
                    if (
                        !creating &&
                        !cardPickerOpen
                    )
                    {
                        onClose();
                    }
                }}
            >
                <section
                    className="product-create-modal"
                    onMouseDown={(event) =>
                        event.stopPropagation()
                    }
                >
                    <header className="product-create-header">
                        <div>
                            <span>
                                CATÁLOGO
                            </span>

                            <h2>
                                Agregar carta
                            </h2>

                            <p>
                                Crea un producto comercial
                                a partir de una carta del catálogo.
                            </p>
                        </div>

                        <button
                            type="button"
                            className="product-create-close"
                            disabled={creating}
                            onClick={onClose}
                        >
                            ×
                        </button>
                    </header>

                    <form
                        className="product-create-form"
                        onSubmit={handleCreate}
                    >
                        <section className="product-create-section">

                            <div className="product-create-section-header">
                                <div>
                                    <span>
                                        1
                                    </span>

                                    <div>
                                        <h3>
                                            Carta
                                        </h3>

                                        <p>
                                            Selecciona la carta
                                            que quieres vender.
                                        </p>
                                    </div>
                                </div>
                            </div>

                            {selectedCard ? (
                                <div className="product-create-selected-card">

                                    <img
                                        src={
                                            selectedCard.imageUrl
                                        }
                                        alt={
                                            selectedCard.name
                                        }
                                    />

                                    <div>
                                        <strong>
                                            {
                                                selectedCard.name
                                            }
                                        </strong>

                                        <span>
                                            {
                                                selectedCard.setName
                                            }
                                        </span>

                                        <span>
                                            {
                                                getCollectorNumber(
                                                    selectedCard
                                                )
                                            }
                                        </span>
                                    </div>

                                    <button
                                        type="button"
                                        disabled={creating}
                                        onClick={() =>
                                            setCardPickerOpen(true)
                                        }
                                    >
                                        Cambiar carta
                                    </button>

                                </div>
                            ) : (
                                <button
                                    type="button"
                                    className="product-create-select-card"
                                    disabled={creating}
                                    onClick={() =>
                                        setCardPickerOpen(true)
                                    }
                                >
                                    Seleccionar carta
                                </button>
                            )}

                        </section>

                        <section className="product-create-section">

                            <div className="product-create-section-header">
                                <div>
                                    <span>
                                        2
                                    </span>

                                    <div>
                                        <h3>
                                            Producto
                                        </h3>

                                        <p>
                                            Define la versión comercial
                                            de la carta.
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="product-create-grid">

                                <label>
                                    Idioma

                                    <select
                                        value={language}
                                        disabled={creating}
                                        onChange={(event) =>
                                            setLanguage(
                                                event.target.value
                                            )
                                        }
                                    >
                                        <option value="ENGLISH">
                                            Inglés
                                        </option>

                                        <option value="JAPANESE">
                                            Japonés
                                        </option>

                                        <option value="SPANISH">
                                            Español
                                        </option>
                                    </select>
                                </label>

                                <label>
                                    Variante

                                    <select
                                        value={variant}
                                        disabled={creating}
                                        onChange={(event) =>
                                            setVariant(
                                                event.target.value
                                            )
                                        }
                                    >
                                        <option value="NORMAL">
                                            Normal
                                        </option>

                                        <option value="HOLO">
                                            Holo
                                        </option>

                                        <option value="REVERSE">
                                            Reverse
                                        </option>

                                        <option value="ENERGY_PATTERN">
                                            Energy Pattern
                                        </option>

                                        <option value="POKEBALL">
                                            Poké Ball
                                        </option>

                                        <option value="MASTERBALL">
                                            Master Ball
                                        </option>
                                    </select>
                                </label>

                                <label>
                                    Condición

                                    <select
                                        value={condition}
                                        disabled={creating}
                                        onChange={(event) =>
                                            setCondition(
                                                event.target.value
                                            )
                                        }
                                    >
                                        <option value="NEAR_MINT">
                                            Near Mint
                                        </option>

                                        <option value="LIGHTLY_PLAYED">
                                            Lightly Played
                                        </option>

                                        <option value="MODERATELY_PLAYED">
                                            Moderately Played
                                        </option>

                                        <option value="HEAVILY_PLAYED">
                                            Heavily Played
                                        </option>

                                        <option value="DAMAGED">
                                            Damaged
                                        </option>
                                    </select>
                                </label>

                                <label>
                                    Stock inicial

                                    <input
                                        type="number"
                                        min="0"
                                        step="1"
                                        value={stock}
                                        disabled={creating}
                                        onChange={(event) =>
                                            setStock(
                                                event.target.value
                                            )
                                        }
                                    />
                                </label>

                            </div>

                        </section>

                        <section className="product-create-section">

                            <div className="product-create-section-header">
                                <div>
                                    <span>
                                        3
                                    </span>

                                    <div>
                                        <h3>
                                            Análisis de precio
                                        </h3>

                                        <p>
                                            Usa el costo y el precio
                                            de referencia para analizar
                                            escenarios de venta.
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="product-create-grid">

                                <label>
                                    Costo unitario CLP

                                    <input
                                        type="number"
                                        min="0"
                                        step="1"
                                        placeholder="8500"
                                        value={
                                            landedCostUnitClp
                                        }
                                        disabled={
                                            analyzing ||
                                            creating
                                        }
                                        onChange={(event) =>
                                            handleCostChange(
                                                event.target.value
                                            )
                                        }
                                    />
                                </label>

                                <label>
                                    Precio referencia local CLP

                                    <input
                                        type="number"
                                        min="1"
                                        step="1"
                                        placeholder="15000"
                                        value={
                                            localReferencePriceClp
                                        }
                                        disabled={
                                            analyzing ||
                                            creating
                                        }
                                        onChange={(event) =>
                                            handleReferenceChange(
                                                event.target.value
                                            )
                                        }
                                    />
                                </label>

                            </div>

                            <button
                                type="button"
                                className="product-create-analyze-button"
                                disabled={
                                    analyzing ||
                                    creating
                                }
                                onClick={() =>
                                    void handleAnalyzePrice()
                                }
                            >
                                {analyzing
                                    ? 'Analizando...'
                                    : 'Analizar precio'}
                            </button>

                            {scenarios.length > 0 && (
                                <div className="product-price-scenarios">

                                    {scenarios.map(
                                        (scenario) => (
                                            <article
                                                key={
                                                    scenario.strategy
                                                }
                                                className={
                                                    price ===
                                                    scenario.salePriceClp
                                                        .toString()
                                                        ? 'product-price-scenario product-price-scenario-selected'
                                                        : 'product-price-scenario'
                                                }
                                            >
                                                <span>
                                                    {
                                                        formatStrategy(
                                                            scenario.strategy
                                                        )
                                                    }
                                                </span>

                                                <strong>
                                                    {
                                                        formatPrice(
                                                            scenario.salePriceClp
                                                        )
                                                    }
                                                </strong>

                                                <dl>
                                                    <div>
                                                        <dt>
                                                            Utilidad
                                                        </dt>

                                                        <dd>
                                                            {
                                                                formatPrice(
                                                                    scenario.profitPerUnitClp
                                                                )
                                                            }
                                                        </dd>
                                                    </div>

                                                    <div>
                                                        <dt>
                                                            Markup
                                                        </dt>

                                                        <dd>
                                                            {
                                                                formatPercentage(
                                                                    scenario.markup
                                                                )
                                                            }
                                                        </dd>
                                                    </div>

                                                    <div>
                                                        <dt>
                                                            Margen
                                                        </dt>

                                                        <dd>
                                                            {
                                                                formatPercentage(
                                                                    scenario.margin
                                                                )
                                                            }
                                                        </dd>
                                                    </div>
                                                </dl>

                                                <button
                                                    type="button"
                                                    disabled={creating}
                                                    onClick={() =>
                                                        selectScenario(
                                                            scenario
                                                        )
                                                    }
                                                >
                                                    Usar este precio
                                                </button>
                                            </article>
                                        )
                                    )}

                                </div>
                            )}

                        </section>

                        <section className="product-create-section">

                            <div className="product-create-section-header">
                                <div>
                                    <span>
                                        4
                                    </span>

                                    <div>
                                        <h3>
                                            Precio de venta
                                        </h3>

                                        <p>
                                            Puedes usar un escenario
                                            sugerido o ingresar
                                            el precio manualmente.
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <label className="product-create-final-price">
                                Precio final CLP

                                <input
                                    type="number"
                                    min="1"
                                    step="1"
                                    placeholder="Precio de venta"
                                    value={price}
                                    disabled={creating}
                                    onChange={(event) =>
                                        setPrice(
                                            event.target.value
                                        )
                                    }
                                />
                            </label>

                        </section>

                        {error && (
                            <p className="cart-feedback cart-feedback-error">
                                {error}
                            </p>
                        )}

                        <footer className="product-create-actions">

                            <button
                                type="button"
                                className="admin-cancel-button"
                                disabled={creating}
                                onClick={onClose}
                            >
                                Cancelar
                            </button>

                            <button
                                type="submit"
                                className="admin-save-button"
                                disabled={
                                    creating ||
                                    !selectedCard
                                }
                            >
                                {creating
                                    ? 'Creando...'
                                    : 'Crear producto'}
                            </button>

                        </footer>

                    </form>

                </section>
            </div>

            <CardPickerModal
                open={cardPickerOpen}
                onClose={() =>
                    setCardPickerOpen(false)
                }
                onSelect={handleCardSelect}
            />
        </>
    );
}

export default ProductCreateModal;