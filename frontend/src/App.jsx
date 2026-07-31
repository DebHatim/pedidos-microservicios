import { useEffect, useMemo, useState } from 'react'
import { fetchProducts } from './api.js'
import { useCart } from './hooks/useCart.js'
import Header from './components/Header.jsx'
import CategoryFilter from './components/CategoryFilter.jsx'
import ProductGrid from './components/ProductGrid.jsx'
import ProductGridSkeleton from './components/ProductGridSkeleton.jsx'
import StatePanel from './components/StatePanel.jsx'
import CartButton from './components/CartButton.jsx'
import CartPanel from './components/CartPanel.jsx'

export default function App() {
    const [products, setProducts] = useState([])
    const [status, setStatus] = useState('loading') // loading | success | error
    const [selectedCategory, setSelectedCategory] = useState('ALL')
    const [reloadKey, setReloadKey] = useState(0)

    const cart = useCart()
    const cartQuantities = useMemo(
        () => new Map(cart.cartItems.map((entry) => [entry.product.id, entry.quantity])),
        [cart.cartItems]
    )

    useEffect(() => {
        let cancelled = false

        setStatus('loading')

        fetchProducts()
            .then((data) => {
                if (cancelled) return
                setProducts(data)
                setStatus('success')
            })
            .catch(() => {
                if (cancelled) return
                setStatus('error')
            })

        return () => {
            cancelled = true
        }
    }, [reloadKey])

    const categories = useMemo(() => {
        const counts = new Map()
        products.forEach((product) => {
            counts.set(product.category, (counts.get(product.category) || 0) + 1)
        })

        return {
            total: products.length,
            list: Array.from(counts.entries())
                .map(([name, count]) => ({ name, count }))
                .sort((a, b) => a.name.localeCompare(b.name))
        }
    }, [products])

    const visibleProducts = useMemo(() => {
        if (selectedCategory === 'ALL') return products
        return products.filter((product) => product.category === selectedCategory)
    }, [products, selectedCategory])

    return (
        <div className="app-shell">
            <Header />

            {status === 'success' && (
                <CategoryFilter
                    categories={categories}
                    selected={selectedCategory}
                    onSelect={setSelectedCategory}
                />
            )}

            {status === 'loading' && <ProductGridSkeleton />}

            {status === 'error' && (
                <StatePanel
                    title="No se ha podido cargar el catálogo"
                    text="Hubo un error comunicando con el servidor, intentelo de nuevo."
                    onRetry={() => setReloadKey((key) => key + 1)}
                />
            )}

            {status === 'success' && visibleProducts.length === 0 && (
                <StatePanel
                    title="Sin productos en esta categoría"
                    text="Prueba a seleccionar otra categoría o vuelve a ver el catálogo completo."
                />
            )}

            {status === 'success' && visibleProducts.length > 0 && (
                <ProductGrid
                    products={visibleProducts}
                    cartQuantities={cartQuantities}
                    onAddToCart={cart.addToCart}
                />
            )}

            <CartButton itemCount={cart.itemCount} onClick={() => cart.setIsOpen(true)} />

            <CartPanel
                isOpen={cart.isOpen}
                onClose={() => cart.setIsOpen(false)}
                cartItems={cart.cartItems}
                total={cart.total}
                submitStatus={cart.submitStatus}
                resultMessage={cart.resultMessage}
                onIncrement={cart.increment}
                onDecrement={cart.decrement}
                onRemove={cart.removeItem}
                onSubmit={cart.submitOrder}
                onDismissResult={() => {
                    cart.setSubmitStatus('idle')
                    cart.setIsOpen(false)
                }}
            />
        </div>
    )
}