package org.minjulog.feedserver.infra.messaging;

import java.security.Principal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StompPrincipal implements Principal {
    private final long userId;
    private final String name;
}
