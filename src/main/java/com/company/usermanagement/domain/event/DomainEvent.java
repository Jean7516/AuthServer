package com.company.usermanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base de todos los eventos de dominio.
 *
 * <p>Un evento de dominio representa algo que ocurrió en el pasado y es relevante
 * para el negocio. Son inmutables. El nombre debe estar en pasado: {@code UserCreated},
 * no {@code CreateUser}.
 *
 * <p>La capa de aplicación los publica vía {@code ApplicationEventPublisher} de Spring,
 * lo que desacopla el dominio de cualquier mecanismo de mensajería.
 */
public abstract sealed class DomainEvent
    permits UserCreatedEvent, UserDeletedEvent, RoleAssignedEvent, RoleRevokedEvent {

    private final UUID    eventId;
    private final Instant occurredOn;

    protected DomainEvent() {
        this.eventId    = UUID.randomUUID();
        this.occurredOn = Instant.now();
    }

    public UUID    getEventId()    { return eventId; }
    public Instant getOccurredOn() { return occurredOn; }

    /** Nombre semántico del evento. Convención: <Aggregate>.<verbo_pasado> */
    public abstract String getEventType();
}
