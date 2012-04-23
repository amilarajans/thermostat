/*
 * Copyright 2012 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.cli;

import java.util.Arrays;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.logging.Level;

import com.redhat.thermostat.common.config.InvalidConfigurationException;
import com.redhat.thermostat.common.storage.ConnectionException;
import com.redhat.thermostat.common.utils.LoggingUtils;

public class Launcher {

    private String[] args;

    public void run(String[] args) {
        initLogging();
        this.args = args;
        registerDefaultCommands();
        if (hasNoArguments() || unknownCommand()) {
            runHelpCommand();
        } else {
            runCommandFromArguments();
        }
    }

    private void initLogging() {
        try {
            LoggingUtils.loadGlobalLoggingConfig();
        } catch (InvalidConfigurationException e) {
            System.err.println("WARNING: Could not read global Thermostat logging configuration.");
        }
        try {
            LoggingUtils.loadUserLoggingConfig();
        } catch (InvalidConfigurationException e) {
            // We intentionally ignore this.
        }
    }

    private boolean hasNoArguments() {
        return args.length == 0;
    }

    private boolean unknownCommand() {
        CommandContextFactory cmdCtxFactory = CommandContextFactory.getInstance();
        CommandRegistry registry = cmdCtxFactory.getCommandRegistry();
        Command cmd = registry.getCommand(args[0]);
        return cmd == null;
    }

    private void runHelpCommand() {
        runCommand("help", new String[0]);
    }

    private void runCommandFromArguments() {
        runCommand(args[0], Arrays.copyOfRange(args, 1, args.length));
    }

    private void runCommand(String cmdName, String[] cmdArgs) {
        try {
            parseArgsAndRunCommand(cmdName, cmdArgs);
        } catch (CommandException e) {
            CommandContextFactory cmdCtxFactory = CommandContextFactory.getInstance();
            cmdCtxFactory.getConsole().getError().println(e.getMessage());
        }
    }

    private void parseArgsAndRunCommand(String cmdName, String[] cmdArgs) throws CommandException {

        Command cmd = getCommand(cmdName);
        CommonCommandOptions commonOpts = new CommonCommandOptions();
        Collection<ArgumentSpec> acceptedOptions = commonOpts.getAcceptedOptionsFor(cmd);
        Arguments args = parseCommandArguments(cmdArgs, acceptedOptions);
        setupLogLevel(args);
        CommandContext ctx = setupCommandContext(cmd, args);
        cmd.run(ctx);
    }

    private void setupLogLevel(Arguments args) {
        if (args.hasArgument(CommonCommandOptions.LOG_LEVEL_ARG)) {
            String levelOption = args.getArgument(CommonCommandOptions.LOG_LEVEL_ARG);
            setLogLevel(levelOption);
        }
    }

    private void setLogLevel(String levelOption) {
        try {
            Level level = Level.parse(levelOption);
            LoggingUtils.setGlobalLogLevel(level);
        } catch (IllegalArgumentException ex) {
            // Ignore this, use default loglevel.
        }
    }

    private Command getCommand(String cmdName) {

        CommandContextFactory cmdCtxFactory = CommandContextFactory.getInstance();
        CommandRegistry registry = cmdCtxFactory.getCommandRegistry();
        Command cmd = registry.getCommand(cmdName);
        return cmd;
    }

    private Arguments parseCommandArguments(String[] cmdArgs, Collection<ArgumentSpec> acceptedArguments)
            throws CommandLineArgumentParseException {

        CommandLineArgumentsParser cliArgsParser = new CommandLineArgumentsParser();
        cliArgsParser.addArguments(acceptedArguments);
        Arguments args = cliArgsParser.parse(cmdArgs);
        return args;
    }

    private CommandContext setupCommandContext(Command cmd, Arguments args) throws CommandException {

        CommandContextFactory cmdCtxFactory = CommandContextFactory.getInstance();
        CommandContext ctx = cmdCtxFactory.createContext(args);
        if (cmd.isStorageRequired()) {
            String dbUrl = ctx.getArguments().getArgument(CommonCommandOptions.DB_URL_ARG);
            try {
                ctx.getAppContextSetup().setupAppContext(dbUrl);
            } catch (ConnectionException ex) {
                throw new CommandException("Could not connect to: " + dbUrl, ex);
            }
        }
        return ctx;
    }


    private void registerDefaultCommands() {
        CommandContextFactory cmdCtxFactory = CommandContextFactory.getInstance();
        CommandRegistry registry = cmdCtxFactory.getCommandRegistry();
        ServiceLoader<Command> cmds = ServiceLoader.load(Command.class);
        registry.registerCommands(cmds);
    }
}
