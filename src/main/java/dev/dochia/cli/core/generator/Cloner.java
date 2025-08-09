package dev.dochia.cli.core.generator;

import dev.dochia.cli.core.model.DochiaHeader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Used to clone specific objects
 */
public class Cloner {

    private Cloner() {
        //ntd
    }

    /**
     * Creates a new set containing cloned copies of the provided collection of DochiaHeader objects.
     *
     * @param items The collection of DochiaHeader objects to clone.
     * @return A new set containing cloned copies of the provided DochiaHeader objects.
     */
    public static Set<DochiaHeader> cloneMe(Collection<DochiaHeader> items) {
        Set<DochiaHeader> clones = new HashSet<>();

        for (DochiaHeader t : items) {
            clones.add(t.copy());
        }

        return clones;
    }
}
