const STEPS = [
    'Order created (PENDING) — persisted in MySQL',
    'Event order-created published to Kafka',
    'inventory-service evaluates and deducts stock',
    'Event order-evaluated published to Kafka',
    'Real-time confirmation pushed via WebSocket/STOMP'
]

export default function OrderFlowTimeline() {
    return (
        <ol className="order-flow-timeline">
            {STEPS.map((step, index) => (
                <li key={step} style={{ animationDelay: `${index * 0.15}s` }}>
                    {step}
                </li>
            ))}
        </ol>
    )
}