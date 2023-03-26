package com.camd67.numberserver;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Server application listening for messages
 * from a client socket connection.
 */
public class App {
    public static final int PORT = 4000;
    public static final int NUM_CONNECTIONS = 3;

    public static void main(String[] args) throws IOException {
        new App().run();
    }

    private final ListeningExecutorService socketProcessingPool = MoreExecutors.listeningDecorator(
        Executors.newFixedThreadPool(NUM_CONNECTIONS)
    );
    private final ScheduledExecutorService reporterService = Executors.newSingleThreadScheduledExecutor();
    private final NumberAggregator numberAggregator = new NumberAggregator();

    /**
     * This is accessed across multiple threads during shutdown operations.
     * We want to make sure the main thread sees this value correctly.
     * This will only flip to true. It never goes back to false.
     */
    private volatile boolean terminationRequested;

    public App() throws IOException {
    }

    /**
     * Coordinates and starts all the tasks our app needs to handle.
     */
    public void run() throws IOException {
        startReporter();
        startSocketListeners();
    }

    private void shutdown() throws IOException {
        // Close everything down.
        // Careful! Missing even a single service here results in a hung java process
        numberAggregator.close();
        reporterService.shutdownNow();
        socketProcessingPool.shutdownNow();
    }

    /**
     * Starts the reporter service which handles printing notifications to the terminal
     */
    private void startReporter() {
        reporterService.scheduleAtFixedRate(
            new Reporter(),
            0,
            10,
            TimeUnit.SECONDS
        );
    }

    /**
     * Starts the socket listeners.
     * This call is blocking! Make sure it comes last.
     */
    private void startSocketListeners() throws IOException {
        try (var server = new ServerSocket(PORT)) {
            while (true) {
                var clientSocket = server.accept();
                var future = socketProcessingPool.submit(new MessageProcessor(clientSocket, numberAggregator));
                // Connect up callbacks for when the message processor finishes
                // This way we can notice when a client requests for termination
                // of the server.
                // Also helps us catch failures and gracefully report them with context.
                Futures.addCallback(
                    future,
                    new FutureCallback<>() {
                        @Override
                        public void onSuccess(MessageProcessor.Result result) {
                            if (result == MessageProcessor.Result.TERMINATE) {
                                terminationRequested = true;
                                try {
                                    server.close();
                                } catch (IOException e) {
                                    // If we tried to shut down the server socket gracefully but that failed
                                    // we really don't have many other options here besides just exiting.
                                    System.err.println("Got an unexpected error closing server socket.");
                                    System.err.println("This isn't recoverable, shutting down forcefully.");
                                    System.exit(1);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            System.err.println("Failure noticed. Continuing processing but closing this connection");
                            System.err.println(throwable.getMessage());
                        }
                    },
                    socketProcessingPool
                );
            }
        } catch (SocketException ex) {
            // First check to see if this exception was caused due to someone requesting it.
            // If so it isn't really an error since we wanted this to happen.
            if (terminationRequested) {
                System.out.println("Shutting down server due to termination request");
            } else {
                // Otherwise it's happening for some other reason and we wanna know about it!
                throw ex;
            }
        } finally {
            shutdown();
        }
    }
}
