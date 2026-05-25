package com.company.usermanagement.domain.event;

import com.company.usermanagement.domain.valueobject.RoleId;
import com.company.usermanagement.domain.valueobject.UserId;

/** Se emite cuando un rol es asignado a un usuario. */
public final class RoleAssignedEvent extends DomainEvent {

    private final UserId userId;
    private final RoleId roleId;
    private final String roleName;
    private final UserId assignedByUserId;

    public RoleAssignedEvent(UserId userId, RoleId roleId, String roleName, UserId assignedByUserId) {
        super();
        this.userId           = userId;
        this.roleId           = roleId;
        this.roleName         = roleName;
        this.assignedByUserId = assignedByUserId;
    }

    public UserId getUserId()           { return userId; }
    public RoleId getRoleId()           { return roleId; }
    public String getRoleName()         { return roleName; }
    public UserId getAssignedByUserId() { return assignedByUserId; }

    @Override public String getEventType() { return "role.assigned"; }
}
