package vdab.extnodes.zwave;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.datatypes.AFEnum;

public class ZWaveSwitch extends ZWaveTarget_A{
	private boolean c_IsOn = false;


	public AnalysisDataDef def_Node(AnalysisDataDef theDataDef){
			AFEnum theEnum = ZWaveNodeInfo.getNodeEnumForClasses("Switch", new short[]{37});
			theDataDef.setEnum(theEnum);

		return theDataDef;
	}


	@Override
	protected void triggerZWaveTarget(int id) {
		if (c_IsOn){
			getZWaveManager().switchOneOff(id);
			c_IsOn = false;
		}
		else {
			getZWaveManager().switchOneOn(id);
			c_IsOn = true;
		}	
		
		
	}

	@Override
	protected void processZWaveTarget(int id, AnalysisData ad) {
		// Get the selected Boolean and turn on or off the switch.
		if (!ad.isBoolean()){
			setError("Expecting boolean value for switch control DATA="+ad);
			return;
		}
		Boolean state = ad.getDataAsBoolean();
		if (state.booleanValue())
			getZWaveManager().switchOneOn(id);
		else
			getZWaveManager().switchOneOff(id);
		
	}

}
