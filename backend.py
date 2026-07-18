from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse, parse_qs
import json
import os
from datetime import datetime

HOST = '127.0.0.1'
PORT = 8001
DATA_FILE = os.path.join(os.path.dirname(__file__), 'offers.json')


def load_data():
    if not os.path.exists(DATA_FILE):
        return []
    try:
        with open(DATA_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception:
        return []


def save_data(items):
    with open(DATA_FILE, 'w', encoding='utf-8') as f:
        json.dump(items, f, ensure_ascii=False, indent=2)


class Handler(BaseHTTPRequestHandler):
    def _set_headers(self, status=200):
        self.send_response(status)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()

    def do_OPTIONS(self):
        self._set_headers(200)

    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path == '/api/rules':
            self._set_headers(200)
            payload = {
                'minPerKm': 400,
                'minPerHour': 6000,
                'platforms': ['uber', 'didi', 'indrive']
            }
            self.wfile.write(json.dumps(payload).encode('utf-8'))
            return

        self._set_headers(404)
        self.wfile.write(json.dumps({'error': 'not found'}).encode('utf-8'))

    def do_POST(self):
        parsed = urlparse(self.path)
        if parsed.path != '/api/validate-offer':
            self._set_headers(404)
            self.wfile.write(json.dumps({'error': 'not found'}).encode('utf-8'))
            return

        try:
            content_length = int(self.headers.get('Content-Length', '0'))
            body = self.rfile.read(content_length)
            payload = json.loads(body.decode('utf-8'))
        except Exception:
            self._set_headers(400)
            self.wfile.write(json.dumps({'error': 'invalid json'}).encode('utf-8'))
            return

        platform = (payload.get('platform') or '').lower()
        amount = float(payload.get('amount') or 0)
        unit_mode = payload.get('mode') or 'km'
        km = float(payload.get('km') or 0)
        minutes = float(payload.get('minutes') or 0)

        if platform not in {'uber', 'didi', 'indrive'}:
            self._set_headers(400)
            self.wfile.write(json.dumps({'error': 'platform not supported'}).encode('utf-8'))
            return
        if amount <= 0:
            self._set_headers(400)
            self.wfile.write(json.dumps({'error': 'amount must be greater than zero'}).encode('utf-8'))
            return

        if unit_mode == 'km':
            if km <= 0:
                self._set_headers(400)
                self.wfile.write(json.dumps({'error': 'km must be greater than zero'}).encode('utf-8'))
                return
            value = amount / km
            threshold = 400
            meets = value >= threshold
            unit = '$/km'
            details = f"{amount:.0f} pesos en {km} km"
        else:
            if minutes <= 0:
                self._set_headers(400)
                self.wfile.write(json.dumps({'error': 'minutes must be greater than zero'}).encode('utf-8'))
                return
            value = (amount / minutes) * 60
            threshold = 6000
            meets = value >= threshold
            unit = '$/hora'
            details = f"{amount:.0f} pesos en {minutes} min"

        item = {
            'platform': platform,
            'amount': round(amount, 2),
            'km': round(km, 2) if unit_mode == 'km' else None,
            'minutes': round(minutes, 2) if unit_mode == 'hora' else None,
            'mode': unit_mode,
            'value': round(value, 2),
            'threshold': threshold,
            'meets': meets,
            'createdAt': datetime.utcnow().isoformat() + 'Z'
        }

        items = load_data()
        items.append(item)
        save_data(items)

        response = {
            'platform': platform,
            'amount': round(amount, 2),
            'unit': unit,
            'value': round(value, 2),
            'threshold': threshold,
            'meets': meets,
            'result': 'cumple' if meets else 'no-cumple',
            'summary': f"Oferta de {platform.upper()}: {details} → {round(value, 0)} {unit}",
            'message': 'Cumple tus requisitos configurados' if meets else 'No cumple tus requisitos configurados'
        }

        self._set_headers(200)
        self.wfile.write(json.dumps(response).encode('utf-8'))


if __name__ == '__main__':
    server = ThreadingHTTPServer((HOST, PORT), Handler)
    print(f'Backend escuchando en http://{HOST}:{PORT}')
    server.serve_forever()
