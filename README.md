# Ruta3 Backend local

Este proyecto incluye un backend minimalista para validar ofertas de Uber, Didi e InDrive.

## Ejecutar

```bash
python backend.py
```

## Endpoint

### GET /api/rules
Retorna las reglas de validación.

### POST /api/validate-offer
Envía la oferta para evaluarla.

Ejemplo:

```json
{
  "platform": "didi",
  "amount": 4500,
  "mode": "km",
  "km": 8
}
```

Respuesta ejemplo:

```json
{
  "platform": "didi",
  "amount": 4500,
  "unit": "$/km",
  "value": 562.5,
  "threshold": 400,
  "meets": true,
  "result": "cumple",
  "summary": "Oferta de DIDI: 4500 pesos en 8 km → 563 $/km",
  "message": "Cumple tus requisitos configurados"
}
```
