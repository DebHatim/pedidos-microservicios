import { useEffect, useState } from 'react'
import { getSystemHealth } from '../api.js'

export default function SystemStatus() {
    const [services, setServices] = useState([])

    useEffect(() => {
        let cancelled = false

        async function poll() {
            const result = await getSystemHealth()
            if (!cancelled) setServices(result)
        }

        poll()
        const interval = setInterval(poll, 10000)

        return () => {
            cancelled = true
            clearInterval(interval)
        }
    }, [])

    if (services.length === 0) return null

    return (
        <div className="system-status">
            <span className="system-status__label">Live system status</span>
            <div className="system-status__list">
                {services.map((service) => (
                    <span key={service.name} className="system-status__item">
                        <span className={`system-status__dot system-status__dot--${service.status}`} />
                        {service.name}
                    </span>
                ))}
            </div>
        </div>
    )
}