# Pull Request 流程

本文档描述 Course Buddy Backend 项目的 Pull Request（PR）创建、审查和合并流程。

---

## 概述

所有代码变更必须通过 Pull Request 流程合并到受保护分支（`main`、`develop`），这是保证代码质量的关键环节。

**PR 的核心价值：**
- 代码审查：发现潜在 Bug 和设计问题
- 知识共享：团队成员了解系统变更
- 质量保证：CI 自动化检查通过后才能合并
- 变更记录：提供清晰的变更历史

---

## PR 生命周期

```
开发者               代码审查者             自动化检查
   │                     │                      │
   ├─ 完成开发 ──────────►│                      │
   │                     │                      │
   ├─ 创建 PR ─────────────────────────────────►│
   │                     │                      ├─ 单元测试
   │                     │                      ├─ 代码构建
   │                     │                      ├─ 代码风格检查
   │                     │                      │
   │◄── 审查反馈 ─────────┤◄─── CI 结果 ─────────┤
   │                     │                      │
   ├─ 修改代码 ──────────►│                      │
   │                     │◄─── 重新触发 CI ──────┤
   │                     │                      │
   │◄── 审查通过 ─────────┤                      │
   │                     │                      │
   ├─ 合并 PR ───────────►│                      │
   │                     │                      │
   ├─ 删除功能分支 ────────►│                      │
```

---

## 创建 PR 前的检查清单

在创建 PR 之前，开发者应自行完成以下检查：

### 代码检查

```bash
# ✅ 确保代码可以成功编译
./mvnw compile

# ✅ 确保所有测试通过
./mvnw test

# ✅ 检查代码风格（如有配置）
./mvnw checkstyle:check

# ✅ 确保没有遗留的调试代码
grep -r "System.out.println\|TODO\|FIXME\|HACK" src/main/java/ --include="*.java"
```

### 提交检查

```bash
# ✅ 确认提交消息符合规范
git log --oneline -10

# ✅ 确保分支与目标分支保持同步
git fetch origin
git rebase origin/develop

# ✅ 删除无意义的临时提交
git rebase -i HEAD~N  # N 为需要整理的提交数
```

### 自测清单

- [ ] 新增功能已手动测试
- [ ] 修复的 Bug 已验证不再复现
- [ ] 相关接口在 Swagger UI 中可正常调用
- [ ] 新增的数据库迁移脚本命名正确（`V{N+1}__{描述}.sql`）
- [ ] 配置变更已更新 `.env.example` 或文档
- [ ] 敏感信息（密钥、密码）未提交到代码

---

## 创建 Pull Request

### 在 GitHub 上创建

1. 推送分支：
   ```bash
   git push origin feature/note-export-pdf
   ```

2. 访问 GitHub 仓库，通常会看到 "Compare & pull request" 提示横幅

3. 选择：
   - **Base branch**：`develop`（日常功能）或 `main`（紧急修复）
   - **Compare branch**：你的功能分支

4. 填写 PR 标题和描述

### PR 标题格式

PR 标题遵循与提交消息相同的 Conventional Commits 格式：

```
feat(note): add PDF export functionality
fix(auth): resolve token expiry edge case
refactor(course): simplify course enrollment logic
docs: update API documentation for file upload
```

### PR 描述模板

```markdown
## 变更说明

<!-- 简要描述本次 PR 的目的和主要变更 -->

本次 PR 实现了笔记 PDF 导出功能，支持将 Markdown 笔记转换为 PDF 格式，
使用 Apache PDFBox 进行 PDF 生成。

## 变更内容

<!-- 列出主要的代码变更 -->

- 新增 `NoteExportController`，提供 `GET /v1/notes/{id}/export/pdf` 接口
- 新增 `NoteExportServiceImpl`，实现 PDF 生成逻辑
- 在 `application.yml` 中添加 PDF 导出配置（页边距、字体大小等）

## 测试验证

<!-- 描述如何测试这些变更 -->

- [ ] 单元测试：`NoteExportServiceImplTest` 覆盖正常路径和边界情况
- [ ] 集成测试：导出包含图片、代码块的笔记，验证 PDF 格式正确
- [ ] 手动测试：通过 Swagger UI 调用导出接口，下载并验证 PDF 内容

## 关联 Issue

<!-- 关联 GitHub Issue -->

Closes #42

## 截图（如适用）

<!-- 如果有 UI 变更或可视化结果，附上截图 -->

## 注意事项

<!-- 任何审查者需要特别关注的事项 -->

- PDF 生成对大型笔记（>100KB）可能较慢，已添加超时配置
- 暂不支持笔记中的视频内容，视频链接会转为文本

## 破坏性变更

<!-- 是否有 Breaking Change？影响范围？ -->

无
```

---

## 代码审查规范

### 审查者职责

1. **理解变更意图**：先读 PR 描述，再看代码
2. **检查正确性**：逻辑是否正确，边界情况是否处理
3. **检查代码规范**：是否符合 [Java 代码规范](../代码规范/Java代码规范.md)
4. **检查安全性**：有无注入、权限漏洞等安全问题
5. **检查测试覆盖**：关键逻辑是否有测试
6. **给出建设性反馈**：不仅指出问题，也给出建议

### 审查优先级

| 级别 | 说明 | 处理要求 |
|------|------|----------|
| **Blocking** | 必须修改才能合并（Bug、安全漏洞、严重违规） | 必须解决 |
| **Suggestion** | 建议改进（代码优化、更好的实现方式） | 建议解决 |
| **Nitpick** | 细节建议（命名、格式等） | 可选 |
| **Question** | 疑问或需要解释的地方 | 需要回复 |

### 审查语言

```
# ✅ 建设性反馈
Suggestion: 这里可以使用 LambdaQueryWrapper 替代字符串拼接，避免潜在的 SQL 注入：
```java
LambdaQueryWrapper<NotePO> wrapper = Wrappers.<NotePO>lambdaQuery()
    .eq(NotePO::getOwnerUsername, username);
```

# ✅ 指出潜在问题
Blocking: 第 45 行直接返回数据库实体 `NotePO`，这会暴露敏感字段（如 deletedAt）。
应该转换为 `NoteVO` 再返回。

# ✅ 提问
Question: 为什么这里选择在 Controller 层而不是 Service 层做权限校验？
有什么特殊原因吗？

# ❌ 不好的反馈
这段代码写得不好，重写一下。（不具体，不建设）
这里不对，改掉。（没有说明理由）
```

### 代码审查重点

```
✅ 必须检查：
- 业务逻辑正确性
- 权限校验（是否正确调用 SecurityUtils.getCurrentUser()）
- 异常处理（是否使用项目标准异常类）
- SQL 注入、XSS 等安全问题
- 数据库操作事务性
- 资源泄漏（文件流、连接等）
- 硬编码的密钥、密码

⚠️ 建议检查：
- 代码可读性
- 命名规范
- 不必要的复杂度
- 重复代码（DRY 原则）
- 性能问题（N+1 查询等）

ℹ️ 可选检查：
- 注释是否充分
- 代码格式
- 测试覆盖率
```

---

## 处理审查反馈

### 作为 PR 作者

```bash
# 根据审查意见修改代码
# ...

# 提交修改
git add .
git commit -m "fix(note): address PR review comments

- Use LambdaQueryWrapper instead of string concatenation
- Convert NotePO to NoteVO before returning
- Add null check for optional categoryId parameter"

# 推送更新（PR 会自动更新）
git push origin feature/note-export-pdf
```

**在 GitHub 上回复审查意见：**

- 修改完成的，回复 "Done" 或说明如何修改
- 有异议的，礼貌地解释理由并讨论
- 已修复但不同意建议的，说明保持原样的理由

### 审查者回复

- 满意修改后，将 Comment 标记为 "Resolved"
- 所有 Blocking 问题解决后，给出 "Approve"

---

## PR 合并规范

### 合并前提条件

- [ ] 至少 **1 位**审查者给出 Approve
- [ ] 所有 CI 检查通过（构建、测试）
- [ ] 所有 Blocking 评论已解决
- [ ] 分支与目标分支保持同步（无冲突）
- [ ] PR 描述完整

### 合并方式

本项目推荐使用 **Squash and merge**（将所有提交压缩为一个）：

```
优点：
- 保持 develop/main 分支历史简洁
- 每个 PR 对应一个提交，易于追溯
- 开发过程中的 WIP 提交不会污染主分支历史

何时使用 Rebase and merge：
- 功能分支的每个提交都是独立、有意义的
- 需要保留完整的提交历史

何时禁止 Merge commit：
- 本项目不使用 Create a merge commit
```

### 合并后的操作

```bash
# 合并后，作者应删除已合并的功能分支

# GitHub 上合并时可勾选 "Delete branch"

# 或者手动删除
git push origin --delete feature/note-export-pdf
git branch -d feature/note-export-pdf
```

---

## 紧急 PR（Hotfix）

生产环境紧急修复可以简化流程：

1. 创建 `hotfix/` 分支，尽快修复
2. **最少 1 人审查**（可以加急审查）
3. CI 通过后立即合并到 `main`
4. **同步到 `develop`**（避免 develop 丢失修复）

```bash
# 创建 hotfix 分支
git checkout -b hotfix/fix-authentication-bypass main

# ... 修复代码 ...
git commit -m "fix(security): prevent authentication bypass via malformed JWT"

# 推送并创建 PR（目标：main）
git push origin hotfix/fix-authentication-bypass

# 合并到 main 后，同步到 develop
git checkout develop
git cherry-pick <hotfix-commit-sha>
git push origin develop
```

---

## CI 自动化检查

每次创建或更新 PR 时，GitHub Actions 会自动执行：

```yaml
# .github/workflows/ci.yml（参考配置）
name: CI

on:
  pull_request:
    branches: [main, develop]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build
        run: ./mvnw compile -B
      - name: Test
        run: ./mvnw test -B
```

**PR 合并前 CI 必须通过。**

---

## 常见问题

### Q：我的 PR 被阻塞了，但没有人审查怎么办？

在团队 Chat 中 @ 相关成员请求审查，说明 PR 的紧迫程度。

### Q：审查者提出的建议我不同意，怎么办？

礼貌地在评论中解释你的理由。如果是 Suggestion（非 Blocking），你可以说明为什么保持原实现。如有分歧，可以邀请第三方参与讨论。

### Q：PR 有冲突怎么解决？

```bash
git fetch origin
git rebase origin/develop   # 或 git merge origin/develop
# 解决冲突后
git push --force-with-lease origin feature/your-branch
```

注意：使用 `--force-with-lease` 而不是 `--force`，更安全。

### Q：已合并的 PR 发现了 Bug 怎么办？

创建新的 `fix/` 分支，通过新的 PR 修复，并在 PR 中关联原始 PR 编号。

---

## 相关文档

- [Git 工作流说明](./Git工作流说明.md)
- [提交消息规范](./提交消息规范.md)
- [Java 代码规范](../代码规范/Java代码规范.md)
