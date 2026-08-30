import {
    useEffect,
    useState
} from 'react';

import type {
    FormEvent
} from 'react';

import {
    compareImportationScenarios,
    createImportation,
    getImportationAnalysis,
    getImportations
} from '../api/importationApi';

import CardPickerModal from '../components/CardPickerModal';

import type {
    CardSearchResult
} from '../api/cardApi';

import type {
    ImportationAnalysis,
    ImportationResponse,
    ImportScenarioComparison
} from '../types/Importation';

interface ImportItemForm {
    key: number;
    cardId: number | null;
    selectedCard: CardSearchResult | null;
    language: string;
    variant: string;
    condition: string;
    quantity: string;
    purchaseUnitPriceClp: string;
    localReferencePriceClp: string;
}

const ORIGINS = [
    'USA',
    'JAPAN'
];

const LANGUAGES = [
    'ENGLISH',
    'JAPANESE',
    'SPANISH'
];

const VARIANTS = [
    'NORMAL',
    'HOLO',
    'REVERSE',
    'ENERGY_PATTERN',
    'POKEBALL',
    'MASTERBALL'
];

const CONDITIONS = [
    'NEAR_MINT',
    'LIGHTLY_PLAYED',
    'MODERATELY_PLAYED',
    'HEAVILY_PLAYED',
    'DAMAGED'
];

function createEmptyItem(key: number): ImportItemForm
{
    return {
        key,
        cardId: null,
        selectedCard: null,
        language: 'ENGLISH',
        variant: 'NORMAL',
        condition: 'NEAR_MINT',
        quantity: '1',
        purchaseUnitPriceClp: '',
        localReferencePriceClp: '',
    };
}

function numberOrZero(value: string): number
{
    if(value.trim() === '')
    {
        return 0;
    }

    return Number(value);
}

function formatClp(value: number): string
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

function formatUsd(value: number): string
{
    return new Intl.NumberFormat(
        'es-CL',
        {
            style: 'currency',
            currency: 'USD',
        }
    ).format(value);
}

function formatPercentage(value: number): string
{
    return `${(value * 100).toFixed(2)}%`;
}

function formatDate(value: string): string
{
    return new Date(value).toLocaleString(
        'es-CL'
    );
}

function ImportationsPage()
{
    const [importations, setImportations] =
        useState<ImportationResponse[]>([]);

    const [origin, setOrigin] =
        useState('USA');

    const [proxyCostClp, setProxyCostClp] =
        useState('');

    const [freightCostClp, setFreightCostClp] =
        useState('');

    const [insuranceCostClp, setInsuranceCostClp] =
        useState('');

    const [otherSharedCostClp, setOtherSharedCostClp] =
        useState('');

    const [customsValueUsd, setCustomsValueUsd] =
        useState('');

    const [items, setItems] =
        useState<ImportItemForm[]>([
            createEmptyItem(0)
        ]);

    const [nextItemKey, setNextItemKey] =
        useState(1);

    const [activeCardItemIndex, setActiveCardItemIndex] =
        useState<number | null>(null);

    const [analysis, setAnalysis] =
        useState<ImportationAnalysis | null>(null);

    const [analysisImportationId, setAnalysisImportationId] =
        useState<number | null>(null);

    const [firstScenarioIds, setFirstScenarioIds] =
        useState<number[]>([]);

    const [secondScenarioIds, setSecondScenarioIds] =
        useState<number[]>([]);

    const [comparison, setComparison] =
        useState<ImportScenarioComparison | null>(null);

    const [loading, setLoading] =
        useState(true);

    const [saving, setSaving] =
        useState(false);

    const [analyzing, setAnalyzing] =
        useState(false);

    const [comparing, setComparing] =
        useState(false);

    const [error, setError] =
        useState('');

    const [message, setMessage] =
        useState('');

    useEffect(() =>
    {
        loadImportations();
    }, []);

    async function loadImportations()
    {
        setLoading(true);
        setError('');

        try{
            const response =
                await getImportations();

            const ordered =
                [...response].sort(
                    (a, b) => b.id - a.id
                );

            setImportations(ordered);
        }
        catch(error)
        {
            setError(
                error instanceof Error
                    ? error.message
                    : 'No se pudieron cargar las importaciones'
            );
        }
        finally
        {
            setLoading(false);
        }
    }

    function updateItem(
        index: number,
        field: keyof ImportItemForm,
        value: string | number | null | CardSearchResult)
    {
        setItems(current =>
            current.map((item, itemIndex) =>
            {
                if(itemIndex !== index)
                {
                    return item;
                }

                return {
                    ...item,
                    [field]: value,
                };
            })
        );
    }

    function openCardPicker(index: number)
    {
        setActiveCardItemIndex(index);
    }

    function closeCardPicker()
    {
        setActiveCardItemIndex(null);
    }

    function handleCardSelect(card: CardSearchResult)
    {
        if(activeCardItemIndex === null)
        {
            return;
        }

        setItems(current =>
            current.map((item, index) =>
            {
                if(index !== activeCardItemIndex)
                {
                    return item;
                }

                return {
                    ...item,
                    cardId: card.id,
                    selectedCard: card,
                };
            })
        );

        setActiveCardItemIndex(null);
    }

    function addItem()
    {
        setItems(current => [
            ...current,
            createEmptyItem(nextItemKey)
        ]);

        setNextItemKey(current =>
            current + 1
        );
    }

    function removeItem(index: number)
    {
        if(items.length === 1)
        {
            return;
        }

        setItems(current =>
            current.filter(
                (_, itemIndex) =>
                    itemIndex !== index
            )
        );
    }

    function resetForm()
    {
        const key =
            nextItemKey;

        setOrigin('USA');
        setProxyCostClp('');
        setFreightCostClp('');
        setInsuranceCostClp('');
        setOtherSharedCostClp('');
        setCustomsValueUsd('');

        setItems([
            createEmptyItem(key)
        ]);

        setNextItemKey(current =>
            current + 1
        );

        setActiveCardItemIndex(null);
    }

    function validateImportation(): string | null
    {
        const proxy =
            numberOrZero(proxyCostClp);

        const freight =
            numberOrZero(freightCostClp);

        const insurance =
            numberOrZero(insuranceCostClp);

        const other =
            numberOrZero(otherSharedCostClp);

        const customs =
            numberOrZero(customsValueUsd);

        if(
            !Number.isFinite(proxy) ||
            !Number.isFinite(freight) ||
            !Number.isFinite(insurance) ||
            !Number.isFinite(other) ||
            !Number.isFinite(customs)
        )
        {
            return 'Los costos deben ser números válidos';
        }

        if(
            proxy < 0 ||
            freight < 0 ||
            insurance < 0 ||
            other < 0 ||
            customs < 0
        )
        {
            return 'Los costos no pueden ser negativos';
        }

        for(const item of items)
        {
            if(
                !item.cardId ||
                !item.selectedCard
            )
            {
                return 'Debes seleccionar una carta válida en cada item';
            }

            const quantity =
                Number(item.quantity);

            const purchasePrice =
                numberOrZero(
                    item.purchaseUnitPriceClp
                );

            const localPrice =
                numberOrZero(
                    item.localReferencePriceClp
                );

            if(
                !Number.isInteger(quantity) ||
                quantity <= 0
            )
            {
                return 'La cantidad debe ser un entero mayor que 0';
            }

            if(
                !Number.isFinite(purchasePrice) ||
                !Number.isFinite(localPrice)
            )
            {
                return 'Los precios deben ser números válidos';
            }

            if(
                purchasePrice < 0 ||
                localPrice < 0
            )
            {
                return 'Los precios de las cartas no pueden ser negativos';
            }
        }

        return null;
    }

    async function handleCreateImportation(
        event: FormEvent<HTMLFormElement>)
    {
        event.preventDefault();

        const validationError =
            validateImportation();

        if(validationError)
        {
            setError(validationError);
            return;
        }

        setSaving(true);
        setError('');
        setMessage('');
        setComparison(null);

        try{
            const created =
                await createImportation({
                    origin,
                    proxyCostClp:
                        numberOrZero(proxyCostClp),
                    freightCostClp:
                        numberOrZero(freightCostClp),
                    insuranceCostClp:
                        numberOrZero(insuranceCostClp),
                    otherSharedCostClp:
                        numberOrZero(otherSharedCostClp),
                    customsValueUsd:
                        numberOrZero(customsValueUsd),
                    items: items.map(item => ({
                        cardId: item.cardId!,
                        language: item.language,
                        variant: item.variant,
                        condition: item.condition,
                        quantity:
                            Number(item.quantity),
                        purchaseUnitPriceClp:
                            numberOrZero(
                                item.purchaseUnitPriceClp
                            ),
                        localReferencePriceClp:
                            numberOrZero(
                                item.localReferencePriceClp
                            ),
                    })),
                });

            const createdAnalysis =
                await getImportationAnalysis(
                    created.id
                );

            setAnalysis(
                createdAnalysis
            );

            setAnalysisImportationId(
                created.id
            );

            setImportations(current =>
                [
                    created,
                    ...current.filter(
                        importation =>
                            importation.id !== created.id
                    ),
                ]
            );

            setMessage(
                `Importación #${created.id} creada correctamente`
            );

            resetForm();
        }
        catch(error)
        {
            setError(
                error instanceof Error
                    ? error.message
                    : 'No se pudo crear la importación'
            );
        }
        finally
        {
            setSaving(false);
        }
    }

    async function handleAnalyze(
        importationId: number)
    {
        setAnalyzing(true);
        setError('');
        setMessage('');

        try{
            const response =
                await getImportationAnalysis(
                    importationId
                );

            setAnalysis(response);

            setAnalysisImportationId(
                importationId
            );
        }
        catch(error)
        {
            setError(
                error instanceof Error
                    ? error.message
                    : 'No se pudo analizar la importación'
            );
        }
        finally
        {
            setAnalyzing(false);
        }
    }

    function toggleFirstScenario(
        importationId: number)
    {
        setComparison(null);

        setFirstScenarioIds(current =>
        {
            if(current.includes(importationId))
            {
                return current.filter(
                    id => id !== importationId
                );
            }

            return [
                ...current,
                importationId
            ];
        });

        setSecondScenarioIds(current =>
            current.filter(
                id => id !== importationId
            )
        );
    }

    function toggleSecondScenario(
        importationId: number)
    {
        setComparison(null);

        setSecondScenarioIds(current =>
        {
            if(current.includes(importationId))
            {
                return current.filter(
                    id => id !== importationId
                );
            }

            return [
                ...current,
                importationId
            ];
        });

        setFirstScenarioIds(current =>
            current.filter(
                id => id !== importationId
            )
        );
    }

    async function handleCompare()
    {
        if(
            firstScenarioIds.length === 0 ||
            secondScenarioIds.length === 0
        )
        {
            setError(
                'Selecciona al menos una importación para cada escenario'
            );

            return;
        }

        setComparing(true);
        setError('');
        setMessage('');

        try{
            const response =
                await compareImportationScenarios({
                    firstScenarioImportationIds:
                    firstScenarioIds,
                    secondScenarioImportationIds:
                    secondScenarioIds,
                });

            setComparison(response);
        }
        catch(error)
        {
            setError(
                error instanceof Error
                    ? error.message
                    : 'No se pudieron comparar los escenarios'
            );
        }
        finally
        {
            setComparing(false);
        }
    }

    return (
        <main className="importations-page">
            <CardPickerModal
                open={activeCardItemIndex !== null}
                onClose={closeCardPicker}
                onSelect={handleCardSelect}
            />

            <section className="importations-header">
                <span>BACKOFFICE</span>

                <h1>Importaciones</h1>

                <p>
                    Registra costos de importación,
                    analiza rentabilidad y compara escenarios.
                </p>
            </section>

            {error && (
                <p className="importations-error">
                    {error}
                </p>
            )}

            {message && (
                <p className="importations-success">
                    {message}
                </p>
            )}

            <section className="importations-section">
                <div className="importations-section-header">
                    <div>
                        <span>NUEVA IMPORTACIÓN</span>

                        <h2>Calculadora</h2>
                    </div>
                </div>

                <form
                    className="importations-form"
                    onSubmit={handleCreateImportation}
                >
                    <div className="importations-general-grid">
                        <label>
                            Origen

                            <select
                                value={origin}
                                onChange={event =>
                                    setOrigin(
                                        event.target.value
                                    )
                                }
                            >
                                {ORIGINS.map(value => (
                                    <option
                                        key={value}
                                        value={value}
                                    >
                                        {value}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label>
                            Valor aduanero USD

                            <input
                                type="number"
                                min="0"
                                step="0.01"
                                placeholder="0.00"
                                value={customsValueUsd}
                                onChange={event =>
                                    setCustomsValueUsd(
                                        event.target.value
                                    )
                                }
                            />
                        </label>

                        <label>
                            Proxy CLP

                            <input
                                type="number"
                                min="0"
                                placeholder="0"
                                value={proxyCostClp}
                                onChange={event =>
                                    setProxyCostClp(
                                        event.target.value
                                    )
                                }
                            />
                        </label>

                        <label>
                            Flete CLP

                            <input
                                type="number"
                                min="0"
                                placeholder="0"
                                value={freightCostClp}
                                onChange={event =>
                                    setFreightCostClp(
                                        event.target.value
                                    )
                                }
                            />
                        </label>

                        <label>
                            Seguro CLP

                            <input
                                type="number"
                                min="0"
                                placeholder="0"
                                value={insuranceCostClp}
                                onChange={event =>
                                    setInsuranceCostClp(
                                        event.target.value
                                    )
                                }
                            />
                        </label>

                        <label>
                            Otros costos CLP

                            <input
                                type="number"
                                min="0"
                                placeholder="0"
                                value={otherSharedCostClp}
                                onChange={event =>
                                    setOtherSharedCostClp(
                                        event.target.value
                                    )
                                }
                            />
                        </label>
                    </div>

                    <div className="importations-items-header">
                        <div>
                            <span>ITEMS</span>

                            <h3>
                                Cartas de la importación
                            </h3>
                        </div>

                        <button
                            type="button"
                            onClick={addItem}
                        >
                            Agregar carta
                        </button>
                    </div>

                    <div className="importations-items">
                        {items.map((item, index) => (
                            <div
                                className="importations-item"
                                key={item.key}
                            >
                                <div className="importations-card-picker-field">
                                    <span className="importations-card-picker-label">
                                        Carta
                                    </span>

                                    {item.selectedCard ? (
                                        <button
                                            type="button"
                                            className="importations-selected-card-button"
                                            onClick={() =>
                                                openCardPicker(index)
                                            }
                                        >
                                            <img
                                                src={item.selectedCard.imageUrl}
                                                alt={item.selectedCard.name}
                                            />

                                            <span>
                                                <strong>
                                                    {item.selectedCard.name}
                                                </strong>

                                                <small>
                                                    {item.selectedCard.setName}
                                                    {' · '}
                                                    {item.selectedCard.number}
                                                    /
                                                    {item.selectedCard.setPrintedTotal}
                                                </small>

                                                <small>
                                                    Cambiar carta
                                                </small>
                                            </span>
                                        </button>
                                    ) : (
                                        <button
                                            type="button"
                                            className="importations-select-card-button"
                                            onClick={() =>
                                                openCardPicker(index)
                                            }
                                        >
                                            Seleccionar carta
                                        </button>
                                    )}
                                </div>

                                <label>
                                    Idioma

                                    <select
                                        value={item.language}
                                        onChange={event =>
                                            updateItem(
                                                index,
                                                'language',
                                                event.target.value
                                            )
                                        }
                                    >
                                        {LANGUAGES.map(value => (
                                            <option
                                                key={value}
                                                value={value}
                                            >
                                                {value}
                                            </option>
                                        ))}
                                    </select>
                                </label>

                                <label>
                                    Variante

                                    <select
                                        value={item.variant}
                                        onChange={event =>
                                            updateItem(
                                                index,
                                                'variant',
                                                event.target.value
                                            )
                                        }
                                    >
                                        {VARIANTS.map(value => (
                                            <option
                                                key={value}
                                                value={value}
                                            >
                                                {value}
                                            </option>
                                        ))}
                                    </select>
                                </label>

                                <label>
                                    Condición

                                    <select
                                        value={item.condition}
                                        onChange={event =>
                                            updateItem(
                                                index,
                                                'condition',
                                                event.target.value
                                            )
                                        }
                                    >
                                        {CONDITIONS.map(value => (
                                            <option
                                                key={value}
                                                value={value}
                                            >
                                                {value}
                                            </option>
                                        ))}
                                    </select>
                                </label>

                                <label>
                                    Cantidad

                                    <input
                                        type="number"
                                        min="1"
                                        step="1"
                                        value={item.quantity}
                                        onChange={event =>
                                            updateItem(
                                                index,
                                                'quantity',
                                                event.target.value
                                            )
                                        }
                                    />
                                </label>

                                <label>
                                    Compra unitaria CLP

                                    <input
                                        type="number"
                                        min="0"
                                        placeholder="0"
                                        value={item.purchaseUnitPriceClp}
                                        onChange={event =>
                                            updateItem(
                                                index,
                                                'purchaseUnitPriceClp',
                                                event.target.value
                                            )
                                        }
                                    />
                                </label>

                                <label>
                                    Referencia local CLP

                                    <input
                                        type="number"
                                        min="0"
                                        placeholder="0"
                                        value={item.localReferencePriceClp}
                                        onChange={event =>
                                            updateItem(
                                                index,
                                                'localReferencePriceClp',
                                                event.target.value
                                            )
                                        }
                                    />
                                </label>

                                <button
                                    type="button"
                                    disabled={items.length === 1}
                                    onClick={() =>
                                        removeItem(index)
                                    }
                                >
                                    Quitar
                                </button>
                            </div>
                        ))}
                    </div>

                    <button
                        className="importations-create-button"
                        type="submit"
                        disabled={saving}
                    >
                        {saving
                            ? 'Calculando...'
                            : 'Crear y calcular'}
                    </button>
                </form>
            </section>

            {analysis && (
                <section className="importations-section">
                    <div className="importations-section-header">
                        <div>
                            <span>ANÁLISIS</span>

                            <h2>
                                Importación #{analysisImportationId}
                            </h2>
                        </div>
                    </div>

                    <div className="importations-summary-grid">
                        <article>
                            <span>Items</span>

                            <strong>
                                {analysis.totalItemCount}
                            </strong>
                        </article>

                        <article>
                            <span>Cartas</span>

                            <strong>
                                {analysis.totalCardQuantity}
                            </strong>
                        </article>

                        <article>
                            <span>Mercadería</span>

                            <strong>
                                {formatClp(
                                    analysis.merchandiseCostClp
                                )}
                            </strong>
                        </article>

                        <article>
                            <span>Costos compartidos</span>

                            <strong>
                                {formatClp(
                                    analysis.totalSharedCostClp
                                )}
                            </strong>
                        </article>

                        <article>
                            <span>Impuestos</span>

                            <strong>
                                {formatClp(
                                    analysis.totalTaxClp
                                )}
                            </strong>
                        </article>

                        <article>
                            <span>Landed cost</span>

                            <strong>
                                {formatClp(
                                    analysis.landedCostTotalClp
                                )}
                            </strong>
                        </article>
                    </div>

                    <div className="importations-strategies">
                        {[
                            analysis.quick,
                            analysis.normal,
                            analysis.slow
                        ].map(strategy => (
                            <article key={strategy.strategy}>
                                <span>
                                    {strategy.strategy}
                                </span>

                                <p>
                                    Ingreso potencial

                                    <strong>
                                        {formatClp(
                                            strategy.potentialRevenueClp
                                        )}
                                    </strong>
                                </p>

                                <p>
                                    Utilidad potencial

                                    <strong>
                                        {formatClp(
                                            strategy.potentialProfitClp
                                        )}
                                    </strong>
                                </p>

                                <p>
                                    Margen

                                    <strong>
                                        {formatPercentage(
                                            strategy.margin
                                        )}
                                    </strong>
                                </p>
                            </article>
                        ))}
                    </div>

                    <div className="importations-viability">
                        <span>
                            Alta: {analysis.highCardQuantity}
                        </span>

                        <span>
                            Media: {analysis.mediumCardQuantity}
                        </span>

                        <span>
                            Baja: {analysis.lowCardQuantity}
                        </span>

                        <span>
                            No viable: {analysis.notViableCardQuantity}
                        </span>
                    </div>

                    <div className="importations-analysis-items">
                        {analysis.items.map(item => (
                            <article key={item.cost.importItemId}>
                                <div>
                                    <strong>
                                        {item.cost.cardName}
                                    </strong>

                                    <span>
                                        {item.cost.quantity} unidades
                                    </span>
                                </div>

                                <div>
                                    <span>
                                        Compra unitaria
                                    </span>

                                    <strong>
                                        {formatClp(
                                            item.cost.purchaseUnitPriceClp
                                        )}
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        Landed unitario
                                    </span>

                                    <strong>
                                        {formatClp(
                                            item.cost.landedCostUnitClp
                                        )}
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        Referencia local
                                    </span>

                                    <strong>
                                        {formatClp(
                                            item.cost.localReferencePriceClp
                                        )}
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        Viabilidad
                                    </span>

                                    <strong>
                                        {item.viability}
                                    </strong>
                                </div>
                            </article>
                        ))}
                    </div>
                </section>
            )}

            <section className="importations-section">
                <div className="importations-section-header">
                    <div>
                        <span>HISTORIAL</span>

                        <h2>
                            Importaciones registradas
                        </h2>
                    </div>

                    <span>
                        {importations.length} importaciones
                    </span>
                </div>

                {loading ? (
                    <p>
                        Cargando importaciones...
                    </p>
                ) : importations.length === 0 ? (
                    <p>
                        No hay importaciones registradas.
                    </p>
                ) : (
                    <div className="importations-history">
                        {importations.map(importation => (
                            <article
                                key={importation.id}
                                className="importations-history-item"
                            >
                                <div>
                                    <strong>
                                        Importación #{importation.id}
                                    </strong>

                                    <span>
                                        {importation.origin}
                                    </span>

                                    <span>
                                        {formatDate(
                                            importation.createdAt
                                        )}
                                    </span>
                                </div>

                                <div>
                                    <span>Cartas</span>

                                    <strong>
                                        {importation.totalCardQuantity}
                                    </strong>
                                </div>

                                <div>
                                    <span>Aduana</span>

                                    <strong>
                                        {formatUsd(
                                            importation.customsValueUsd
                                        )}
                                    </strong>
                                </div>

                                <button
                                    type="button"
                                    disabled={analyzing}
                                    onClick={() =>
                                        handleAnalyze(
                                            importation.id
                                        )
                                    }
                                >
                                    Analizar
                                </button>
                            </article>
                        ))}
                    </div>
                )}
            </section>

            <section className="importations-section">
                <div className="importations-section-header">
                    <div>
                        <span>COMPARADOR</span>

                        <h2>
                            Comparar escenarios
                        </h2>
                    </div>
                </div>

                {importations.length < 2 ? (
                    <p>
                        Necesitas al menos dos importaciones
                        para utilizar el comparador.
                    </p>
                ) : (
                    <>
                        <div className="importations-scenarios">
                            <div>
                                <h3>Escenario A</h3>

                                {importations.map(importation => (
                                    <label key={importation.id}>
                                        <input
                                            type="checkbox"
                                            checked={
                                                firstScenarioIds.includes(
                                                    importation.id
                                                )
                                            }
                                            onChange={() =>
                                                toggleFirstScenario(
                                                    importation.id
                                                )
                                            }
                                        />

                                        Importación #{importation.id}
                                        {' · '}
                                        {importation.origin}
                                        {' · '}
                                        {importation.totalCardQuantity}
                                        {' cartas'}
                                    </label>
                                ))}
                            </div>

                            <div>
                                <h3>Escenario B</h3>

                                {importations.map(importation => (
                                    <label key={importation.id}>
                                        <input
                                            type="checkbox"
                                            checked={
                                                secondScenarioIds.includes(
                                                    importation.id
                                                )
                                            }
                                            onChange={() =>
                                                toggleSecondScenario(
                                                    importation.id
                                                )
                                            }
                                        />

                                        Importación #{importation.id}
                                        {' · '}
                                        {importation.origin}
                                        {' · '}
                                        {importation.totalCardQuantity}
                                        {' cartas'}
                                    </label>
                                ))}
                            </div>
                        </div>

                        <button
                            type="button"
                            disabled={comparing}
                            onClick={handleCompare}
                        >
                            {comparing
                                ? 'Comparando...'
                                : 'Comparar escenarios'}
                        </button>
                    </>
                )}

                {comparison && (
                    <div className="importations-comparison">
                        <div className="importations-comparison-scenarios">
                            <article>
                                <span>ESCENARIO A</span>

                                <strong>
                                    {comparison.firstScenario.importationCount}
                                    {' importaciones'}
                                </strong>

                                <p>
                                    Cartas:
                                    {' '}
                                    {comparison.firstScenario.totalCardQuantity}
                                </p>

                                <p>
                                    Landed cost:
                                    {' '}
                                    {formatClp(
                                        comparison.firstScenario.landedCostTotalClp
                                    )}
                                </p>

                                <p>
                                    QUICK:
                                    {' '}
                                    {formatClp(
                                        comparison.firstScenario.quick.potentialProfitClp
                                    )}
                                </p>

                                <p>
                                    NORMAL:
                                    {' '}
                                    {formatClp(
                                        comparison.firstScenario.normal.potentialProfitClp
                                    )}
                                </p>

                                <p>
                                    SLOW:
                                    {' '}
                                    {formatClp(
                                        comparison.firstScenario.slow.potentialProfitClp
                                    )}
                                </p>
                            </article>

                            <article>
                                <span>ESCENARIO B</span>

                                <strong>
                                    {comparison.secondScenario.importationCount}
                                    {' importaciones'}
                                </strong>

                                <p>
                                    Cartas:
                                    {' '}
                                    {comparison.secondScenario.totalCardQuantity}
                                </p>

                                <p>
                                    Landed cost:
                                    {' '}
                                    {formatClp(
                                        comparison.secondScenario.landedCostTotalClp
                                    )}
                                </p>

                                <p>
                                    QUICK:
                                    {' '}
                                    {formatClp(
                                        comparison.secondScenario.quick.potentialProfitClp
                                    )}
                                </p>

                                <p>
                                    NORMAL:
                                    {' '}
                                    {formatClp(
                                        comparison.secondScenario.normal.potentialProfitClp
                                    )}
                                </p>

                                <p>
                                    SLOW:
                                    {' '}
                                    {formatClp(
                                        comparison.secondScenario.slow.potentialProfitClp
                                    )}
                                </p>
                            </article>
                        </div>

                        <div className="importations-differences">
                            <p>
                                Diferencia landed cost

                                <strong>
                                    {formatClp(
                                        comparison.landedCostDifferenceClp
                                    )}
                                </strong>
                            </p>

                            <p>
                                Diferencia QUICK

                                <strong>
                                    {formatClp(
                                        comparison.quickProfitDifferenceClp
                                    )}
                                </strong>
                            </p>

                            <p>
                                Diferencia NORMAL

                                <strong>
                                    {formatClp(
                                        comparison.normalProfitDifferenceClp
                                    )}
                                </strong>
                            </p>

                            <p>
                                Diferencia SLOW

                                <strong>
                                    {formatClp(
                                        comparison.slowProfitDifferenceClp
                                    )}
                                </strong>
                            </p>
                        </div>
                    </div>
                )}
            </section>
        </main>
    );
}

export default ImportationsPage;