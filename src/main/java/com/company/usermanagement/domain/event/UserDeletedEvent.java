package com.company.usermanagement.domain.event;

import com.company.usermanagement.domain.valueobject.Email;
import com.company.usermanagement.domain.valueobject.UserId;

/** Se emite cuando un usuario es eliminado (borrado lógico). */
public final class UserDeletedEvent extends DomainEvent {

    private final UserId userId;
    private final Email  email;
    private final UserId deletedByUserId;

    public UserDeletedEvent(UserId userId, Email email, UserId deletedByUserId) {
        super();
        this.userId          = userId;
        this.email           = email;
        this.deletedByUserId = deletedByUserId;
    }

    public UserId getUserId()          { return userId; }
    public Email  getEmail()           { return email; }
    public UserId getDeletedByUserId() { return deletedByUserId; }

    @Override public String getEventType() { return "user.deleted"; }
}
