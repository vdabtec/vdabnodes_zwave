package vdab.extnodes.zwave;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.datatypes.AFEnum;

public class ZWaveSensor extends ZWaveSource_A {
	public AnalysisDataDef def_Node(AnalysisDataDef theDataDef){
		AFEnum theEnum = ZWaveNodeInfo.getNodeEnumForClasses("Sensor", new short[]{ZWaveClassCode.SENSOR_BINARY, ZWaveClassCode.SENSOR_MULTILEVEL});
		theDataDef.setEnum(theEnum);

		return theDataDef;
	}
	@Override
	public void publishIndividualEvent(AnalysisData ad) {
		this.publishNewEvent(ad);
	}

}
