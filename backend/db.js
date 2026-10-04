const Database = require('better-sqlite3');
const path = require('path');

const db = new Database(path.join(__dirname, 'fieldsync.db'));
db.pragma('journal_mode = WAL');

db.exec(`
  CREATE TABLE IF NOT EXISTS forms (
    id TEXT PRIMARY KEY,
    device_id TEXT NOT NULL,
    agent_name TEXT NOT NULL,
    client_name TEXT NOT NULL,
    form_type TEXT NOT NULL,
    payload TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'synced',
    created_at TEXT NOT NULL,
    synced_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
  );
  CREATE INDEX IF NOT EXISTS idx_forms_device ON forms(device_id);
  CREATE INDEX IF NOT EXISTS idx_forms_created ON forms(created_at);
`);

module.exports = db;
