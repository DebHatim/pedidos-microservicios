const currencyFormatter = new Intl.NumberFormat('en-US', {
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
                <p className="cart-row__unit-price">{currencyFormatter.format(product.price)} / unit</p>
            </div>

            <div className="cart-row__controls">
                <div className="qty-stepper">
                    <button
                        type="button"
                        aria-label={`Remove one unit of ${product.name}`}
                        onClick={() => onDecrement(product.id)}
                    >
                        −
                    </button>
                    <span>{quantity}</span>
                    <button
                        type="button"
                        aria-label={`Add one unit of ${product.name}`}
                        onClick={() => onIncrement(product.id)}
                        disabled={atStockLimit}
                        title={atStockLimit ? 'You have reached the available stock' : undefined}
                    >
                        +
                    </button>
                </div>

                <button
                    type="button"
                    className="cart-row__remove"
                    onClick={() => onRemove(product.id)}
                    aria-label={`Remove ${product.name} from order`}
                >
                    Remove
                </button>
            </div>
        </li>
    )
}