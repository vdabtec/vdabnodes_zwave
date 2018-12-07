package vdab.extnodes.zwave;

import java.io.File;

import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.AnalysisObject;
import com.lcrc.af.constants.FilePathType;
import com.lcrc.af.file.AFFileUtility;


public class ZWaveCommonAttributes {
	private static ZWaveCommonAttributes s_Instance = new ZWaveCommonAttributes();
	private static AnalysisDataDef[] s_ZWaveManger_ddefs = new AnalysisDataDef[]{
		AnalysisObject.getClassAttributeDataDef(ZWaveCommonAttributes.class,"ZWavePort")
		.setRequired().setEditOrder(11).setRefreshOnChange().setAttrDesc("Communication port where the ZWave Controller is attached"),
		AnalysisObject.getClassAttributeDataDef(ZWaveCommonAttributes.class,"ZWaveConfigDirectory")
				.setRequired().setEditOrder(12).setRefreshOnChange().setAttrDesc("Config directory for the OpenZWave installation")
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
		if (c_ZWaveConfigDirectory == null){
			String path = AFFileUtility.getAFDirPath("ext/openzwave_config");
			if (new File(path).exists())
				c_ZWaveConfigDirectory = path;
		}
		return  c_ZWaveConfigDirectory;
	}
	public void set_ZWaveConfigDirectory(String dir){
		c_ZWaveConfigDirectory = dir;
	}
	public AnalysisDataDef def_ZWaveConfigDirectory(AnalysisDataDef theDataDef){
		String[] dirs = AFFileUtility.getAllDirectories(FilePathType.ABSOLUTE, c_ZWaveConfigDirectory);
		theDataDef.setAllPickValues(dirs);
		return theDataDef;
	}



}
