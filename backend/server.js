require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');
const formsRoutes = require('./routes/forms');

const app = express();
const PORT = process.env.PORT || 8080;

app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(morgan('combined'));
app.use(rateLimit({ windowMs: 60000, max: 300 }));

app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'FieldSync', timestamp: new Date().toISOString() });
});

app.use('/api/forms', formsRoutes);

app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: 'Internal server error' });
});

app.listen(PORT, () => {
  console.log(`FieldSync backend running on port ${PORT}`);
});
