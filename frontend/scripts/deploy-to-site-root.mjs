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

  // Clean old assets before deploying new ones.
  // Old non-destructive strategy caused assets to accumulate (7000+ files),
  // and browsers loading cached index.html would resolve stale chunk hashes,
  // making the site appear "rolled back" to an old version.
  // Nginx already sets no-cache on index.html, so users always get the latest
  // index.html which references the correct new chunk hashes.
  if (await exists(destAssets)) {
    console.log(`Cleaning old assets in ${destAssets} ...`)
    await rmrf(destAssets)
  }
  await copyDir(srcAssets, destAssets)
  
  // Log deployed asset count for verification
  const deployedAssets = await fs.readdir(destAssets)
  console.log(`Deployed ${deployedAssets.length} asset files (clean deploy)`)

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

