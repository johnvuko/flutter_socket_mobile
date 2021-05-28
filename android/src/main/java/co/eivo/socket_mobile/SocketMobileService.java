package co.eivo.socket_mobile;

import android.util.Log;

import com.socketmobile.capture.AppKey;
import com.socketmobile.capture.CaptureError;
import com.socketmobile.capture.Property;
import com.socketmobile.capture.client.CaptureClient;
import com.socketmobile.capture.client.Configuration;
import com.socketmobile.capture.client.ConnectionCallback;
import com.socketmobile.capture.client.ConnectionState;
import com.socketmobile.capture.client.DataEvent;
import com.socketmobile.capture.client.DeviceClient;
import com.socketmobile.capture.client.DeviceState;
import com.socketmobile.capture.client.DeviceStateEvent;
import com.socketmobile.capture.client.callbacks.PropertyCallback;

import java.util.ArrayList;
import java.util.logging.Level;

interface SocketMobileServiceDelegate {
    public void didChangeDevices(ArrayList<SocketMobileDevice> devices);
    public void didReadData(SocketMobileDevice device, String message);
}

public class SocketMobileService implements ConnectionCallback, CaptureClient.Listener {

    private CaptureClient capture;
    private SocketMobileServiceDelegate delegate;
    private ArrayList<SocketMobileDevice> devices = new ArrayList<SocketMobileDevice>();

    private SocketMobileService()
    {}

    private static SocketMobileService instance = null;

    public static SocketMobileService getInstance()
    {
        if (instance == null) {
            instance = new SocketMobileService();
        }
        return instance;
    }

    public void setDelegate(SocketMobileServiceDelegate delegate) {
        this.delegate = delegate;
    }

    public void configure(String developerId, String appKey, String appId) {
        // avoid bug when hot reload
        // else should close `_capture`
        if (this.capture != null) {
            Log.w("SocketMobileService", "SocketMobile already configured");
            return;
        }

        AppKey appkey = new AppKey(appKey, appId, developerId);
        Configuration configuration = new Configuration();
        configuration.appKey(appkey);
        configuration.enableLogging(Level.ALL);

        this.capture = new CaptureClient(appkey);

        capture.setListener(this);
        capture.connect(this);
    }

    @Override
    public void onConnectionStateChanged(ConnectionState connectionState) {
        if (connectionState.hasError()) {
            if(connectionState.getError().getCode() == CaptureError.BLUETOOTH_NOT_ENABLED) {
                Log.e("SocketMobileService", "onConnectionStateChanged error: CaptureError.BLUETOOTH_NOT_ENABLED "+ connectionState.getError().getMessage());
            }
            else if(connectionState.getError().getCode() == CaptureError.COMPANION_NOT_INSTALLED) {
                Log.e("SocketMobileService", "onConnectionStateChanged error: CaptureError.COMPANION_NOT_INSTALLED "+ connectionState.getError().getMessage());
            }
            else if(connectionState.getError().getCode() == CaptureError.SERVICE_NOT_RUNNING) {
                Log.e("SocketMobileService", "onConnectionStateChanged error: CaptureError.SERVICE_NOT_RUNNING "+ connectionState.getError().getMessage());
            }
            else if(connectionState.getError().getCode() == CaptureError.UNABLE_TO_PARSE_RESPONSE) {
                Log.e("SocketMobileService", "onConnectionStateChanged error: CaptureError.UNABLE_TO_PARSE_RESPONSE "+ connectionState.getError().getMessage());
            }
            else {
                Log.e("SocketMobileService", "onConnectionStateChanged error: unknown " + connectionState.getError().getMessage());
            }
        }
        else {
            if (connectionState.isConnecting()) {
                Log.d("SocketMobileService", "onConnectionStateChanged: connecting");
            }
            else if (connectionState.isConnected()) {
                Log.d("SocketMobileService", "onConnectionStateChanged: connected");
            }
            else if (connectionState.isReady()) {
                Log.d("SocketMobileService", "onConnectionStateChanged: ready");
            }
            else if (connectionState.isDisconnecting()) {
                Log.d("SocketMobileService", "onConnectionStateChanged: disconnecting");
            }
            else if (connectionState.isDisconnected()) {
                Log.d("SocketMobileService", "onConnectionStateChanged: disconnected");
            }
        }
    }

    @Override
    public void onDeviceStateEvent(DeviceStateEvent deviceStateEvent) {
        if (deviceStateEvent.getState().intValue() == DeviceState.GONE) {
            Log.d("SocketMobileService", "Device " + deviceStateEvent.getDevice().getDeviceGuid() + " gone");
            this.removeDevice(deviceStateEvent.getDevice());
        }
        else if (deviceStateEvent.getState().intValue() == DeviceState.AVAILABLE) {
            Log.d("SocketMobileService", "Device " + deviceStateEvent.getDevice().getDeviceGuid() + " available");
            deviceStateEvent.getDevice().open();
        }
        else if (deviceStateEvent.getState().intValue() == DeviceState.OPEN) {
            Log.d("SocketMobileService", "Device " + deviceStateEvent.getDevice().getDeviceGuid() + " open");
        }
        else if (deviceStateEvent.getState().intValue() == DeviceState.READY) {
            Log.d("SocketMobileService", "Device " + deviceStateEvent.getDevice().getDeviceGuid() + " ready");
            this.addDevice(deviceStateEvent.getDevice());
        }
        else {
            Log.d("SocketMobileService", "Device " + deviceStateEvent.getDevice().getDeviceGuid() + " unknown state");
        }
    }

    private void addDevice(final DeviceClient device) {
        final ArrayList<SocketMobileDevice> devices = this.devices;
        final SocketMobileServiceDelegate delegate = this.delegate;

        device.getProperty(Property.create(Property.DEVICE_FRIENDLY_NAME), new PropertyCallback() {
            @Override
            public void onComplete(CaptureError captureError, final Property propertyName) {
                device.getProperty(Property.create(Property.DEVICE_BLUETOOTH_ADDRESS), new PropertyCallback() {
                    @Override
                    public void onComplete(CaptureError captureError, Property propertyAddress) {
                        String address = "";

                        for(int value: propertyAddress.array) {
                            address += String.format("%02X:", value);
                        }

                        address = address.substring(0, address.length() - 1);

                        devices.add(new SocketMobileDevice(propertyName.getString(), address, device.getDeviceGuid()));

                        if (delegate != null) {
                            delegate.didChangeDevices(devices);
                        }
                    }
                });
            }
        });
    }

    private void removeDevice(final DeviceClient device) {
        int index = -1;
        int i = 0;

        for(final SocketMobileDevice mobileDevice: this.devices) {
            if (mobileDevice.guid == device.getDeviceGuid()) {
                index = i;
                break;
            }

            i++;
        }

        if (index >= 0) {
            this.devices.remove(index);
        }
        else {
            Log.w("SocketMobileService", "unknown device to remove");
        }

        if (this.delegate != null) {
            this.delegate.didChangeDevices(this.devices);
        }
    }

    @Override
    public void onData(DataEvent dataEvent) {
        Log.d("SocketMobileService", "onData");

        if (delegate == null) {
            return;
        }

        SocketMobileDevice device = null;

        for(final SocketMobileDevice mobileDevice: this.devices) {
            if (mobileDevice.guid == dataEvent.getDevice().getDeviceGuid()) {
                device = mobileDevice;
                break;
            }
        }

        if (device == null) {
            Log.w("SocketMobileService", "receive data on unknown device");
            return;
        }

        String message = dataEvent.getData().getString();
        delegate.didReadData(device, message);
    }

    @Override
    public void onError(CaptureError captureError) {
        if(captureError.getCode() == CaptureError.BLUETOOTH_NOT_ENABLED) {
            Log.e("SocketMobileService", "error: CaptureError.BLUETOOTH_NOT_ENABLED "+ captureError.getMessage());
        }
        else if(captureError.getCode() == CaptureError.COMPANION_NOT_INSTALLED) {
            Log.e("SocketMobileService", "error: CaptureError.COMPANION_NOT_INSTALLED "+ captureError.getMessage());
        }
        else if(captureError.getCode() == CaptureError.SERVICE_NOT_RUNNING) {
            Log.e("SocketMobileService", "error: CaptureError.SERVICE_NOT_RUNNING "+ captureError.getMessage());
        }
        else if(captureError.getCode() == CaptureError.UNABLE_TO_PARSE_RESPONSE) {
            Log.e("SocketMobileService", "error: CaptureError.UNABLE_TO_PARSE_RESPONSE "+ captureError.getMessage());
        }
        else {
            Log.e("SocketMobileService", "error: unknown " + captureError.getMessage());
        }
    }

}