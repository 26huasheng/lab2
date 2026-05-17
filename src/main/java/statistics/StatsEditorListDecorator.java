package statistics;

import command.CommandManager;
import editor.IEditor;

public class StatsEditorListDecorator implements IEditor {

    private final IEditor delegate;

    private final SessionStatsObserver observer;

    public StatsEditorListDecorator(IEditor delegate, SessionStatsObserver observer) {
        this.delegate = delegate;
        this.observer = observer;
    }

    public String getFormattedInfoLine(String filePath, boolean isActive, boolean isModified) {
        long durationMs = observer.getDuration(delegate);
        String durationStr = observer.format(durationMs);

        StringBuilder sb = new StringBuilder();
        if (isActive) {
            sb.append("> ");
        } else {
            sb.append("  ");
        }
        sb.append(filePath);
        if (isModified) {
            sb.append("*");
        }
        sb.append(" (").append(durationStr).append(")");
        return sb.toString();
    }

    @Override
    public boolean isModified() {
        return delegate.isModified();
    }

    @Override
    public void setModified(boolean modified) {
        delegate.setModified(modified);
    }

    @Override
    public String getFilePath() {
        return delegate.getFilePath();
    }

    @Override
    public void save() {
        delegate.save();
    }

    @Override
    public String getPlainTextContent() {
        return delegate.getPlainTextContent();
    }

    @Override
    public CommandManager getCommandManager() {
        return delegate.getCommandManager();
    }
}
