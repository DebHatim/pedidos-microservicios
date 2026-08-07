import { useState, useEffect } from 'react'
import { getOrders } from '../api.js'

export function useOrders() {
    const [orders, setOrders] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    const fetchOrders = async () => {
        setLoading(true)
        setError(null)
        try {
            const data = await getOrders()
            setOrders(data)
        } catch (err) {
            setError(err.message || 'Error desconocido al cargar pedidos')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchOrders()
    }, [])

    return { orders, loading, error, refreshOrders: fetchOrders }
}