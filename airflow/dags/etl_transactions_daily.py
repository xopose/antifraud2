from __future__ import annotations

import pandas as pd
import requests
from datetime import datetime
from airflow import DAG
from airflow.operators.python import PythonOperator

DATA_PATH = "/opt/airflow/data/transactions_sample.csv"
SPRING_URL = "http://antifraud-api:8080/airflow_transactions"

#Даг для подтягивания данных из csv
def load_and_send_transactions():
    df = pd.read_csv(DATA_PATH)

    df = df[[
        "transaction_id",
        "account_id",
        "amount",
        "currency",
        "merchant",
        "country"
    ]]

    df = df.drop_duplicates(subset=["transaction_id"])

    df["amount"] = pd.to_numeric(df["amount"], errors="coerce")
    df["account_id"] = pd.to_numeric(df["account_id"], errors="coerce")

    df = df.fillna({
        "amount": 0.0,
        "account_id": 0,
        "currency": "UNKNOWN",
        "merchant": "UNKNOWN",
        "country": "UNKNOWN",
    })

    for _, row in df.iterrows():
        payload = {
            "transaction_id": str(row["transaction_id"]),
            "account_id": int(row["account_id"]),
            "amount": float(row["amount"]),
            "currency": row["currency"],
            "country": row["country"],
            "merchant": row["merchant"],
        }
        print(payload)

        response = requests.post(
            SPRING_URL,
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=10,
        )

        if response.status_code != 200:
            raise Exception(
                f"Failed tx={payload['transaction_id']} "
                f"status={response.status_code} body={response.text}"
            )

        print(f"Sent {payload['transaction_id']}")



with DAG(
        dag_id="etl_transactions_daily",
        start_date=datetime(2024, 1, 1),
        schedule=None,
        catchup=False,
        tags=["antifraud"],
) as dag:

    task = PythonOperator(
        task_id="load_and_send_transactions",
        python_callable=load_and_send_transactions,
    )
