import ProductCard from './ProductCard.jsx'

export default function ProductGrid({ products, cartQuantities, onAddToCart }) {
    return (
        <div className="product-grid">
            {products.map((product) => (
                <ProductCard
                    key={product.id}
                    product={product}
                    quantityInCart={cartQuantities.get(product.id) || 0}
                    onAdd={onAddToCart}
                />
            ))}
        </div>
    )
}