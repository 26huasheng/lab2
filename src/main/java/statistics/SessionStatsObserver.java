package statistics;

import java.util.HashMap;
import java.util.Map;

import editor.IEditor;
import workspace.observer.IWorkspaceObserver;

public class SessionStatsObserver implements IWorkspaceObserver {

    private final Map<IEditor, Long> cumulativeTimeMs = new HashMap<>();

    private final Map<IEditor, Long> lastActiveTimeMs = new HashMap<>();

    @Override
    public void onFileActivated(IEditor editor) {
        lastActiveTimeMs.put(editor, System.currentTimeMillis());
    }

    @Override
    public void onFileDeactivated(IEditor editor) {
        Long lastActive = lastActiveTimeMs.remove(editor);
        if (lastActive != null) {
            long elapsed = System.currentTimeMillis() - lastActive;
            Long current = cumulativeTimeMs.get(editor);
            if (current == null) {
                current = 0L;
            }
            cumulativeTimeMs.put(editor, current + elapsed);
        }
    }

    @Override
    public void onFileClosed(IEditor editor) {
        Long lastActive = lastActiveTimeMs.remove(editor);
        if (lastActive != null) {
            long elapsed = System.currentTimeMillis() - lastActive;
            Long current = cumulativeTimeMs.get(editor);
            if (current == null) {
                current = 0L;
            }
            cumulativeTimeMs.put(editor, current + elapsed);
        }
        cumulativeTimeMs.remove(editor);
    }

    public long getDuration(IEditor editor) {
        long cumulative = 0L;
        Long cumVal = cumulativeTimeMs.get(editor);
        if (cumVal != null) {
            cumulative = cumVal;
        }
        Long lastActive = lastActiveTimeMs.get(editor);
        if (lastActive != null) {
            cumulative += (System.currentTimeMillis() - lastActive);
        }
        return cumulative;
    }

    public String format(long ms) {
        long totalSeconds = ms / 1000;
        if (totalSeconds < 60) {
            return totalSeconds + "\u79D2";
        }
        long totalMinutes = totalSeconds / 60;
        if (totalMinutes < 60) {
            return totalMinutes + "\u5206\u949F";
        }
        long totalHours = totalMinutes / 60;
        long remainingMinutes = totalMinutes % 60;
        if (totalHours < 24) {
            return totalHours + "\u5C0F\u65F6" + remainingMinutes + "\u5206\u949F";
        }
        long days = totalHours / 24;
        long remainingHours = totalHours % 24;
        return days + "\u5929" + remainingHours + "\u5C0F\u65F6";
    }
}
