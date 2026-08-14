export default function CartButton({ itemCount, onClick }) {
    return (
        <button type="button" className="cart-button" onClick={onClick} aria-label="Open cart">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path
                    d="M3 4h2l1.6 10.6a2 2 0 0 0 2 1.7h8.6a2 2 0 0 0 2-1.6L21 8H6"
                    stroke="currentColor"
                    strokeWidth="1.8"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                />
                <circle cx="10" cy="20" r="1.4" fill="currentColor" />
                <circle cx="17" cy="20" r="1.4" fill="currentColor" />
            </svg>
            <span>Order</span>
            {itemCount > 0 && <span className="cart-button__badge">{itemCount}</span>}
        </button>
    )
}