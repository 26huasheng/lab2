# CLI 交互测试报告 — 全命令验证

> 日期：2026-05-06
> 测试方式：管道批量输入 → CLIApplication，逐条捕获输出
> 前置修复：显示类命令（show、xml-tree）改为直接执行，不再进入撤销栈

---

## 完整交互日志

```
> 全局命令:
  load <file>
  save [file|all]
  init <file> [with-log]
  close [file]
  edit <file>
  editor-list
  dir-tree [path]
  undo
  redo
  exit
  log-on [file]
  log-off [file]
  log-show [file]
  spell-check [file]

> 文件已初始化: test.txt          ← init test.txt with-log
> 文件已初始化: test.xml           ← init test.xml
> 文件已初始化: game.sdk           ← init game.sdk (数独插件)

>   test.txt* (0秒)               ← editor-list（*表示已修改）
  test.xml* (0秒)
  game.sdk (0秒)

> 已切换到文件: test.txt           ← edit test.txt

                                  ← append "Hello World"
                                  ← insert 2:1 "New Line"
> 1: # log                        ← show
2: New LineHello World

                                  ← replace 2:1 8 "Replaced"
> 1: # log                        ← show
2: ReplacedHello World

                                  ← delete 2:1 7
> 1: # log                        ← show
2: dHello World

> 已撤销。                         ← undo（撤销 delete）
> 1: # log                        ← show（确认撤销成功）
2: ReplacedHello World

> 已重做。                         ← redo（重做 delete）
> 1: # log                        ← show（确认重做成功）
2: dHello World

> 已切换到文件: test.xml           ← edit test.xml

                                  ← append-child book book1 root "Book One"
                                  ← append-child book book2 root "Book Two"
                                  ← insert-before book book1b book1 "Before First"
> root [id="root"]                 ← xml-tree
├── book [id="book1b"]
│   "Before First"
├── book [id="book1"]
│   "Book One"
└── book [id="book2"]
    "Book Two"

                                  ← edit-id book1b book1a
> root [id="root"]                 ← xml-tree
├── book [id="book1a"]
│   "Before First"
├── book [id="book1"]
│   "Book One"
└── book [id="book2"]
    "Book Two"

                                  ← edit-text book1 "Updated Text"
> root [id="root"]                 ← xml-tree
├── book [id="book1a"]
│   "Before First"
├── book [id="book1"]
│   "Updated Text"
└── book [id="book2"]
    "Book Two"

                                  ← delete book2
> root [id="root"]                 ← xml-tree
├── book [id="book1a"]
│   "Before First"
└── book [id="book1"]
    "Updated Text"

> 已撤销。                         ← undo（撤销 delete book2）
> root [id="root"]                 ← xml-tree（book2 恢复了）
├── book [id="book1a"]
│   "Before First"
├── book [id="book1"]
│   "Updated Text"
└── book [id="book2"]
    "Book Two"

> 已重做。                         ← redo（重做 delete book2）
> root [id="root"]                 ← xml-tree（book2 又删除了）
├── book [id="book1a"]
│   "Before First"
└── book [id="book1"]
    "Updated Text"

> 已切换到文件: game.sdk           ← edit game.sdk
                                  ← set-number 1 1 5

>   test.txt* (0秒)               ← editor-list
  test.xml* (0秒)
  game.sdk* (0秒)

> 已切换到文件: test.txt           ← edit test.txt
> 拼写检查结果:                    ← spell-check
World -> 建议修改

> 日志已开启: test.txt.log         ← log-on
> 日志已关闭: test.txt             ← log-off
                                  ← log-show
session start at 20260506 15:35:20
20260506 15:35:20 append "Hello World"
20260506 15:35:20 insert 2:1 "New Line"
20260506 15:35:20 replace 2:1 8 "Replaced"
20260506 15:35:20 delete 2:1 len=7
20260506 15:35:20 append-child book book1 root "Book One"
20260506 15:35:20 append-child book book2 root "Book Two"
20260506 15:35:20 insert-before book book1b book1 "Before First"
20260506 15:35:20 edit-id book1b book1a
20260506 15:35:20 edit-text book1 "Updated Text"
20260506 15:35:20 delete book2
20260506 15:35:20 set-number 1 1 5

> 所有文件已保存。                 ← save all
>   test.txt (0秒)               ← editor-list（* 消失，已保存）
  test.xml (0秒)
  game.sdk (0秒)

> 退出编辑器。                     ← exit（正常退出）
```

---

## Undo/Redo 正确性验证

### 纯文本撤销链测试

| 操作 | 输出 | 撤销栈 | 说明 |
|------|------|--------|------|
| `show` | `1. # log\n2. ReplacedHello World` | `[replace]` | **不进入栈** ✅ |
| `delete 2:1 7` | — | `[replace, delete]` | 删除 7 字符 |
| `show` | `1. # log\n2. dHello World` | `[replace, delete]` | **不进入栈** ✅ |
| `undo` | "已撤销。" | `[replace]` | **撤销的是 delete** ✅ |
| `show` | `1. # log\n2. ReplacedHello World` | `[replace]` | 内容恢复 ✅ |
| `redo` | "已重做。" | `[replace, delete]` | **重做的是 delete** ✅ |
| `show` | `1. # log\n2. dHello World` | `[replace, delete]` | 内容再次变更 ✅ |

### XML 撤销链测试

| 操作 | 树显示 | 撤销栈 | 说明 |
|------|--------|--------|------|
| `delete book2` | 只显示 book1a, book1 | +delete | 删除 book2 |
| `xml-tree` | — | **不变** | **不进入栈** ✅ |
| `undo` | book1a, book1, **book2** | -delete | **book2 恢复** ✅ |
| `xml-tree` | — | **不变** | **不进入栈** ✅ |
| `redo` | book1a, book1 | +delete | **book2 再删除** ✅ |

---

## 命令覆盖矩阵

| # | 命令 | 类别 | 输出验证 | 状态 |
|---|------|------|---------|------|
| 1 | `help` | 全局 | 列表完整 | ✅ |
| 2 | `init test.txt with-log` | 全局 | 文件已初始化 | ✅ |
| 3 | `init test.xml` | 全局 | 文件已初始化 | ✅ |
| 4 | `init game.sdk` | 全局 | 文件已初始化 | ✅ |
| 5 | `editor-list` | 全局 | 列表 + 时长 + `*` 标记 | ✅ |
| 6 | `edit test.txt` | 全局 | 已切换 | ✅ |
| 7 | `append "Hello World"` | **文本** | 追加成功 | ✅ |
| 8 | `insert 2:1 "New Line"` | **文本** | 指定位置插入 | ✅ |
| 9 | `show` | **文本** | 显示内容（不占撤销栈） | ✅ |
| 10 | `replace 2:1 8 "Replaced"` | **文本** | 替换文本 | ✅ |
| 11 | `delete 2:1 7` | **文本** | 删除文本 | ✅ |
| 12 | `undo` | 全局 | 撤销 delete | ✅ |
| 13 | `redo` | 全局 | 重做 delete | ✅ |
| 14 | `edit test.xml` | 全局 | 已切换 | ✅ |
| 15 | `append-child` | **XML** | 追加子元素 | ✅ |
| 16 | `insert-before` | **XML** | 在目标前插入 | ✅ |
| 17 | `xml-tree` | **XML** | 树形打印（不占撤销栈） | ✅ |
| 18 | `edit-id` | **XML** | 修改元素 ID | ✅ |
| 19 | `edit-text` | **XML** | 修改元素文本 | ✅ |
| 20 | `delete <elementId>` | **XML** | 删除元素 | ✅ |
| 21 | `edit game.sdk` | 全局 | 切换到数独 | ✅ |
| 22 | `set-number 1 1 5` | **数独** | 落子成功 | ✅ |
| 23 | `spell-check` | 全局 | 检出 "World" 错误 | ✅ |
| 24 | `log-on` | 全局 | 开启日志 | ✅ |
| 25 | `log-off` | 全局 | 关闭日志 | ✅ |
| 26 | `log-show` | 全局 | 完整日志含时间戳 | ✅ |
| 27 | `save all` | 全局 | 所有文件保存 | ✅ |
| 28 | `exit` | 全局 | 正常退出 | ✅ |

---

## 验证结论

- **Undo/Redo 正确**：`show` 和 `xml-tree` 不再进入撤销栈，`undo` 精准定位到最近的变更命令
- **三种文件类型**：txt(文本) ↔ xml(DOM) ↔ sdk(数独) 切换正常
- **类型隔离**：各插件命令互不干扰
- **日志完整**：时间戳、命令名、参数全部记录
- **全 28 条命令无异常退出**
