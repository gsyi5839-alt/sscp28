import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import os from 'node:os'
import path from 'node:path'
import test from 'node:test'
import { pathToFileURL } from 'node:url'
import ts from 'typescript'

const frontendRoot = path.resolve(new URL('..', import.meta.url).pathname)
const sourceFile = path.join(frontendRoot, 'src/utils/bettingSealState.ts')

async function loadSealStateModule() {
  const source = await fs.readFile(sourceFile, 'utf8')
  const compiled = ts.transpileModule(source, {
    compilerOptions: {
      module: ts.ModuleKind.ES2022,
      target: ts.ScriptTarget.ES2022,
    },
  }).outputText
  const tempDir = await fs.mkdtemp(path.join(os.tmpdir(), 'betting-seal-state-'))
  const tempFile = path.join(tempDir, 'bettingSealState.mjs')
  await fs.writeFile(tempFile, compiled, 'utf8')
  return import(pathToFileURL(tempFile).href)
}

const { isBettingSealed, resolvePendingResultIssue } = await loadSealStateModule()

test('keeps betting sealed after draw time until next issue data opens a new seal window', () => {
  const drawTimestamp = Date.parse('2026-05-06T12:00:00+08:00')
  const sealTimestamp = drawTimestamp - 10_000

  assert.equal(isBettingSealed({
    nowMs: sealTimestamp - 1,
    sealTimestamp,
    drawTimestamp,
    drawIssue: '1001',
  }), false)

  assert.equal(isBettingSealed({
    nowMs: sealTimestamp,
    sealTimestamp,
    drawTimestamp,
    drawIssue: '1001',
  }), true)

  assert.equal(isBettingSealed({
    nowMs: drawTimestamp + 5_000,
    sealTimestamp,
    drawTimestamp,
    drawIssue: '1001',
  }), true)

  const nextDrawTimestamp = Date.parse('2026-05-06T12:05:00+08:00')
  assert.equal(isBettingSealed({
    nowMs: drawTimestamp + 5_000,
    sealTimestamp: nextDrawTimestamp - 10_000,
    drawTimestamp: nextDrawTimestamp,
    drawIssue: '1002',
    pendingResultIssue: '',
    preDrawIssue: '1001',
  }), false)
})

test('keeps betting sealed when next issue arrives before the previous result is confirmed', () => {
  const previousDrawTimestamp = Date.parse('2026-05-06T12:00:00+08:00')
  const nextDrawTimestamp = Date.parse('2026-05-06T12:05:00+08:00')

  assert.equal(isBettingSealed({
    nowMs: previousDrawTimestamp + 5_000,
    sealTimestamp: nextDrawTimestamp - 10_000,
    drawTimestamp: nextDrawTimestamp,
    drawIssue: '1002',
    pendingResultIssue: '1001',
    preDrawIssue: '1000',
  }), true)

  assert.equal(isBettingSealed({
    nowMs: previousDrawTimestamp + 5_000,
    sealTimestamp: nextDrawTimestamp - 10_000,
    drawTimestamp: nextDrawTimestamp,
    drawIssue: '1002',
    pendingResultIssue: '1001',
    preDrawIssue: '1001',
  }), false)
})

test('does not seal when upstream time or issue data is missing', () => {
  assert.equal(isBettingSealed({
    nowMs: Date.now(),
    sealTimestamp: 0,
    drawTimestamp: 0,
    drawIssue: '',
  }), false)
})

test('clears pending result issue once that issue is already confirmed', () => {
  assert.equal(resolvePendingResultIssue('1001', '1000'), '1001')
  assert.equal(resolvePendingResultIssue('1001', '1001'), '')
  assert.equal(resolvePendingResultIssue('', '1001'), '')
})
