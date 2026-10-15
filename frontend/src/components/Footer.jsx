const STACK = ['Java 21', 'Spring Boot 4.1', 'Kafka', 'MySQL', 'Redis', 'React 18', 'Docker', 'OpenTelemetry']

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const GRAFANA_URL = import.meta.env.VITE_GRAFANA_URL || 'http://localhost:3000'
const JAEGER_URL = import.meta.env.VITE_JAEGER_URL || 'http://localhost:16686'

export default function Footer() {
    return (
        <footer className="site-footer">
            <div className="site-footer__stack">
                {STACK.map((tech) => (
                    <span key={tech} className="site-footer__badge">{tech}</span>
                ))}
            </div>
            <nav className="site-footer__links">
                <a href={`${API_BASE_URL}/order-service/swagger-ui.html`} target="_blank" rel="noreferrer">Orders API docs</a>
                <a href={`${API_BASE_URL}/inventory-service/swagger-ui.html`} target="_blank" rel="noreferrer">Inventory API docs</a>
                <a href={GRAFANA_URL} target="_blank" rel="noreferrer">Grafana dashboard</a>
                <a href={JAEGER_URL} target="_blank" rel="noreferrer">Jaeger traces</a>
                <a href="https://github.com/DebHatim/pedidos-microservicios" target="_blank" rel="noreferrer">GitHub repo</a>
            </nav>
        </footer>
    )
}