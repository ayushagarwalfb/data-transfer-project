package org.dataportabilityproject.types.transfer.models.calendar;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Class that stores and contains utilities for calendar recurrence information.
 * All terminology and vocabulary comes from RFC 5545.
 * See https://tools.ietf.org/html/rfc5545 for further information.
 */
public class RecurrenceRule {

  public static final String RRULE = "RRULE";
  public static final String RDATE = "RDATE";
  public static final String EXDATE = "EXDATE";

  private final RRule rRule;
  private final RDate rDate;
  private final ExDate exDate;

  public RRule getRRule() {
    return rRule;
  }

  public RDate getRDate() {
    return rDate;
  }

  public ExDate getExDate() {
    return exDate;
  }

  public List<String> getStringList() {
    List<String> stringList = new LinkedList<>();
    if (rRule != null) {
      stringList.add(rRule.toString());
    }
    if (rDate != null) {
      stringList.add(rDate.toString());
    }
    if (exDate != null) {
      stringList.add(exDate.toString());
    }

    return stringList;
  }

  private RecurrenceRule(RRule rRule, RDate rDate, ExDate exDate) {
    this.rRule = rRule;
    this.rDate = rDate;
    this.exDate = exDate;
  }

  static final String FREQ = "FREQ";
  static final String UNTIL = "UNTIL";
  static final String COUNT = "COUNT";
  static final String INTERVAL = "INTERVAL";
  static final String WKST = "WKST";
  static final String VALUE = "VALUE";
  static final String TZID = "TZID";

  enum Freq {
    SECONDLY,
    MINUTELY,
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
  }

  enum ByRule {
    BYSECOND,
    BYMINUTE,
    BYHOUR,
    BYDAY,
    BYMONTHDAY,
    BYYEARDAY,
    BYWEEKNO,
    BYMONTH,
    BYSETPOS
  }

  static List<String> byRuleList = Arrays.stream(ByRule.values()).map(a -> a.toString())
      .collect(Collectors.toList());

  enum Day {
    SU,
    MO,
    TU,
    WE,
    TH,
    FR,
    SA
  }

  static List<String> dayList = Arrays.stream(Day.values()).map(a -> a.toString())
      .collect(Collectors.toList());

  enum TimeType {
    DATETIME,
    DATE,
    PERIOD,
    UNDEFINED;
  }

  /**
   * A method for turning an RRule string into an {@link RRule}.
   *
   * @param rRuleString a string containing RRule information, excluding any prefixes (e.g.,
   * RRULE:)
   * @return an {@link RRule}
   */
  public static RRule parseRRuleString(String rRuleString) {
    RRule.Builder builder = RRule.builder();

    List<String> components = Arrays.asList(rRuleString.split(";"));
    Map<ByRule, String> byRuleMapInput = new HashMap<>();
    for (String property : components) {
      Preconditions.checkArgument(property.contains("="), "Cannot parse " + property);
      String[] split = property.split("=");
      String key = split[0];
      String value = split[1];
      if (key.equals(FREQ)) {
        builder.freq(Freq.valueOf(value));
      } else if (key.equals(UNTIL)) {
        builder.until(value); // TODO(olsona): consider parsing further?
      } else if (key.equals(COUNT)) {
        builder.count(Integer.parseInt(value));
      } else if (key.equals(INTERVAL)) {
        builder.interval(Integer.parseInt(value));
      } else if (byRuleList.contains(key)) {
        byRuleMapInput.put(ByRule.valueOf(key), value);
      } else if (key.equals(WKST)) {
        builder.wkst(Day.valueOf(value));
      }
    }
    if (!byRuleMapInput.isEmpty()) {
      builder.byRuleMap(byRuleMapInput);
    }
    return builder.build();
  }

  /**
   * A method for parsing an RDate string into an {@link RDate}
   *
   * @param rDateString a string containing RDate information, excluding any prefixes (e.g.,
   * RDATE:)
   * @return an {@link RDate}
   */
  public static RDate parseRDateString(String rDateString) {
    RDate.Builder builder = RDate.builder();
    Preconditions.checkArgument(rDateString != null, "Input is null");
    StringTokenizer stringTokenizer = new StringTokenizer(rDateString, ":");
    String token = stringTokenizer.nextToken();
    while (stringTokenizer.hasMoreTokens()) {
      Preconditions.checkArgument(token.contains("="), "Cannot parse " + token);
      String[] split = token.split("=");
      String key = split[0];
      String value = split[1];
      if (key.equals(VALUE)) {
        builder.value(TimeType.valueOf(value));
      } else if (key.equals(TZID)) {
        builder.tzidparam(value);
      }
      token = stringTokenizer.nextToken();
    }
    builder.rdtval(Arrays.asList(token.split(",")));

    return builder.build();
  }

  public static ExDate parseExDateString(String exDateString) {
    ExDate.Builder builder = ExDate.builder();
    Preconditions.checkArgument(exDateString != null, "Input is null");
    StringTokenizer stringTokenizer = new StringTokenizer(exDateString, ":");
    String token = stringTokenizer.nextToken();
    while (stringTokenizer.hasMoreTokens()) {
      Preconditions.checkArgument(token.contains("="), "Cannot parse " + token);
      String[] split = token.split("=");
      String key = split[0];
      String value = split[1];
      if (key.equals(VALUE)) {
        builder.value(TimeType.valueOf(value));
      } else if (key.equals(TZID)) {
        builder.tzidparam(value);
      }
      token = stringTokenizer.nextToken();
    }
    builder.exdtval(Arrays.asList(token.split(",")));

    return builder.build();
  }

  @AutoValue
  public abstract static class RRule {

    abstract Freq freq();

    @Nullable abstract String until();  // represents a date/time

    @Nullable abstract Integer count();

    @Nullable abstract Integer interval();

    @Nullable abstract Map<ByRule, String> byRuleMap(); // TODO(olsona); parse properties further

    @Nullable abstract Day wkst();

    static RRule create(Freq freq, String until, int count, int interval,
        Map<ByRule, String> byRuleMap, Day wkst) {
      return builder()
          .freq(freq)
          .until(until)
          .count(count)
          .interval(interval)
          .byRuleMap(byRuleMap)
          .wkst(wkst)
          .build();
    }

    static Builder builder() {
      return new org.dataportabilityproject.types.transfer.models.calendar.AutoValue_RecurrenceRule_RRule.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {

      public abstract Builder freq(Freq freq);

      public abstract Builder until(String until);

      public abstract Builder count(Integer count);

      public abstract Builder interval(Integer interval);

      public abstract Builder byRuleMap(Map<ByRule, String> byRuleMap);

      public abstract Builder wkst(Day wkst);

      public abstract RRule build();
    }
  }

  @AutoValue
  public abstract static class RDate {

    @Nullable abstract TimeType value();

    @Nullable abstract String tzidparam();

    abstract List<String> rdtval();

    static RDate create(@Nullable TimeType value, @Nullable String tzidparam, List<String> rdtval) {
      return builder()
          .value(value)
          .tzidparam(tzidparam)
          .rdtval(rdtval)
          .build();
    }

    static Builder builder() {
      return new org.dataportabilityproject.types.transfer.models.calendar.AutoValue_RecurrenceRule_RDate.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {

      abstract Builder value(TimeType value);

      abstract Builder tzidparam(String tzidparam);

      abstract Builder rdtval(List<String> rdtval);

      abstract RDate build();
    }
  }

  @AutoValue
  public abstract static class ExDate {

    @Nullable abstract TimeType value();

    @Nullable abstract String tzidparam();

    abstract List<String> exdtval();

    public static ExDate create(TimeType value, String tzidparam, List<String> exdtval) {
      return builder()
          .value(value)
          .tzidparam(tzidparam)
          .exdtval(exdtval)
          .build();
    }

    public static Builder builder() {
      return new org.dataportabilityproject.types.transfer.models.calendar.AutoValue_RecurrenceRule_ExDate.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder value(TimeType value);

      public abstract Builder tzidparam(String tzidparam);

      public abstract Builder exdtval(List<String> exdtval);

      public abstract ExDate build();
    }
  }

  public static class Builder {

    private RRule rRule;
    private RDate rDate;
    private ExDate exDate;

    public Builder() {
    }

    public Builder setRRule(RRule rRule) {
      this.rRule = rRule;
      return this;
    }

    public Builder setRDate(RDate rDate) {
      this.rDate = rDate;
      return this;
    }

    public Builder setExDate(ExDate exDate) {
      this.exDate = exDate;
      return this;
    }

    public RecurrenceRule build() {
      return new RecurrenceRule(rRule, rDate, exDate);
    }
  }
}
