package editor;

import command.CommandManager;

public abstract class EditorDecorator implements IEditor {

    protected IEditor delegate;

    public EditorDecorator(IEditor delegate) {
        this.delegate = delegate;
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
