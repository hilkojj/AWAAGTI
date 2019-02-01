package DBSummariser;

public class IntermediateSummariser extends Summariser
{
	public IntermediateSummariser(long unixTime, long dir)
	{
		super(dir);
		this.unixTime = unixTime;
	}
	
	protected String makeReadFilesFileName(int second)
	{
		String name =  String.format("%s%02d/%s_%s_sum.awaagti",
				this.dir,
				second,
				s2Type.toString().toLowerCase(),
				sType.toString().toLowerCase());
		return name;
	}
}
