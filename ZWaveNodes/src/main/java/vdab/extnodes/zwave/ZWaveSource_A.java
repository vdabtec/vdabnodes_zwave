package vdab.extnodes.zwave;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.AnalysisSource;
import com.lcrc.af.datatypes.AFEnum;

public abstract class ZWaveSource_A  extends AnalysisSource {
	

	private String c_ZWavePort;
	private ZWaveManager c_ZWaveManager;
	private Integer c_Node;
	
	public void set_ZWavePort(String port){
		c_ZWavePort = port;
	}

	public String get_ZWavePort(){
		return  c_ZWavePort;
	}
	public void set_Node(Integer nodeId){
		c_Node = nodeId;
	}
	public Integer get_Node(){
		return c_Node;
	}


	public void _init(){
		initManager();
		super._init();

	}
	public void _start() {
		if (initManager()){
			super._start();
			return ;
		}
		_disable();
		return;
	}
	public void _stop() {
		super._stop();
		c_ZWaveManager.removeZWaveSource(this);
	}
	private boolean initManager(){
		c_ZWaveManager = ZWaveManager.getZWaveManager(c_ZWavePort);
		if (c_ZWaveManager != null){
			c_ZWaveManager.addZWaveSource(this);
			c_ZWaveManager.init();		
			return c_ZWaveManager.isInitialized();
		}
		return false;
		
	}
	public abstract void publishIndividualEvent(AnalysisData ad);

}
