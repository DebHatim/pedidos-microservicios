export default function CategoryFilter({ categories, selected, onSelect }) {
    return (
        <div className="category-filter" role="group" aria-label="Filtrar por categoría">
            <span className="category-filter__label">Categoría</span>

            <button
                type="button"
                className="pill"
                data-active={selected === 'ALL'}
                aria-pressed={selected === 'ALL'}
                onClick={() => onSelect('ALL')}
            >
                Todos
                <span className="pill__count">{categories.total}</span>
            </button>

            {categories.list.map(({ name, count }) => (
                <button
                    key={name}
                    type="button"
                    className="pill"
                    data-active={selected === name}
                    aria-pressed={selected === name}
                    onClick={() => onSelect(name)}
                >
                    {name}
                    <span className="pill__count">{count}</span>
                </button>
            ))}
        </div>
    )
}