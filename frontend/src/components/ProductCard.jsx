const STOCK_MAX_REFERENCE = 50

function getStockLevel(stock) {
    if (stock <= 0) return 'out'
    if (stock <= 10) return 'low'
    return 'normal'
}

function getStockLabel(stock, level) {
    if (level === 'out') return 'Out of stock'
    if (level === 'low') return `Only ${stock} left`
    return `${stock} units`
}

const currencyFormatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'EUR'
})

export default function ProductCard({ product, quantityInCart = 0, onAdd }) {
    const level = getStockLevel(product.stock)
    const fillPercent = Math.min(100, Math.round((product.stock / STOCK_MAX_REFERENCE) * 100))
    const isOutOfStock = level === 'out'
    const atCartLimit = quantityInCart >= product.stock

    return (
        <article className="product-card">
            <div className="product-card__media">
                <img src={product.imageUrl} alt={product.name} loading="lazy" />
                <span className="product-card__category">{product.category}</span>
            </div>

            <div className="product-card__body">
                <h2 className="product-card__name">{product.name}</h2>
                <p className="product-card__description">{product.description}</p>

                <div className="product-card__footer">
                    <span className="product-card__price">{currencyFormatter.format(product.price)}</span>

                    <div className="stock-gauge">
                        <div className="stock-gauge__label" data-level={level}>
                            {getStockLabel(product.stock, level)}
                        </div>
                        <div className="stock-gauge__track">
                            <div
                                className="stock-gauge__fill"
                                data-level={level}
                                style={{ width: `${fillPercent}%` }}
                            />
                        </div>
                    </div>
                </div>

                <button
                    type="button"
                    className="product-card__add"
                    onClick={() => onAdd(product)}
                    disabled={isOutOfStock || atCartLimit}
                >
                    {isOutOfStock
                        ? 'Out of stock'
                        : atCartLimit
                            ? 'Max stock in order'
                            : quantityInCart > 0
                                ? `Add another (${quantityInCart} in order)`
                                : 'Add to order'}
                </button>
            </div>
        </article>
    )
}