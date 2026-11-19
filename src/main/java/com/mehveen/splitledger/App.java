package com.mehveen.splitledger;

import com.mehveen.splitledger.cli.Cli;
import com.mehveen.splitledger.io.LedgerStore;
import com.mehveen.splitledger.ledger.Ledger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

/**
 * Entry point.
 * <pre>
 *   java -jar splitledger.jar                         # interactive, empty ledger
 *   java -jar splitledger.jar trip.csv                # interactive, open a saved ledger
 *   java -jar splitledger.jar --script demo.txt       # run commands from a file
 * </pre>
 */
public final class App {

    private App() {
    }

    static void interactive(Cli cli) throws IOException {
        System.out.println("SplitLedger - split group expenses fairly. Type 'help' to begin.");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        while (true) {
            System.out.print("splitledger> ");
            String line = in.readLine();
            if (line == null || !cli.execute(line)) {
                break;
            }
        }
    }

    /** Runs every line of a script file through the CLI; handy for demos. */
    public static void runScript(Cli cli, Path script) throws IOException {
        for (String line : Files.readAllLines(script, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.strip().startsWith("#")) {
                continue;
            }
            System.out.println("splitledger> " + line);
            if (!cli.execute(line)) {
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Cli cli = new Cli(new Ledger(), System.out, Clock.systemDefaultZone());
        if (args.length == 2 && "--script".equals(args[0])) {
            runScript(cli, Path.of(args[1]));
            return;
        }
        if (args.length == 1 && Files.exists(Path.of(args[0]))) {
            cli = new Cli(new LedgerStore().load(Path.of(args[0])), System.out, Clock.systemDefaultZone());
        }
        interactive(cli);
    }
}
