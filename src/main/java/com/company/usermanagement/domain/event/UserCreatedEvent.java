package com.company.usermanagement.domain.event;

import com.company.usermanagement.domain.valueobject.Email;
import com.company.usermanagement.domain.valueobject.UserId;

/** Se emite cuando un nuevo usuario es registrado exitosamente en el sistema. */
public final class UserCreatedEvent extends DomainEvent {

    private final UserId userId;
    private final Email  email;

    public UserCreatedEvent(UserId userId, Email email) {
        super();
        this.userId = userId;
        this.email  = email;
    }

    public UserId getUserId() { return userId; }
    public Email  getEmail()  { return email; }

    @Override public String getEventType() { return "user.created"; }
}
