import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'

const SITE_ROOT = process.env.SITE_ROOT || '/www/wwwroot/www.bcbbs3.cn'
const FRONTEND_DIR = path.resolve(process.cwd())
const DIST_DIR = path.resolve(FRONTEND_DIR, 'dist')

async function exists(p) {
  try {
    await fs.access(p)
    return true
  } catch {
    return false
  }
}

async function rmrf(p) {
  await fs.rm(p, { recursive: true, force: true })
}

async function copyFile(src, dest) {
  await fs.mkdir(path.dirname(dest), { recursive: true })
  await fs.copyFile(src, dest)
}

async function copyDir(srcDir, destDir) {
  await fs.mkdir(destDir, { recursive: true })
  const entries = await fs.readdir(srcDir, { withFileTypes: true })
  await Promise.all(
    entries.map(async (ent) => {
      const src = path.join(srcDir, ent.name)
      const dest = path.join(destDir, ent.name)
      if (ent.isDirectory()) return copyDir(src, dest)
      if (ent.isFile()) return copyFile(src, dest)
    })
  )
}

async function main() {
  if (!(await exists(DIST_DIR))) {
    throw new Error(`dist 不存在：${DIST_DIR}（请先运行 npm run build）`)
  }

  // dist 根目录内容（index.html、favicon、images、vite.svg 等来自 publicDir 的内容）
  // 之前只同步了 index.html + assets，容易遗漏 publicDir 的文件更新，属于潜在 BUG。
  const distEntries = await fs.readdir(DIST_DIR, { withFileTypes: true })

  const srcAssets = path.join(DIST_DIR, 'assets')
  const destAssets = path.join(SITE_ROOT, 'assets')

  if (!(await exists(srcAssets))) throw new Error(`缺少 ${srcAssets}`)

  // 重要：不要删除站点根目录的 assets。
  // 原因：用户浏览器可能缓存了旧的 index-*.js（或旧的路由 chunk 引用）。
  // 如果我们删除旧 hash 文件，会导致动态 import 404（你截图里的 ChangePassword-*.js 404）。
  // 解决：只“覆盖/新增”本次构建的资源，保留历史 hash 文件，避免线上用户缓存不一致时白屏/无法跳转。
  await copyDir(srcAssets, destAssets)

  // 同步 dist 根目录的其它文件/目录到站点根目录（不做删除，只覆盖/新增）
  await Promise.all(
    distEntries
      .filter((ent) => ent.name !== 'assets')
      .map(async (ent) => {
        const src = path.join(DIST_DIR, ent.name)
        const dest = path.join(SITE_ROOT, ent.name)
        if (ent.isDirectory()) return copyDir(src, dest)
        if (ent.isFile()) return copyFile(src, dest)
      })
  )

  // eslint-disable-next-line no-console
  console.log(`Deployed frontend to ${SITE_ROOT}`)
}

main().catch((err) => {
  // eslint-disable-next-line no-console
  console.error(err)
  process.exit(1)
})

