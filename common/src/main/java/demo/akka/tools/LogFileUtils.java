package demo.akka.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;


public class LogFileUtils {
  private final static String LOG_FILE = "logs/application-test.log";
  public final static Long timefactor =
      new Long(System.getProperty("akka.test.timefactor", "1"));


  private static boolean watch(String fileName, Predicate<String> predicate, long timeOut)
      throws IOException, InterruptedException {
    WatchService watcher = FileSystems.getDefault().newWatchService();
    Path pathFileName = Paths.get(fileName);
    if (!pathFileName.isAbsolute()) {
      pathFileName = Paths.get(fileName).toAbsolutePath();
    }

    Path dir = pathFileName.getParent();
    dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

    Long startTime = System.currentTimeMillis();

    try (BufferedReader input = new BufferedReader(new FileReader(pathFileName.toString()))) {
      StringBuilder sb = new StringBuilder();

      // read data in file until now
      input.lines().forEach(sb::append);

      // check for the condition for the data already present in the file
      if (predicate.test(sb.toString())) {
        watcher.close();
        return true;
      }

      // if condition is not satisfied wait for the specified timeout
      while (true) {
        if (System.currentTimeMillis() - startTime > timeOut) {
          watcher.close();
          return false;
        }

        WatchKey key = watcher.poll(1, TimeUnit.SECONDS);
        if (key == null) {
          continue;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
          @SuppressWarnings("unchecked")
          WatchEvent<Path> ev = (WatchEvent<Path>) event;
          Path eventFileName = ev.context();

          if (eventFileName.toString().equals(pathFileName.getFileName().toString())) {
            // append only new lines to the already read ones
            input.lines().forEach(sb::append);

            // check for the condition
            if (predicate.test(sb.toString())) {
              watcher.close();
              return true;
            }

          }
        }

        boolean valid = key.reset();
        if (!valid) {
          break;
        }
      }
      return false;
    }
  }

  /**
   * Returns true if the pattern appears in the log file within the specified amount of time(msec).
   *
   * @param pattern String pattern to look for in the log file
   * @param timeout in milliseconds
   * @return true if the pattern appears in the log file
   */
  public static boolean waitLog(String pattern, int timeout) {
    try {
      return watch(LOG_FILE, s -> s.contains(pattern), timeout * timefactor);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean waitLog(Predicate<String> predicate, int timeout) {
    try {
      return watch(LOG_FILE, predicate, timeout * timefactor);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void cleanLog() throws IOException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    try (RandomAccessFile file = new RandomAccessFile(LOG_FILE, "rws")) {
      file.setLength(0L);
      file.write(("(CleanLog: " + sdf.format(new Date()) + ")\n\r").getBytes());
      file.close();
    } catch (FileNotFoundException e) {
      System.out.println(LOG_FILE + ": no such file or directory");
    } catch (IOException exc) {
      System.out.println(exc.getMessage());
    }
  }

}
