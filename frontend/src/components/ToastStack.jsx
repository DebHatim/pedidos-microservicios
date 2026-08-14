export default function ToastStack({ toasts, onDismiss }) {
    if (toasts.length === 0) return null

    return (
        <div className="toast-stack" role="status" aria-live="polite">
            {toasts.map((toast) => (
                <div key={toast.id} className={`toast toast--${toast.variant}`}>
                    <p className="toast__text">{toast.text}</p>
                    <button
                        type="button"
                        className="toast__close"
                        onClick={() => onDismiss(toast.id)}
                        aria-label="Close notification"
                    >
                        ×
                    </button>
                </div>
            ))}
        </div>
    )
}