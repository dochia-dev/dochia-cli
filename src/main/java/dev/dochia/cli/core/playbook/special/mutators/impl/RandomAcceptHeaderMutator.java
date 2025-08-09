package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.HeadersMutator;
import dev.dochia.cli.core.generator.Cloner;
import dev.dochia.cli.core.generator.simple.StringGenerator;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.util.CommonUtils;
import com.google.common.net.HttpHeaders;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Set;

/**
 * Send random values in the accept header.
 */
@Singleton
public class RandomAcceptHeaderMutator implements HeadersMutator {

    @Override
    public Collection<DochiaHeader> mutate(Collection<DochiaHeader> headers) {
        Set<DochiaHeader> clone = Cloner.cloneMe(headers);
        String randomValue = StringGenerator.getUnsupportedMediaTypes()
                .get(CommonUtils.random().nextInt(StringGenerator.getUnsupportedMediaTypes().size()));

        clone.add(DochiaHeader.builder()
                .name(HttpHeaders.ACCEPT)
                .value(randomValue)
                .build());
        return clone;
    }

    @Override
    public String description() {
        return "replace the accept header with random unsupported media types ";
    }
}
