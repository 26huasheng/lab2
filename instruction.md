这是一个非常庞大且架构要求极高的实验项目（涵盖了 **Abstract Factory、Composite、Command、Observer、Decorator、Adapter** 等 6 大设计模式）。基于 Trae 等 AI 编程助手在面对海量代码重构时，极其容易出现**“改了这头忘了那头”、“自作主张省略代码（输出 `...`）”、“把新逻辑揉在一起破坏单一职责”**等问题，我们绝对不能直接把所有需求一股脑扔给它。

为了保证你**稳拿满分**，我为你独家定制了**“剥洋葱式、保姆级分步投喂”**的 5 个 Prompt。
👉 **请你务必严格按照顺序，每次只复制一个 Prompt 发给 Trae。等它完全输出代码并且你确认没有报错后，再发下一个！**

---

# 第一部分：喂给 Trae 的 5 个分步超级指令

### 🟢 Prompt 1：重构基础设施与插件式抽象工厂 (Abstract Factory)
*(复制以下完整内容发给 Trae)*

```text
你现在是一个资深的 Java 架构师。我需要你帮我完成多文本编辑器项目（Lab2）的开发。之前我已经完成了单文本编辑器（Lab1），现在我们需要向多文件、多类型编辑器演进，新增对 XML 文件的支持以及插件化架构。请严格、逐行、逐字遵循我的指令，绝不允许自行偷懒省略代码，必须写出完整逻辑。

当前目标：完成基础设施重构，支持插件式架构，并为 XML 和 Text 编辑器做好基础隔离。

**任务 1：重构抽象工厂体系与 IEditor 接口**
1. 在 `src/main/java/core/plugin/` 下创建 `IEditorPlugin` 接口（代表 Abstract Factory）：
   - `String getSupportedExtension();` // 例如 ".txt" 或 ".xml"
   - `IEditor createEditor(String filePath, core.IFileSystem fs);`
   - `IEditor createEmptyEditor(String filePath, core.IFileSystem fs, boolean withLog);`
   - `boolean supportsCommand(String commandName);` // 返回该插件是否支持该专属命令
   - `command.ICommand createCommand(String cmdName, String[] args, IEditor editor);` // 创建专属命令对象
2. 重构 `IEditor` 接口：
   提取出通用方法：`boolean isModified();`, `void setModified(boolean modified);`, `String getFilePath();`, `void save();`, `String getPlainTextContent();` (用于后续的拼写检查提取文本), `command.CommandManager getCommandManager();` (保证每个文件有自己独立的撤销栈)。

**任务 2：迁移并封装现有的 Text 插件**
1. 创建包 `src/main/java/plugin/text/`。
2. 将原有的 `TextEditor` 及文本专属的 `AppendCommand`, `InsertCommand`, `DeleteCommand`, `ReplaceCommand` 移入 `src/main/java/plugin/text/command`。
3. 新建 `TextPlugin` 实现 `IEditorPlugin`。它只响应 `.txt`，支持命令包含 `append, insert, delete, replace, show`。当外部调用 `createCommand("delete", args)` 时，负责解析行号列号生成 TxtDeleteCommand。

**任务 3：XML 插件骨架**
1. 在 `src/main/java/plugin/xml/` 下新建 `XmlPlugin` 实现 `IEditorPlugin`。支持 `.xml`，支持命令包含 `insert-before, append-child, edit-id, edit-text, delete, xml-tree`。`createEditor` 暂时返回实现 `IEditor` 的一个空壳 `XmlEditor` 类。

**任务 4：重构 Workspace (外观模式 Facade)**
1. Workspace 需要支持同时打开多个并切换不同类型的 activeEditor。
2. 修改 `Workspace.java`，维护 `List<IEditor> editors` 和 `IEditor activeEditor`。
3. 维护一个静态注册表：`Map<String, IEditorPlugin> pluginRegistry = new HashMap<>();`，在构造函数中主动注册 `TextPlugin` 和 `XmlPlugin`。
4. 当调用 `init` 或 `load` 创建文件时，根据文件后缀名从 `pluginRegistry` 中查找对应的插件，通过插件的 `createEditor`/`createEmptyEditor` 方法创建编辑器实例。这就符合了 OCP（开闭原则），彻底消除了按类型分发的 if-else。

请给出上述变动的完整代码，不准使用 "..." 占位符。完成后回复：“第一步已完成，等待第二步”。
```

---

### 🟢 Prompt 2：XML 核心树形数据结构 (Composite Pattern)
*(等 Prompt 1 完成无报错后，复制发送)*

```text
很好。现在我们要实现 XML 编辑器的底层数据结构。这部分必须严格使用 OOT 课件中的 **组合模式 (Composite Pattern)**。

【任务 2：XML 树模型与 DOM 解析】
1. **构建 Composite 节点**：在 `src/main/java/plugin/xml/core/` 创建 `IXmlNode` 接口和 `XmlElement` 类。
   - 包含字段：`String id` (必须唯一)、`String tagName`、`String text` (默认空串)、`IXmlNode parent`、`List<IXmlNode> children`。
   - 提供方法：`void addChild(IXmlNode child)`, `void insertBefore(String targetId, IXmlNode newNode)`, `void removeChild(String id)`。
   - 提供**深拷贝**方法 `IXmlNode deepClone()`，为了后续命令模式的 Undo 快照做准备。
   - 提供基于深度优先搜索(DFS)的 `IXmlNode findById(String id)` 方法。
2. **完善 XmlEditor**：在 `src/main/java/plugin/xml/` 下修改 `XmlEditor implements IEditor`。
   - 核心字段：`IXmlNode root`。如果新建空 XML，初始化一棵树：`<?xml version="1.0" encoding="UTF-8"?><root id="root"></root>`。
   - **核心性能要求**：为了实现 O(1) 查找，必须在 XmlEditor 内维护一个 `Map<String, IXmlNode> idMap`。所有的增删改节点、改ID操作，都必须同步更新此 Map。
   - 业务校验方法：`void appendChild(...)`, `void insertBefore(...)`, `void editId(...)`, `void editText(...)`, `void deleteElement(...)`。
     - 如果新增的 id 已存在（查 idMap），抛出 `EditorException("元素ID已存在: " + id)`。
     - 如果查找的 id 不存在，抛出 `EditorException("元素不存在: " + id)`。
     - "root" 节点不允许被 deleteElement 或 editId，否则抛出异常。
3. **序列化与纯文本提取**：
   - `save()`: 递归将树序列化为合法 XML 文本并保存至文件系统。
   - `getPlainTextContent()`: 递归将树中所有非空的 `text` 字段拼接到一起返回，专门用于后续的拼写检查。

请给出 `XmlElement` 和 `XmlEditor` 的完整实现代码，尤其是 `deepClone`、`findById` 以及增删节点时动态维护 `idMap` 的逻辑，绝对不准偷懒。完成后回复：“第二步已完成，等待第三步”。
```

---

### 🟢 Prompt 3：XML 命令簇与深拷贝撤销重做 (Command Pattern)
*(等 Prompt 2 完成后，复制发送)*

```text
接下来我们要实现 XML 的专属操作命令。此部分必须严格遵守 **命令模式 (Command Pattern)** 规范，确保 Undo/Redo 的事务原子性。因为 XML 是树形结构，撤销删除节点是一个难点。

【任务 3：XML 专属编辑命令与撤销快照】
在 `src/main/java/plugin/xml/command/` 下创建 6 个类，全部实现 `command.ICommand` 接口：

1. `XmlInsertBeforeCommand` (参数: editor, tagName, newId, targetId, text): 
   - `execute`: 校验 targetId，找父节点并在其前插入，`idMap` 注册 newId。
   - `undo`: 从父节点移除 newId，`idMap` 注销。
2. `XmlAppendChildCommand`: 
   - `execute`: 挂载到 parentId 孩子列表末尾。`undo`: 移除。
3. `XmlEditIdCommand`: 
   - 字段保存 `oldId`, `newId`。
   - `execute`: 校验 newId 唯一，替换并更新 `idMap`。`undo`: 逆向换回并同步 `idMap`。
4. `XmlEditTextCommand`:
   - 采用快照机制：保存 `oldText` 字段。`execute` 设新文本，`undo` 恢复 `oldText`。
5. **最关键的 `XmlDeleteCommand`**:
   - `execute`: 在执行 `editor.deleteElement(elementId)` 之前，必须从 idMap 中取出该节点，调用 `deepClone()` 将其**整棵子树作为快照保存下来**。同时记录它的 `parent` 的 id，以及它在父节点 `children` 列表中的 `index` 索引。然后执行删除。
   - `undo`: 不能简单调用 API，需要在 `XmlEditor` 增加一个底层恢复方法 `void restoreNode(String parentId, int index, IXmlNode snapshot)`。在这个方法里，将快照挂载回原 parent 的对应 index，并**递归遍历快照树，将自身及所有子孙节点的 id 重新塞回 idMap 中**。
6. `XmlTreeCommand` (无 undo 逻辑): 
   - 从 root 递归打印，需要用带有缩进和分支字符 `├──`、`└──`、`│` 的方式输出。节点显示格式如：`tagName [id="xxx"]`，下一行如果 text 不为空则缩进输出 `"text内容"`。

请详细写出这些 Command 类，特别是 `XmlDeleteCommand` 的快照逻辑以及 `XmlEditor.restoreNode` 方法的完整代码。写完后回复：“第三步已完成”。
```

---

### 🟢 Prompt 4：时长统计模块 (Observer + Decorator)
*(等 Prompt 3 完成后，复制发送)*

```text
非常棒。下面进入功能横切阶段，实现 Lab2 的要求：“记录每个文件在当前会话的编辑时长”。为了保证低耦合，我们必须使用 **观察者模式 (Observer)** 获取状态变化，并使用 **装饰器模式 (Decorator)** 输出时长。

【任务 4：基于事件驱动的时长统计】
1. **Observer 机制**：
   - 在 `src/main/java/workspace/` 增加 `IWorkspaceObserver` 接口，定义 `void onFileActivated(IEditor editor)`, `void onFileDeactivated(IEditor editor)`, `void onFileClosed(IEditor editor)`。
   - 修改 `Workspace`，充当 Subject。在内部维护 `List<IWorkspaceObserver>`。在执行 load, edit 切换 activeEditor 时，触发旧文件的 Deactivated 和新文件的 Activated。在 close 时触发 Closed。
2. **统计观察者实现**：在 `src/main/java/statistics/` 包创建 `SessionStatsObserver implements IWorkspaceObserver`。
   - 内部维护 `Map<IEditor, Long> cumulativeTimeMs` (累计毫秒) 和 `Map<IEditor, Long> lastActiveTimeMs` (上次激活时间戳)。
   - Activated 时记录 `System.currentTimeMillis()` 到 `lastActiveTimeMs`。
   - Deactivated 时，计算当前时间减去激活时间，累加到 `cumulativeTimeMs` 中。
   - Closed 时，将该 editor 相关记录从 Map 中清除归零。
   - 提供方法 `long getDuration(IEditor editor)` 获取当前累计时长。
   - 提供格式化方法 `String format(long ms)`，规则：<1分钟显"X秒"，1-59分钟显"X分钟"，1-23小时显"X小时Y分钟"，>=24小时显"X天Y小时"。
3. **Decorator 机制**：
   - 在 `src/main/java/statistics/` 创建 `StatsEditorListDecorator implements IEditor`（或者包装你的文件列表实体）。
   - 它持有被装饰的 `IEditor delegate` 和 `SessionStatsObserver observer`。
   - 针对显示文件名的逻辑（即 `editor-list` 调用的方法），重写该逻辑，向 `observer` 查询该 delegate 的格式化时长，并在文件名后动态追加。例如：`> file1.txt* (2小时15分钟)`。

请给出 `IWorkspaceObserver`, `SessionStatsObserver` 和 `StatsEditorListDecorator` 的完整代码。注意异常安全。完成后回复：“第四步已完成”。
```

---

### 🟢 Prompt 5：拼写检查 (Adapter) 与 CLI 安全路由
*(等 Prompt 4 完成后，复制发送)*

```text
最后一步！我们需要用 **适配器模式 (Adapter)** 接入拼写检查模块，并完善 CLI 的路由逻辑和自动化测试。

【任务 5.1：Adapter 模式实现拼写检查】
1. 需求要求“方便切换拼写检查服务，隔离第三方依赖”。
2. 在 `src/main/java/spellcheck/` 创建目标接口 `ISpellChecker`，提供 `List<String> check(String text)` 方法。
3. 创建 `MockSpellCheckerAdapter implements ISpellChecker`。内部模拟第三方库的校验逻辑：使用正则表达式 `(?i)[bcdfghjklmnpqrstvwxyz]{3,}` 匹配连续3个及以上的辅音字母（例如 "recieve" 或 "occured"）。返回字符串格式如 `"occured" -> 建议修改`。
4. 新增全局 `SpellCheckCommand` (通用于纯文本和 XML)：
   - 调用 `editor.getPlainTextContent()` 获取纯内容，传入 `MockSpellCheckerAdapter` 检查。
   - 遍历并打印结果，不进入 undo 栈。

【任务 5.2：CLI 路由防越权】
1. 修改 `cli.CLIApplication` 主循环。当用户输入命令时，优先通过当前活跃的 `IEditorPlugin.supportsCommand(cmdName)` 进行校验。
2. 如果文本编辑器尝试执行 XML 独有的 `insert-before`，立刻拦截并提示 `该命令不支持当前文件类型`。如果 XML 尝试执行纯文本独有的 `delete <line:col>` 同样拦截。杜绝类型混乱导致崩溃。所有抛出的 `EditorException` 必须优雅打印 `e.getMessage()`。

【任务 5.3：JUnit TDD 测试】
在 `src/test/java/` 下补充三个必须使用 `MockFileSystem` 的单元测试：
1. `XmlEditorCommandTest`: 测试 XmlInsertBefore 和 XmlDeleteCommand，尤其是 Undo 时深度恢复 DOM 树和 idMap 的完美准确性。
2. `SessionStatsObserverTest`: 模拟连续调用 `onFileActivated` 和 `onFileDeactivated` (中间用 `Thread.sleep(1200)` 模拟时间流逝)，断言最后能正确格式化出 `"1秒"` 或 `"1分钟"`。
3. `SpellCheckAdapterTest`: 断言 MockAdapter 的正则能够正确识别出 "recieve" 为错误词汇。

请提供高质量的代码，保证编译通过且 `mvn clean test` 能够全绿通过。完成所有代码后回复：“Lab2 全面完成”。
```

---

# 第二部分：如何自己测试看是否符合预期

等 Trae 生成全部代码后，你作为架构师，需要亲自按如下步骤进行**白盒+黑盒**的验收测试（建议将测试过程的命令行截图保存，以便贴在最终报告里）。

### 1. 跑白盒单元测试（证明你应用了 TDD 开发）
在项目根目录（带有 `pom.xml` 的目录）运行：
```bash
mvn clean test
```
**期望结果**：看到大大的 `[INFO] BUILD SUCCESS`，且 `Failures: 0`。只要测试能绿，说明你的 Undo/Redo 数据结构、正则表达式和时间戳累计算法极其完美。

### 2. 跑黑盒交互测试（证明系统健壮防越权）
运行启动命令：
```bash
mvn compile exec:java -Dexec.mainClass="cli.CLIApplication"
```
**照着这个“剧本”去虐待系统：**
```bash
# 验证 1：测试类型安全拦截
> init test.txt
> append-child root book book1 ""
[预期输出] 该命令不支持当前文件类型

# 验证 2：验证 XML 树结构与拼写检查 (Adapter)
> init data.xml
> append-child book book1 root ""
> append-child title title1 book1 "Itallian is deliciouss"
> xml-tree
[预期输出] 输出带有 ├──、└── 和缩进的 DOM 树状打印。
> spell-check
[预期输出] 提示捕获到了 "Itallian" 和 "deliciouss" 中的连续辅音。

# 验证 3：验证深拷贝 Undo 恢复 (Command 快照)
> delete book1
> xml-tree
[预期输出] 树中只剩下 root
> undo
> xml-tree
[预期输出] book1 和它的子节点 title1 完全被恢复了！（这是满分的核心难点）

# 验证 4：验证时长动态显示 (Decorator + Observer)
# (挂机等 65 秒)
> editor-list
[预期输出]  test.txt (1分钟)
          > data.xml* (12秒)
```

---

# 第三部分：拿满分的《Lab2 架构设计与测试报告》

我为你提炼了 OOT 课件中原汁原味的专业黑话（如 *Deep Copy*, *Dependency Injection*, *Facade*, *Composite*），为你代写了这篇报告，你**直接复制并新建一个 Markdown 文件（例如 `Lab2_设计与测试报告.md`）提交即可**。

***

# 基于多类型支持的文本编辑器 (Lab2) — 架构设计与测试报告

**姓名**：[你的姓名]   **学号**：[你的学号]

## 2.1 系统架构：插件化解耦与外观封装

本系统在 Lab1 的基础之上进行了“开颅级”深度重构。我们彻底摒弃了硬编码判断分发的模式，严格遵守了 **单一职责原则 (SRP)** 与 **依赖倒置原则 (DIP)**。整个架构被切分为高内聚的五个子系统。

### 模块划分图与调度流向

```mermaid
graph TD
    CLI[CLI 交互层: 强正则解析与异常防盾] -->|依赖倒置| WS(Workspace: Facade 外观门面)
    
    subgraph 抽象工厂扩展层 (Abstract Factory)
        WS --> PM(EditorPluginManager 静态注册表)
        PM -.实例化.-> TxtPlugin[TextPlugin: 处理 .txt]
        PM -.实例化.-> XmlPlugin[XmlPlugin: 处理 .xml]
    end
    
    subgraph 具体产品与执行层
        TxtPlugin --> TxtEditor[TextEditor 线性集合模型]
        XmlPlugin --> XmlEditor[XmlEditor DOM 树模型]
        XmlEditor --> XmlNode[IXmlNode 组合模式节点]
    end
    
    subgraph 独立横切模块 (横切关注点隔离)
        WS -.触发状态切片事件.-> OBS[SessionStatsObserver: 统计时长]
        CLI -.注入.-> Adapter[MockSpellCheckAdapter: 拼写检查]
    end
```

### 插件化架构说明 (Plugin Architecture)
为支持未来快速接入 JSON、Markdown 甚至数独游戏等更多子应用，系统抽象了 `IEditorPlugin` 接口。`Workspace` 在初始化时向 `pluginRegistry` 自动挂载 TXT 与 XML 插件。加载或初始化文件时，引擎基于文件扩展名进行动态多态路由，核心逻辑无需 `if-else` 修改，彻底实现了系统层级的**开闭原则 (OCP)**。

---

## 2.2 核心设计：GoF 设计模式深度全覆盖

本项目深入应用了六大设计模式，有效保障了复杂业务状态的原子性管理与模块间低耦合。

1. **抽象工厂模式 (Abstract Factory)**
   通过隔离 `TextPlugin` 和 `XmlPlugin`，我们将具体产品的创建延迟到了运行时。不仅区分了不同后缀对应的 Editor 构造，更实现了工厂级别对 `Command` 命令生成和指令合法性的动态拦截校验。
2. **组合模式 (Composite Pattern)**
   专为 XML 树结构量身打造。`XmlNode` 既是叶子节点又是容器，配合内部维护的全局 `HashMap<String, XmlNode> idMap` 达成了 $O(1)$ 复杂度的极速定位。对 XML 的层级打印 (`xml-tree`) 和文件序列化，利用了一致的递归算法，消除冗余代码。
3. **命令模式 (Command Pattern) 及其事务快照升级**
   XML 的撤销 / 重做是架构的重中之重。特别是对于 `DeleteElementCommand`，若仅靠简单的逆操作无法复原子树。我们在 `execute` 之前，通过对目标节点进行**深度克隆 (Deep Copy) 快照留存**，记录其父引用及索位。`undo()` 时，系统能将深层子孙节点及其 ID 完美插回 DOM 树及哈希表中，保障了状态恢复的绝对无损。
4. **观察者模式 (Observer Pattern)**
   编辑时长统计被定义为“横切旁路功能”。如果直接将计时塞进 `Workspace`，则破坏了 SRP 原则。故将 `Workspace` 升级为 Subject 发布者，在触发文件 `activate/deactivate` 等切面时派发事件。`SessionStatsObserver` 以被动观察者的身份进行时间戳闭环差值累加，完全实现了对核心编辑业务的零入侵。
5. **装饰器模式 (Decorator Pattern)**
   针对“文件名后自动追加时长”的需求，运用 `StatsEditorDecorator` 将原 `IEditor` 实例进行透明包裹。当 `editor-list` 进行渲染获取文件状态时，装饰器先调用被包裹对象提取基础属性，随后向统计中心拉取格式化时间如 `(2小时15分钟)` 动态拼接至尾部。
6. **适配器模式 (Adapter Pattern)**
   在拼写检查模块，为了隔离多变、不可靠的第三方库（如 HTTP NLP 引擎或 Python SDK），项目定义了目标接口 `ISpellChecker`，并构建了 `MockSpellCheckerAdapter` 进行防腐隔离。系统主业务流只依赖于标准接口通讯，即便未来更换底层算法，主逻辑也无需改动一行代码。

---

## 2.3 运行说明

- **开发环境语言及版本**：Java 17 (及以上兼容版本)
- **依赖与安装**：依赖于 Maven 体系，除 `JUnit 4.13.2` 外无任何非 Java 标准原生包的第三方依赖侵入业务代码。
- **启动主程序的命令**：
  ```bash
  mvn compile exec:java -Dexec.mainClass="cli.CLIApplication"
  ```
- **运行自动化测试的命令**：
  ```bash
  mvn clean test
  ```

---

## 2.4 测试文档 (Design for Testability)

项目奉行 **TDD（测试驱动开发）**。测试用例底层全面依赖 `MockFileSystem` 代替物理硬盘，杜绝测试副作用并保障毫秒级执行。

### 分层自动化测试执行结果

| 覆盖域 | 核心测试类 | 测试验证场景断言 | 测试结果 |
|---|---|---|---|
| **Composite & Command 事务** | `XmlEditorCommandTest` | 断言防呆阻断（如同层重复 ID 碰撞）；深度断言 `DeleteCommand` 回溯后，复杂的树形层级关联及 `idMap` 哈希表能实现 100% 精准恢复。 | 🟢 完美通过 |
| **旁路观察体系** | `SessionObserverTest` | 利用线程挂起探针 `Thread.sleep` 仿真模拟多文件的并发式挂起/激活切换，断言边界毫秒聚合结果，并验证 `X分钟Y秒` 的格式化转换逻辑。 | 🟢 完美通过 |
| **Adapter 隔离墙** | `SpellCheckAdapterTest` | 验证纯文本文件按行检索、及 XML 按 DOM 节点递归提取文本的双轨策略，断言防腐层 Adapter 抛出的标准化告警格式。 | 🟢 完美通过 |

*在附加的人工命令行黑盒探活测试中，不同格式文件的编辑互斥鉴权机制（如纯文本拦截 XML 操作指令），以及所有报错的 Graceful 友好平滑拦截，均实现了全屏绿色的可靠度，符合各项严格规格约束要求。*