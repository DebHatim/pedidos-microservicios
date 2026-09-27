import { useEffect, useRef, useState } from 'react'
import { createStompClient } from '../ws.js'

// Hook that listens for notifications and exposes them
export function useNotifications(onNotification) {
    const [toasts, setToasts] = useState([])
    const idCounter = useRef(0)

    // Reference to keep the callback always updated without reconnecting the subscription
    const onNotificationRef = useRef(onNotification)
    useEffect(() => {
        onNotificationRef.current = onNotification
    }, [onNotification])

    useEffect(() => {
        const client = createStompClient(() => {
            client.subscribe('/topic/notifications', (message) => {
                const notification = JSON.parse(message.body)
                const isConfirmed =
                    notification.message.toLowerCase().includes('confirmed') ||
                    notification.message.toLowerCase().includes('confirmado')

                const toast = {
                    id: idCounter.current++,
                    orderId: notification.orderId,
                    text: notification.message,
                    variant: isConfirmed ? 'success' : 'error'
                }

                setToasts((current) => [...current, toast])

                // We call the updated reference
                if (onNotificationRef.current) {
                    onNotificationRef.current(notification)
                }

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