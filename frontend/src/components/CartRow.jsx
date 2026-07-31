const currencyFormatter = new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR'
})

export default function CartRow({ entry, onIncrement, onDecrement, onRemove }) {
    const { product, quantity } = entry
    const atStockLimit = quantity >= product.stock

    return (
        <li className="cart-row">
            <img className="cart-row__thumb" src={product.imageUrl} alt="" />

            <div className="cart-row__info">
                <p className="cart-row__name">{product.name}</p>
                <p className="cart-row__unit-price">{currencyFormatter.format(product.price)} / ud.</p>
            </div>

            <div className="cart-row__controls">
                <div className="qty-stepper">
                    <button
                        type="button"
                        aria-label={`Quitar una unidad de ${product.name}`}
                        onClick={() => onDecrement(product.id)}
                    >
                        −
                    </button>
                    <span>{quantity}</span>
                    <button
                        type="button"
                        aria-label={`Añadir una unidad de ${product.name}`}
                        onClick={() => onIncrement(product.id)}
                        disabled={atStockLimit}
                        title={atStockLimit ? 'Has alcanzado el stock disponible' : undefined}
                    >
                        +
                    </button>
                </div>

                <button
                    type="button"
                    className="cart-row__remove"
                    onClick={() => onRemove(product.id)}
                    aria-label={`Quitar ${product.name} del pedido`}
                >
                    Quitar
                </button>
            </div>
        </li>
    )
}