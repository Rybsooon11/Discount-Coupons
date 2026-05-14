# DiscountCoupons

## Uruchomienie

Potrzebujesz Javy 21+ i Dockera.

1. Odpal Postgresa:

   ```bash
   docker-compose up -d
   ```

2. Uruchom aplikację (port 8080):

   ```bash
   DB_PASSWORD=coupons ./mvnw spring-boot:run
   ```

3. Stwórz kupon i wykorzystaj go:

   ```bash
   curl -X POST localhost:8080/api/v1/coupons \
     -H 'Content-Type: application/json' \
     -d '{"code":"WIOSNA","maxUses":2,"countryCode":"PL"}'

   curl -X POST localhost:8080/api/v1/coupons/redeem \
     -H 'Content-Type: application/json' \
     -d '{"code":"WIOSNA","userId":"u1"}'
   ```

Strategię redempcji przełączasz przez `--coupons.strategy=atomic-update` lub
`--coupons.strategy=sharded-counter` przy starcie aplikacji.

Testy: `./mvnw test`.

## Wyniki testów wydajnościowych

Gatling 3.13.4, closed injection, 1000 concurrent users, 60s na scenariusz.
Postgres 16 w docker-compose, Spring Boot 3.5 z profilem `perf` (geo zastubowane).

### Tabela

| Scenariusz | maxUses | Total req | Throughput | Mean | p50 | p95 | p99 | Max |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Atomic, contention | 10 000 | 11 168 | 186 rps | 5 799 ms | 6 118 | 8 693 | 9 879 | 19 138 |
| Sharded, contention | 10 000 | 188 637 | 3 144 rps | 319 ms | 309 | 539 | 690 | 1 509 |
| Atomic, throughput | 10 000 000 | 11 212 | 162 rps | 5 694 ms | 5 966 | 8 345 | 9 660 | 15 318 |
| Sharded, throughput | 10 000 000 | 227 188 | 3 606 rps | 264 ms | 256 | 482 | 563 | 1 452 |

W fazie contention liczone są wszystkie odpowiedzi 200 i 409 jako OK.
Sharded zedrenował pulę w pierwszych kilku sekundach, potem przez resztę testu
odbijał szybkie 409 z lockless filtra `WHERE current_uses < max_uses`.

### Wnioski

Sharded vs Atomic w teście throughput (same statusy 200):

- 22 razy wyższy throughput, 3 606 rps vs 162 rps
- 22 razy niższe średnie opóźnienie, 264 ms vs 5 694 ms
- 17 razy niższy p99, 563 ms vs 9 660 ms

Nie ma jednego rozwiązania dla każdego problemu. Można pójść dalej w stronę
architektur znanych z Shopify czy podobnych dużych systemów, w tym z dodaniem
cache'u. Wybór zależy od oczekiwanego ruchu i wymagań biznesowych.
Rozwiązanie z sharded encjami jest rozwiazaniem które może być wystarczające dla większości przypadków
i jest stosowane przez duże korporacje jak Google. 
Mogłem też napisać rozwiązanie z Redis + Lua które jest jeszcze wydajniejsze ale może to być również Overengineering. 
To zależy, jak wszystko w tej branży.

### Reprodukcja

```bash
docker-compose up -d

DB_PASSWORD=coupons SPRING_PROFILES_ACTIVE=perf ./mvnw spring-boot:run \
  -Dspring-boot.run.arguments=--coupons.strategy=atomic-update

./mvnw -Pperf gatling:test \
  -Dgatling.simulationClass=com.example.discountcoupons.perf.CouponThroughputSimulation
```

Dla sharded: zatrzymaj aplikację, `docker-compose down -v && docker-compose up -d`,
uruchom ponownie z `--coupons.strategy=sharded-counter`.

Override parametrów: `-Dperf.users=N`, `-Dperf.duration=sec`, `-Dperf.maxUses=N`.
Raporty HTML w `target/gatling/<simulation>-<timestamp>/index.html`.
