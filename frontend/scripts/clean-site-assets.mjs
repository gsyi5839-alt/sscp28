import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'

const STANDARD_SITE_ROOT = '/www/wwwroot/www.bcbbs3.cn'
const SITE_ROOT = process.env.SITE_ROOT || STANDARD_SITE_ROOT
const ALLOW_CUSTOM_SITE_ROOT = process.env.ALLOW_CUSTOM_SITE_ROOT === 'true'
const DRY_RUN = process.env.DEPLOY_DRY_RUN === 'true'
const ASSETS_DIR = path.join(SITE_ROOT, 'assets')

async function exists(p) {
  try {
    await fs.access(p)
    return true
  } catch {
    return false
  }
}

async function assertSiteRootPolicy() {
  if (SITE_ROOT !== STANDARD_SITE_ROOT && !ALLOW_CUSTOM_SITE_ROOT) {
    throw new Error(
      `Refusing non-standard site root: ${SITE_ROOT}. Set ALLOW_CUSTOM_SITE_ROOT=true to override.`
    )
  }
}

async function cleanAssetsDir() {
  if (!(await exists(ASSETS_DIR))) {
    throw new Error(`Assets directory not found: ${ASSETS_DIR}`)
  }

  const entries = await fs.readdir(ASSETS_DIR, { withFileTypes: true })
  if (entries.length === 0) {
    console.log(`No assets to clean in ${ASSETS_DIR}`)
    return
  }

  if (DRY_RUN) {
    console.log(`Dry-run: ${entries.length} entries would be removed from ${ASSETS_DIR}`)
    return
  }

  for (const entry of entries) {
    await fs.rm(path.join(ASSETS_DIR, entry.name), { recursive: true, force: true })
  }

  console.log(`Cleaned ${entries.length} entries from ${ASSETS_DIR}`)
}

async function main() {
  await assertSiteRootPolicy()
  await cleanAssetsDir()
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
