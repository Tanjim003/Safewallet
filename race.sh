#!/bin/bash
curl -s -X POST http://localhost:8080/api/wallet/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $1" \
  -d '{"recipientPhone": "01884353431", "amount": 600}' &
curl -s -X POST http://localhost:8080/api/wallet/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $1" \
  -d '{"recipientPhone": "01884353431", "amount": 600}' &
wait
