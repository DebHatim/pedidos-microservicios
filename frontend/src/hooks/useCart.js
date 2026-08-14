import {useEffect, useMemo, useRef, useState} from 'react'
import {createOrder, getOrder} from '../api.js'
import {createStompClient} from "../ws.js";

// The cart lives as Map<productId, { product, quantity }>
export function useCart() {
    const [items, setItems] = useState(new Map())
    const [isOpen, setIsOpen] = useState(false)
    const [submitStatus, setSubmitStatus] = useState('idle') // idle | submitting | waiting | confirmed | rejected | error
    const [resultMessage, setResultMessage] = useState('')

    const pendingOrderIdRef = useRef(null)

    // Single STOMP connection for the entire session, subscribes to order notifications
    useEffect(() => {
        const client = createStompClient(() => {
            client.subscribe('/topic/notifications', (message) => {
                const notification = JSON.parse(message.body)

                if (notification.orderId !== pendingOrderIdRef.current) return

                const isConfirmed = notification.message.toLowerCase().includes('confirmed')
                setSubmitStatus(isConfirmed ? 'confirmed' : 'rejected')
                setResultMessage(notification.message)
                pendingOrderIdRef.current = null
            })
        })

        client.onWebSocketClose = () => {
            if (pendingOrderIdRef.current) {
                setSubmitStatus('error')
                setResultMessage('The connection to the server was lost. We were unable to confirm the order.')
                pendingOrderIdRef.current = null
            }
        }

        return () => {
            client.deactivate()
        }
    }, []);

    // useEffect for the fallback
    useEffect(() => {
        // If we're not expecting an order, we don't do anything.
        if (submitStatus !== 'waiting' || !pendingOrderIdRef.current) return;

        let pollInterval;

        // We start an 8-second timer
        const timeoutId = setTimeout(() => {
            console.log('STOMP taking a long time, starting polling...');

            // When the 8s are finished, we launch the setInterval every 3s
            pollInterval = setInterval(async () => {
                try {
                    const orderId = pendingOrderIdRef.current;
                    if (!orderId) {
                        clearInterval(pollInterval);
                        return;
                    }

                    // We make a GET request to the backend
                    const order = await getOrder(orderId);

                    // If the status is no longer PENDING, we update the status and clear the polling.
                    if (order.status !== 'PENDING') {
                        const isConfirmed = order.status === 'CONFIRMED';

                        setSubmitStatus(isConfirmed ? 'confirmed' : 'rejected');
                        setResultMessage(`Order ${isConfirmed ? 'confirmed' : 'rejected'}`);
                        pendingOrderIdRef.current = null;

                        clearInterval(pollInterval);
                    }
                } catch (error) {
                    console.error("Error during polling:", error);
                    // setSubmitStatus('error') if fails
                }
            }, 3000);

        }, 8000);

        return () => {
            clearTimeout(timeoutId);
            if (pollInterval) {
                clearInterval(pollInterval); // Cancel polling
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