# GitHub 推送失败 — 排查与处理（本仓库）

远程仓库：[rainzl/LegalDevAssistant](https://github.com/rainzl/LegalDevAssistant.git)

---

## 1. 常见现象

| 现象 | 可能原因 |
|------|----------|
| `git push` 长时间无输出后失败 / `Could not connect to server` | 本机到 **`github.com:443`（HTTPS）** 的 TCP 被墙、公司策略或网络异常。 |
| `Permission denied (publickey)` | 已改用 **SSH**，但 GitHub 账户未添加本机 **SSH 公钥**，或用了错误账户。 |
| `403` / `Authentication failed`（HTTPS） | 需使用 **Personal Access Token (PAT)** 代替密码，或凭据过期。 |

---

## 2. 本仓库已采用的连接方式（推荐）

在部分网络环境下，**直连 `github.com:443` 的 HTTPS 不可用**，但 **`ssh.github.com:443` 可用**。

本仓库已配置为：

- **远程地址**：`git@ssh.github.com:rainzl/LegalDevAssistant.git`
- **仅对本仓库生效**：`core.sshCommand = ssh -p 443`（通过 `git config` 写入 **`.git/config`**，未改全局配置）

首次通过 SSH 连接时，若提示 **Host key**，请确认指纹与 [GitHub SSH 指纹文档](https://docs.github.com/zh/authentication/keeping-your-account-and-data-secure/githubs-ssh-key-fingerprints) 一致后接受。

---

## 3. 你必须完成的一步：把 SSH 公钥加到 GitHub

1. 在本机查看公钥（PowerShell）：
   ```powershell
   Get-Content $env:USERPROFILE\.ssh\id_ed25519.pub
   ```
   若没有 `id_ed25519.pub`，可使用 `id_rsa.pub`，或 `ssh-keygen -t ed25519 -C "你的邮箱"` 生成一对密钥。

2. 浏览器打开：GitHub → **Settings** → **SSH and GPG keys** → **New SSH key**，粘贴公钥**整行**保存。

3. 测试（应出现 “successfully authenticated” 字样）：
   ```powershell
   ssh -T -p 443 git@ssh.github.com
   ```

4. 再推送：
   ```powershell
   cd <本仓库根目录>
   git push -u origin main
   ```

---

## 4. 若你更想继续用 HTTPS

需先保证本机能访问 **`https://github.com`**（例如 VPN、系统代理）。

然后可任选：

- **凭据**：使用 [Personal Access Token](https://github.com/settings/tokens) 作为密码；Windows 上常由 **Git Credential Manager** 弹窗保存。
- **仅为 GitHub 配置 HTTP 代理**（示例，端口按你本机代理软件修改）：
  ```powershell
  git config --global http.https://github.com.proxy http://127.0.0.1:7890
  ```
  恢复取消代理：
  ```powershell
  git config --global --unset http.https://github.com.proxy
  ```

将远程改回 HTTPS 示例：

```powershell
git remote set-url origin https://github.com/rainzl/LegalDevAssistant.git
git config --unset core.sshCommand
```

---

## 5. 自检命令（Windows）

```powershell
# HTTPS 443 是否通
Test-NetConnection github.com -Port 443

# SSH 走 443 是否通
Test-NetConnection ssh.github.com -Port 443

# 当前远程与本仓库 SSH 命令
git remote -v
git config --get core.sshCommand
```

---

## 6. 仍失败时

- 确认 GitHub 仓库名、用户名无误，且你对 **`rainzl/LegalDevAssistant`** 有 **push** 权限。  
- 空仓库的**第一次**推送一般为：`git push -u origin main`（分支名与本地一致）。  
- 查阅 GitHub 状态页：[www.githubstatus.com](https://www.githubstatus.com/)
