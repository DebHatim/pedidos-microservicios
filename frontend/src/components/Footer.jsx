const STACK = ['Java 21', 'Spring Boot 4.1', 'Kafka', 'MySQL', 'Redis', 'React 18', 'Docker', 'OpenTelemetry']

export default function Footer() {
    return (
        <footer className="site-footer">
            <div className="site-footer__stack">
                {STACK.map((tech) => (
                    <span key={tech} className="site-footer__badge">{tech}</span>
                ))}
            </div>
            <nav className="site-footer__links">
                <a href="http://localhost:8081/swagger-ui.html" target="_blank" rel="noreferrer">Orders API docs</a>
                <a href="http://localhost:8082/swagger-ui.html" target="_blank" rel="noreferrer">Inventory API docs</a>
                <a href="http://localhost:3000" target="_blank" rel="noreferrer">Grafana dashboard</a>
                <a href="http://localhost:16686" target="_blank" rel="noreferrer">Jaeger traces</a>
                <a href="https://github.com/DebHatim/pedidos-microservicios" target="_blank" rel="noreferrer">GitHub repo</a>
            </nav>
        </footer>
    )
}