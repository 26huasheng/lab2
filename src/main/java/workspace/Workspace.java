package workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import command.CommandManager;
import command.ICommand;
import core.EditorException;
import core.IFileSystem;
import core.plugin.IEditorPlugin;
import core.plugin.PluginRegistry;
import editor.IEditor;
import log.FileLogger;
import log.ICommandObserver;
import log.ISubject;
import workspace.observer.IWorkspaceObserver;

public class Workspace implements ISubject {

    private final IFileSystem fs;

    private final List<IEditor> editors;

    private IEditor activeEditor;

    private final List<ICommandObserver> commandObservers;

    private final List<IWorkspaceObserver> workspaceObservers;

    public Workspace(IFileSystem fs) {
        this.fs = fs;
        this.editors = new ArrayList<>();
        this.commandObservers = new ArrayList<>();
        this.workspaceObservers = new ArrayList<>();
    }

    @Override
    public void attachObserver(ICommandObserver o) {
        commandObservers.add(o);
    }

    @Override
    public void detachObserver(ICommandObserver o) {
        commandObservers.remove(o);
    }

    @Override
    public void notifyObservers(String msg) {
        for (ICommandObserver observer : commandObservers) {
            observer.onCommandExecuted(msg);
        }
    }

    public void attachWorkspaceObserver(IWorkspaceObserver o) {
        workspaceObservers.add(o);
    }

    public void detachWorkspaceObserver(IWorkspaceObserver o) {
        workspaceObservers.remove(o);
    }

    private void notifyFileActivated(IEditor editor) {
        for (IWorkspaceObserver observer : workspaceObservers) {
            try {
                observer.onFileActivated(editor);
            } catch (Exception e) {
                System.err.println("[Warning] \u89C2\u5BDF\u8005\u89E6\u53D1\u5931\u8D25: " + e.getMessage());
            }
        }
    }

    private void notifyFileDeactivated(IEditor editor) {
        for (IWorkspaceObserver observer : workspaceObservers) {
            try {
                observer.onFileDeactivated(editor);
            } catch (Exception e) {
                System.err.println("[Warning] \u89C2\u5BDF\u8005\u89E6\u53D1\u5931\u8D25: " + e.getMessage());
            }
        }
    }

    private void notifyFileClosed(IEditor editor) {
        for (IWorkspaceObserver observer : workspaceObservers) {
            try {
                observer.onFileClosed(editor);
            } catch (Exception e) {
                System.err.println("[Warning] \u89C2\u5BDF\u8005\u89E6\u53D1\u5931\u8D25: " + e.getMessage());
            }
        }
    }

    private void switchActiveEditor(IEditor newEditor) {
        if (activeEditor != null && activeEditor != newEditor) {
            notifyFileDeactivated(activeEditor);
        }
        activeEditor = newEditor;
        if (activeEditor != null) {
            notifyFileActivated(activeEditor);
        }
    }

    public void executeEditorCommand(ICommand cmd) {
        if (activeEditor == null) {
            throw new EditorException("\u6CA1\u6709\u6D3B\u52A8\u6587\u4EF6");
        }
        CommandManager cmdManager = activeEditor.getCommandManager();
        cmdManager.executeCommand(cmd, logMsg -> notifyObservers(logMsg));
    }

    public void executeEditorCommandOn(ICommand cmd, IEditor editor) {
        CommandManager cmdManager = editor.getCommandManager();
        cmdManager.executeCommand(cmd, logMsg -> notifyObservers(logMsg));
    }

    public void init(String file, boolean withLog) {
        if (fs.exists(file)) {
            throw new EditorException("\u6587\u4EF6\u5DF2\u5B58\u5728: " + file);
        }

        IEditorPlugin plugin = PluginRegistry.detectPlugin(file);
        if (plugin == null) {
            throw new EditorException("\u4E0D\u652F\u6301\u7684\u6587\u4EF6\u7C7B\u578B: " + file);
        }

        IEditor editor = plugin.createEmptyEditor(file, fs, withLog);
        editors.add(editor);
        switchActiveEditor(editor);

        if (withLog) {
            String logFilePath = file + ".log";
            FileLogger fileLogger = new FileLogger(fs, logFilePath);
            attachObserver(fileLogger);
        }
    }

    public void load(String file) {
        if (!fs.exists(file)) {
            throw new EditorException("\u6587\u4EF6\u4E0D\u5B58\u5728: " + file);
        }

        IEditorPlugin plugin = PluginRegistry.detectPlugin(file);
        if (plugin == null) {
            throw new EditorException("\u4E0D\u652F\u6301\u7684\u6587\u4EF6\u7C7B\u578B: " + file);
        }

        IEditor editor = plugin.createEditor(file, fs);
        editors.add(editor);
        switchActiveEditor(editor);

        String plainContent = editor.getPlainTextContent();
        if (plainContent != null && !plainContent.isEmpty()) {
            String firstLine = plainContent.contains("\n") ? plainContent.split("\n", 2)[0] : plainContent;
            if (firstLine.equals("# log")) {
                String logFilePath = file + ".log";
                FileLogger fileLogger = new FileLogger(fs, logFilePath);
                attachObserver(fileLogger);
            }
        }
    }

    public void save(String file) {
        IEditor editor = findEditorByPath(file);
        if (editor == null) {
            throw new EditorException("\u6587\u4EF6\u672A\u6253\u5F00: " + file);
        }
        editor.save();
    }

    public void saveAll() {
        for (IEditor editor : editors) {
            editor.save();
        }
    }

    public void close(String file) {
        IEditor editor = findEditorByPath(file);
        if (editor == null) {
            throw new EditorException("\u6587\u4EF6\u672A\u6253\u5F00: " + file);
        }

        if (editor.isModified()) {
            throw new EditorException("\u6587\u4EF6\u672A\u4FDD\u5B58\uFF0C\u8BF7\u5148\u4FDD\u5B58");
        }

        boolean wasActive = (activeEditor == editor);
        if (wasActive) {
            notifyFileDeactivated(editor);
        }

        editors.remove(editor);
        notifyFileClosed(editor);

        if (wasActive) {
            if (!editors.isEmpty()) {
                switchActiveEditor(editors.get(0));
            } else {
                activeEditor = null;
            }
        }
    }

    public void edit(String file) {
        IEditor editor = findEditorByPath(file);
        if (editor == null) {
            throw new EditorException("\u6587\u4EF6\u672A\u6253\u5F00: " + file);
        }
        if (activeEditor == editor) {
            return;
        }
        if (activeEditor != null) {
            notifyFileDeactivated(activeEditor);
        }
        activeEditor = editor;
        notifyFileActivated(activeEditor);
    }

    public IEditor getActiveEditor() {
        if (activeEditor == null) {
            throw new EditorException("\u6CA1\u6709\u6D3B\u52A8\u6587\u4EF6");
        }
        return activeEditor;
    }

    public IEditor getEditor(String file) {
        return findEditorByPath(file);
    }

    public String getActiveFilePath() {
        if (activeEditor == null) {
            return null;
        }
        return activeEditor.getFilePath();
    }

    public IEditorPlugin getActivePlugin() {
        String path = getActiveFilePath();
        if (path == null) {
            return null;
        }
        return PluginRegistry.detectPlugin(path);
    }

    public List<String> getOpenedFiles() {
        List<String> paths = new ArrayList<>();
        for (IEditor editor : editors) {
            paths.add(editor.getFilePath());
        }
        return paths;
    }

    public List<IEditor> getAllEditors() {
        return new ArrayList<>(editors);
    }

    public WorkspaceMemento saveState() {
        List<String> openedFiles = getOpenedFiles();
        String activeFilePath = activeEditor != null ? activeEditor.getFilePath() : null;
        Map<String, Boolean> modifiedStates = new HashMap<>();
        for (IEditor editor : editors) {
            modifiedStates.put(editor.getFilePath(), editor.isModified());
        }
        return new WorkspaceMemento(openedFiles, activeFilePath, modifiedStates);
    }

    public void restoreState(WorkspaceMemento memento) {
        for (Map.Entry<String, Boolean> entry : memento.getModifiedStates().entrySet()) {
            IEditor editor = findEditorByPath(entry.getKey());
            if (editor != null) {
                editor.setModified(entry.getValue());
            }
        }
    }

    private IEditor findEditorByPath(String filePath) {
        for (IEditor editor : editors) {
            if (editor.getFilePath() != null && editor.getFilePath().equals(filePath)) {
                return editor;
            }
        }
        return null;
    }
}
