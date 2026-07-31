export default function ProductGridSkeleton({ count = 8 }) {
    return (
        <div className="product-grid" aria-hidden="true">
            {Array.from({ length: count }).map((_, index) => (
                <div className="skeleton-card" key={index}>
                    <div className="skeleton-card__media" />
                    <div className="skeleton-card__body">
                        <div className="skeleton-line skeleton-line--title" />
                        <div className="skeleton-line" />
                        <div className="skeleton-line skeleton-line--short" />
                    </div>
                </div>
            ))}
        </div>
    )
}