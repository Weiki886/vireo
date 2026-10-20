# Vireo 仓库协作规范（AGENTS.md）

> 本文件随仓库版本控制，是所有 AI 编程工具（Codex、Claude Code 等）与贡献者在本仓库工作时必须遵循的工程规范。
> 它由 `github-engineering-workflow` 规范按本仓库实际情况裁剪落地，内容自包含，不依赖特定 AI 工具。

## 仓库概况

- Monorepo，两个子项目：
  - `vireo-backend/`：Spring Boot 3 / Java 21 / Maven，端口 8123，context-path `/api`
  - `vireo-frontend/`：Vue 3 + Vite 7 + TypeScript / npm
- 默认分支 `main`，远程 `origin`（GitHub 公开仓库）。
- CI（GitHub Actions，按路径过滤触发）：
  - `backend-ci`：`vireo-backend/**` 变化时执行 `./mvnw -B -ntp verify`
  - `frontend-ci`：`vireo-frontend/**` 变化时执行 `npm ci && npm run build`（含类型检查）

## 核心原则

- GitHub 是工程事实来源；每个实质改动走完整链路：Issue → 分支 → Commit → PR → Review/CI → Merge → 删除分支。
- 个人学习项目按 solo 裁剪：无 Milestone、无强制第二人 Review、无部署门禁；但审计链（Issue、验证、自审）不省略。
- 重大或难以逆转的决策写 ADR，追加记录，不覆盖旧决策。
- 流程强度与风险匹配；不臆造审批人数、覆盖率、阈值；未配置的能力如实标注为缺口。

## 语言约定

- **必须英文**：分支名、Commit 标题、PR 标题、Tag、Label。
- **可用中文**：Commit 正文、Issue 标题与正文、PR 正文。

## 标准工作链路

### 1. Issue

- 每个实质改动先建 Issue；个人项目无需归属 Milestone。
- 内容至少包含：背景与价值、目标、非目标、风险级别与依据、设计要点、验收标准、验证计划。
- 打 Labels：至少一个 `type:*` 和一个 `risk:*`。
- 一个 Issue 应能由一个小而聚焦的 PR 完成；过大就按可独立验证的增量拆分。

### 2. 分支

- 从最新 `main` 创建：`git checkout main && git pull && git checkout -b <type>/<issue>-<slug>`。
- 命名示例：`feat/12-user-login`、`chore/5-ci-setup`；短生命周期，合并后即删（本地与远程）。
- 不直接在 `main` 上开发或提交。

### 3. Commit

- 格式：`<type>(<scope>): <英文祈使句>`，如 `feat(auth): add session validation`。
- 每个 Commit 表达一个完整意图；提交前运行对应子项目的验证命令并如实记录结果。
- 禁止提交密钥、生成物（`node_modules`、`dist`、`target`）、IDE 配置（`.idea`）。

### 4. Pull Request

- 标题英文；正文可用中文，包含：`Closes #<issue>`、风险分级与依据、关键改动、非目标、验证证据、AI 使用说明（使用范围 / 关键 Prompt 摘要 / 人工复核）。
- Labels 继承 Issue 的 `type:*` 与 `risk:*`。
- 小而聚焦；包含多个目标或跨多个领域时先拆分。

### 5. Review、CI 与 Merge

- 先自审再合并；AI Review 只能补充，不能替代人工 Review。
- CI 检查（`backend-ci` / `frontend-ci`）失败不得合并。注意：仅改动根目录文档等非子项目路径时 CI 不会触发（路径过滤），属预期行为。
- 合并后立即删除短生命周期分支，并执行本地收尾：

  `git checkout main && git pull && git branch -d <branch>`

### 6. 验证命令（提交 / PR 前必跑对应项）

- 后端：`cd vireo-backend && ./mvnw -B -ntp verify`
- 前端：`cd vireo-frontend && npm ci && npm run build`

## Labels 目录

- `type:feature` / `type:bug` / `type:docs` / `type:chore` / `type:security`（与 Commit type 词表对齐）
- `risk:low` / `risk:standard` / `risk:high` / `risk:emergency`
- 个人项目采用最小集合；确有需要时再扩 `area:*`、`priority:*`，不提前建全。

## 风险分级

- `low`：文档、测试、初始化、不改变运行行为的改动。
- `standard`：边界清楚、可独立验证和回滚的一般功能或缺陷修复。
- `high`：身份权限、资金、隐私、公开 API、数据迁移、基础设施、供应链或较大爆炸半径。
- `emergency`：为恢复服务或处置安全事件必须加速的改动（须授权并留痕）。

## Git 与远程安全边界

- 远程写操作（push、merge、建/改 Issue、PR、标签、Release、ruleset）必须有用户明确授权。
- 禁止 `push --force`、删除远程分支/Tag、改写共享历史、绕过保护规则，除非用户明确指定精确目标并授权。
- 不盲目 `git add -A`；暂存前逐项检查变更清单，确认无凭据与生成物。
- 密钥、令牌、个人信息不得进入代码、Commit、Issue、PR、日志或 AI Prompt；发现泄漏优先轮换，仅从后续 Commit 删除字符串不能消除泄漏。
- 远程状态必须通过实际命令或 GitHub 证据确认，不能根据本地状态推断。
- `CLAUDE.md` 是个人学习用本地文件，经 `.git/info/exclude` 保持忽略，不得提交。

## 已知缺口（如实标注，不得虚构已配置）

- 分支保护 / ruleset 未启用：CI 通过目前不是合并的强制条件。
- 无部署 / CD、无 Release 流程、无 Milestone。
- 前端 ESLint 未纳入 CI（脚本带 `--fix`，待评估 CI 模式）。

## 每次交付的汇报格式

1. 交付目标与关联 Issue；
2. 创建或修改的分支、Commit、PR、Label、Release 及链接/标识；
3. 实际执行的本地验证与 CI 结果；
4. 风险级别与依据；
5. 未满足门禁、例外与阻塞；
6. 下一项最小可执行动作。

没有证据的项目必须明确写为未执行、未验证或 `UNKNOWN`。
