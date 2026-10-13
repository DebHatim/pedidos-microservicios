import SystemStatus from './SystemStatus.jsx'

export default function Header() {
    return (
        <header className="site-header">
            <div className="site-header__top">
                <div>
                    <p className="site-header__eyebrow">Inventory · Ordering System</p>
                    <h1 className="site-header__title">Product catalog</h1>
                    <p className="site-header__subtitle">Real-time stock from the warehouse.</p>
                </div>
                <SystemStatus />
            </div>
        </header>
    )
}