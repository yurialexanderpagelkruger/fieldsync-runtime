const express = require('express');
const db = require('../db');

const router = express.Router();

router.get('/ping', (req, res) => {
  res.json({ ok: true, serverTime: new Date().toISOString() });
});

router.get('/', (req, res) => {
  const limit = Math.min(parseInt(req.query.limit) || 100, 500);
  const rows = db.prepare('SELECT * FROM forms ORDER BY created_at DESC LIMIT ?').all(limit);
  res.json({ count: rows.length, data: rows.map(r => ({ ...r, payload: JSON.parse(r.payload) })) });
});

router.get('/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM forms WHERE id = ?').get(req.params.id);
  if (!row) return res.status(404).json({ error: 'Form not found' });
  res.json({ ...row, payload: JSON.parse(row.payload) });
});

router.post('/sync', (req, res) => {
  const { records } = req.body;
  if (!Array.isArray(records)) {
    return res.status(400).json({ error: 'records must be an array' });
  }

  const insert = db.prepare(`
    INSERT INTO forms (id, device_id, agent_name, client_name, form_type, payload, status, created_at, synced_at, updated_at)
    VALUES (@id, @device_id, @agent_name, @client_name, @form_type, @payload, @status, @created_at, @synced_at, @updated_at)
    ON CONFLICT(id) DO UPDATE SET
      payload = excluded.payload,
      status = excluded.status,
      synced_at = excluded.synced_at,
      updated_at = excluded.updated_at
  `);

  const synced = [];
  const failed = [];

  const tx = db.transaction((items) => {
    for (const item of items) {
      try {
        if (!item.id || !item.device_id || !item.payload) {
          failed.push({ id: item.id, reason: 'invalid' });
          continue;
        }
        const now = new Date().toISOString();
        insert.run({
          id: item.id,
          device_id: item.device_id,
          agent_name: item.agent_name || 'unknown',
          client_name: item.client_name || 'unknown',
          form_type: item.form_type || 'general',
          payload: JSON.stringify(item.payload),
          status: 'synced',
          created_at: item.created_at || now,
          synced_at: now,
          updated_at: now
        });
        synced.push(item.id);
      } catch (e) {
        failed.push({ id: item.id, reason: e.message });
      }
    }
  });

  tx(records);

  res.json({
    success: true,
    syncedCount: synced.length,
    failedCount: failed.length,
    synced,
    failed,
    serverTime: new Date().toISOString()
  });
});

router.delete('/:id', (req, res) => {
  const result = db.prepare('DELETE FROM forms WHERE id = ?').run(req.params.id);
  if (result.changes === 0) return res.status(404).json({ error: 'Form not found' });
  res.json({ success: true });
});

module.exports = router;
