#!/usr/bin/env python3
import argparse, csv, random, uuid
from datetime import datetime, timedelta, timezone

COUNTRIES = ["RU","US","CA","DE","NL","FR","GB","TR","AE"]
CURRENCIES = ["RUB","USD","EUR"]
MERCHANTS = ["MEGAMARKET","UBER","NETFLIX","AMAZON","APPLE","GOOGLE","SPOTIFY","BOOKING","IKEA","ZARA","STEAM","AIRBNB"]

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--out", default='transactions_sample.csv')
    p.add_argument("--rows", type=int, default=10000)
    p.add_argument("--accounts", type=int, default=5000)
    args = p.parse_args()

    now = datetime.now(timezone.utc)
    start = now - timedelta(days=7)

    with open(args.out, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["transaction_id","account_id","amount","currency","merchant","country"])
        for i in range(args.rows):
            acc = random.randint(1, args.accounts)
            amount = round(random.random()*20000, 2)
            if random.random() < 0.02:  # иногда большие суммы
                amount = round(30000 + random.random()*70000, 2)
            currency = random.choice(CURRENCIES)
            merchant = random.choice(MERCHANTS)
            country = random.choice(COUNTRIES)
            w.writerow([str(uuid.uuid4()), acc, amount, currency, merchant, country])

    print(f"OK: wrote {args.rows} rows to {args.out}")

if __name__ == "__main__":
    main()
