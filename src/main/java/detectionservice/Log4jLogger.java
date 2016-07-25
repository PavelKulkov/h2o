package detectionservice;

import org.apache.log4j.Logger;

/**
 * Created by Pavel Kulkov  on 18.07.2016.
 */
public class Log4jLogger implements net.data.technology.jraft.Logger {

    private Logger logger;

    public Log4jLogger(Logger logger){
        this.logger = logger;
    }

    public void debug(String format, Object... args) {
        if(args != null){
            this.logger.debug(String.format(format, args));
        }else{
            this.logger.debug(format);
        }
    }

    public void info(String format, Object... args) {
        if(args != null){
            this.logger.info(String.format(format, args));
        }else{
            this.logger.info(format);
        }
    }

    public void warning(String format, Object... args) {
        if(args != null){
            this.logger.warn(String.format(format, args));
        }else{
            this.logger.warn(format);
        }
    }

    public void error(String format, Object... args) {
        if(args != null){
            this.logger.error(String.format(format, args));
        }else{
            this.logger.error(format);
        }
    }

    @Override
    public void error(String format, Throwable error, Object... args) {
        if(args != null){
            this.logger.error(String.format(format, args), error);
        }else{
            this.logger.error(format, error);
        }
    }

}
