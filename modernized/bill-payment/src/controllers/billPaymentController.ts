import { Router, Request, Response } from 'express';
import { BillPaymentService } from '../services/billPaymentService';
import { BillPaymentError, BillPaymentErrorCode } from '../errors';

/**
 * REST controller — the modern front door that replaces the BMS 3270 screen
 * (CICS transaction CB00 / map COBIL0A) driven by COBIL00C.
 *
 *   GET  /accounts/:acctId/balance        -> READ-ACCTDAT-FILE (display balance)
 *   POST /accounts/:acctId/bill-payment   -> PROCESS-ENTER-KEY (pay in full)
 */

const ERROR_STATUS: Record<BillPaymentErrorCode, number> = {
  ACCT_ID_REQUIRED: 400,
  ACCT_ID_INVALID: 400,
  INVALID_CONFIRM: 400,
  ACCOUNT_NOT_FOUND: 404,
  XREF_NOT_FOUND: 404,
  NOTHING_TO_PAY: 422,
  TRAN_ID_DUPLICATE: 409,
  LOOKUP_FAILED: 502,
  UPDATE_FAILED: 502,
};

export function createBillPaymentRouter(service: BillPaymentService): Router {
  const router = Router();

  router.get('/accounts/:acctId/balance', async (req: Request, res: Response) => {
    try {
      const result = await service.getBalance(req.params.acctId ?? '');
      res.json(result);
    } catch (err) {
      handleError(err, res);
    }
  });

  router.post('/accounts/:acctId/bill-payment', async (req: Request, res: Response) => {
    try {
      const confirm = typeof req.body?.confirm === 'string' ? req.body.confirm : 'Y';
      const result = await service.processEnter({
        acctId: req.params.acctId ?? '',
        confirm,
      });
      res.status(result.paid ? 201 : 200).json(result);
    } catch (err) {
      handleError(err, res);
    }
  });

  return router;
}

function handleError(err: unknown, res: Response): void {
  if (err instanceof BillPaymentError) {
    res.status(ERROR_STATUS[err.code]).json({ code: err.code, message: err.message });
    return;
  }
  const message = err instanceof Error ? err.message : 'Unexpected error';
  res.status(500).json({ code: 'INTERNAL_ERROR', message });
}
