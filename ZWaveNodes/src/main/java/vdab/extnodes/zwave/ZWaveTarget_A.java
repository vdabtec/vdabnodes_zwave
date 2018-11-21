package vdab.extnodes.zwave;

import vdab.api.node.Target_A;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisEvent;

public abstract class ZWaveTarget_A extends Target_A {

	private String c_ZWavePort;
	private ZWaveManager c_ZWaveManager;
	private Integer c_Node;
	
	public void set_ZWavePort(String port){
		c_ZWavePort = port;
	}

	public String get_ZWavePort(){
		return  c_ZWavePort;
	}

	public void set_Node(Integer node){
		c_Node = node;
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
	private boolean initManager(){
		c_ZWaveManager = ZWaveManager.getZWaveManager(c_ZWavePort);
		if (c_ZWaveManager != null){
			c_ZWaveManager.addZWaveTarget(this);
			c_ZWaveManager.init();		
			return c_ZWaveManager.isInitialized();
		}
		return false;
		
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
