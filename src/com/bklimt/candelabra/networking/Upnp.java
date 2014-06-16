package com.bklimt.candelabra.networking;

import java.util.logging.Logger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import bolts.Continuation;
import bolts.Task;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;

public class Upnp {
  public static Task<String> findIpAddress(final Context context) {
    final Logger logger = Logger.getLogger(Upnp.class.getName());

    final Task<String>.TaskCompletionSource ipAddressTask = Task.create();
    final Task<Void>.TaskCompletionSource shutdownTask = Task.create();

    // UPnP discovery is asynchronous, we need a callback
    final RegistryListener listener = new RegistryListener() {
      public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        logger.info("Discovery started: " + device.getDisplayString());
      }

      public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
        logger.info("Discovery failed: " + device.getDisplayString() + " => " + ex);
      }

      public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        logger.info("Remote device available: " + device.getDisplayString());

        // Royal Philips Electronics Philips hue bridge 2012 929000226503
        if (device.getDisplayString().contains("Philips hue bridge")) {
          DeviceDetails details = device.getDetails();
          logger.info("Device: " + device.getDisplayString());
          logger.info("Friendly name: " + details.getFriendlyName());
          logger.info("Serial number: " + details.getSerialNumber());
          logger.info("UPC: " + details.getUpc());
          logger.info("Base URL: " + details.getBaseURL());
          logger.info("Presentation URI: " + details.getPresentationURI());
          logger.info("Manufacturer name: " + details.getManufacturerDetails().getManufacturer());
          logger.info("Manufacturer URI: " + details.getManufacturerDetails().getManufacturerURI());
          logger.info("Model name: " + details.getModelDetails().getModelName());
          logger.info("Model number: " + details.getModelDetails().getModelNumber());
          logger.info("Model description: " + details.getModelDetails().getModelDescription());
          
          ipAddressTask.setResult(details.getBaseURL().getHost());
        }
      }

      public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
        logger.info("Remote device updated: " + device.getDisplayString());
      }

      public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        logger.info("Remote device removed: " + device.getDisplayString());
      }

      public void localDeviceAdded(Registry registry, LocalDevice device) {
        logger.info("Local device added: " + device.getDisplayString());
      }

      public void localDeviceRemoved(Registry registry, LocalDevice device) {
        logger.info("Local device removed: " + device.getDisplayString());
      }

      public void beforeShutdown(Registry registry) {
        logger.info("Before shutdown, the registry has devices: " + registry.getDevices().size());
      }

      public void afterShutdown() {
        logger.info("Shutdown of registry complete.");
        shutdownTask.setResult(null);
      }
    };

    final ServiceConnection serviceConnection = new ServiceConnection() {
      private AndroidUpnpService upnpService;

      public void onServiceConnected(ComponentName className, IBinder service) {
        logger.info("Service connected: " + className.toShortString());

        upnpService = (AndroidUpnpService) service;

        // Refresh the list with all known devices
        for (@SuppressWarnings("rawtypes") Device device : upnpService.getRegistry().getDevices()) {
          logger.info("Device exists: " + device.getDisplayString());
          if (device instanceof RemoteDevice) {
            listener.remoteDeviceAdded(upnpService.getRegistry(), (RemoteDevice) device);
          }
        }

        // Getting ready for future device advertisements
        upnpService.getRegistry().addListener(listener);

        // Search asynchronously for all devices
        upnpService.getControlPoint().search();
      }

      public void onServiceDisconnected(ComponentName className) {
        logger.info("Service disconnected.");
        upnpService = null;
      }
    };

    context.bindService(new Intent(context, AndroidUpnpServiceImpl.class), serviceConnection,
        Context.BIND_AUTO_CREATE);

    return ipAddressTask.getTask().continueWithTask(new Continuation<String, Task<Void>>() {
      @Override
      public Task<Void> then(Task<String> task) throws Exception {
        logger.info("Stopping Cling...");
        context.unbindService(serviceConnection);
        return shutdownTask.getTask();
      }
    }).continueWithTask(new Continuation<Void, Task<String>>() {
      @Override
      public Task<String> then(Task<Void> task) throws Exception {
        logger.info("Cling stopped.");
        return ipAddressTask.getTask();
      }
    });
  }
}
