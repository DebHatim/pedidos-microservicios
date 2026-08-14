export default function StatePanel({ title, text, onRetry }) {
    return (
        <div className="state-panel">
            <p className="state-panel__title">{title}</p>
            <p className="state-panel__text">{text}</p>
            {onRetry && (
                <button type="button" className="state-panel__retry" onClick={onRetry}>
                    Retry
                </button>
            )}
        </div>
    )
}