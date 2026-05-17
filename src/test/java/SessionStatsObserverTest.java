import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import editor.IEditor;
import plugin.text.TextEditor;
import statistics.SessionStatsObserver;

public class SessionStatsObserverTest {

    private SessionStatsObserver observer;

    private IEditor editor;

    @Before
    public void setUp() {
        observer = new SessionStatsObserver();
        editor = new TextEditor();
    }

    @Test
    public void testFormatLessThanOneMinute() {
        String result = observer.format(45000);
        assertEquals("45\u79D2", result);
    }

    @Test
    public void testFormatOneToFiftyNineMinutes() {
        String result = observer.format(1500000);
        assertEquals("25\u5206\u949F", result);
    }

    @Test
    public void testFormatOneToTwentyThreeHours() {
        String result = observer.format(8100000);
        assertEquals("2\u5C0F\u65F615\u5206\u949F", result);
    }

    @Test
    public void testFormatMoreThanOneDay() {
        String result = observer.format(97200000);
        assertEquals("1\u59293\u5C0F\u65F6", result);
    }

    @Test
    public void testDurationTracking() throws InterruptedException {
        observer.onFileActivated(editor);

        Thread.sleep(1200);

        observer.onFileDeactivated(editor);

        long duration = observer.getDuration(editor);
        assertTrue("\u6301\u7EED\u65F6\u957F\u5E94\u81F3\u5C11\u4E3A1\u79D2\uFF0C\u5B9E\u9645: " + duration + "ms", duration >= 1000);

        String formatted = observer.format(duration);
        assertNotNull(formatted);
        assertTrue(formatted.contains("\u79D2"));
    }

    @Test
    public void testMultipleActivateDeactivate() throws InterruptedException {
        observer.onFileActivated(editor);
        Thread.sleep(500);
        observer.onFileDeactivated(editor);

        observer.onFileActivated(editor);
        Thread.sleep(700);
        observer.onFileDeactivated(editor);

        long duration = observer.getDuration(editor);
        assertTrue("\u7D2F\u8BA1\u65F6\u957F\u5E94\u81F3\u5C11\u4E3A1200ms\uFF0C\u5B9E\u9645: " + duration + "ms", duration >= 1200);
    }

    @Test
    public void testCloseRemovesTracking() throws InterruptedException {
        observer.onFileActivated(editor);
        Thread.sleep(100);
        observer.onFileClosed(editor);

        observer.onFileActivated(editor);
        long durationAfterReopen = observer.getDuration(editor);
        assertTrue("\u91CD\u65B0\u6FC0\u6D3B\u540E\u65F6\u957F\u5E94\u4ECE0\u5F00\u59CB\uFF0C\u5B9E\u9645: " + durationAfterReopen + "ms", durationAfterReopen < 500);
    }
}
