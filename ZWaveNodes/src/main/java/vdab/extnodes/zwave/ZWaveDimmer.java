package vdab.extnodes.zwave;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.datatypes.AFEnum;

public class ZWaveDimmer extends ZWaveTarget_A{
	private boolean c_IsOn = false;


	public AnalysisDataDef def_Node(AnalysisDataDef theDataDef){
			AFEnum theEnum = ZWaveNodeInfo.getNodeEnumForClasses("Dimmer", new short[]{38});
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
		
	
		
		if (ad.isNumeric()){  // DIM IT
			
			// TODO - call manager.setSwitchLevel
		}
		else if (ad.isBoolean()){ // SWITCH IT
			// TODO - Same code as switch.
			
		}
		else {
			
			// TODO - seterror
			return;
			
		}
			
	}

}
