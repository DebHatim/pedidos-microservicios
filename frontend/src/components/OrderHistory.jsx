import React from 'react'
import { useOrders } from '../hooks/useOrders.js'
import StatePanel from './StatePanel.jsx'

export function OrderHistory() {
    const { orders, loading, error, refreshOrders } = useOrders()

    if (loading) {
        return (
            <StatePanel title="Loading history..." text="Fetching your recent orders list."/>
        )
    }

    if (error) {
        return (
            <StatePanel title="Error loading orders" text={`There was a problem: ${error}`} onRetry={refreshOrders}/>
        )
    }

    if (!orders || orders.length === 0) {
        return (
            <StatePanel title="No orders placed" text="You haven't placed any orders in the store yet."/>
        )
    }

    return (
        <div className="order-history-panel">
            <h2 className="order-history-title">Order History</h2>

            <div className="order-history-table-wrapper">
                <table className="order-history-table">
                    <thead>
                    <tr>
                        <th>Order ID</th>
                        <th>Status</th>
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
                                            {order.status || 'Unknown'}
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