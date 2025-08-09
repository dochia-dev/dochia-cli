package dev.dochia.cli.core.playbook.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks playbooks which will run after first phase playbooks. Second phase
 * playbooks need input produced by the first phase playbooks, this being the
 * reason for running after.
 * Unless explicitly specified otherwise, a Playbook is considered to run in
 * the first phase by default.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StatefulPlaybook {
}
