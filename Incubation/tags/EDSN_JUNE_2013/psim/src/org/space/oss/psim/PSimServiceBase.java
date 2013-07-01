package org.space.oss.psim;

public class PSimServiceBase implements PSimService 
{
	protected PSim psim_;

	@Override
	public void init(PSim psim, Config cfg) 
	{
		psim_ = psim;
	}

	@Override
	public PSim getPSim() { return psim_; }
	
	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void save(String dir) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void load(String dir) {
		// TODO Auto-generated method stub
		
	}

}
