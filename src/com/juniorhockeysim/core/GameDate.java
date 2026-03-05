package com.juniorhockeysim.core;

import java.io.Serializable;

public class GameDate implements Serializable, Comparable<GameDate> {

    private int day;
    private int month;
    private int year;

    private static final int[] DAYS_IN_MONTH = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public GameDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    private int daysInMonth(int m, int y) {
        if (m == 2 && isLeapYear(y)) return 29;
        return DAYS_IN_MONTH[m];
    }

    private boolean isLeapYear(int y) {
        return (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0);
    }

    public void nextDay() {
        day++;
        if (day > daysInMonth(month, year)) {
            day = 1;
            month++;
        }
        if (month > 12) {
            month = 1;
            year++;
        }
    }

    public int getDay()   { return day;   }
    public int getMonth() { return month; }
    public int getYear()  { return year;  }

    public boolean isSameDate(GameDate other) {
        return this.day == other.day && this.month == other.month && this.year == other.year;
    }

    public boolean isAfter(GameDate other) {
        return this.compareTo(other) > 0;
    }

    public boolean isBefore(GameDate other) {
        return this.compareTo(other) < 0;
    }

    @Override
    public int compareTo(GameDate other) {
        if (this.year != other.year) return Integer.compare(this.year, other.year);
        if (this.month != other.month) return Integer.compare(this.month, other.month);
        return Integer.compare(this.day, other.day);
    }

    public GameDate copy() {
        return new GameDate(day, month, year);
    }

    public int toAbsoluteDays() {
        return year * 365 + month * 31 + day;
    }


    public String toShortString() {
        String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                           "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month] + " " + day;
    }

    @Override
    public String toString() {
        return String.format("%s %d, %d", monthName(month), day, year);
    }

    private String monthName(int m) {
        // Standard calendar months, 1=January through 12=December
        String[] names = {
            "", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        if (m >= 1 && m <= 12) return names[m];
        return "Month" + m;
    }
}
