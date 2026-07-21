# Ruta3 PWA

Ruta3 valida ofertas de Uber, Didi e InDrive desde JavaScript, sin servidor Python local.

## Ejecutar

Abre `index.html` en navegador o usa Capacitor para Android con el contenido de `www`.

```bash
npm run sync:android
```

## Servicio de ofertas

La logica que antes vivia en el backend local esta en `offerService.js`.

Funciones disponibles:

```js
await window.Ruta3OfferService.getRules();
await window.Ruta3OfferService.validateOffer({
  platform: 'didi',
  amount: 4500,
  mode: 'km',
  km: 8
});
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
  "summary": "Oferta de DIDI: 4500 pesos en 8 km -> 563 $/km",
  "message": "Cumple tus requisitos configurados"
}
```
