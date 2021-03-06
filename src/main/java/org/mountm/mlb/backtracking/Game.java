package org.mountm.mlb.backtracking;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

/**
 * Immutable object representing a baseball game. Implements Comparable so that
 * a List of games can be sorted.
 * 
 */
public class Game implements Comparable<Game> {

	private Stadium stadium;
	private DateTime date;

	private static final int TIME_OF_GAME = 240;
	private static final int NINE_AM = 32400000;
	private static final int TEN_PM = 79200000;
	private static final int MAX_DRIVING = 720;

	public Game(Stadium home, DateTime date) {
		this.stadium = home;
		this.date = date;
	}

	public Game() {
		this.stadium = Stadium.ARI;
		this.date = DateTime.now();
	}

	public Stadium getStadium() {
		return stadium;
	}

	public DateTime getDate() {
		return date;
	}

	/**
	 * @return the day of the year that this game occurs on
	 */
	public int dayOfYear() {
		// used as a more meaningful key in the missedStadiums map
		return date.getDayOfYear();
	}
	
	public int getStartTime() {
		return 1440 * date.getDayOfYear() + date.getMinuteOfDay();
	}

	public int stadiumIndex() {
		return stadium.getIndex();
	}

	/**
	 * 
	 * @param g
	 *            The game you are going to next.
	 * @return The strict driving time between this game and the next game.
	 */
	public int getMinutesTo(Game g) {
		return stadium.getMinutesTo(g.stadium);
	}

	/**
	 * Determines if the specified game can be reached from this game in a
	 * reasonable amount of time.
	 * 
	 * @param g
	 *            The game we are attempting to reach
	 * @return <code>true</code> if the specified game can be reached from this
	 *         game; <code>false</code> otherwise
	 */
	public boolean canReach(Game g) {
		if (date.isAfter(g.date.minusMinutes(TIME_OF_GAME))) {
			return false;
		}
		int dayDiff = g.date.getDayOfYear() - date.getDayOfYear();
		int drivingTime = stadium.getMinutesTo(g.stadium);
		if (dayDiff == 0) {
			return Minutes.minutesBetween(date.plusMinutes(TIME_OF_GAME), g.date).getMinutes() > drivingTime;
		}
		int drivingAfterGame = Minutes.minutesBetween(date.plusMinutes(TIME_OF_GAME),
				date.withMillisOfDay(TEN_PM).plusHours(stadium.getTimeZone())).getMinutes();
		if (drivingAfterGame > 0) {
			drivingTime -= drivingAfterGame;
		}
		while (dayDiff > 1 && drivingTime > 0) {
			drivingTime -= MAX_DRIVING;
			dayDiff--;
		}
		if (dayDiff == 1) {
			return Minutes.minutesBetween(g.date.withMillisOfDay(NINE_AM).plusHours(g.stadium.getTimeZone()),
					g.date).getMinutes() > drivingTime;
		}
		return true;
	}

	@Override
	public String toString() {
		return stadium + " " + date.plusMinutes(30).toString("M/dd hh:mm aa");
	}
	
	public String lpString() {
		return stadium + date.plusMinutes(30).toString("MMMddHH");
	}

	/**
	 * Compares two games based on their start times and locations
	 * 
	 * @return 0 if the games start at the same time and stadium; a value less
	 *         than 0 if this game starts before the specified game; and a value
	 *         greater than 0 if this game starts after the specified game.
	 */
	public int compareTo(Game g) {
		int startTimeDiff = Long.compare(date.getMillis(), g.date.getMillis());
		return startTimeDiff == 0 ? stadium.compareTo(g.stadium) : startTimeDiff;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stadium == null) ? 0 : stadium.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Game other = (Game) obj;
		if (stadium != other.stadium)
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		return true;
	}

}
