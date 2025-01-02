import java.util.concurrent.Semaphore;
import java.util.Random;

class ReadWriteLock {
    private Semaphore rwMutex = new Semaphore(1); // Semaphore to control access to the shared data for writers
    private Semaphore mutex = new Semaphore(1); // Semaphore to control access to the readCount variable
    private int readCount = 0; // Tracks the number of active readers
    private int sharedData = -1; // The shared data, initialized with a default value

    // Method for readers to acquire the read lock
    public void readLock(int readerId) {
        try {
            mutex.acquire(); // Ensure mutual exclusion while modifying readCount
            readCount++;
            if (readCount == 1) {
                rwMutex.acquire(); // The first reader locks access for writers
            }
            mutex.release(); // Release the mutex after updating readCount
            System.out.println("Reader " + readerId + " read: " + sharedData); // Log the read action
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
        }
    }

    // Method for readers to release the read lock
    public void readUnlock(int readerId) {
        try {
            mutex.acquire(); // Ensure mutual exclusion while modifying readCount
            readCount--;
            if (readCount == 0) {
                rwMutex.release(); // The last reader unlocks access for writers
            }
            mutex.release(); // Release the mutex after updating readCount
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
        }
    }

    // Method for writers to acquire the write lock
    public void writeLock(int writerId) {
        try {
            rwMutex.acquire(); // Writers need exclusive access to the shared data
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
        }
    }

    // Method for writers to release the write lock
    public void writeUnlock(int writerId) {
        rwMutex.release(); // Writers release exclusive access to the shared data
    }

    // Method for writers to update the shared data
    public void writeData(int writerId) {
        sharedData = new Random().nextInt(2000); // Generate a random value for the shared data
        System.out.println("Writer " + writerId + " writing: " + sharedData); // Log the write action
    }
}

public class Main {
    public static void main(String[] args) {
        ReadWriteLock lock = new ReadWriteLock(); // Create an instance of ReadWriteLock to manage synchronization
        final boolean[] running = {true}; // A shared flag to control the lifecycle of threads

        // Define the behavior of reader threads
        Runnable reader = () -> {
            int readerId = Integer.parseInt(Thread.currentThread().getName().split(" ")[1]);
            while (running[0]) { // Keep reading as long as the running flag is true
                lock.readLock(readerId); // Acquire the read lock
                try {
                    Thread.sleep(200); // Simulate time spent reading the data
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
                }
                lock.readUnlock(readerId); // Release the read lock
                try {
                    Thread.sleep(300); // Pause before attempting to read again
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
                }
            }
        };

        // Define the behavior of writer threads
        Runnable writer = () -> {
            int writerId = Integer.parseInt(Thread.currentThread().getName().split(" ")[1]);
            while (running[0]) { // Keep writing as long as the running flag is true
                System.out.println("Writer " + writerId + " waiting in queue."); // Log the waiting status
                lock.writeLock(writerId); // Acquire the write lock
                lock.writeData(writerId); // Update the shared data
                try {
                    Thread.sleep(500); // Simulate time spent writing the data
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
                }
                System.out.println("Writer " + writerId + " finished writing"); // Log the completion of the write action
                lock.writeUnlock(writerId); // Release the write lock
                try {
                    Thread.sleep(300); // Pause before attempting to write again
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
                }
            }
        };

        Thread[] readers = new Thread[3]; // Create an array to hold reader threads
        Thread[] writers = new Thread[2]; // Create an array to hold writer threads

        // Initialize and start reader threads
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new Thread(reader, "Reader " + (i + 1)); // Assign unique names to readers
        }

        // Initialize and start writer threads
        for (int i = 0; i < writers.length; i++) {
            writers[i] = new Thread(writer, "Writer " + (i + 1)); // Assign unique names to writers
        }

        for (Thread t : readers) t.start(); // Start all reader threads
        for (Thread t : writers) t.start(); // Start all writer threads

        try {
            Thread.sleep(10000); // Let the program run for 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
        }

        running[0] = false; // Signal all threads to stop

        // Wait for all reader threads to finish
        for (Thread t : readers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Wait for all writer threads to finish
        for (Thread t : writers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Program has stopped."); // Log the termination of the program
    }
}