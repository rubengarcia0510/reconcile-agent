# API contract

Base URL (demo): `http://re8yswiamtllmdbuzstwkq9u.64.177.59.13.sslip.io`

All bodies are JSON unless noted. Errors look like `{"error": "message"}`.

## Flow

1. `POST /api/runs`: upload invoices (PDF) and the bank statement (CSV). Returns a `runId`.
2. `POST /api/runs/{runId}/analyze`: reconcile and detect anomalies.
3. `GET /api/runs/{runId}/anomalies`: list anomalies for the approval queue.
4. `POST /api/anomalies/{id}/approve` or `/reject`: human decision.

## Endpoints

### `GET /health`

`200` returns `{"status": "ok"}`

### `POST /api/runs`

`multipart/form-data`

- `invoices`: 1 to 30 PDF files (repeat the field once per file)
- `statement`: one CSV file with header `date,description,amount,currency,reference`
  (ISO dates, dot decimals, payments are negative amounts)

`200` returns:

```json
{
  "runId": "20376951-f706-45ce-a4df-1d3ba00f3f88",
  "invoicesProcessed": 20,
  "invoicesFailed": 0,
  "transactionsLoaded": 21,
  "failedFiles": []
}
```

`400` means an invalid CSV, no invoices, or more than 30 invoices.

This call takes roughly 10 to 20 seconds for 20 invoices (there are no progress events yet).

### `GET /api/invoices?runId={runId}`

`200` returns a list of:

```json
{
  "id": "uuid",
  "runId": "uuid",
  "supplier": "ACME Supplies S.A.",
  "number": "0001-00045821",
  "amount": 184250.00,
  "currency": "ARS",
  "issueDate": "2026-10-14"
}
```

### `POST /api/runs/{runId}/analyze`

No body. Safe to call again: previous anomalies of that run are replaced.

`200` returns:

```json
{
  "runId": "uuid",
  "matched": 17,
  "unmatchedInvoices": 3,
  "unmatchedTransactions": 4,
  "anomalies": []
}
```

`anomalies` holds a list of Anomaly objects (see below). `404` means the run was not found.

### `GET /api/runs/{runId}/anomalies`

`200` returns a list of:

```json
{
  "id": "uuid",
  "runId": "uuid",
  "type": "DUPLICATE_INVOICE",
  "invoiceId": "uuid",
  "transactionId": null,
  "explanation": "Invoice 0001-00045802 from Logistica Andina S.A. appears more than once with the same amount",
  "status": "PENDING"
}
```

- `type`: `DUPLICATE_INVOICE`, `UNUSUAL_AMOUNT`, `AMOUNT_MISMATCH`, `PAYMENT_WITHOUT_INVOICE`
- `status`: `PENDING`, `APPROVED`, `REJECTED`
- `invoiceId` or `transactionId` can be `null` depending on the type.

### `POST /api/anomalies/{id}/approve` and `POST /api/anomalies/{id}/reject`

No body. `200` returns the updated anomaly.

`404` means an unknown id. `409` means the anomaly was already reviewed.

## Not available yet

- CORS is not enabled. Serve the frontend from the same origin (reverse proxy `/api`) or ask for CORS.
- No progress events while a run is processing.
- No endpoint to list past runs or bank transactions. Keep the `runId` in the client.
- No authentication.
