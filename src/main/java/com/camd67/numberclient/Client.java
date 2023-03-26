package com.camd67.numberclient;

import com.camd67.numberserver.App;
import com.camd67.util.NumberGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Client that connects up to our server.
 * Can accept an arbitrary list of messages and waits between messages.
 */
public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 0 && args[0].equals("user")) {
            new Client().runAsUser();
        } else if (args.length > 0 && args[0].equals("infinite")) {
            new Client().runInfinite();
        } else if (args.length > 0 && args[0].equals("count")) {
            new Client().runUp(args[1]);
        } else {
            new Client().run();
        }
    }

    /**
     * The messages to send up to the server.
     * Usually these will be 9-digit numbers, but they could be anything.
     */
    private final List<String> messages;

    /**
     * Amount of time to wait in between sending numbers.
     */
    private final List<Integer> waits;


    public Client() {
        messages = new ArrayList<>();
        waits = new ArrayList<>();
        for (var i = 0; i < 10; i++) {
            messages.add(NumberGenerator.generate());
            waits.add(0);
        }
    }

    public Client(List<String> messages, List<Integer> waits) {
        if (waits.size() != messages.size()) {
            throw new IllegalArgumentException("Numbers and waits must be equal length");
        }

        this.messages = messages;
        this.waits = waits;
    }

    private void run() throws IOException, InterruptedException {
        try (
            var socket = new Socket("localhost", App.PORT);
            var output = new PrintWriter(socket.getOutputStream(), true);
        ) {
            for (var i = 0; i < messages.size(); i++) {
                output.println(messages.get(i));
                var wait = waits.get(i);
                if (wait > 0) {
                    // Since we're just waiting to put some spacing between calls
                    // no need to do fancier stuff
                    Thread.sleep(wait);
                }
            }
        }
    }

    private void runUp(String startInput) throws IOException {
        var current = Integer.parseInt(startInput);
        try (
            var socket = new Socket("localhost", App.PORT);
            var output = new PrintWriter(socket.getOutputStream(), true);
        ) {
            while (true) {
                if (current > 1_000_000_000) {
                    System.out.println("Reached max value");
                    return;
                }
                output.println(String.format("%09d", current));
                current++;
            }
        }
    }

    private void runInfinite() throws IOException {
        try (
            var socket = new Socket("localhost", App.PORT);
            var output = new PrintWriter(socket.getOutputStream(), true);
        ) {
            while (true) {
                output.println(NumberGenerator.generate());
            }
        }
    }

    private void runAsUser() throws IOException {
        System.out.println("Connecting...");
        try (
            var socket = new Socket("localhost", App.PORT);
            var output = new PrintWriter(socket.getOutputStream(), true);
            var stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
            System.out.println("Connected");
            var userInput = stdIn.readLine();
            while (userInput != null) {
                // Check for a closed connection
                // This won't happen immediately, instead it'll only happen after some time and user input but...
                // good enough for now.
                if (output.checkError()) {
                    System.err.println("Error writing to socket. Connection may have been closed.");
                    return;
                }

                output.println(userInput);

                if (userInput.equals("terminate")) {
                    return;
                }

                userInput = stdIn.readLine();
            }
        }
    }
}
