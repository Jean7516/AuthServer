package com.company.usermanagement.domain.event;

import com.company.usermanagement.domain.valueobject.RoleId;
import com.company.usermanagement.domain.valueobject.UserId;

/** Se emite cuando un rol es revocado de un usuario. */
public final class RoleRevokedEvent extends DomainEvent {

    private final UserId userId;
    private final RoleId roleId;
    private final String roleName;

    public RoleRevokedEvent(UserId userId, RoleId roleId, String roleName) {
        super();
        this.userId   = userId;
        this.roleId   = roleId;
        this.roleName = roleName;
    }

    public UserId getUserId() { return userId; }
    public RoleId getRoleId() { return roleId; }
    public String getRoleName() { return roleName; }

    @Override public String getEventType() { return "role.revoked"; }
}
