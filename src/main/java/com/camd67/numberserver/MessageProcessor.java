package com.camd67.numberserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Manages processing messages for a single client connection.
 * Once this is done processing messages it will return to the caller.
 */
public class MessageProcessor implements Callable<MessageProcessor.Result> {

    public enum Result {
        TERMINATE,
        CONTINUE,
        BAD_REQUEST,
        ;
    }

    private final Socket clientSocket;
    private final NumberAggregator numberAggregator;

    /**
     * Pattern to match and validate the client input is formatted with:
     * - only digits
     * - exactly 9 characters long
     */
    private final Pattern validationPattern = Pattern.compile("[0-9]{9}");

    public MessageProcessor(Socket clientSocket, NumberAggregator numberAggregator) {
        this.clientSocket = clientSocket;
        this.numberAggregator = numberAggregator;
    }

    private boolean validateNumber(String number) {
        return validationPattern.matcher(number).matches();
    }

    @Override
    public Result call() throws IOException {
        try (
                var input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            var inputLine = input.readLine();
            while (inputLine != null) {
                if (inputLine.equals("terminate")) {
                    return Result.TERMINATE;
                } else if (!validateNumber(inputLine)) {
                    return Result.BAD_REQUEST;
                }

                numberAggregator.acceptNumber(inputLine);

                inputLine = input.readLine();
            }
            return Result.CONTINUE;
        } finally {
            clientSocket.close();
        }
    }
}

