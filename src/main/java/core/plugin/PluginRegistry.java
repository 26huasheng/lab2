package core.plugin;

import java.util.HashMap;
import java.util.Map;

public class PluginRegistry {

    private static final Map<String, IEditorPlugin> registry = new HashMap<>();

    public static void register(IEditorPlugin plugin) {
        if (plugin == null || plugin.getSupportedExtension() == null) {
            return;
        }
        registry.put(plugin.getSupportedExtension().toLowerCase(), plugin);
    }

    public static IEditorPlugin getPluginForExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return null;
        }
        return registry.get(extension.toLowerCase());
    }

    public static IEditorPlugin detectPlugin(String filePath) {
        if (filePath == null) {
            return null;
        }
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex < 0) {
            return null;
        }
        String ext = filePath.substring(dotIndex);
        return getPluginForExtension(ext);
    }
}
