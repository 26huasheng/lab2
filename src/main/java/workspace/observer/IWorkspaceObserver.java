package workspace.observer;

import editor.IEditor;

public interface IWorkspaceObserver {

    void onFileActivated(IEditor editor);

    void onFileDeactivated(IEditor editor);

    void onFileClosed(IEditor editor);
}
