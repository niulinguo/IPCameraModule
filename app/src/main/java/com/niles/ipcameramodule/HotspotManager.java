package com.niles.ipcameramodule;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Niles
 * Date 2018/10/9 13:02
 * Email niulinguo@163.com
 */
public class HotspotManager {

    private final List<HotspotClientInfo> mInfoList = new ArrayList<>();

    public void refresh() {
        mInfoList.clear();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            String[] names = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split("\\s{2,}");
                if (names == null) {
                    names = split;
                } else {
                    HotspotClientInfo.Builder builder = new HotspotClientInfo.Builder();
                    for (int i = 0; i < names.length; i++) {
                        String key = names[i];
                        String value = split[i];
                        switch (key) {
                            case "IP address": {
                                builder.setIPAddress(value);
                                break;
                            }
                            case "HW type": {
                                builder.setHWType(value);
                                break;
                            }
                            case "Flags": {
                                builder.setFlags(value);
                                break;
                            }
                            case "HW address": {
                                builder.setHWAddress(value);
                                break;
                            }
                            case "Mask": {
                                builder.setMask(value);
                                break;
                            }
                            case "Device": {
                                builder.setDevice(value);
                                break;
                            }
                        }
                    }
                    mInfoList.add(builder.build());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<HotspotClientInfo> getInfoList() {
        return mInfoList;
    }
}
