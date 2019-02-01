package DBReader;

/**
 * QueryFilterTest unit tests QueryFilter.
 * 
 * @author remi
 */
class QueryFilterTest
{
	public static void main(String[] args) throws Exception
	{
		QueryFilter q = new QueryFilter("temp,<,10");
		a(q.execute(15) == false);
		a(q.execute(10) == false);
		a(q.execute(9) == true);
		a(q.execute(-1) == true);
		
		q = new QueryFilter("temp,>,10");
		a(q.execute(15) == true);
		a(q.execute(10) == false);
		a(q.execute(9) == false);
		a(q.execute(-1) == false);
		
		q = new QueryFilter("temp,<=,10");
		a(q.execute(15) == false);
		a(q.execute(10) == true);
		a(q.execute(9) == true);
		a(q.execute(-1) == true);

		q = new QueryFilter("temp,>=,10");
		a(q.execute(15) == true);
		a(q.execute(10) == true);
		a(q.execute(9) == false);
		a(q.execute(-1) == false);

		q = new QueryFilter("temp,==,10");
		a(q.execute(15) == false);
		a(q.execute(10) == true);
		a(q.execute(9) == false);
		a(q.execute(-1) == false);

		q = new QueryFilter("temp,!=,10");
		a(q.execute(15) == true);
		a(q.execute(10) == false);
		a(q.execute(9) == true);
		a(q.execute(-1) == true);
		
		System.out.println("yey");
	}
	
	private static void a(boolean b) throws Exception
	{
		if (b) {
			return;
		}

		throw new Exception("assert is false");
	}
}
