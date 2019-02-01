package DBSummariser;

import shared.DBValue;

public class Main
{
	public static void main(String[] args)
	{

		long fromUTS;
		long toUTS;

		if (args.length < 1) {
			System.out.println("Usage: db_summariser {unix time stamp: from} [unix time stamp: to, defaults to Now if not specified]");
			System.out.println("Generates summary files for every 100, 10*100, 100*100, etc between the given timestamps.");
			System.out.println("ERROR: Please provide unixtimestamp of when to start making summuries.");

			return;
		}

		fromUTS = Long.parseLong(args[0]);
		if (args.length >= 2) {
			toUTS = Long.parseLong(args[1]);
		} else {
			toUTS = (int)(System.currentTimeMillis()/1000);
		}
		
		boolean dryRun = args.length >= 3 && args[2].equalsIgnoreCase("dryrun");
		
		System.out.println("Start db_summariser");
		System.out.print("Summarise from ");
		System.out.print(fromUTS);
		System.out.print(" to ");
		System.out.println(toUTS);
		
		if (dryRun) {
			System.out.println("Dry run!");
		}
		
		long now = fromUTS/100*100;
		for (; now < toUTS/100*100; now+=100) {
			summarise(now);
			
			if (now % 10000 == 0) {
				summariseIntermediate(now, now/100-1);
				System.out.println("Oke dan");
			}
			if (now % 1000000 == 0) {
				summariseIntermediate(now, now/10000-1);
			}
			if (now % 100000000 == 0) {
				summariseIntermediate(now, now/1000000-1);
			}
		}
	}
	
	public static void summarise(long now)
	{
		Summariser sum = new Summariser(now);

		boolean needs = false;

		for (Summariser.SummaryType sType : Summariser.SummaryType.values()) {
			for (DBValue s2Type : DBValue.values()) {
				sum.setsType(sType);
				sum.setS2Type(s2Type);
				if (sum.alreadyExists()) {
					System.out.println(now + " already exists");
					return;
				}
				needs = true;
			}
		}

		if (!needs) {
			return;

		}
		
		int read = sum.readFiles();
		if (read == 0) {
			return;
		}

		for (Summariser.SummaryType sType : Summariser.SummaryType.values()) {
			for (DBValue s2Type : DBValue.values()) {
				sum.setsType(sType);
				sum.setS2Type(s2Type);
				sum.summarise();
			}
		}
	}
	
	public static void summariseIntermediate(long now, long dir)
	{
		Summariser sum;
		for (Summariser.SummaryType sType : Summariser.SummaryType.values()) {
			for (DBValue s2Type : DBValue.values()) {
				sum = new IntermediateSummariser(now, dir);
				sum.setsType(sType);
				sum.setS2Type(s2Type);
				
				if (sum.alreadyExists()) {
					System.out.println(dir + " already exists");
					continue;
				}

				int read = sum.readFiles();
				if (read == 0) {
					continue;
				}

				sum.summarise();
			}
		}
	}
}

