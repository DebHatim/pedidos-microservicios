import React from 'react'
import { useOrders } from '../hooks/useOrders.js'
import StatePanel from './StatePanel.jsx'

export function OrderHistory() {
    const { orders, loading, error, refreshOrders } = useOrders()

    if (loading) {
        return (
            <StatePanel title="Cargando historial..." text="Obteniendo la lista de tus pedidos recientes."/>
        )
    }

    if (error) {
        return (
            <StatePanel title="Error al cargar pedidos" text={`Hubo un problema: ${error}`} onRetry={refreshOrders}/>
        )
    }

    if (!orders || orders.length === 0) {
        return (
            <StatePanel title="Sin pedidos registrados" text="Aún no has realizado ningún pedido en la tienda."/>
        )
    }

    return (
        <div className="order-history-panel">
            <h2 className="order-history-title">Historial de Pedidos</h2>

            <div className="order-history-table-wrapper">
                <table className="order-history-table">
                    <thead>
                    <tr>
                        <th>ID Pedido</th>
                        <th>Estado</th>
                        <th>Total</th>
                    </tr>
                    </thead>
                    <tbody>
                    {orders.map((order) => {
                        return (
                            <tr key={order.id}>
                                <td className="order-id">#{order.id}</td>
                                <td>
                                        <span className="order-status-badge">
                                            {order.status || 'Desconocido'}
                                        </span>
                                </td>
                                <td className="order-total">${order.total?.toFixed(2)}</td>
                            </tr>
                        )
                    })}
                    </tbody>
                </table>
            </div>
        </div>
    )
}