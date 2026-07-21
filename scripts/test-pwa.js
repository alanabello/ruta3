const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const requiredFiles = [
  'www/index.html',
  'www/manifest.json',
  'www/service-worker.js',
  'www/icon-192.png',
  'www/icon-512.png',
  'capacitor.config.json',
  'android/app/src/main/AndroidManifest.xml',
];

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

for (const file of requiredFiles) {
  assert(fs.existsSync(path.join(root, file)), `Missing required file: ${file}`);
}

const manifest = JSON.parse(fs.readFileSync(path.join(root, 'www/manifest.json'), 'utf8'));
assert(manifest.name && manifest.short_name, 'Manifest needs name and short_name');
assert(manifest.display === 'standalone', 'Manifest display must be standalone');
assert(manifest.start_url, 'Manifest needs start_url');
assert(Array.isArray(manifest.icons) && manifest.icons.length >= 2, 'Manifest needs app icons');

const indexHtml = fs.readFileSync(path.join(root, 'www/index.html'), 'utf8');
assert(indexHtml.includes('<link rel="manifest" href="manifest.json">'), 'index.html must link manifest.json');
assert(indexHtml.includes('serviceWorker.register'), 'index.html must register the service worker');
assert(!indexHtml.includes('downloads/ruta3.apk'), 'App shell must not trigger APK downloads');
assert(!indexHtml.includes('id="install-apk"'), 'App shell must not show APK install link');
assert(!indexHtml.includes('id="fab-quick"'), 'Manual offer calculator button must be removed');
assert(!indexHtml.includes('id="quick-modal"'), 'Manual offer calculator modal must be removed');
assert(!indexHtml.includes('quick-pickup-min'), 'Manual offer calculator inputs must be removed');
assert(indexHtml.includes('id="setting-min-km"'), 'Settings tab must include min per km input');
assert(indexHtml.includes('id="setting-min-hour"'), 'Settings tab must include min per hour input');
assert(indexHtml.includes('Ruta3Settings'), 'Web app must sync settings with the native Android plugin');

const serviceWorker = fs.readFileSync(path.join(root, 'www/service-worker.js'), 'utf8');
for (const asset of ['./index.html', './manifest.json', './icon-192.png', './icon-512.png']) {
  assert(serviceWorker.includes(asset), `Service worker must cache ${asset}`);
}

const capacitorConfig = JSON.parse(fs.readFileSync(path.join(root, 'capacitor.config.json'), 'utf8'));
assert(capacitorConfig.appId === 'com.ruta3.app', 'Capacitor appId must be com.ruta3.app');
assert(capacitorConfig.webDir === 'www', 'Capacitor webDir must be www');

const androidManifest = fs.readFileSync(path.join(root, 'android/app/src/main/AndroidManifest.xml'), 'utf8');
const appStart = androidManifest.indexOf('<application');
const appEnd = androidManifest.indexOf('</application>');
const serviceIndex = androidManifest.indexOf('android:name=".FloatingOverlayService"');
assert(appStart !== -1 && appEnd !== -1, 'Android manifest must contain an application element');
assert(serviceIndex > appStart && serviceIndex < appEnd, 'FloatingOverlayService must be declared inside application');

console.log('PWA and Android packaging checks passed.');
