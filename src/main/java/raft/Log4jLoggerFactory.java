package raft;

import net.data.technology.jraft.Logger;
import net.data.technology.jraft.LoggerFactory;
import org.apache.log4j.LogManager;
import raft.Log4jLogger;

public class Log4jLoggerFactory implements LoggerFactory {
    public Logger getLogger(Class<?> clazz) {
        return new Log4jLogger(LogManager.getLogger(clazz));
    }

}
