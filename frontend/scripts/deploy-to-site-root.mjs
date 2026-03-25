import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'
import { execFileSync } from 'node:child_process'

const STANDARD_SITE_ROOT = '/www/wwwroot/www.bcbbs3.cn'
const SITE_ROOT = process.env.SITE_ROOT || STANDARD_SITE_ROOT
const ALLOW_CUSTOM_SITE_ROOT = process.env.ALLOW_CUSTOM_SITE_ROOT === 'true'
const FRONTEND_DIR = path.resolve(process.cwd())
const PROJECT_ROOT = path.resolve(FRONTEND_DIR, '..')
const DIST_DIR = path.resolve(FRONTEND_DIR, 'dist')
const META_FILE = path.join(SITE_ROOT, 'deploy-meta.json')
const DEPLOY_LOCK_FILE = path.join(SITE_ROOT, '.deploy.lock')
const DRY_RUN = process.env.DEPLOY_DRY_RUN === 'true'

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

async function listFilesRecursively(dir) {
  if (!(await exists(dir))) return []
  const out = []

  async function walk(currentDir) {
    const entries = await fs.readdir(currentDir, { withFileTypes: true })
    for (const ent of entries) {
      const full = path.join(currentDir, ent.name)
      if (ent.isDirectory()) {
        await walk(full)
      } else if (ent.isFile()) {
        out.push(full)
      }
    }
  }

  await walk(dir)
  return out
}

async function countFilesRecursively(dir) {
  const files = await listFilesRecursively(dir)
  return files.length
}

function nowStamp() {
  return new Date().toISOString().replace(/[:.]/g, '-')
}

function getGitCommit() {
  try {
    return execFileSync('git', ['-C', PROJECT_ROOT, 'rev-parse', '--short', 'HEAD'], {
      encoding: 'utf8'
    }).trim()
  } catch {
    return 'unknown'
  }
}

async function readText(filePath) {
  return fs.readFile(filePath, 'utf8')
}

async function assertSiteRootPolicy() {
  if (SITE_ROOT !== STANDARD_SITE_ROOT && !ALLOW_CUSTOM_SITE_ROOT) {
    throw new Error(
      `非标准部署目录被拒绝：${SITE_ROOT}。如需覆盖，请设置 ALLOW_CUSTOM_SITE_ROOT=true`
    )
  }
}

async function acquireDeployLock() {
  try {
    const payload = {
      pid: process.pid,
      startedAt: new Date().toISOString(),
      siteRoot: SITE_ROOT
    }
    await fs.writeFile(DEPLOY_LOCK_FILE, `${JSON.stringify(payload)}\n`, {
      encoding: 'utf8',
      flag: 'wx'
    })
  } catch (err) {
    if (err && typeof err === 'object' && 'code' in err && err.code === 'EEXIST') {
      throw new Error(`检测到进行中的部署任务，请稍后重试。锁文件：${DEPLOY_LOCK_FILE}`)
    }
    throw err
  }
}

async function releaseDeployLock() {
  await rmrf(DEPLOY_LOCK_FILE)
}

async function cleanupTempAssetDirs() {
  const entries = await fs.readdir(SITE_ROOT, { withFileTypes: true })
  const tempDirs = entries
    .filter((ent) => ent.isDirectory() && (ent.name.startsWith('.assets-next-') || ent.name.startsWith('.assets-prev-')))
    .map((ent) => path.join(SITE_ROOT, ent.name))

  await Promise.all(tempDirs.map((dir) => rmrf(dir)))
}

function extractAssetRefs(indexHtml) {
  const refs = new Set()
  const re = /(?:src|href)=["'](?:\.\/|\/)?(assets\/[^"'?#]+(?:\?[^"']*)?)["']/g
  let match
  while ((match = re.exec(indexHtml)) !== null) {
    refs.add(match[1])
  }
  return [...refs]
}

async function validateDistIntegrity(distDir) {
  const distIndex = path.join(distDir, 'index.html')
  const distAssets = path.join(distDir, 'assets')

  if (!(await exists(distIndex))) {
    throw new Error(`缺少构建产物：${distIndex}`)
  }
  if (!(await exists(distAssets))) {
    throw new Error(`缺少构建产物：${distAssets}`)
  }

  const html = await readText(distIndex)
  const refs = extractAssetRefs(html)
  const missing = []

  await Promise.all(
    refs.map(async (ref) => {
      const fp = path.join(distDir, ref)
      if (!(await exists(fp))) {
        missing.push(ref)
      }
    })
  )

  if (missing.length > 0) {
    throw new Error(`dist 完整性校验失败，index.html 引用了不存在的资源：${missing.join(', ')}`)
  }

  return {
    distIndex,
    distAssets,
    referencedAssetCount: refs.length
  }
}

async function atomicSwapAssets(srcAssets, destAssets) {
  const stamp = nowStamp()
  const stagedAssets = path.join(SITE_ROOT, `.assets-next-${stamp}`)
  const backupAssets = path.join(SITE_ROOT, `.assets-prev-${stamp}`)

  await rmrf(stagedAssets)
  await rmrf(backupAssets)

  await copyDir(srcAssets, stagedAssets)

  let hasBackup = false
  try {
    if (await exists(destAssets)) {
      await fs.rename(destAssets, backupAssets)
      hasBackup = true
    }
    await fs.rename(stagedAssets, destAssets)
    return {
      backupAssets: hasBackup ? backupAssets : null
    }
  } catch (err) {
    // Best-effort rollback for assets when swap fails.
    if (!(await exists(destAssets)) && (await exists(backupAssets))) {
      await fs.rename(backupAssets, destAssets)
    }
    await rmrf(stagedAssets)
    throw err
  }
}

async function rollbackAssets(destAssets, backupAssets) {
  if (!backupAssets || !(await exists(backupAssets))) {
    return false
  }

  const failedAssets = `${destAssets}.failed-${nowStamp()}`
  try {
    if (await exists(destAssets)) {
      await fs.rename(destAssets, failedAssets)
    }
    await fs.rename(backupAssets, destAssets)
    await rmrf(failedAssets)
    return true
  } catch (err) {
    // Keep failed assets for manual inspection if rollback itself fails.
    return false
  }
}

async function syncDistRootEntries(distDir) {
  const entries = await fs.readdir(distDir, { withFileTypes: true })
  await Promise.all(
    entries
      .filter((ent) => ent.name !== 'assets')
      .map(async (ent) => {
        const src = path.join(distDir, ent.name)
        const dest = path.join(SITE_ROOT, ent.name)
        if (ent.isDirectory()) return copyDir(src, dest)
        if (ent.isFile()) return copyFile(src, dest)
      })
  )
}

async function verifyDeployedIndex(siteRoot) {
  const deployedIndex = path.join(siteRoot, 'index.html')
  if (!(await exists(deployedIndex))) {
    throw new Error(`部署后校验失败：缺少 ${deployedIndex}`)
  }

  const html = await readText(deployedIndex)
  const refs = extractAssetRefs(html)
  const missing = []

  await Promise.all(
    refs.map(async (ref) => {
      const fp = path.join(siteRoot, ref)
      if (!(await exists(fp))) {
        missing.push(ref)
      }
    })
  )

  if (missing.length > 0) {
    throw new Error(`部署后校验失败，index.html 引用了不存在的线上资源：${missing.join(', ')}`)
  }

  return refs.length
}

async function writeDeployMeta({ assetFileCount, assetDirCount, referencedAssetCount }) {
  const payload = {
    deployedAt: new Date().toISOString(),
    siteRoot: SITE_ROOT,
    gitCommit: getGitCommit(),
    // Keep assetCount for backward compatibility; now it means real file count.
    assetCount: assetFileCount,
    assetFileCount,
    assetDirCount,
    referencedAssetCount
  }

  await fs.writeFile(META_FILE, `${JSON.stringify(payload, null, 2)}\n`, 'utf8')
}

async function runHealthCheck() {
  const url = process.env.DEPLOY_HEALTHCHECK_URL
  if (!url) return

  console.log(`Running health check: ${url}`)
  const resp = await fetch(url, {
    method: 'GET',
    headers: {
      'Cache-Control': 'no-cache'
    }
  })

  if (!resp.ok) {
    throw new Error(`健康检查失败：${url} 返回 ${resp.status}`)
  }

  console.log(`Health check passed: ${resp.status}`)
}

async function main() {
  await assertSiteRootPolicy()
  if (!(await exists(DIST_DIR))) {
    throw new Error(`dist 不存在：${DIST_DIR}（请先运行 npm run build）`)
  }
  await acquireDeployLock()

  let swapResult = null
  const destAssets = path.join(SITE_ROOT, 'assets')

  try {
    await cleanupTempAssetDirs()
    const { distAssets } = await validateDistIntegrity(DIST_DIR)
    const distAssetFileCount = await countFilesRecursively(distAssets)
    console.log(`dist assets file count: ${distAssetFileCount}`)

    if (DRY_RUN) {
      console.log('Dry-run mode enabled. Validation passed, no files were deployed.')
      return
    }

    console.log(`Deploying assets to ${destAssets} ...`)
    swapResult = await atomicSwapAssets(distAssets, destAssets)

    const deployedAssetFileCount = await countFilesRecursively(destAssets)
    if (deployedAssetFileCount !== distAssetFileCount) {
      throw new Error(
        `部署后资源文件数不一致：dist=${distAssetFileCount}, deployed=${deployedAssetFileCount}`
      )
    }

    const deployedAssetTopEntries = await fs.readdir(destAssets)
    console.log(
      `Deployed ${deployedAssetFileCount} asset files in ${deployedAssetTopEntries.length} top-level entries (atomic swap, no pre-clean required)`
    )

    await syncDistRootEntries(DIST_DIR)

    const deployedRefs = await verifyDeployedIndex(SITE_ROOT)
    await writeDeployMeta({
      assetFileCount: deployedAssetFileCount,
      assetDirCount: deployedAssetTopEntries.length,
      referencedAssetCount: deployedRefs
    })
    await runHealthCheck()

    // Deployment fully succeeded; old assets can be safely removed now.
    if (swapResult?.backupAssets) {
      await rmrf(swapResult.backupAssets)
    }

    console.log(`Deployed frontend to ${SITE_ROOT}`)
    console.log(`Wrote deploy metadata: ${META_FILE}`)
  } catch (err) {
    // Rollback to previous assets when deployment fails after swap.
    if (swapResult?.backupAssets) {
      const rolledBack = await rollbackAssets(destAssets, swapResult.backupAssets)
      if (rolledBack) {
        console.error('Deployment failed. Assets rolled back to previous version.')
      } else {
        console.error('Deployment failed. Asset rollback did not complete automatically.')
      }
    }
    throw err
  } finally {
    await releaseDeployLock()
  }
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
