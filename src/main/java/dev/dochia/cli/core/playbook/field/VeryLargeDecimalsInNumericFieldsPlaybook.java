package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.generator.simple.NumberGenerator;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.report.TestCaseListener;
import jakarta.inject.Singleton;

/**
 * Playbook that sends very large decimals in numeric fields. Size of the large
 * decimals is controlled by the {@code --largeStringsSize} argument.
 */
@Singleton
@FieldPlaybook
public class VeryLargeDecimalsInNumericFieldsPlaybook extends VeryLargeIntegersInNumericFieldsPlaybook {

    /**
     * Creates a new VeryLargeDecimalsInNumericFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     * @param pa to get the size of the large decimals
     */
    public VeryLargeDecimalsInNumericFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp, ProcessingArguments pa) {
        super(sc, lr, cp, pa);
    }

    @Override
    public String getTheActualData() {
        return NumberGenerator.generateVeryLargeDecimal(processingArguments.getLargeStringsSize() / 4);
    }
}
