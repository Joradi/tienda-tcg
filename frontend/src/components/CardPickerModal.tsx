import {
    useEffect,
    useState
} from 'react';

import type {
    FormEvent
} from 'react';

import {
    getCollectorNumber,
    searchCards
} from '../api/cardApi';

import type {
    CardSearchResult
} from '../api/cardApi';

interface CardPickerModalProps {
    open: boolean;
    onClose: () => void;
    onSelect: (card: CardSearchResult) => void;
}

function CardPickerModal({open, onClose, onSelect}: CardPickerModalProps)
{
    const [search, setSearch] = useState('');

    const [results, setResults] = useState<CardSearchResult[]>([]);

    const [loading, setLoading] = useState(false);

    const [error, setError] = useState('');

    const [previewCard, setPreviewCard] = useState<CardSearchResult | null>(null);

    useEffect(() =>
    {
        if(!open)
        {
            setSearch('');
            setResults([]);
            setError('');
            setPreviewCard(null);

            return;
        }

        function handleKeyDown(event: KeyboardEvent)
        {
            if(event.key === 'Escape')
            {
                if(previewCard)
                {
                    setPreviewCard(null);
                    return;
                }

                onClose();
            }
        }

        window.addEventListener('keydown', handleKeyDown);

        return () =>
        {
            window.removeEventListener('keydown', handleKeyDown);
        };
    }, [open, onClose, previewCard]);

    async function handleSearch()
    {
        const value = search.trim();

        if(value.length < 2)
        {
            setResults([]);

            setError('Escribe al menos 2 caracteres');

            return;
        }

        setLoading(true);
        setError('');

        try{
            const response = await searchCards(value);

            setResults(response);

            if(response.length === 0)
            {
                setError('No se encontraron cartas');
            }
        }
        catch(error)
        {
            setResults([]);

            setError(
                error instanceof Error ? error.message
                    : 'No se pudieron buscar las cartas');
        }
        finally
        {
            setLoading(false);
        }
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>)
    {
        event.preventDefault();

        handleSearch();
    }

    function handleSelect(card: CardSearchResult)
    {
        onSelect(card);
        onClose();
    }

    function openPreview(card: CardSearchResult)
    {
        setPreviewCard(card);
    }

    function closePreview()
    {
        setPreviewCard(null);
    }

    if(!open)
    {
        return null;
    }

    return (
        <div className="card-picker-backdrop" onMouseDown={onClose}>
            <section className="card-picker-modal" onMouseDown={event => event.stopPropagation()}>
                <header className="card-picker-header">
                    <div>
                        <span>CATÁLOGO</span>

                        <h2>Seleccionar carta</h2>
                    </div>

                    <button type="button" className="card-picker-close" onClick={onClose}>
                        ×
                    </button>
                </header>

                <form className="card-picker-search" onSubmit={handleSubmit}>
                    <input type="search" autoFocus autoComplete="off" placeholder="Nombre o código 001/018..." value={search}
                        onChange={event =>
                            setSearch(event.target.value)}/>

                    <button type="submit" disabled={loading}>
                        {loading
                            ? 'Buscando...'
                            : 'Buscar'}
                    </button>
                </form>

                {error && (
                    <p className="card-picker-message">
                        {error}
                    </p>
                )}

                <div className="card-picker-results">
                    {results.map(card => (
                        <article
                            className="card-picker-result"
                            key={card.id}
                        >
                            <button type="button" className="card-picker-image-button" onClick={() =>
                                openPreview(card)} title="Ampliar imagen">
                                <img
                                    src={card.imageUrl}
                                    alt={card.name}
                                />
                            </button>

                            <div className="card-picker-result-info">
                                <strong>
                                    {card.name}
                                </strong>

                                <span>
                                    {card.setName}
                                </span>

                                <span>
                                    {getCollectorNumber(card)}
                                </span>
                            </div>

                            <button
                                type="button"
                                onClick={() =>
                                    handleSelect(card)
                                }
                            >
                                Elegir
                            </button>
                        </article>
                    ))}
                </div>
            </section>

            {previewCard && (
                <div
                    className="card-preview-backdrop"
                    onMouseDown={event =>
                    {
                        event.stopPropagation();
                        closePreview();
                    }}
                >
                    <section
                        className="card-preview"
                        onMouseDown={event =>
                            event.stopPropagation()
                        }
                    >
                        <button
                            type="button"
                            className="card-preview-close"
                            onClick={closePreview}
                        >
                            ×
                        </button>

                        <img
                            src={previewCard.imageUrl}
                            alt={previewCard.name}
                        />

                        <div className="card-preview-info">
                            <strong>
                                {previewCard.name}
                            </strong>

                            <span>
                                {previewCard.setName}
                            </span>

                            <span>
                                {getCollectorNumber(
                                    previewCard
                                )}
                            </span>
                        </div>

                        <button
                            type="button"
                            className="card-preview-select"
                            onClick={() =>
                                handleSelect(
                                    previewCard
                                )
                            }
                        >
                            Elegir esta carta
                        </button>
                    </section>
                </div>
            )}
        </div>
    );
}

export default CardPickerModal;