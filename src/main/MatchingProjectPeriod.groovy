package main

class MatchingProjectPeriod {
	ProjectPeriod period
	boolean periodMatch

	public MatchingProjectPeriod(boolean m, ProjectPeriod p){
		this.periodMatch = m
		this.period = p
	}
}
