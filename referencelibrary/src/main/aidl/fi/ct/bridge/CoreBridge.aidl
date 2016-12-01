// WishBridge.aidl
package fi.ct.bridge;

import fi.ct.bridge.AppBridge;
// Declare any non-default types here with import statements

interface CoreBridge {

  void sendAppToCore(in byte[] wsid, in byte[] data);
  void open(AppBridge service);
  void registerProcessDeath(in IBinder clientDeathListener,in byte[] wsid);
}

