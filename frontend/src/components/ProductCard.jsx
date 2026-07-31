const STOCK_MAX_REFERENCE = 50

function getStockLevel(stock) {
    if (stock <= 0) return 'out'
    if (stock <= 10) return 'low'
    return 'normal'
}

function getStockLabel(stock, level) {
    if (level === 'out') return 'Sin stock'
    if (level === 'low') return `Últimas ${stock} unidades`
    return `${stock} unidades`
}

const currencyFormatter = new Intl.NumberFormat('es-ES', {
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
                        ? 'Sin stock'
                        : atCartLimit
                            ? 'Stock máximo en el pedido'
                            : quantityInCart > 0
                                ? `Añadir otra (${quantityInCart} en el pedido)`
                                : 'Añadir al pedido'}
                </button>
            </div>
        </article>
    )
}