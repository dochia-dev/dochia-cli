package dev.dochia.cli.core.playbook.special.mutators.api;

import dev.dochia.cli.core.model.DochiaHeader;

import java.util.Collection;

/**
 * Marker interface for mutators that mutate the request body.
 */
public interface BodyMutator extends Mutator {


    @Override
    default Collection<DochiaHeader> mutate(Collection<DochiaHeader> headers) {
        return headers;
    }
}
