// Novvy WebADB — browser-only ADB over WebUSB.
// Bundled to web/adb.js and exposed as window.NovvyAdb.
import { AdbDaemonWebUsbDeviceManager } from "@yume-chan/adb-daemon-webusb";
import { AdbDaemonTransport, Adb, ADB_DEFAULT_AUTHENTICATORS } from "@yume-chan/adb";
import AdbWebCredentialStore from "@yume-chan/adb-credential-web";

const REMOTE_PATH = "/data/local/tmp/novvy.apk";
let _store = null;

function store() {
  if (!_store) _store = new AdbWebCredentialStore("Novvy");
  return _store;
}

function manager() {
  if (typeof navigator === "undefined" || !navigator.usb) {
    throw new Error(
      "WebUSB is not available. Use desktop Chrome or Edge over HTTPS, and enable USB debugging on your phone."
    );
  }
  if (AdbDaemonWebUsbDeviceManager.BROWSER) {
    return AdbDaemonWebUsbDeviceManager.BROWSER;
  }
  return new AdbDaemonWebUsbDeviceManager(navigator.usb);
}

export function supported() {
  return typeof navigator !== "undefined" && !!navigator.usb;
}

export function adbVersion() {
  return "Novvy WebADB (yume-chan/adb 2.6.4, WebUSB)";
}

// Equivalent of `adb devices`: returns authorized USB devices.
export async function listDevices() {
  const mgr = manager();
  const devices = await mgr.getDevices();
  return devices.map((d) => ({ serial: d.serial, name: d.name || d.serial }));
}

// Prompt the user to pick a connected USB phone (browser chooser).
export async function requestDevice() {
  const mgr = manager();
  const device = await mgr.requestDevice();
  if (!device) return null;
  return { serial: device.serial, name: device.name || device.serial };
}

async function connect(serial) {
  const mgr = manager();
  let devices = await mgr.getDevices();
  let device = devices.find((d) => d.serial === serial);
  if (!device) {
    device = await mgr.requestDevice();
  }
  if (!device) {
    throw new Error("Device not found. Reconnect the phone via USB and try again.");
  }
  const connection = await device.connect();
  const transport = await AdbDaemonTransport.authenticate({
    serial: device.serial,
    connection,
    credentialStore: store(),
    authenticators: ADB_DEFAULT_AUTHENTICATORS,
  });
  return new Adb(transport);
}

// Push the APK to the device and install it with `pm install`.
export async function installApk(serial, apkUrl, onProgress) {
  const adb = await connect(serial);
  const report = (msg) => {
    if (onProgress) onProgress(msg);
  };
  try {
    report("Downloading APK...");
    const res = await fetch(apkUrl);
    if (!res.ok) throw new Error("APK download failed (HTTP " + res.status + ")");
    const blob = await res.blob();
    report("Pushing " + blob.size + " bytes to device...");

    const sync = await adb.sync();
    try {
      await sync.write({ file: blob.stream(), filename: REMOTE_PATH });
    } finally {
      try {
        await sync.dispose();
      } catch (e) {
        /* ignore */
      }
    }

    report("Installing (pm install)...");
    const out = await adb.subprocess.noneProtocol.spawnWaitText([
      "pm",
      "install",
      "-r",
      REMOTE_PATH,
    ]);
    try {
      await adb.subprocess.noneProtocol.spawnWaitText(["rm", "-f", REMOTE_PATH]);
    } catch (e) {
      /* ignore */
    }
    const ok = /success/i.test(out);
    return { ok, output: (out || "").trim() || (ok ? "Success" : "Install returned no output") };
  } finally {
    try {
      await adb.close();
    } catch (e) {
      /* ignore */
    }
  }
}