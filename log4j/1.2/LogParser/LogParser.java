import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.receivers.varia.LogFilePatternReceiver;

public class LogParser extends LogFilePatternReceiver {
    final static Logger logger = Logger.getLogger(LogParser.class);

    public void doPost(LoggingEvent event) {
        System.out.println(event.getTimeStamp());
        System.out.println(event.getMessage());
    }
    public static void main(String[] args) {
        (new LogParser()).run();
    }
    private void run(){
        LogFilePatternReceiver r = new LogParser();
        r.setLogFormat("TIMESTAMP LEVEL [THREAD] CLASS (FILE:LINE) - MESSAGE");
        r.setFileURL("file:///tmp/log");
        r.setTimestampFormat("yyyy-MM-dd HH:mm:ss,SSS");
        r.setTailing(true);
        r.setLoggerRepository(logger.getLoggerRepository());

        r.activateOptions();
    }
}
