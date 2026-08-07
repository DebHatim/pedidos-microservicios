import React from 'react'
import { useOrders } from '../hooks/useOrders.js'

export function OrderHistory() {
    const { orders, loading, error, refreshOrders } = useOrders()

    if (loading) {
        return <p>Cargando tu historial de pedidos...</p>
    }

    if (error) {
        return (
            <div>
                <p>Hubo un problema: {error}</p>
                <button onClick={refreshOrders}>Reintentar</button>
            </div>
        )
    }

    if (orders.length === 0) {
        return <p>Aún no has realizado ningún pedido.</p>
    }

    return (
        <div className="order-history">
            <h2>Historial de Pedidos</h2>

            <table style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
                <thead>
                <tr style={{ borderBottom: '2px solid #eee' }}>
                    <th>ID Pedido</th>
                    <th>Fecha</th>
                    <th>Estado</th>
                    <th>Total</th>
                </tr>
                </thead>
                <tbody>
                {orders.map((order) => {
                    const dateDisplay = order.date
                        ? new Date(order.date).toLocaleDateString()
                        : 'N/A'

                    return (
                        <tr key={order.id} style={{ borderBottom: '1px solid #eee' }}>
                            <td style={{ padding: '8px 0' }}>#{order.id}</td>
                            <td>{dateDisplay}</td>
                            <td>
                                <strong>{order.status || 'Desconocido'}</strong>
                            </td>
                            <td>${order.total?.toFixed(2)}</td>
                        </tr>
                    )
                })}
                </tbody>
            </table>
        </div>
    )
}