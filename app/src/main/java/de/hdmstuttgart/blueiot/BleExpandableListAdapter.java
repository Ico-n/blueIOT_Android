package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BleExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private LayoutInflater inflater;

    private List<BluetoothGattService> gattServices;
    private HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>> characteristicCollection;

    public BleExpandableListAdapter(Context context) {
        super();

        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.gattServices = new ArrayList<>();
        this.characteristicCollection = new HashMap<>();
    }

    public void addService(BluetoothGattService service) {
        if (!this.gattServices.contains(service)) {
            this.gattServices.add(service);
        }
    }

    public BluetoothGattService getService(int position) {
        return this.gattServices.get(position);
    }

    public void addCharacteristics(BluetoothGattService service, List<BluetoothGattCharacteristic> characteristics) {
        if (!this.characteristicCollection.containsKey(service)) {
            this.characteristicCollection.put(service, characteristics);
        }
    }

    public BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service, UUID characteristicUUID) {
        if (this.characteristicCollection.containsKey(service)) {
            for (BluetoothGattCharacteristic characteristic : this.characteristicCollection.get(service)) {
                if (characteristic.getUuid().equals(characteristicUUID)) {
                    return characteristic;
                }
            }
        }

        return null;
    }

    public void clear() {
        this.gattServices.clear();
        this.characteristicCollection.clear();
    }

    @Override
    public int getGroupCount() {
        return this.gattServices.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.characteristicCollection.get(this.gattServices.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.gattServices.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.characteristicCollection.get(this.gattServices.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        BluetoothGattService service = (BluetoothGattService) getGroup(groupPosition);
        String serviceDescription = "";
        String serviceUUID = "";
        if (service != null) {
            if (service.getType() == 0) {
                serviceDescription = "Primary Service";
            }
            else if (service.getType() == 1) {
                serviceDescription = "Secondary Service";
            }
            else {
                serviceDescription = "Unknown Service";
            }

            serviceUUID = "UUID: " + service.getUuid().toString();
        }

        convertView = this.inflater.inflate(R.layout.list_group_item, null);
        TextView textView_serviceDescription = (TextView) convertView.findViewById(R.id.service_description);
        TextView textView_serviceUUID = (TextView) convertView.findViewById(R.id.service_uuid);

        textView_serviceDescription.setText(serviceDescription);
        textView_serviceUUID.setText(serviceUUID);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) getChild(groupPosition, childPosition);
        String characteristicUUID = "";
        String characteristicPermissions = "";
        String characteristicProperties = "";
        String characteristicWriteType = "";
        if (characteristic != null) {
            characteristicUUID = characteristic.getUuid().toString();
            characteristicPermissions = getCharacteristicPermissionDescription(characteristic);
            characteristicProperties = getCharacteristicPropertyDescription(characteristic);
            characteristicWriteType = getCharacteristicWriteTypeDescription(characteristic);
        }

        convertView = this.inflater.inflate(R.layout.list_child_item, null);

        TextView textView_characteristicDescription = (TextView) convertView.findViewById(R.id.characteristic_description);
        textView_characteristicDescription.setText("Characteristic UUID: " + characteristicUUID + ", Permission: " + characteristicPermissions + ", Properties: " + characteristicProperties + ", WriteType: " + characteristicWriteType);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private String getCharacteristicPermissionDescription(BluetoothGattCharacteristic characteristic) {
        switch (characteristic.getPermissions()) {
            case 1:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PERMISSION_READ_Description);
            case 2:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PERMISSION_READ_ENCRYPTED_Description);
            case 4:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PERMISSION_READ_ENCRYPTED_MITM_Description);
            case 16:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PERMISSION_WRITE_Description);
            case 32:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PERMISSION_WRITE_ENCRYPTED_Description);
            case 64:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PERMISSION_WRITE_ENCRYPTED_MITM_Description);
            case 128:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PERMISSION_WRITE_SIGNED_Description);
            case 256:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PERMISSION_WRITE_SIGNED_MITM_Description);
            default:
                return String.valueOf(characteristic.getPermissions());
        }
    }

    private String getCharacteristicPropertyDescription(BluetoothGattCharacteristic characteristic) {
        switch (characteristic.getProperties()) {
            case 1:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PROPERTY_BROADCAST_Description);
            case 2:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PROPERTY_READ_Description);
            case 4:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PROPERTY_WRITE_NO_RESPONSE_Description);
            case 8:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PROPERTY_WRITE_Description);
            case 16:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PROPERTY_NOTIFY_Description);
            case 32:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PROPERTY_INDICATE_Description);
            case 64:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PROPERTY_SIGNED_WRITE_Description);
            case 128:
                return this.context.getString(R.string.BluetoothGattCharacteristic_PROPERTY_EXTENDED_PROPS_Description);
            default:
                return String.valueOf(characteristic.getProperties());
        }
    }

    private String getCharacteristicWriteTypeDescription(BluetoothGattCharacteristic characteristic) {
        switch (characteristic.getWriteType()) {
            case 1:
                return this.context.getString(R.string.BluetoothGattCharacteristic_WRITE_TYPE_NO_RESPONSE_Description);
            case 2:
                return this.context.getString(R.string.BluetoothGattCharacteristic_WRITE_TYPE_DEFAULT_Description);
            case 4:
                return this.context.getString(R.string.BluetoothGattCharacteristic_WRITE_TYPE_SIGNED_Description);
            default:
                return String.valueOf(characteristic.getWriteType());
        }
    }
}