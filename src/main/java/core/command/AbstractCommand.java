package core.command;

import java.text.SimpleDateFormat;
import java.util.Date;

import command.ICommand;

public abstract class AbstractCommand implements ICommand {

    protected final String timestamp;

    public AbstractCommand() {
        this.timestamp = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
    }
}
