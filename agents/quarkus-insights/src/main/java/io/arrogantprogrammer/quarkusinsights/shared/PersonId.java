package io.arrogantprogrammer.quarkusinsights.shared;

import java.util.Objects;
import java.util.UUID;

/**
 * Shared kernel — opaque identifier for a {@code Person} owned by another bounded context.
 *
 * <p>Layer: domain (shared kernel). The Episodes subdomain references presenters and speakers
 * by this value object, never by a direct Person reference or a Person class from this
 * codebase. The People context will own the Person aggregate when it is added later; until
 * then, this subdomain trusts the IDs it is given and never dereferences them.
 *
 * @param value the underlying UUID; never {@code null}
 */
public record PersonId(UUID value) {

    /**
     * Canonical constructor enforcing non-null.
     *
     * @param value the underlying UUID
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public PersonId {
        Objects.requireNonNull(value, "PersonId value must not be null");
    }

    /**
     * Parse a string-form UUID into a {@code PersonId}.
     *
     * @param raw the canonical UUID string
     * @return a {@code PersonId} wrapping the parsed UUID
     * @throws NullPointerException if {@code raw} is {@code null}
     * @throws IllegalArgumentException if {@code raw} is not a valid UUID
     */
    public static PersonId of(String raw) {
        Objects.requireNonNull(raw, "PersonId raw value must not be null");
        return new PersonId(UUID.fromString(raw));
    }
}
