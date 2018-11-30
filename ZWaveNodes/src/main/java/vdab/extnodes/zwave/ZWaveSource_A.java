package vdab.extnodes.zwave;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisSource;

public abstract class ZWaveSource_A  extends AnalysisSource {
	

	private ZWaveManager c_ZWaveManager;
	private Integer c_Node;
	public ZWaveSource_A(){
		try {
			addDelegatedAttribute("ZWavePort", ZWaveCommonAttributes.getInstance());
			addDelegatedAttribute("ZWaveConfigDirectory", ZWaveCommonAttributes.getInstance());
		}
		catch (Exception e){
			setError("Unable to set delegated attributes");
		}
	}
	public void set_LogLevel(Integer level){
		super.set_LogLevel(level);
		if (c_ZWaveManager != null)
			c_ZWaveManager.updateLogLevel();
	}

	public void set_Node(Integer nodeId){
		c_Node = nodeId;
	}
	public Integer get_Node(){
		return c_Node;
	}

	public void _init(){
		c_ZWaveManager = ZWaveManager.initManager(this);
		super._init();
	}
	public void _start() {
		if (c_ZWaveManager == null){
			c_ZWaveManager = ZWaveManager.initManager(this);
			if (c_ZWaveManager == null){
				_disable();
				return;
			}
		}
		super._start();	
	}
	public void _stop() {
		if (c_ZWaveManager != null)
			c_ZWaveManager.removeZWaveNode(this);
		super._stop();
	}

	public abstract void publishIndividualEvent(AnalysisData ad);

}
