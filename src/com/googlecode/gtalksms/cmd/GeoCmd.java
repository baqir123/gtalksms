package com.googlecode.gtalksms.cmd;

import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;

import com.googlecode.gtalksms.LocationService;
import com.googlecode.gtalksms.MainService;
import com.googlecode.gtalksms.R;
import com.googlecode.gtalksms.panels.GeoPopup;
import com.googlecode.gtalksms.xmpp.XmppMsg;

public class GeoCmd extends Command {
    public final String[] commands = {"geo", "where"};

    public GeoCmd(MainService mainService) {
        super(mainService);
    }
    
    public String[] getCommands() {
        return commands;
    }

    @Override
    public void execute(String cmd, String args) {
        if (cmd.equals("geo")) {
            geo(args);
        } else if (cmd.equals("where")) {
            send(getString(R.string.chat_start_locating));
            startLocatingPhone();
        }  
    }
    
    /** Open geolocalization application */
    private void geo(String text) {
        List<Address> addresses = geoDecode(text);
        if (addresses != null) {
            if (addresses.size() > 1) {
                XmppMsg addr = new XmppMsg(getString(R.string.chat_specify_details));
                addr.newLine();
                for (Address address : addresses) {
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addr.appendLine(address.getAddressLine(i));
                    }
                }
                send(addr);
            } else if (addresses.size() == 1) {
                launchExternal(addresses.get(0).getLatitude() + "," + addresses.get(0).getLongitude());
            }
        } else {
            send(getString(R.string.chat_no_match_for, text));
            // For emulation testing
            // GeoManager.launchExternal("48.833199,2.362232");
        }
    }
    
    /** Starts the geolocation service */
    public void startLocatingPhone() {
        Intent intent = new Intent(_context, LocationService.class);
        intent.setAction(LocationService.START_SERVICE);
        _context.startService(intent);
    }

    /** Stops the geolocation service */
    public void stopLocatingPhone() {
        Intent intent = new Intent(_context, LocationService.class);
        intent.setAction(LocationService.STOP_SERVICE);
        _context.startService(intent);
    }

    /** Return List of <Address> from searched location */
    public List<Address> geoDecode(String searchedLocation) {
        try {
            Geocoder geo = new Geocoder(_context, Locale.getDefault());
            List<Address> addresses = geo.getFromLocationName(searchedLocation, 10);
            if (addresses != null && addresses.size() > 0) {
               return addresses;
            }
        }
        catch(Exception ex) {
        }

        return null;
    }

    /** launches an activity on the url */
    public void launchExternal(String url) {
        Intent popup = new Intent(_context, GeoPopup.class);
        popup.putExtra("url", url);
        popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        _context.startActivity(popup);
    }

    @Override
    public void stop() {
        stopLocatingPhone();
    }
}