package vdab.extnodes.zwave;

import vdab.api.node.Target_A;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisEvent;

public abstract class ZWaveTarget_A extends Target_A {

	private ZWaveManager c_ZWaveManager;
	private Integer c_Node;
	
	public ZWaveTarget_A(){
		try {
			addDelegatedAttribute("ZWavePort", ZWaveCommonAttributes.getInstance());
			addDelegatedAttribute("ZWaveConfigDirectory", ZWaveCommonAttributes.getInstance());
		}
		catch (Exception e){
			setError("Unable to set delegated attributes");
		}	}
	public void set_LogLevel(Integer level){
		super.set_LogLevel(level);
		if (c_ZWaveManager != null)
			c_ZWaveManager.updateLogLevel();
	}
	
	public void set_Node(Integer node){
		c_Node = node;
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
	
	protected ZWaveManager getZWaveManager(){
		return c_ZWaveManager;
	}

	public synchronized void processEvent(AnalysisEvent ev){
		if (c_Node == null){
			setWarning("Node not defined can't process event ");
			return;	
		}
		// If it is a trigger, call the trigger method of the sub class.
		if (ev.isTriggerEvent()){
			triggerZWaveTarget(c_Node.intValue());
			return;
		}
		AnalysisData ad =  getSelectedData(ev);
		if (ad == null ){
			setWarning("Selected data not available EVENT="+ev);
			return;
		}
		if (!ad.isSimple()){
			setError("Selected data must represent a simple value");
			return;
		}
		// If it gets here its got a single data value that was selected.
		processZWaveTarget(c_Node.intValue(),  ad);


	}
	protected abstract void triggerZWaveTarget(int nodeId);
	protected abstract void processZWaveTarget(int nodeId, AnalysisData ad);
}
