up:
	docker compose up -d --build

down:
	docker compose down -v

logs:
	docker compose logs -f --tail=200

build:
	docker compose build

demo-failover:
	@echo "Stopping kafka-2 (demo of broker failure)..."
	docker stop kafka-2
	@echo "Now send a transaction via HTTP: curl -X POST http://localhost:8080/transactions ..."
	@echo "Restart broker: docker start kafka-2"

gen200k:
	python3 data/generate_transactions_csv.py --out data/transactions_200k.csv --rows 200000
