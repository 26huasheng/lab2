package cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import command.ICommand;
import core.EditorException;
import core.IFileSystem;
import core.LocalFileSystem;
import core.plugin.IEditorPlugin;
import core.plugin.PluginRegistry;
import editor.IEditor;
import log.FileLogger;
import plugin.sudoku.SudokuPlugin;
import plugin.text.TextPlugin;
import plugin.xml.XmlPlugin;
import spellcheck.MockSpellCheckerAdapter;
import spellcheck.SpellCheckCommand;
import statistics.SessionStatsObserver;
import statistics.StatsEditorListDecorator;
import workspace.Workspace;

public class CLIApplication {

    private static IFileSystem fs;

    private static Workspace workspace;

    private static SessionStatsObserver statsObserver;

    private static MockSpellCheckerAdapter spellChecker;

    private static java.util.Map<String, FileLogger> fileLoggers = new java.util.HashMap<>();

    private static final Set<String> GLOBAL_COMMANDS = new java.util.HashSet<>();

    private static final Set<String> READONLY_COMMANDS = new java.util.HashSet<>();

    static {
        GLOBAL_COMMANDS.add("help");
        GLOBAL_COMMANDS.add("load");
        GLOBAL_COMMANDS.add("save");
        GLOBAL_COMMANDS.add("init");
        GLOBAL_COMMANDS.add("close");
        GLOBAL_COMMANDS.add("edit");
        GLOBAL_COMMANDS.add("editor-list");
        GLOBAL_COMMANDS.add("dir-tree");
        GLOBAL_COMMANDS.add("undo");
        GLOBAL_COMMANDS.add("redo");
        GLOBAL_COMMANDS.add("exit");
        GLOBAL_COMMANDS.add("log-on");
        GLOBAL_COMMANDS.add("log-off");
        GLOBAL_COMMANDS.add("log-show");
        GLOBAL_COMMANDS.add("spell-check");

        READONLY_COMMANDS.add("show");
        READONLY_COMMANDS.add("xml-tree");
    }

    public static void main(String[] args) {
        fs = new LocalFileSystem();
        workspace = new Workspace(fs);

        PluginRegistry.register(new TextPlugin());
        PluginRegistry.register(new XmlPlugin());
        PluginRegistry.register(new SudokuPlugin());

        statsObserver = new SessionStatsObserver();
        workspace.attachWorkspaceObserver(statsObserver);
        spellChecker = new MockSpellCheckerAdapter();

        Scanner scanner = new Scanner(System.in);
        Pattern paramPattern = Pattern.compile("([^\\s\"']+)|\"([^\"]*)\"|'([^']*)'");

        while (true) {
            try {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }

                if (input.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }

                List<String> params = new ArrayList<>();
                Matcher m = paramPattern.matcher(input);
                while (m.find()) {
                    if (m.group(1) != null) {
                        params.add(m.group(1));
                    } else if (m.group(2) != null) {
                        params.add(m.group(2));
                    } else if (m.group(3) != null) {
                        params.add(m.group(3));
                    }
                }

                if (params.isEmpty()) {
                    continue;
                }

                String command = params.get(0);

                if (GLOBAL_COMMANDS.contains(command)) {
                    switch (command) {
                        case "load":
                            handleLoad(params);
                            break;
                        case "save":
                            handleSave(params);
                            break;
                        case "init":
                            handleInit(params);
                            break;
                        case "close":
                            handleClose(params);
                            break;
                        case "edit":
                            handleEdit(params);
                            break;
                        case "editor-list":
                            handleEditorList();
                            break;
                        case "dir-tree":
                            handleDirTree(params);
                            break;
                        case "undo":
                            handleUndo();
                            break;
                        case "redo":
                            handleRedo();
                            break;
                        case "exit":
                            handleExit();
                            break;
                        case "log-on":
                            handleLogOn(params);
                            break;
                        case "log-off":
                            handleLogOff(params);
                            break;
                        case "log-show":
                            handleLogShow(params);
                            break;
                        case "spell-check":
                            handleSpellCheck(params);
                            break;
                    }
                } else {
                    handlePluginCommand(command, params);
                }

            } catch (EditorException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("[SysError] " + e.getMessage());
            }
        }
    }

    private static void printHelp() {
        System.out.println("\u5168\u5C40\u547D\u4EE4:");
        System.out.println("  load <file>");
        System.out.println("  save [file|all]");
        System.out.println("  init <file> [with-log]");
        System.out.println("  close [file]");
        System.out.println("  edit <file>");
        System.out.println("  editor-list");
        System.out.println("  dir-tree [path]");
        System.out.println("  undo");
        System.out.println("  redo");
        System.out.println("  exit");
        System.out.println("  log-on [file]");
        System.out.println("  log-off [file]");
        System.out.println("  log-show [file]");
        System.out.println("  spell-check [file]");

        IEditorPlugin activePlugin = workspace.getActivePlugin();
        if (activePlugin != null) {
            Set<String> pluginCommands = activePlugin.getSupportedCommands();
            if (!pluginCommands.isEmpty()) {
                System.out.println("\u5F53\u524D\u6587\u4EF6\u4E13\u5C5E\u547D\u4EE4 (" + activePlugin.getSupportedExtension() + "):");
                for (String cmd : pluginCommands) {
                    System.out.println("  " + cmd);
                }
            }
        }
    }

    private static void handlePluginCommand(String command, List<String> params) {
        IEditor editor = workspace.getActiveEditor();
        String activeFile = workspace.getActiveFilePath();
        if (activeFile == null) {
            throw new EditorException("\u6CA1\u6709\u6D3B\u52A8\u6587\u4EF6");
        }

        IEditorPlugin plugin = PluginRegistry.detectPlugin(activeFile);
        if (plugin == null) {
            throw new EditorException("\u65E0\u6CD5\u8BC6\u522B\u5F53\u524D\u6587\u4EF6\u7C7B\u578B");
        }

        if (!plugin.supportsCommand(command)) {
            throw new EditorException("\u5F53\u524D\u6587\u4EF6\u7C7B\u578B\u4E0D\u652F\u6301\u547D\u4EE4: " + command);
        }

        String[] cmdArgs = new String[params.size() - 1];
        for (int i = 1; i < params.size(); i++) {
            cmdArgs[i - 1] = params.get(i);
        }

        ICommand cmd = plugin.createCommand(command, cmdArgs, editor);

        if (READONLY_COMMANDS.contains(command)) {
            cmd.execute();
        } else {
            workspace.executeEditorCommand(cmd);
        }
    }

    private static void handleLoad(List<String> params) {
        if (params.size() < 2) {
            System.out.println("\u7528\u6CD5: load <file>");
            return;
        }
        workspace.load(params.get(1));
        System.out.println("\u6587\u4EF6\u5DF2\u52A0\u8F7D: " + params.get(1));
    }

    private static void handleSave(List<String> params) {
        if (params.size() >= 2 && params.get(1).equalsIgnoreCase("all")) {
            workspace.saveAll();
            System.out.println("\u6240\u6709\u6587\u4EF6\u5DF2\u4FDD\u5B58\u3002");
        } else if (params.size() >= 2) {
            workspace.save(params.get(1));
            System.out.println("\u6587\u4EF6\u5DF2\u4FDD\u5B58: " + params.get(1));
        } else {
            String activeFile = workspace.getActiveFilePath();
            if (activeFile != null) {
                workspace.save(activeFile);
                System.out.println("\u6587\u4EF6\u5DF2\u4FDD\u5B58: " + activeFile);
            } else {
                System.out.println("\u6CA1\u6709\u6D3B\u52A8\u6587\u4EF6\uFF0C\u8BF7\u6307\u5B9A\u6587\u4EF6\u8DEF\u5F84\u6216\u4F7F\u7528 save all");
            }
        }
    }

    private static void handleInit(List<String> params) {
        if (params.size() < 2) {
            System.out.println("\u7528\u6CD5: init <file> [with-log]");
            return;
        }
        boolean withLog = params.size() >= 3 && params.get(2).equalsIgnoreCase("with-log");
        workspace.init(params.get(1), withLog);
        System.out.println("\u6587\u4EF6\u5DF2\u521D\u59CB\u5316: " + params.get(1));
    }

    private static void handleClose(List<String> params) {
        if (params.size() >= 2) {
            workspace.close(params.get(1));
            System.out.println("\u6587\u4EF6\u5DF2\u5173\u95ED: " + params.get(1));
        } else {
            String activeFile = workspace.getActiveFilePath();
            if (activeFile != null) {
                workspace.close(activeFile);
                System.out.println("\u6587\u4EF6\u5DF2\u5173\u95ED: " + activeFile);
            } else {
                System.out.println("\u6CA1\u6709\u6D3B\u52A8\u6587\u4EF6");
            }
        }
    }

    private static void handleEdit(List<String> params) {
        if (params.size() < 2) {
            System.out.println("\u7528\u6CD5: edit <file>");
            return;
        }
        workspace.edit(params.get(1));
        System.out.println("\u5DF2\u5207\u6362\u5230\u6587\u4EF6: " + params.get(1));
    }

    private static void handleEditorList() {
        List<String> openedFiles = workspace.getOpenedFiles();
        if (openedFiles.isEmpty()) {
            System.out.println("\u6CA1\u6709\u6253\u5F00\u7684\u6587\u4EF6\u3002");
            return;
        }
        String activeFile = workspace.getActiveFilePath();
        for (String file : openedFiles) {
            IEditor editor = workspace.getEditor(file);
            boolean isActive = file.equals(activeFile);
            boolean isModified = editor.isModified();
            StatsEditorListDecorator decorator = new StatsEditorListDecorator(editor, statsObserver);
            String line = decorator.getFormattedInfoLine(file, isActive, isModified);
            System.out.println(line);
        }
    }

    private static void handleDirTree(List<String> params) {
        String path = params.size() >= 2 ? params.get(1) : ".";
        ITreeNode root = new FileNodeAdapter(fs, path);
        TreePrinter.printTree(root);
    }

    private static void handleUndo() {
        IEditor editor = workspace.getActiveEditor();
        editor.getCommandManager().undo();
        System.out.println("\u5DF2\u64A4\u9500\u3002");
    }

    private static void handleRedo() {
        IEditor editor = workspace.getActiveEditor();
        editor.getCommandManager().redo();
        System.out.println("\u5DF2\u91CD\u505A\u3002");
    }

    private static void handleExit() {
        workspace.WorkspaceMemento memento = workspace.saveState();
        java.util.Scanner exitScanner = new java.util.Scanner(System.in);

        if (memento.getModifiedStates() != null) {
            for (String file : memento.getOpenedFiles()) {
                Boolean isModified = memento.getModifiedStates().get(file);
                if (isModified != null && isModified) {
                    System.out.print("\u6587\u4EF6\u5DF2\u4FEE\u6539\uFF0C\u662F\u5426\u4FDD\u5B58? (y/n) ");
                    String ans = exitScanner.nextLine();
                    if ("y".equalsIgnoreCase(ans.trim())) {
                        workspace.save(file);
                    }
                }
            }
        }

        try {
            java.util.List<String> confLines = new java.util.ArrayList<>();
            confLines.add(memento.getActiveFile() != null ? memento.getActiveFile() : "");
            confLines.addAll(memento.getOpenedFiles());
            fs.writeLines(".workspace.conf", confLines);
        } catch (Exception e) {
            System.out.println("[Warning] \u5DE5\u4F5C\u533A\u72B6\u6001\u4FDD\u5B58\u5931\u8D25: " + e.getMessage());
        }

        System.out.println("\u9000\u51FA\u7F16\u8F91\u5668\u3002");
        System.exit(0);
    }

    private static void handleLogOn(List<String> params) {
        String file = params.size() >= 2 ? params.get(1) : workspace.getActiveFilePath();
        if (file == null) {
            System.out.println("\u6CA1\u6709\u6D3B\u52A8\u6587\u4EF6");
            return;
        }
        String logFilePath = file + ".log";
        FileLogger fileLogger = new FileLogger(fs, logFilePath);
        workspace.attachObserver(fileLogger);
        fileLoggers.put(file, fileLogger);
        System.out.println("\u65E5\u5FD7\u5DF2\u5F00\u542F: " + logFilePath);
    }

    private static void handleLogOff(List<String> params) {
        String file = params.size() >= 2 ? params.get(1) : workspace.getActiveFilePath();
        if (file == null) {
            System.out.println("\u6CA1\u6709\u6D3B\u52A8\u6587\u4EF6");
            return;
        }
        FileLogger fileLogger = fileLoggers.remove(file);
        if (fileLogger != null) {
            workspace.detachObserver(fileLogger);
        }
        System.out.println("\u65E5\u5FD7\u5DF2\u5173\u95ED: " + file);
    }

    private static void handleLogShow(List<String> params) {
        String file = params.size() >= 2 ? params.get(1) : workspace.getActiveFilePath();
        if (file == null) {
            System.out.println("\u6CA1\u6709\u6D3B\u52A8\u6587\u4EF6");
            return;
        }
        String logFilePath = file + ".log";
        if (!fs.exists(logFilePath)) {
            System.out.println("\u65E5\u5FD7\u6587\u4EF6\u4E0D\u5B58\u5728: " + logFilePath);
            return;
        }
        List<String> logLines = fs.readLines(logFilePath);
        for (String line : logLines) {
            System.out.println(line);
        }
    }

    private static void handleSpellCheck(List<String> params) {
        String file = params.size() >= 2 ? params.get(1) : workspace.getActiveFilePath();
        if (file == null) {
            System.out.println("\u6CA1\u6709\u6D3B\u52A8\u6587\u4EF6");
            return;
        }

        IEditor editor = workspace.getEditor(file);
        if (editor == null) {
            System.out.println("\u6587\u4EF6\u672A\u6253\u5F00: " + file);
            return;
        }

        SpellCheckCommand cmd = new SpellCheckCommand(editor, spellChecker);
        cmd.execute();
    }
}
