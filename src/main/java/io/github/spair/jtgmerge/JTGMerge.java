package io.github.spair.jtgmerge;

import io.github.spair.jtgmerge.command.CleanMap;
import io.github.spair.jtgmerge.command.ConvertMap;
import picocli.CommandLine;

import static picocli.CommandLine.RunAll;
import static picocli.CommandLine.Command;

@Command(
        name = "JTGMerge",
        description = "Cli-based app for BYOND .dmm files with TGM support.",
        version = {"JTGMerge @|yellow v1.0|@\t(c) 2018", "Code is licensed under a MIT-style license"},
        subcommands = {CleanMap.class, ConvertMap.class},
        mixinStandardHelpOptions = true
)
public class JTGMerge implements Runnable {

    private static CommandLine CMD;

    public static void main(final String[] args) {
        CMD = new CommandLine(new JTGMerge());
        CMD.parseWithHandlers(new RunAll().andExit(0), CommandLine.defaultExceptionHandler().andExit(1));
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
