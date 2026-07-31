import CartRow from './CartRow.jsx'

const currencyFormatter = new Intl.NumberFormat('es-ES', {
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

            <aside className="cart-panel" role="dialog" aria-label="Carrito de pedido">
                <div className="cart-panel__header">
                    <h2>Tu pedido</h2>
                    <button type="button" className="cart-panel__close" onClick={onClose} aria-label="Cerrar carrito">
                        ×
                    </button>
                </div>

                {submitStatus === 'waiting' && (
                    <div className="cart-result cart-result--success">
                        <p className="cart-result__title">Pedido enviado</p>
                        <p className="cart-result__text">
                            Se está comprobando el stock disponible. Recibirás la confirmación en tiempo real
                            en cuanto inventory-service procese el pedido.
                        </p>
                    </div>
                )}

                {submitStatus === 'confirmed' && (
                    <div className="cart-result cart-result--success">
                        <p className="cart-result__title">Pedido confirmado</p>
                        <p className="cart-result__text">{resultMessage}</p>
                        <button type="button" className="state-panel__retry" onClick={onDismissResult}>
                            Seguir comprando
                        </button>
                    </div>
                )}

                {submitStatus === 'rejected' && (
                    <div className="cart-result" style={{ background: '#F7E6E1', border: '1px solid var(--color-out-stock)' }}>
                        <p className="cart-result__title" style={{ color: 'var(--color-out-stock)' }}>Pedido rechazado</p>
                        <p className="cart-result__text">{resultMessage}</p>
                        <button type="button" className="state-panel__retry" onClick={onDismissResult}>
                            Seguir comprando
                        </button>
                    </div>
                )}

                {!isFinalResult && isEmpty && (
                    <p className="cart-panel__empty">Todavía no has añadido ningún producto.</p>
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
                                No se ha podido enviar el pedido. Comprueba la conexión e inténtalo de nuevo.
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
                                {isSubmitting ? 'Enviando pedido…' : 'Confirmar pedido'}
                            </button>
                        </div>
                    </>
                )}
            </aside>
        </>
    )
}