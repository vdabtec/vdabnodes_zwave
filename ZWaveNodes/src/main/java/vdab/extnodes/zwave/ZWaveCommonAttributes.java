package vdab.extnodes.zwave;

import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.AnalysisObject;


public class ZWaveCommonAttributes {
	private static ZWaveCommonAttributes s_Instance = new ZWaveCommonAttributes();
	private static AnalysisDataDef[] s_ZWaveManger_ddefs = new AnalysisDataDef[]{
		AnalysisObject.getClassAttributeDataDef(ZWaveCommonAttributes.class,"ZWavePort")
		.setRequired().setEditOrder(11).setAttrDesc("Communication port where the ZWave Controller is attached"),
		AnalysisObject.getClassAttributeDataDef(ZWaveCommonAttributes.class,"ZWaveConfigDirectory")
				.setRequired().setEditOrder(12).setAttrDesc("Config directory for the OpenZWave installation")
		};

	public static ZWaveCommonAttributes getInstance(){
		return s_Instance;
	}
	private String c_ZWavePort;
	private String c_ZWaveConfigDirectory;
	
	public String get_ZWavePort(){
		return  c_ZWavePort;
	}
	public AnalysisDataDef def_ZWavePort(AnalysisDataDef theDataDef){
		String[] ports = ZWaveManager.getZWavePorts();
		if (ports.length > 0)
			theDataDef.setAllPickValues(ports);
		return theDataDef;
	}
	public void set_ZWavePort(String port){
		c_ZWavePort = port;
	}
	public String get_ZWaveConfigDirectory(){
		return  c_ZWaveConfigDirectory;
	}
	public void set_ZWaveConfigDirectory(String dir){
		c_ZWaveConfigDirectory = dir;
	}

}
