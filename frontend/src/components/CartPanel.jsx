import CartRow from './CartRow.jsx'

const currencyFormatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'EUR'
})

export default function CartPanel({
                                      isOpen,
                                      onClose,
                                      cartItems,
                                      total,
                                      submitStatus,
                                      resultMessage,
                                      onIncrement,
                                      onDecrement,
                                      onRemove,
                                      onSubmit,
                                      onDismissResult
                                  }) {
    if (!isOpen) return null

    const isEmpty = cartItems.length === 0
    const isSubmitting = submitStatus === 'submitting'
    const isFinalResult = submitStatus === 'waiting' || submitStatus === 'confirmed' || submitStatus === 'rejected'

    return (
        <>
            <div className="cart-backdrop" onClick={onClose}/>

            <aside className="cart-panel" role="dialog" aria-label="Order cart">
                <div className="cart-panel__header">
                    <h2>Your order</h2>
                    <button type="button" className="cart-panel__close" onClick={onClose} aria-label="Close cart">
                        ×
                    </button>
                </div>

                {submitStatus === 'waiting' && (
                    <div className="cart-result cart-result--success">
                        <p className="cart-result__title">Order sent</p>
                        <p className="cart-result__text">
                            Checking available stock. You will receive real-time confirmation
                            as soon as inventory-service processes your order.
                        </p>
                    </div>
                )}

                {submitStatus === 'confirmed' && (
                    <div className="cart-result cart-result--success">
                        <p className="cart-result__title">Order confirmed</p>
                        <p className="cart-result__text">{resultMessage}</p>
                        <button type="button" className="state-panel__retry" onClick={onDismissResult}>
                            Continue shopping
                        </button>
                    </div>
                )}

                {submitStatus === 'rejected' && (
                    <div className="cart-result" style={{ background: '#F7E6E1', border: '1px solid var(--color-out-stock)' }}>
                        <p className="cart-result__title" style={{ color: 'var(--color-out-stock)' }}>Order rejected</p>
                        <p className="cart-result__text">{resultMessage}</p>
                        <button type="button" className="state-panel__retry" onClick={onDismissResult}>
                            Continue shopping
                        </button>
                    </div>
                )}

                {!isFinalResult && isEmpty && (
                    <p className="cart-panel__empty">You haven't added any products yet.</p>
                )}

                {!isFinalResult && !isEmpty && (
                    <>
                        <ul className="cart-row-list">
                            {cartItems.map((entry) => (
                                <CartRow
                                    key={entry.product.id}
                                    entry={entry}
                                    onIncrement={onIncrement}
                                    onDecrement={onDecrement}
                                    onRemove={onRemove}
                                />
                            ))}
                        </ul>

                        {submitStatus === 'error' && (
                            <p className="cart-panel__error">
                                Failed to submit order. Please check your connection and try again.
                            </p>
                        )}

                        <div className="cart-panel__footer">
                            <div className="cart-panel__total">
                                <span>Total</span>
                                <span>{currencyFormatter.format(total)}</span>
                            </div>
                            <button
                                type="button"
                                className="cart-panel__submit"
                                onClick={onSubmit}
                                disabled={isSubmitting}
                            >
                                {isSubmitting ? 'Submitting order…' : 'Confirm order'}
                            </button>
                        </div>
                    </>
                )}
            </aside>
        </>
    )
}