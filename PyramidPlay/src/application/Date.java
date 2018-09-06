package application;

import java.io.Serializable;

/**
 * This class represents a date
 * @author Jacob Parcell
 *
 */
public class Date implements Comparable<Date>
{

	private int year;
	private int month;
	private int day;

	/**
	 * Default Constructor
	 */
	public Date()
	{
		day = 1;
		month = 1;
		year = 1995;
	}

	/**
	 * Constructor given day, month, and year
	 * @param d		day
	 * @param m		month
	 * @param y		year
	 */
	public Date(int d, int m, int y)
	{
		day = d;
		month = m;
		year = y;		
	}

	/**
	 * Constructor that takes information from a Date object
	 * @param d		given Date
	 */
	public Date(String d)
	{
		month = Integer.parseInt(d.substring(0,2));
		day = Integer.parseInt(d.substring(3, 5));
		year = Integer.parseInt(d.substring(6));
	}

	/**
	 * Formats Date information to be printed
	 * @return		formatted date information
	 */
	public String toString()
	{
		String m = Integer.toString(month);

		String d = Integer.toString(day);

		if(month < 10)
		{
			m = "0" + month;
		}

		if(day < 10)
		{
			d = "0" + day;
		}


		String date = m + "/" + d + "/" + this.year;

		return date;
	}

	/**
	 * Retrieves day
	 * @return		day of the month
	 */
	public int getDay()
	{
		return day;
	}

	/**
	 * Retrieves month
	 * @return		month number
	 */
	public int getMonth()
	{
		return month;
	}

	/**
	 * Retrieves year
	 * @return		year
	 */
	public int getYear()
	{
		return year;
	}

	@Override
	public int compareTo(Date arg0) 
	{

		int year1 = this.getYear();
		int year2 = arg0.getYear();

		if(year1 == year2)
		{
			int month1 = this.getMonth();
			int month2 = arg0.getMonth();

			if(month1 == month2)
			{
				int day1 = this.getDay();
				int day2 = arg0.getDay();

				//if years and months are equal, compare days
				return compareInt(day1, day2);
			}
			else
			{
				//if months are not equal, compare months
				return compareInt(month1, month2);
			}
		}
		else
		{
			//if years are not equal, compare years
			return compareInt(year1, year2);
		}
	}

	/**
	 * Compares two integers and returns integers similar to the default compareTo function
	 * @param i1	first integer
	 * @param i2	second integer
	 * @return		0 if integers are the same, -1 if first integer is smaller, 1 if first integer is larger
	 */
	public static int compareInt(int i1, int i2)
	{
		if(i1 == i2)
		{
			return 0;
		}
		else if(i1 < i2)
		{
			return -1;
		}
		else
		{
			return 1;
		}
	}

	/**
	 * Compares information in two Date objects and determines equality
	 * @param d		Date to be compared to
	 * @return		true if dates are equivalent, false if dates are different
	 */
	public boolean equals(Date d)
	{
		int comparison = compareInt(this.year, d.year) + compareInt(this.month, d.month) + compareInt(this.day, d.day);

		if(compareInt(this.year, d.year) == 0 && compareInt(this.month, d.month) == 0 && compareInt(this.day, d.day) == 0)
		{
			//year, month, and day are the same values
			return true;
		}
		else
		{
			return false;
		}
	}
}

