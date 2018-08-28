package io.github.spair.jtgmerge;

import io.github.spair.jtgmerge.command.Clean;
import io.github.spair.jtgmerge.command.Convert;
import io.github.spair.jtgmerge.command.Merge;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.RunLast;

import java.util.Optional;

import static picocli.CommandLine.Command;

@Command(
        name = "JTGMerge",
        descriptionHeading = "%nDescription:%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        description = "CLI-based app for BYOND .dmm files with TGM support.",
        version = {"JTGMerge @|yellow v1.0|@\t(c) 2018", "Code is licensed under a MIT-style license"},
        subcommands = {Clean.class, Convert.class, Merge.class},
        mixinStandardHelpOptions = true)
public class JTGMerge implements Runnable {

    private static CommandLine CMD;

    public static void main(final String[] args) {
        CMD = new CommandLine(new JTGMerge());
        val results = CMD.parseWithHandlers(new RunLast(), CommandLine.defaultExceptionHandler().andExit(1), args);
        System.exit(results == null ? 0 : (int) Optional.ofNullable(results.get(0)).orElse(0));
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
