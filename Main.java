public class Main {
    public static void main(String[] args) {
        ReadWriteLock lock = new ReadWriteLock(); // Create an instance of ReadWriteLock to manage synchronization

        // A flag to control the lifecycle of threads
        final boolean[] running = {true};

        // Define the behavior for reader threads
        Runnable reader = () -> {
            // Extract the unique reader ID from the thread name
            int readerId = Integer.parseInt(Thread.currentThread().getName().split(" ")[1]);
            while (running[0]) { // Keep reading as long as the running flag is true
                lock.readLock(readerId); // Acquire the read lock to safely access shared data
                try {
                    Thread.sleep(200); // Simulate the time spent reading the data
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
                }
                lock.readUnlock(readerId); // Release the read lock after finishing
                try {
                    Thread.sleep(300); // Pause briefly before attempting to read again
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
                }
            }
        };

        // Define the behavior for writer threads
        Runnable writer = () -> {
            // Extract the unique writer ID from the thread name
            int writerId = Integer.parseInt(Thread.currentThread().getName().split(" ")[1]);
            while (running[0]) { // Keep writing as long as the running flag is true
                System.out.println("Writer " + writerId + " waiting in queue."); // Log that the writer is waiting for access
                lock.writeLock(writerId); // Acquire the write lock to safely update shared data
                lock.writeData(writerId); // Update the shared data with new random values
                try {
                    Thread.sleep(500); // Simulate the time spent writing the data
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
                }
                System.out.println("Writer " + writerId + " finished writing"); // Log that the writer has completed its task
                lock.writeUnlock(writerId); // Release the write lock after finishing
                try {
                    Thread.sleep(300); // Pause briefly before attempting to write again
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status if thread is interrupted
                }
            }
        };

        // Create arrays to hold reader and writer threads
        Thread[] readers = new Thread[3];
        Thread[] writers = new Thread[2];

        // Initialize and name reader threads
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new Thread(reader, "Reader " + (i + 1));
        }

        // Initialize and name writer threads
        for (int i = 0; i < writers.length; i++) {
            writers[i] = new Thread(writer, "Writer " + (i + 1));
        }

        // Start all reader threads
        for (Thread t : readers) t.start();

        // Start all writer threads
        for (Thread t : writers) t.start();

        // Let the program run for 10 seconds
        try {
            Thread.sleep(10000); // The main thread sleeps, allowing readers and writers to work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status if the main thread is interrupted
        }

        running[0] = false; // Signal all threads to stop by setting the running flag to false
    }
}
