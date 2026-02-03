package org.minjulog.feedserver.infra.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

@AllArgsConstructor
@Getter
public class StompPrincipal implements Principal {
    private final long userId;
    private final String name;
}
