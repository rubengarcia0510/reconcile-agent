export type AnomalyType =
  | 'DUPLICATE_INVOICE'
  | 'UNUSUAL_AMOUNT'
  | 'AMOUNT_MISMATCH'
  | 'PAYMENT_WITHOUT_INVOICE';

export type AnomalyStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export type ReviewDecision = 'approve' | 'reject';

export interface Invoice {
  id: string;
  runId: string;
  supplier: string | null;
  number: string | null;
  amount: number | null;
  currency: string | null;
  issueDate: string | null;
}

export interface Anomaly {
  id: string;
  runId: string;
  type: AnomalyType;
  invoiceId: string | null;
  transactionId: string | null;
  explanation: string;
  status: AnomalyStatus;
}

export interface RunSummary {
  runId: string;
  invoicesProcessed: number;
  invoicesFailed: number;
  transactionsLoaded: number;
  failedFiles: string[];
}

export interface AnalysisResult {
  runId: string;
  matched: number;
  unmatchedInvoices: number;
  unmatchedTransactions: number;
  anomalies: Anomaly[];
}
