import express, { Express } from 'express';
import { Repositories, createSeededRepositories } from './repositories';
import { BillPaymentService } from './services/billPaymentService';
import { createBillPaymentRouter } from './controllers/billPaymentController';

/** Build the Express app. Repositories are injectable for testing. */
export function createApp(repos: Repositories = createSeededRepositories()): Express {
  const app = express();
  app.use(express.json());

  const service = new BillPaymentService(repos);

  app.get('/health', (_req, res) => res.json({ status: 'ok' }));
  app.use('/api', createBillPaymentRouter(service));

  return app;
}
