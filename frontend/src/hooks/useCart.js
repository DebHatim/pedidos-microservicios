import {useEffect, useMemo, useRef, useState} from 'react'
import {createOrder, getOrder} from '../api.js'
import {createStompClient} from "../ws.js";

// El carrito vive como Map<productId, { product, quantity }>
export function useCart() {
    const [items, setItems] = useState(new Map())
    const [isOpen, setIsOpen] = useState(false)
    const [submitStatus, setSubmitStatus] = useState('idle') // idle | submitting | waiting | confirmed | rejected | error
    const [resultMessage, setResultMessage] = useState('')

    const pendingOrderIdRef = useRef(null)

    // Conexion STOMP unica para toda la sesion, se suscribe a las notificaciones de los pedidos
    useEffect(() => {
        const client = createStompClient(() => {
            client.subscribe('/topic/notifications', (message) => {
                const notification = JSON.parse(message.body)

                if (notification.orderId !== pendingOrderIdRef.current) return

                const isConfirmed = notification.message.toLowerCase().includes('confirmado')
                setSubmitStatus(isConfirmed ? 'confirmed' : 'rejected')
                setResultMessage(notification.message)
                pendingOrderIdRef.current = null
            })
        })

        client.onWebSocketClose = (event) => {
            if (pendingOrderIdRef.current) {
                setSubmitStatus('error')
                setResultMessage('Se perdió la conexión con el servidor. No pudimos confirmar el pedido.')
                pendingOrderIdRef.current = null
            }
        }

        return () => {
            client.deactivate()
        }
    }, []);

    // useEffect para el fallback
    useEffect(() => {
        // Si no estamos esperando un pedido, no hacemos nada
        if (submitStatus !== 'waiting' || !pendingOrderIdRef.current) return;

        let pollInterval;

        // Lanzamos un temporizador de 8 segundos
        const timeoutId = setTimeout(() => {
            console.log('STOMP tardando mucho, iniciando polling...');

            // Cuando terminen los 8s, lanzamos el setInterval cada 3s
            pollInterval = setInterval(async () => {
                try {
                    const orderId = pendingOrderIdRef.current;
                    if (!orderId) {
                        clearInterval(pollInterval);
                        return;
                    }

                    // Hacemos GET al backend
                    const order = await getOrder(orderId);

                    // Si el estado ya no es PENDING, actualizamos el estado y limpiamos el polling
                    if (order.status !== 'PENDING') {
                        const isConfirmed = order.status === 'CONFIRMED';

                        setSubmitStatus(isConfirmed ? 'confirmed' : 'rejected');
                        setResultMessage(`Pedido ${isConfirmed ? 'confirmado' : 'rechazado'}`);
                        pendingOrderIdRef.current = null;

                        clearInterval(pollInterval);
                    }
                } catch (error) {
                    console.error("Error durante el polling:", error);
                    // setSubmitStatus('error') si falla
                }
            }, 3000);

        }, 8000);

        return () => {
            clearTimeout(timeoutId);
            if (pollInterval) {
                clearInterval(pollInterval); // Cancelar el polling
            }
        };
    }, [submitStatus]);

    function addToCart(product) {
        setItems((current) => {
            const next = new Map(current)
            const existing = next.get(product.id)
            const currentQuantity = existing ? existing.quantity : 0

            if (currentQuantity >= product.stock) return current

            next.set(product.id, { product, quantity: currentQuantity + 1 })
            return next
        })
        setSubmitStatus('idle')
        setIsOpen(true)
    }

    function increment(productId) {
        setItems((current) => {
            const entry = current.get(productId)
            if (!entry || entry.quantity >= entry.product.stock) return current

            const next = new Map(current)
            next.set(productId, { ...entry, quantity: entry.quantity + 1 })
            return next
        })
    }

    function decrement(productId) {
        setItems((current) => {
            const entry = current.get(productId)
            if (!entry) return current

            const next = new Map(current)
            if (entry.quantity <= 1) {
                next.delete(productId)
            } else {
                next.set(productId, { ...entry, quantity: entry.quantity - 1 })
            }
            return next
        })
    }

    function removeItem(productId) {
        setItems((current) => {
            const next = new Map(current)
            next.delete(productId)
            return next
        })
    }

    function clearCart() {
        setItems(new Map())
    }

    const cartItems = useMemo(() => Array.from(items.values()), [items])

    const itemCount = useMemo(
        () => cartItems.reduce((sum, entry) => sum + entry.quantity, 0),
        [cartItems]
    )

    const total = useMemo(
        () => cartItems.reduce((sum, entry) => sum + entry.product.price * entry.quantity, 0),
        [cartItems]
    )

    async function submitOrder() {
        if (cartItems.length === 0) return

        setSubmitStatus('submitting')

        const orderDTO = {
            total,
            items: cartItems.map((entry) => ({
                productId: entry.product.id,
                quantity: entry.quantity
            }))
        }

        try {
            const {orderId} = await createOrder(orderDTO)
            pendingOrderIdRef.current = orderId
            setSubmitStatus('waiting')
            clearCart()
        } catch {
            setSubmitStatus('error')
        }
    }

    return {
        cartItems,
        itemCount,
        total,
        isOpen,
        setIsOpen,
        submitStatus,
        setSubmitStatus,
        resultMessage,
        addToCart,
        increment,
        decrement,
        removeItem,
        submitOrder
    }
}