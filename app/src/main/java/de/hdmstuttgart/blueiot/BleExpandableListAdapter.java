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

/**
 * Custom ExpandableListAdapter that is used to inspect a remote BLE-Device.
 * Handles the services of the BLE-Device as the internal group and a list of BluetoothGattCharacteristics as the internal children of each view created.
 */
public class BleExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private LayoutInflater inflater;

    //Internal Collections:
    //Group
    private List<BluetoothGattService> gattServices;
    //Children
    private HashMap<BluetoothGattService, List<BluetoothGattCharacteristic>> characteristicCollection;

    /**
     * Constructor
     * @param context ApplicationContext used to inflate layout components
     */
    public BleExpandableListAdapter(Context context) {
        super();

        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.gattServices = new ArrayList<>();
        this.characteristicCollection = new HashMap<>();
    }

    /**
     * Adds a service to the collection
     * @param service The BluetoothGattService to be added
     */
    public void addService(BluetoothGattService service) {
        if (!this.gattServices.contains(service)) {
            this.gattServices.add(service);
        }
    }

    /**
     * Allows retrieving a service from the adapter (e.g. when the item is being clicked in the ExpandableListView)
     * @param position The position in the adapter
     * @return The BluetoothGattService at the specified position
     */
    public BluetoothGattService getService(int position) {
        return this.gattServices.get(position);
    }

    /**
     * Adds a collection of BluetoothGattCharacteristics to the Hashmap associating it with the service that was handed over
     * @param service The parent service that the characteristics belong to
     * @param characteristics A list of characteristics that belong to a BluetoothGattService
     */
    public void addCharacteristics(BluetoothGattService service, List<BluetoothGattCharacteristic> characteristics) {
        if (!this.characteristicCollection.containsKey(service)) {
            this.characteristicCollection.put(service, characteristics);
        }
    }

    /**
     * Allows retrieving a specific BluetoothGattCharacteristic from the adapter (e.g. when the item is being clicked in the ExpandableListView)
     * @param service The parent service that the characteristic belongs to
     * @param characteristicUUID The unique identifier for this characteristic
     * @return The BluetoothGattCharacteristic associated with the specified UUID or null, if none is found
     */
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

    /**
     * Clears all of the internal collections
     */
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

    /**
     * Adapter method that is called for each item in the internal collection that holds the group-elements (i.e. this.gattServices).
     * This method is called internally and is used to generate the View that is shown within the ExpandableListView for each of the item in the list.
     * @param groupPosition The position of the group in the adapter
     * @param isExpanded Boolean value indicating whether or not the element is expanded
     * @param convertView The old view to reuse, if possible. Should check if this view is non-null.
     * @param parent The parent that the view will be attached to
     * @return A View that is displayed in the ExpandableListView
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        //Get the Group-Element from the collection
        BluetoothGattService service = (BluetoothGattService) getGroup(groupPosition);
        String serviceDescription = "";
        String serviceUUID = "";
        if (service != null) {
            //Set the Service-Type
            if (service.getType() == 0) {
                serviceDescription = "Primary Service";
            }
            else if (service.getType() == 1) {
                serviceDescription = "Secondary Service";
            }
            else {
                serviceDescription = "Unknown Service";
            }

            //Set the Service-UUID
            serviceUUID = "UUID: " + service.getUuid().toString();
        }

        //Inflate custom layout resource
        convertView = this.inflater.inflate(R.layout.list_group_item, null);

        //Get references to UI-Components
        TextView textView_serviceDescription = (TextView) convertView.findViewById(R.id.service_description);
        TextView textView_serviceUUID = (TextView) convertView.findViewById(R.id.service_uuid);

        //Customize TextViews
        textView_serviceDescription.setText(serviceDescription);
        textView_serviceUUID.setText(serviceUUID);

        return convertView;
    }

    /**
     * Adapter method that is called for each item in the internal collection that holds the children-elements (i.e. this.characteristicCollection).
     * This method is called internally and is used to generate the View that is shown within the ExpandableListView for each of the item in the list.
     * @param groupPosition The position of the group in the adapter
     * @param childPosition The position of the child in the adapter
     * @param isLastChild Boolean value indicating whether this child is the last in the collection
     * @param convertView The old view to reuse, if possible. Should check if this view is non-null.
     * @param parent The parent that the view will be attached to
     * @return A View that is displayed in the ExpandableListView
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        //Get the Child-Element from the HashMap
        BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) getChild(groupPosition, childPosition);
        String characteristicUUID = "";
        String characteristicPermissions = "";
        String characteristicProperties = "";
        String characteristicWriteType = "";
        if (characteristic != null) {
            //Set Characteristic values
            characteristicUUID = characteristic.getUuid().toString();
            characteristicPermissions = getCharacteristicPermissionDescription(characteristic);
            characteristicProperties = getCharacteristicPropertyDescription(characteristic);
            characteristicWriteType = getCharacteristicWriteTypeDescription(characteristic);
        }

        //Inflate custom layout resource
        convertView = this.inflater.inflate(R.layout.list_child_item, null);

        //Get references to UI-Components & customize
        TextView textView_characteristicDescription = (TextView) convertView.findViewById(R.id.characteristic_description);
        textView_characteristicDescription.setText("Characteristic UUID: " + characteristicUUID + ", Permission: " + characteristicPermissions + ", Properties: " + characteristicProperties + ", WriteType: " + characteristicWriteType);

        return convertView;
    }

    /**
     * (Dis-)Allow selecting child elements
     * @param groupPosition The position of the group in the adapter
     * @param childPosition The position of the child in the adapter
     * @return Boolean value indicating whether the child can be selected or not
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * Converts the integer value that indicates the permissions of a BluetoothGattCharacteristic into a readable String
     * @param characteristic The BluetoothGattCharacteristic to inspect
     * @return String-form of the permissions
     */
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

    /**
     * Converts the integer value that indicates the properties of a BluetoothGattCharacteristic into a readable String
     * @param characteristic The BluetoothGattCharacteristic to inspect
     * @return String-form of the properties
     */
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

    /**
     * Converts the integer value that indicates the WriteType of a BluetoothGattCharacteristic into a readable String
     * @param characteristic The BluetoothGattCharacteristic to inspect
     * @return String-form of the WriteType
     */
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