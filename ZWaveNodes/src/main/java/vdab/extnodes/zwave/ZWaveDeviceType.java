package vdab.extnodes.zwave;

import com.lcrc.af.datatypes.AFEnum;

public class ZWaveDeviceType {
	public final static int UNDEFINED = 0;
	public final static int SWITCH_BINARY = 37;
	public final static int SWITCH_MULTILEVEL = 38;

	private static AFEnum s_EnumZWaveCommandClassType = new AFEnum("ZWaveDeviceType")
	.addEntry(ZWaveDeviceType.SWITCH_BINARY,"Switch")
	.addEntry(ZWaveDeviceType.SWITCH_MULTILEVEL, "Dimmer");

	public static AFEnum getEnum(){
		return s_EnumZWaveCommandClassType ;
	}
}
