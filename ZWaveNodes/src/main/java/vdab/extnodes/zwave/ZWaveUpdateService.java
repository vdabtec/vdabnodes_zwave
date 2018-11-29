package vdab.extnodes.zwave;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.AnalysisEvent;
import com.lcrc.af.AnalysisService;
import com.lcrc.af.constants.OutputEventType;
import com.lcrc.af.datatypes.AFEnum;

public class ZWaveUpdateService extends AnalysisService{

	private ZWaveManager c_ZWaveManager;
	private Integer c_Node;
	private Integer c_ClassCode;
	private Integer c_Index;
	public ZWaveUpdateService(){
		addDelegatedAttribute("ZWavePort", ZWaveCommonAttributes.getInstance());
		addDelegatedAttribute("ZWaveConfigDirectory", ZWaveCommonAttributes.getInstance());
		set_OutputType(OutputEventType.BOOLEAN); // Always outputs a boolean succeeded or failed.
	}
	public void set_LogLevel(Integer level){
		super.set_LogLevel(level);
		if (c_ZWaveManager != null)
			c_ZWaveManager.updateLogLevel();
	}

	public void set_Node(Integer nodeId){
		c_Node = nodeId;
	}
	public AnalysisDataDef def_Node(AnalysisDataDef theDataDef){
		AFEnum theEnum = ZWaveNodeInfo.getNodeEnum();
		theDataDef.setEnum(theEnum);
		return theDataDef;
	}
	public Integer get_Node(){
		return c_Node;
	}
	public void set_ClassCode(Integer code){
		c_ClassCode = code;
	}
	public Integer get_ClassCode(){
		return c_ClassCode;
	}
	public AnalysisDataDef def_ClassCode(AnalysisDataDef theDataDef){
		if (c_Node != null ){
			AFEnum theEnum = ZWaveNodeInfo.getClassCodeEnum(c_Node.shortValue());
			if (theEnum != null)
				theDataDef.setEnum(theEnum);
		}
		return theDataDef;
	}
	public void set_AttributeName(Integer index){
		c_Index = index;
	}
	public Integer get_AttributeName(){
		return c_Index;
	}
	public AnalysisDataDef def_AttributeName(AnalysisDataDef theDataDef){
		if (c_ClassCode != null && c_Node != null){
			AFEnum theEnum = ZWaveNodeInfo.getValueIndexEnumForClass(c_Node.shortValue(), c_ClassCode.shortValue());
			if (theEnum != null)
				theDataDef.setEnum(theEnum);
		}
		return theDataDef;
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
	public synchronized void processEvent(AnalysisEvent ev){
		AnalysisData ad =  getSelectedData(ev);
		if (ad == null ){
			setWarning("Selected data not available EVENT="+ev);
			return;
		}
		if (!ad.isSimple()){
			setError("Selected data must represent a simple value");
			return;
		}
		boolean ok = c_ZWaveManager.setZWaveValue(ad,c_Node.intValue(), c_ClassCode.intValue(), c_Index.intValue());
		if (ok)
			serviceCompleted(ev);
		else
			serviceFailed(ev, 1);
	}

}
