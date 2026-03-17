package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.HeadersMutator;
import dev.dochia.cli.core.generator.Cloner;
import dev.dochia.cli.core.model.DochiaHeader;
import com.google.common.net.HttpHeaders;
import dev.dochia.cli.core.util.DochiaRandom;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Set;

/**
 * Sends dummy values in the transfer encoding header
 */
@Singleton
public class RandomTransferEncodingHeaderMutator implements HeadersMutator {
    @Override
    public Collection<DochiaHeader> mutate(Collection<DochiaHeader> headers) {
        Set<DochiaHeader> clone = Cloner.cloneMe(headers);
        clone.add(DochiaHeader.builder()
                .name(HttpHeaders.TRANSFER_ENCODING)
                .value(DochiaRandom.next(10))
                .build());
        return clone;
    }

    @Override
    public String description() {
        return "replace the transfer encoding header with random values";
    }
}
