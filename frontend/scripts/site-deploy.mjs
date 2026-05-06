import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'
import { execFileSync } from 'node:child_process'

const PROJECT_ROOT = path.resolve(process.cwd(), '..')
const FRONTEND_DIR = path.resolve(PROJECT_ROOT, 'frontend')
const DIST_DIR = path.resolve(FRONTEND_DIR, 'dist')
const STANDARD_SITE_ROOT = '/www/wwwroot/www.bcbbs3.cn'
const SITE_ROOT = process.env.SITE_ROOT || STANDARD_SITE_ROOT
const ALLOW_CUSTOM_SITE_ROOT = process.env.ALLOW_CUSTOM_SITE_ROOT === 'true'
const DRY_RUN = process.env.DEPLOY_DRY_RUN === 'true'
const CLEAN_ASSETS_ONLY = process.argv.includes('--clean-assets-only')
const SITE_ASSETS_DIR = path.join(SITE_ROOT, 'assets')
const DEPLOY_META_FILE = path.join(SITE_ROOT, 'deploy-meta.json')
const DEPLOY_LOCK_FILE = path.join(SITE_ROOT, '.deploy.lock')

async function exists(targetPath) {
  try {
    await fs.access(targetPath)
    return true
  } catch {
    return false
  }
}

async function ensureSiteRootPolicy() {
  if (SITE_ROOT !== STANDARD_SITE_ROOT && !ALLOW_CUSTOM_SITE_ROOT) {
    throw new Error(`Refusing non-standard site root: ${SITE_ROOT}. Set ALLOW_CUSTOM_SITE_ROOT=true to override.`)
  }
}

async function removeTree(targetPath) {
  await fs.rm(targetPath, { recursive: true, force: true })
}

async function copyFile(source, target) {
  await fs.mkdir(path.dirname(target), { recursive: true })
  await fs.copyFile(source, target)
}

async function copyTree(sourceDir, targetDir) {
  await fs.mkdir(targetDir, { recursive: true })
  const entries = await fs.readdir(sourceDir, { withFileTypes: true })
  for (const entry of entries) {
    const source = path.join(sourceDir, entry.name)
    const target = path.join(targetDir, entry.name)
    if (entry.isDirectory()) {
      await copyTree(source, target)
    } else if (entry.isFile()) {
      await copyFile(source, target)
    }
  }
}

async function walkFiles(targetDir) {
  const files = []

  async function visit(currentDir) {
    const entries = await fs.readdir(currentDir, { withFileTypes: true })
    for (const entry of entries) {
      const currentPath = path.join(currentDir, entry.name)
      if (entry.isDirectory()) {
        await visit(currentPath)
      } else if (entry.isFile()) {
        files.push(currentPath)
      }
    }
  }

  await visit(targetDir)
  return files
}

async function countFiles(targetDir) {
  return (await walkFiles(targetDir)).length
}

function extractAssetRefs(html) {
  const refs = new Set()
  const pattern = /(?:src|href)=["'](?:\.\/|\/)?(assets\/[^"'?#]+(?:\?[^"']*)?)["']/g
  let match
  while ((match = pattern.exec(html)) !== null) {
    refs.add(match[1])
  }
  return [...refs]
}

function getGitCommit() {
  try {
    return execFileSync('git', ['-C', PROJECT_ROOT, 'rev-parse', '--short', 'HEAD'], { encoding: 'utf8' }).trim()
  } catch {
    return 'unknown'
  }
}

async function readText(filePath) {
  return fs.readFile(filePath, 'utf8')
}

async function acquireLock() {
  await fs.writeFile(
    DEPLOY_LOCK_FILE,
    JSON.stringify({
      pid: process.pid,
      startedAt: new Date().toISOString(),
      siteRoot: SITE_ROOT
    }, null, 2) + '\n',
    { encoding: 'utf8', flag: 'wx' }
  )
}

async function releaseLock() {
  await removeTree(DEPLOY_LOCK_FILE)
}

async function cleanupTempDirs() {
  if (!(await exists(SITE_ROOT))) return
  const entries = await fs.readdir(SITE_ROOT, { withFileTypes: true })
  await Promise.all(
    entries
      .filter((entry) => entry.isDirectory() && (entry.name.startsWith('.assets-next-') || entry.name.startsWith('.assets-prev-')))
      .map((entry) => removeTree(path.join(SITE_ROOT, entry.name)))
  )
}

async function validateDist() {
  const indexPath = path.join(DIST_DIR, 'index.html')
  const assetsDir = path.join(DIST_DIR, 'assets')

  if (!(await exists(indexPath))) {
    throw new Error(`Missing build artifact: ${indexPath}`)
  }
  if (!(await exists(assetsDir))) {
    throw new Error(`Missing build artifact: ${assetsDir}`)
  }

  const html = await readText(indexPath)
  const refs = extractAssetRefs(html)
  const missing = []

  for (const ref of refs) {
    if (!(await exists(path.join(DIST_DIR, ref)))) {
      missing.push(ref)
    }
  }

  if (missing.length > 0) {
    throw new Error(`dist validation failed, missing assets referenced by index.html: ${missing.join(', ')}`)
  }

  return {
    indexPath,
    assetsDir,
    assetRefCount: refs.length,
    assetFileCount: await countFiles(assetsDir)
  }
}

async function deployAssets(sourceAssetsDir, targetAssetsDir) {
  const stamp = new Date().toISOString().replace(/[:.]/g, '-')
  const stagedDir = path.join(SITE_ROOT, `.assets-next-${stamp}`)
  const backupDir = path.join(SITE_ROOT, `.assets-prev-${stamp}`)

  await removeTree(stagedDir)
  await removeTree(backupDir)
  await copyTree(sourceAssetsDir, stagedDir)

  let hasBackup = false
  try {
    if (await exists(targetAssetsDir)) {
      await fs.rename(targetAssetsDir, backupDir)
      hasBackup = true
    }
    await fs.rename(stagedDir, targetAssetsDir)
    return { backupDir: hasBackup ? backupDir : null }
  } catch (error) {
    if (!(await exists(targetAssetsDir)) && (await exists(backupDir))) {
      await fs.rename(backupDir, targetAssetsDir)
    }
    await removeTree(stagedDir)
    throw error
  }
}

async function rollbackAssets(targetAssetsDir, backupDir) {
  if (!backupDir || !(await exists(backupDir))) {
    return false
  }

  const failedTarget = `${targetAssetsDir}.failed-${new Date().toISOString().replace(/[:.]/g, '-')}`
  try {
    if (await exists(targetAssetsDir)) {
      await fs.rename(targetAssetsDir, failedTarget)
    }
    await fs.rename(backupDir, targetAssetsDir)
    await removeTree(failedTarget)
    return true
  } catch {
    return false
  }
}

async function syncRootEntries() {
  const entries = await fs.readdir(DIST_DIR, { withFileTypes: true })
  for (const entry of entries) {
    if (entry.name === 'assets') continue
    const source = path.join(DIST_DIR, entry.name)
    const target = path.join(SITE_ROOT, entry.name)
    if (entry.isDirectory()) {
      await copyTree(source, target)
    } else if (entry.isFile()) {
      await copyFile(source, target)
    }
  }
}

async function verifyDeployedIndex() {
  const indexPath = path.join(SITE_ROOT, 'index.html')
  if (!(await exists(indexPath))) {
    throw new Error(`Deployment verification failed: missing ${indexPath}`)
  }

  const html = await readText(indexPath)
  const refs = extractAssetRefs(html)
  const missing = []
  for (const ref of refs) {
    if (!(await exists(path.join(SITE_ROOT, ref)))) {
      missing.push(ref)
    }
  }

  if (missing.length > 0) {
    throw new Error(`Deployment verification failed, missing online assets: ${missing.join(', ')}`)
  }

  return refs.length
}

async function writeMeta({ assetFileCount, assetDirCount, assetRefCount }) {
  const payload = {
    deployedAt: new Date().toISOString(),
    siteRoot: SITE_ROOT,
    gitCommit: getGitCommit(),
    assetCount: assetFileCount,
    assetFileCount,
    assetDirCount,
    referencedAssetCount: assetRefCount
  }

  await fs.writeFile(DEPLOY_META_FILE, `${JSON.stringify(payload, null, 2)}\n`, 'utf8')
}

async function runHealthCheck() {
  const url = process.env.DEPLOY_HEALTHCHECK_URL
  if (!url) return

  const response = await fetch(url, {
    method: 'GET',
    headers: { 'Cache-Control': 'no-cache' }
  })

  if (!response.ok) {
    throw new Error(`Health check failed: ${url} returned ${response.status}`)
  }
}

async function main() {
  await ensureSiteRootPolicy()
  if (CLEAN_ASSETS_ONLY) {
    if (!(await exists(SITE_ASSETS_DIR))) {
      console.log(`No assets directory found at ${SITE_ASSETS_DIR}`)
      return
    }

    const entries = await fs.readdir(SITE_ASSETS_DIR, { withFileTypes: true })
    if (entries.length === 0) {
      console.log(`No assets to clean in ${SITE_ASSETS_DIR}`)
      return
    }

    if (DRY_RUN) {
      console.log(`Dry-run: ${entries.length} entries would be removed from ${SITE_ASSETS_DIR}`)
      return
    }

    for (const entry of entries) {
      await removeTree(path.join(SITE_ASSETS_DIR, entry.name))
    }
    console.log(`Cleaned ${entries.length} entries from ${SITE_ASSETS_DIR}`)
    return
  }

  if (!(await exists(DIST_DIR))) {
    throw new Error(`dist does not exist: ${DIST_DIR}. Run npm run build first.`)
  }

  await acquireLock()
  let deployResult = null

  try {
    await cleanupTempDirs()
    const dist = await validateDist()

    if (DRY_RUN) {
      console.log(`Dry-run passed: ${dist.assetFileCount} asset files, ${dist.assetRefCount} referenced assets.`)
      return
    }

    const targetAssetsDir = path.join(SITE_ROOT, 'assets')
    deployResult = await deployAssets(dist.assetsDir, targetAssetsDir)
    const deployedCount = await countFiles(targetAssetsDir)
    if (deployedCount !== dist.assetFileCount) {
      throw new Error(`Asset count mismatch after deploy: dist=${dist.assetFileCount}, deployed=${deployedCount}`)
    }

    await syncRootEntries()
    const assetRefCount = await verifyDeployedIndex()
    const targetEntries = await fs.readdir(targetAssetsDir)
    await writeMeta({
      assetFileCount: deployedCount,
      assetDirCount: targetEntries.length,
      assetRefCount
    })
    await runHealthCheck()

    if (deployResult?.backupDir) {
      await removeTree(deployResult.backupDir)
    }

    console.log(`Deployed frontend to ${SITE_ROOT}`)
  } catch (error) {
    if (deployResult?.backupDir) {
      await rollbackAssets(path.join(SITE_ROOT, 'assets'), deployResult.backupDir)
    }
    throw error
  } finally {
    await releaseLock()
  }
}

main().catch((error) => {
  console.error(error)
  process.exit(1)
})
