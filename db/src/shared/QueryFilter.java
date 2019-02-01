package shared;

/**
 * QueryFilter parses query filter string formats and allows for
 * checking if an integer complies with it.
 * 
 * @author remi
 */
public class QueryFilter
{
	
	enum Operand
	{
		EQUALS,
		LESS_THAN,
		LESS_THAN_OR_EQUALS,
		GREATER_THAN,
		GREATER_THAN_OR_EQUALS,
		NOT_EQUALS
	}
	
	private DBValue variable;
	private Operand operand;
	private int b;

	public QueryFilter(String filter) throws Exception
	{
		this.parseFilter(filter);
	}
	
	/**
	 * execute this QueryFilter on a DataPoint.
	 * The 'variable' part of the QueryFilter string (see constructor)
	 * determines which value of the DataPoint is used for the comparison.
	 * 
	 * @param dp The DataPoint.
	 * 
	 * @return boolean
	 */
	public boolean execute(DataPoint dp)
	{
		int a = 0;
		switch (this.variable) {
		case TEMP:
			a = dp.temp;
		default:
		}

		return this.execute(a);
	}
	
	/**
	 * execute this QueryFilter on this integer.
	 * 
	 * @param a Integer
	 * 
	 * @return boolean
	 */
	public boolean execute(int a)
	{
		switch (this.operand) {
		case EQUALS:
			return a == this.b;
		case LESS_THAN:
			return a < this.b;
		case LESS_THAN_OR_EQUALS:
			return a <= this.b;
		case GREATER_THAN:
			return a > this.b;
		case GREATER_THAN_OR_EQUALS:
			return a >= this.b;
		case NOT_EQUALS:
			return a != this.b;
		}

		return false;
	}

	private void parseFilter(String filter) throws Exception
	{
		String[] items = filter.split(",");
		
		if (items.length < 3) {
			throw new Exception("incorrect filter format");
		}
		
		this.variable = this.parseVariable(items[0]);
		if (this.variable == null) {
			throw new Exception("incorrect filter variable");
		}
		
		this.operand = this.parseOperand(items[1]);
		if (this.operand == null) {
			throw new Exception("incorrect filter operand");
		}
		
		this.b = Integer.parseInt(items[2]);
	}
	
	private DBValue parseVariable(String str) // TODO: use ToStrating() on DBValue
	{
		switch (str) {
		case "temp":
			return DBValue.TEMP;
		}
		
		return null;
	}
	
	private Operand parseOperand(String str)
	{
		switch (str) {
		case "==":
			return Operand.EQUALS;
		case ">":
			return Operand.GREATER_THAN;
		case ">=":
			return Operand.GREATER_THAN_OR_EQUALS;
		case "<":
			return Operand.LESS_THAN;
		case "<=":
			return Operand.LESS_THAN_OR_EQUALS;
		case "!=":
			return Operand.NOT_EQUALS;
		}	
		
		return null;
	}
}
