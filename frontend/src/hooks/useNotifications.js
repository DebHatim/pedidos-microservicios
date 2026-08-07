import { useEffect, useRef, useState } from 'react'
import { createStompClient } from '../ws.js'

// Hook que escucha las notificaciones y las expone
export function useNotifications() {
    const [toasts, setToasts] = useState([])
    const idCounter = useRef(0)

    useEffect(() => {
        const client = createStompClient(() => {
            client.subscribe('/topic/notifications', (message) => {
                const notification = JSON.parse(message.body)
                const isConfirmed = notification.message.toLowerCase().includes('confirmado')

                const toast = {
                    id: idCounter.current++,
                    orderId: notification.orderId,
                    text: notification.message,
                    variant: isConfirmed ? 'success' : 'error'
                }

                setToasts((current) => [...current, toast])

                setTimeout(() => {
                    setToasts((current) => current.filter((t) => t.id !== toast.id))
                }, 6000)
            })
        })

        return () => client.deactivate()
    }, [])

    function dismiss(id) {
        setToasts((current) => current.filter((t) => t.id !== id))
    }

    return { toasts, dismiss }
}