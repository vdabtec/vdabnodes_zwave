package vdab.extnodes.zwave;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.AnalysisEvent;
import com.lcrc.af.AnalysisTarget;


public class ZWaveTarget extends AnalysisTarget {
	static {
		ZWaveDeviceType.getEnum();
	}
	private String c_ZWavePort;
	private ZWaveManager c_ZWaveManager;
	private Integer c_DeviceType;
	private String c_NodeName;
	private boolean c_IsOn = false;

	public void set_ZWavePort(String port){
		c_ZWavePort = port;
	}

	public String get_ZWavePort(){
		return  c_ZWavePort;
	}
	public void set_DeviceType(Integer type){
		c_DeviceType = type;
	}
	public Integer get_DeviceType(){
		return  c_DeviceType ;
	}
	public void set_NodeName(String name){
		c_NodeName = name;
	}
	public String get_NodeName(){
		return c_NodeName;
	}
	public AnalysisDataDef def_NodeName(AnalysisDataDef theDataDef){
		if (c_DeviceType != null){
			String[] nodes = ZWaveNodeInfo.getNodesSupportingClass((short) c_DeviceType.intValue());
			if (nodes.length > 0)
				theDataDef.setAllPickValues(nodes);
		}
		return theDataDef;
	}

	public void _start(){
		c_ZWaveManager = ZWaveManager.getZWaveManager(c_ZWavePort);
		c_ZWaveManager.addZWaveTarget(this);
		super._start();
	}

	public synchronized void processEvent(AnalysisEvent ev){
		/*
		AnalysisData ad =  getSelectedData(ev);
		if (ad == null ){
			setWarning("Selected data not available EVENT="+ev);
			return;
		}
		*/
		
		if (c_IsOn){
			c_ZWaveManager.switchAllOff();
			c_IsOn = false;
		}
		else {
			c_ZWaveManager.switchAllOn();
			c_IsOn = true;
		}
	}
}
