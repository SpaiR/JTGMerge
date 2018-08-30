package io.github.spair.jtgmerge;

import io.github.spair.jtgmerge.command.Clean;
import io.github.spair.jtgmerge.command.Convert;
import io.github.spair.jtgmerge.command.Merge;
import picocli.CommandLine;
import picocli.CommandLine.RunLast;

import static picocli.CommandLine.Command;

@Command(
        name = "JTGMerge",
        descriptionHeading = "%nDescription:%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        description =
                "CLI-based app for .dmm files which vastly improves mapping experience.%n"
              + "Able to work with BYOND format as well as with TGM.",
        version = {"JTGMerge @|yellow v1.0|@\t(c) 2018", "Code is licensed under a MIT-style license"},
        subcommands = {Clean.class, Convert.class, Merge.class},
        mixinStandardHelpOptions = true)
public class JTGMerge implements Runnable {

    private static CommandLine CMD;

    public static void main(final String[] args) {
        CMD = new CommandLine(new JTGMerge());
        CMD.parseWithHandlers(new RunLast().andExit(0), CommandLine.defaultExceptionHandler().andExit(1), args);
    }

    @Override
    public void run() {
        if (!CMD.getParseResult().hasSubcommand()) {
            CMD.printVersionHelp(System.out);
            System.out.println();
            CMD.usage(System.out);
        }
    }
}
