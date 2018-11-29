package vdab.extnodes.zwave;

import com.lcrc.af.datatypes.AFEnum;

public class ZWaveClassCode {
	public final static int UNDEFINED = 0;
	public final static int BASIC = 32;
	public final static int SWITCH_BINARY = 37;
	public final static int SWITCH_MULTILEVEL = 38;
	public final static int SWITCH_ALL = 39;
	public final static int	SENSOR_BINARY = 48;
	public final static int SENSOR_MULTILEVEL = 49;
	public final static int ZWAVEPLUS_INFO = 94;
	public final static int CONFIGURATION = 112;
	public final static int ALARM = 113;	
	public final static int MANUFACTURER_SPECIFIC = 114;
	public final static int POWERLEVEL = 115;
	public final static int BATTERY = 128;
	public final static int WAKE_UP = 132;
	public final static int VERSION = 134;

	private static AFEnum s_EnumZWaveClassCode = new AFEnum("ZWaveClassCode")
	.addEntry(ZWaveClassCode.BASIC,"Basic")
	.addEntry(ZWaveClassCode.SWITCH_BINARY,"Switch-Binary")
	.addEntry(ZWaveClassCode.SWITCH_MULTILEVEL, "Switch-Multilevel")
	.addEntry(ZWaveClassCode.SWITCH_ALL, "Switch-All")
	.addEntry(ZWaveClassCode.SENSOR_BINARY,"Sensor-Binary")
	.addEntry(ZWaveClassCode.SENSOR_MULTILEVEL, "Sensor-Multilevel")
	.addEntry(ZWaveClassCode.ZWAVEPLUS_INFO, "ZWavePlus-Info")
	.addEntry(ZWaveClassCode.CONFIGURATION, "Configuration")
	.addEntry(ZWaveClassCode.ALARM, "Alarm")
	.addEntry(ZWaveClassCode.MANUFACTURER_SPECIFIC, "Manufacturer-Specific")
	.addEntry(ZWaveClassCode.POWERLEVEL, "Powerlevel")
	.addEntry(ZWaveClassCode.BATTERY, "Battery")
	.addEntry(ZWaveClassCode.WAKE_UP, "Wake-Up")
	.addEntry(ZWaveClassCode.VERSION, "Version")
	;
	public static AFEnum getEnum(){
		return s_EnumZWaveClassCode ;
	}
}
