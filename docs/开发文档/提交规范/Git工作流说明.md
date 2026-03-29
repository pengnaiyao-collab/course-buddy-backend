# Git 工作流说明

本文档描述 Course Buddy Backend 项目的 Git 分支策略和工作流程。

---

## 分支策略

本项目采用 **GitHub Flow** 为基础的简化工作流，适合持续交付的小团队。

### 核心分支

| 分支 | 保护状态 | 说明 |
|------|----------|------|
| `main` | 🔒 受保护 | 生产代码，仅通过 PR 合并，随时可部署 |
| `develop` | 🔒 受保护 | 集成分支，功能开发完成后先合并到此 |

### 辅助分支（短期）

| 前缀 | 说明 | 示例 |
|------|------|------|
| `feature/` | 新功能开发 | `feature/note-export-pdf` |
| `fix/` | Bug 修复 | `fix/jwt-token-expiry` |
| `hotfix/` | 生产紧急修复 | `hotfix/security-patch-2024-01` |
| `refactor/` | 代码重构 | `refactor/note-service-cleanup` |
| `docs/` | 文档更新 | `docs/api-documentation` |
| `chore/` | 依赖升级/配置变更 | `chore/upgrade-spring-boot-3.2.3` |

---

## 分支命名规范

### 格式

```
{类型}/{简短描述}
```

- 全小写
- 单词间用连字符 `-` 分隔
- 描述简洁，不超过 50 个字符
- 可选：包含 Issue 编号 `{类型}/#{issue-number}-{描述}`

### 示例

```bash
# ✅ 好的分支名
feature/note-export-pdf
feature/#42-add-note-tagging
fix/auth-token-refresh
fix/#87-notes-pagination-wrong-count
hotfix/sql-injection-prevention
refactor/clean-up-note-service
docs/update-api-documentation
chore/bump-springdoc-2.4.0

# ❌ 不好的分支名
new-feature          # 缺少类型前缀
Feature/NoteExport   # 大写
fix_login_bug        # 下划线
my-branch            # 无意义名称
zhangsan/work        # 个人名称
```

---

## 日常开发工作流

### 标准流程

```bash
# 第 1 步：从最新的 develop 创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/note-export-pdf

# 第 2 步：开发功能，频繁提交（小步提交）
# 每次提交做一件事，遵循提交消息规范
git add src/main/java/com/coursebuddy/service/impl/NoteExportServiceImpl.java
git commit -m "feat(note): implement PDF export using PDFBox"

git add src/main/java/com/coursebuddy/controller/NoteExportController.java
git commit -m "feat(note): add PDF export endpoint POST /v1/notes/{id}/export/pdf"

git add src/test/java/com/coursebuddy/service/NoteExportServiceTest.java
git commit -m "test(note): add unit tests for NoteExportServiceImpl"

# 第 3 步：保持分支与 develop 同步（避免大量冲突）
git fetch origin
git rebase origin/develop   # 推荐 rebase 保持线性历史

# 如果有冲突：
# git add <resolved-files>
# git rebase --continue

# 第 4 步：推送分支并创建 Pull Request
git push origin feature/note-export-pdf

# 然后在 GitHub 上创建 PR：feature/note-export-pdf → develop
```

### 紧急 Hotfix 流程

```bash
# 紧急 Bug 直接从 main 创建 hotfix 分支
git checkout main
git pull origin main
git checkout -b hotfix/security-patch-jwt-validation

# 修复 Bug
git commit -m "fix(security): validate JWT issuer claim to prevent token forgery"

# 推送并创建 PR 到 main
git push origin hotfix/security-patch-jwt-validation

# PR 合并到 main 后，也需要同步到 develop
git checkout develop
git pull origin develop
git merge main  # 或 cherry-pick 相关提交
```

---

## 常用 Git 操作

### 日常操作

```bash
# 查看当前状态
git status

# 查看修改内容
git diff
git diff --staged  # 已暂存的修改

# 暂存指定文件
git add src/main/java/com/coursebuddy/service/impl/NoteServiceImpl.java

# 暂存所有变更（谨慎使用，确认没有不需要的文件）
git add -p          # 交互式暂存（推荐，逐块确认）

# 提交
git commit -m "feat(note): add note tagging functionality"

# 查看提交历史
git log --oneline --graph --decorate -20

# 修改最后一次提交（未推送时）
git commit --amend
```

### 分支管理

```bash
# 列出本地分支
git branch

# 列出所有分支（含远程）
git branch -a

# 删除已合并的本地分支
git branch -d feature/completed-feature

# 强制删除未合并分支（确认不需要时）
git branch -D feature/abandoned-feature

# 清理已删除的远程分支引用
git remote prune origin

# 一键清理所有已合并的本地分支
git branch --merged develop | grep -v "develop\|main" | xargs git branch -d
```

### Rebase 操作

```bash
# 将分支变基到 develop 的最新状态（保持线性历史）
git fetch origin
git rebase origin/develop

# 交互式 Rebase（整理最近 5 次提交：合并/重排/修改）
git rebase -i HEAD~5

# 常用 rebase 命令：
# pick   - 保留提交（默认）
# squash - 合并到上一个提交（常用：将多个小提交合并为一个）
# reword - 修改提交消息
# drop   - 删除提交
# fixup  - 合并到上一个提交，丢弃本提交的消息
```

### Stash 操作

```bash
# 临时保存未提交的修改（切换分支时用）
git stash push -m "WIP: implementing PDF export"

# 查看 stash 列表
git stash list

# 恢复最近的 stash
git stash pop

# 恢复指定 stash
git stash apply stash@{0}
```

---

## 代码合并规范

### Rebase vs Merge

本项目推荐使用 **Rebase** 保持线性历史：

```bash
# ✅ 推荐：Rebase 保持线性历史
git rebase origin/develop

# 在 GitHub 上创建 PR 时，使用 "Squash and merge" 或 "Rebase and merge"

# ❌ 避免：Merge 产生 Merge Commit（除非有充分理由）
git merge origin/develop  # 会产生 "Merge branch 'develop' into feature/xxx"
```

### PR 合并策略

在 GitHub 仓库设置中，推荐配置：

| 合并方式 | 使用场景 |
|----------|----------|
| **Squash and merge** | 功能分支（将所有提交压缩为一个）|
| **Rebase and merge** | 提交已整理干净的分支 |
| **Create a merge commit** | 🚫 不推荐（会增加无意义的合并提交） |

---

## 冲突解决

### 解决 Rebase 冲突

```bash
# 执行 rebase 遇到冲突时
git rebase origin/develop

# 1. 查看冲突文件
git status

# 2. 编辑冲突文件，选择保留哪方的代码
# 冲突标记：
# <<<<<<< HEAD（你的修改）
# =======
# >>>>>>> origin/develop（目标分支的修改）

# 3. 标记冲突已解决
git add <resolved-file>

# 4. 继续 rebase
git rebase --continue

# 如果无法解决，放弃 rebase
git rebase --abort
```

### 使用 IDEA 解决冲突

1. 在 IDEA 中，冲突文件会标红
2. 右键 → **Git** → **Resolve Conflicts**
3. IDEA 提供三列对比视图，可以逐行选择保留哪方的修改

---

## Git 配置建议

### 全局配置

```bash
# 用户信息（必须配置）
git config --global user.name "Your Name"
git config --global user.email "you@example.com"

# 默认分支名
git config --global init.defaultBranch main

# 换行符（Windows 用 true，macOS/Linux 用 input）
git config --global core.autocrlf input   # macOS/Linux
git config --global core.autocrlf true    # Windows

# 默认推送行为（仅推送当前分支）
git config --global push.default current

# 美化 log 输出别名
git config --global alias.lg "log --oneline --graph --decorate --all"
git config --global alias.st "status -sb"

# Pull 时默认 rebase
git config --global pull.rebase true
```

### .gitignore

项目 `.gitignore` 应包含（通常已配置）：

```gitignore
# Java
target/
*.class
*.jar（非 executable JAR）

# IDE
.idea/
*.iml
.vscode/
.eclipse/

# 环境文件（绝对不提交密钥！）
.env
.env.local
*.env

# 系统文件
.DS_Store
Thumbs.db
```

---

## 查看项目历史

```bash
# 查看提交历史（美化）
git log --oneline --graph --decorate -20

# 查看某个文件的修改历史
git log --follow -p src/main/java/com/coursebuddy/service/impl/NoteServiceImpl.java

# 查找某段代码是谁写的
git blame src/main/java/com/coursebuddy/controller/NoteController.java

# 查找引入某个 Bug 的提交
git bisect start
git bisect bad              # 当前版本有 Bug
git bisect good v1.0.0      # 已知好的版本
# Git 会自动 checkout 中间版本，你测试后告诉 Git 好/坏
git bisect good / git bisect bad
```

---

## 相关文档

- [提交消息规范](./提交消息规范.md)
- [Pull Request 流程](./Pull_Request流程.md)
