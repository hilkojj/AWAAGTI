package shared;

/**
 * QueryFilter parses query filter string formats and allows for
 * checking if an integer complies with it.
 * 
 * @author remi, timo
 */
public class QueryFilter
{

	/**
	 * // allowed
	 * Operand.EQUALS
	 * Operand.valueOf("EQUALS")
	 * Operand.fromString("==")
	 *
	 * // NOT allowed
	 * Operand.valueOf("==")
	 */
	enum Operand
	{
		EQUALS("=="),
		LESS_THAN("<"),
		LESS_THAN_OR_EQUALS("<="),
		GREATER_THAN(">"),
		GREATER_THAN_OR_EQUALS(">="),
		NOT_EQUALS("!="),
		BETWEEN("between");

		String key;
		Operand(String key)
		{
			this.key = key;
		}

		public String getKey()
		{
			return key;
		}

		public static Operand fromString(String str)
		{
			for (Operand type : Operand.values()) {
				if (!type.getKey().equals(str)) {
					continue;
				}
				return type;
			}
			return null;
		}
	}

	private String originalInput = "";

	public String getOriginalInput() {
		return originalInput;
	}

	private DBValue variable;
	private Operand operand;
	private int b;
	private int c;

	public QueryFilter() { }

	/**
	 * @param input the comma separated String that will get parsed to a filter
	 * @throws Exception
	 */
	public QueryFilter(String input) throws Exception
	{
		this.originalInput = input;
		this.parseFilter(input);
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
		if (this.originalInput.equals("")) {
			return true;
		}

		int a = 0;
		switch (this.variable) {
		case TEMP:
			a = dp.getTemp() / 10;
			break;
		case WIND:
			a = dp.getWindSpeed() / 10;
			break;
		default:
			System.out.println("ERROR: unimplemented DataPoint variable in QueryFilter");
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
		if (originalInput.equals("")) {
			return true;
		}

		switch (this.operand) {
		case EQUALS:
			return a == this.b;
		case LESS_THAN:
			return a <  this.b;
		case LESS_THAN_OR_EQUALS:
			return a <= this.b;
		case GREATER_THAN:
			return a >  this.b;
		case GREATER_THAN_OR_EQUALS:
			return a >= this.b;
		case NOT_EQUALS:
			return a != this.b;
		case BETWEEN:
			return a > this.b && a < this.c;
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

		this.operand = Operand.fromString(items[1]);
		if (this.operand == null) {
			throw new Exception("incorrect filter operand");
		}

		this.b = Integer.parseInt(items[2]);
		if (this.operand == Operand.BETWEEN && items.length == 4) {
			this.c = Integer.parseInt(items[3]);
		}
	}
	
	private DBValue parseVariable(String str)
	{
		return DBValue.valueOf(str.toUpperCase());
	}
}
