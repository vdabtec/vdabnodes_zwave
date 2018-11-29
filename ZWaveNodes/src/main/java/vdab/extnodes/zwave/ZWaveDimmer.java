package vdab.extnodes.zwave;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.datatypes.AFEnum;

public class ZWaveDimmer extends ZWaveTarget_A{
	private boolean c_IsOn = false;
	private int c_LastDimLevel = 99;
	public  Integer get_DimmerLevel(){
		return c_LastDimLevel;
	
	}
	public AnalysisDataDef def_Node(AnalysisDataDef theDataDef){
			AFEnum theEnum = ZWaveNodeInfo.getNodeEnumForClasses("Dimmer", new short[]{ZWaveClassCode.SWITCH_MULTILEVEL});
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
        if (ad.isNumeric()){      //DIM
            int level = ad.getDataAsInteger().intValue();
            if (level >= 0 && level <= 99){
                getZWaveManager().setSwitchLevel( id, level );
                c_LastDimLevel = level;
            }
            else {
                setWarning("Level must be between 0 and 100. VALUE=" + level);
            }
        }
        else if (ad.isBoolean()){ // SWITCH IT          
            Boolean level = ad.getDataAsBoolean();
            if (level.booleanValue())
                getZWaveManager().setSwitchLevel( id, c_LastDimLevel );
            else
                getZWaveManager().setSwitchLevel( id, 0 );
        }
        else {
            setError("Expecting boolean or numeric value DATA="+ad);
            return;       
        }
    }
}
