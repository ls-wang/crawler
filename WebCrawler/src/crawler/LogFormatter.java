package crawler;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

  private static final MessageFormat fmt = new MessageFormat("{0,date,yyyy-MM-dd HH:mm:ss} {1} [{2}] {3}\n");

  public LogFormatter() {
    super();
  }

  @Override public String format(LogRecord record) {
    Object[] args = new Object[5];
    args[0] = new Date(record.getMillis());
    args[1] = record.getLevel();
    args[2] = record.getLoggerName() == null ? "root" : record.getLoggerName();
    args[3] = record.getMessage();
    return fmt.format(args);
  }

}