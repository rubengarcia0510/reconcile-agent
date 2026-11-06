#!/usr/bin/env python3
"""Generates the demo data: 20 invoice PDFs and a bank statement CSV."""
import csv
import datetime as dt
import os
import random

OUT = "mock-data"
rnd = random.Random(42)
SUPPLIERS = ["ACME Supplies S.A.", "Grafica del Sur SRL", "Logistica Andina S.A.",
             "Tecno Insumos SRL", "Papelera Norte S.A.", "Servicios Cloud SRL"]
used = {210000, 133000}


def ar(value):
    text = f"{value:,.2f}"
    return text.replace(",", "X").replace(".", ",").replace("X", ".")


def esc(text):
    return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")


def pdf_bytes(lines):
    body = ["BT", "/F1 12 Tf", "16 TL", "50 790 Td"]
    for line in lines:
        body.append("(%s) Tj T*" % esc(line))
    body.append("ET")
    stream = "\n".join(body).encode("latin-1")
    objects = [
        b"<< /Type /Catalog /Pages 2 0 R >>",
        b"<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R "
        b"/Resources << /Font << /F1 5 0 R >> >> >>",
        b"<< /Length %d >>\nstream\n" % len(stream) + stream + b"\nendstream",
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>",
    ]
    out = bytearray(b"%PDF-1.4\n")
    offsets = []
    for number, obj in enumerate(objects, 1):
        offsets.append(len(out))
        out += b"%d 0 obj\n" % number + obj + b"\nendobj\n"
    xref = len(out)
    out += b"xref\n0 %d\n" % (len(objects) + 1)
    out += b"0000000000 65535 f \n"
    for offset in offsets:
        out += b"%010d 00000 n \n" % offset
    out += b"trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n" % (len(objects) + 1, xref)
    return bytes(out)


def invoice_lines(inv):
    subtotal = round(inv["amount"] / 1.21, 2)
    vat = round(inv["amount"] - subtotal, 2)
    return [
        inv["supplier"],
        "Tax invoice type A",
        "Invoice N\u00b0 %s" % inv["number"],
        "Date: %s" % inv["date"].strftime("%d/%m/%Y"),
        "Customer: Reconcile Demo S.A.",
        "",
        "Goods and services",
        "Subtotal: ARS %s" % ar(subtotal),
        "VAT 21%%: ARS %s" % ar(vat),
        "Total: ARS %s" % ar(inv["amount"]),
    ]


def random_amount():
    while True:
        value = rnd.randrange(90000, 420000, 50)
        if value not in used:
            used.add(value)
            return float(value)


invoices, payments = [], []
start = dt.date(2026, 10, 1)
for i in range(16):
    inv = {"supplier": SUPPLIERS[i % len(SUPPLIERS)], "number": "0001-%08d" % (45800 + i),
           "date": start + dt.timedelta(days=rnd.randint(0, 24)), "amount": random_amount()}
    invoices.append(("invoice-%02d.pdf" % (i + 1), inv))
    payments.append((inv["date"] + dt.timedelta(days=rnd.randint(0, 4)), "Transfer to " + inv["supplier"],
                     -inv["amount"], "ARS", ("FC " + inv["number"]) if i < 10 else ""))

invoices.append(("invoice-17-duplicate.pdf", dict(invoices[2][1])))

unusual = {"supplier": SUPPLIERS[1], "number": "0001-00045816", "date": dt.date(2026, 10, 9), "amount": 5600000.0}
invoices.append(("invoice-18-unusual.pdf", unusual))
payments.append((dt.date(2026, 10, 10), "Transfer to " + unusual["supplier"], -unusual["amount"], "ARS",
                 "FC " + unusual["number"]))

mismatch = {"supplier": SUPPLIERS[3], "number": "0001-00045817", "date": dt.date(2026, 10, 15), "amount": 210000.0}
invoices.append(("invoice-19-mismatch.pdf", mismatch))
payments.append((dt.date(2026, 10, 17), "Transfer to " + mismatch["supplier"], -207500.0, "ARS", ""))

unpaid = {"supplier": SUPPLIERS[4], "number": "0001-00045818", "date": dt.date(2026, 10, 21), "amount": 133000.0}
invoices.append(("invoice-20-unpaid.pdf", unpaid))

payments.append((dt.date(2026, 10, 12), "Transfer to Unknown Vendor", -75000.0, "ARS", ""))
payments.append((dt.date(2026, 10, 23), "Debit bank maintenance fee", -48300.0, "ARS", ""))
payments.append((dt.date(2026, 10, 20), "Customer deposit", 310000.0, "ARS", ""))

os.makedirs(os.path.join(OUT, "invoices"), exist_ok=True)
for name, inv in invoices:
    with open(os.path.join(OUT, "invoices", name), "wb") as handle:
        handle.write(pdf_bytes(invoice_lines(inv)))

payments.sort(key=lambda p: p[0])
with open(os.path.join(OUT, "bank-statement.csv"), "w", newline="") as handle:
    writer = csv.writer(handle)
    writer.writerow(["date", "description", "amount", "currency", "reference"])
    for day, description, amount, currency, reference in payments:
        writer.writerow([day.isoformat(), description, "%.2f" % amount, currency, reference])

print("%d invoices, %d bank rows" % (len(invoices), len(payments)))
print("Planted anomalies: 1 duplicate (invoice-17), 1 unusual amount (invoice-18),")
print("1 amount mismatch (invoice-19), 2 payments without invoice (75000.00 and 48300.00).")
print("Unpaid invoice (invoice-20) is not an anomaly: it stays unmatched.")
