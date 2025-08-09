package dev.dochia.cli.core.playbook.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Additional marker for {@link FieldPlaybook} and {@link HeaderPlaybook} that are populating fuzzed data with Unicode Control Emojis.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EmojiPlaybook {
}
