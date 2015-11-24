package main;

import java.util.Date;

public class ProjectPeriod {
	private Date startDate

	private Date endDate

	private String binPath

	private String srcPath

	private String libPaths
	
	private String buildSystem
	
	public ProjectPeriod(Date startDate = null, Date finalDate = null, String binPath = "/bin", 
		String srcPath = "/src", String libPaths = null, String buildSystem = null)
	{
		this.startDate = startDate
		this.endDate = finalDate
		this.binPath = binPath
		this.srcPath = srcPath
		this.libPaths = libPaths
		this.buildSystem = buildSystem
	}
	
	public Date getStartDate()
	{
		startDate
	}
	
	public Date getEndDate()
	{
		endDate
	}
	
	public String getBinPath()
	{
		binPath
	}
	
	public String getSrcPath()
	{
		srcPath
	}
	
	public String getLibPaths()
	{
		libPaths
	}
	
	public String getBuildSystem()
	{
		buildSystem
	}
}
