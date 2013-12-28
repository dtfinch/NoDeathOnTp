/*
 NoDeathOnTp: Attempt to resolving the death bug after a teleportation
 Copyright (C) 2013  Michel Blanchet

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.tabinol.nodeathontp;

import java.util.Calendar;

public class PlayerTpData {
    
    private final Calendar lastTpTime;
    private final int lastX;
    private final int lastZ;
    private Long lastReLocation;
    
    public PlayerTpData(Calendar lastTpTime, int lastX, int lastZ) {
        
        this.lastTpTime = lastTpTime;
        this.lastX = lastX;
        this.lastZ = lastZ;
        this.lastReLocation = Long.MIN_VALUE;
    }
    
    public Calendar getLastTpTime() {
        
        return lastTpTime;
    }
    
    public int getLastX() {
        
        return lastX;
    }
    
    public int getLastZ() {
        
        return lastZ;
    }
    
    public Long getLastReLocation() {
        
        return lastReLocation;
    }
    
    public void setLastReLocationToNow(Long now) {

        this.lastReLocation = now;
    }
}
