const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

const root = path.resolve(__dirname, '..');
const androidDir = path.join(root, 'android');
const bundledJdk21 = 'C:\\Program Files\\Microsoft\\jdk-21.0.11.10-hotspot';
const javaHome = (fs.existsSync(bundledJdk21) ? bundledJdk21 : process.env.JAVA_HOME);

function quoteArg(value) {
  return `"${String(value).replace(/"/g, '\\"')}"`;
}

function quoteCommand(value) {
  return String(value).includes(' ') ? quoteArg(value) : String(value);
}

function quoteCommandArg(value) {
  return String(value).includes(' ') ? quoteArg(value) : String(value);
}

function run(command, args, options = {}) {
  const isWindows = process.platform === 'win32';
  const result = spawnSync(
    isWindows ? 'cmd.exe' : command,
    isWindows ? ['/d', '/s', '/c', [quoteCommand(command), ...args.map(quoteCommandArg)].join(' ')] : args,
    {
    cwd: options.cwd || root,
    env: options.env || process.env,
    stdio: 'inherit',
    shell: false,
    }
  );

  if (result.error) {
    console.error(result.error.message);
    process.exit(1);
  }

  if (result.status !== 0) {
    process.exit(result.status || 1);
  }
}

if (!javaHome) {
  throw new Error('JDK not found. Set JAVA_HOME to a JDK installation path.');
}

const env = {
  ...process.env,
  JAVA_HOME: javaHome,
  Path: `${path.join(javaHome, 'bin')};${process.env.Path || process.env.PATH || ''}`,
};

const apkSource = path.join(androidDir, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk');
const targets = [
  path.join(root, 'www', 'downloads', 'ruta3.apk'),
  path.join(root, 'downloads', 'ruta3.apk'),
];

for (const target of targets) {
  if (fs.existsSync(target)) {
    fs.unlinkSync(target);
  }
}

run('npx.cmd', ['cap', 'sync', 'android'], { env });
run(path.join(androidDir, 'gradlew.bat'), ['assembleDebug'], { cwd: androidDir, env });

for (const target of targets) {
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.copyFileSync(apkSource, target);
}

console.log(`Android APK ready: ${path.relative(root, targets[0])}`);
