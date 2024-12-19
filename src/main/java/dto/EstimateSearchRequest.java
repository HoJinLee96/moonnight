package dto;

import java.time.LocalDate;
import java.time.YearMonth;


public class EstimateSearchRequest {
  
  private Status status = Status.ALL;
  private int page;
  private int size;
  private SortType sortType = SortType.DESC;
  private PeriodType periodType = PeriodType.ALL;
  private LocalDate startDate;
  private LocalDate endDate;
  private String year;
  private String month;
  private SearchType searchType;
  private String searchWords;

  public enum PeriodType {
    ALL, TODAY, DAYS7, DAYS30, MONTHLY, RANGE;
  }
  
  public enum Status {
    ALL, RECEIVED, IN_PROGRESS, COMPLETED, DELETE
  }
  
  public enum SortType {
    ASC, DESC
  }
  
  public enum SearchType {
    ESTIMATE_SEQ, NAME, PHONE, EMAIL, ADDRESS;
  }

  public EstimateSearchRequest() {
  }

  public EstimateSearchRequest(Status status, int page, int size, SortType sortType,PeriodType periodType) {
    validatePagination(page, size);
    this.status = status;
    this.page = page;
    this.size = size;
    this.sortType = sortType;
    this.periodType = periodType;
  }

  public EstimateSearchRequest(Status status, int page, int size, SortType sortType, PeriodType periodType, LocalDate startDate, LocalDate endDate) {
    validatePagination(page, size);
    periodValidate(periodType, startDate, endDate);
    this.periodType = periodType;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public EstimateSearchRequest(Status status, int page, int size, SortType sortType, PeriodType periodType, String year, String month) {
    validatePagination(page, size);
    periodValidate(periodType, year, month);
    this.periodType = periodType;
    this.year = year;
    this.month = month;
  }
  
  public EstimateSearchRequest(Status status, int page, int size, SortType sortType, PeriodType periodType, LocalDate startDate, LocalDate endDate, SearchType searchType, String searchWords) {
    validatePagination(page, size);
    periodValidate(periodType, startDate, endDate);
    searchValidate(searchType, searchWords);
    this.periodType = periodType;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public EstimateSearchRequest(Status status, int page, int size, SortType sortType, PeriodType periodType, String year, String month, SearchType searchType, String searchWords) {
    validatePagination(page, size);
    periodValidate(periodType, year, month);
    searchValidate(searchType, searchWords);
    this.periodType = periodType;
    this.year = year;
    this.month = month;
  }
  
  private void validatePagination(int page, int size) {
    if (page < 1 || size < 10) {
        throw new IllegalArgumentException("페이지 1이상, 사이즈 10이상 값이 필요합니다.");
    }
}

  private void periodValidate(PeriodType periodType, LocalDate startDate, LocalDate endDate) {
    LocalDate today = LocalDate.now();
    if (periodType == PeriodType.RANGE) {
      if (startDate == null || endDate == null) {
        throw new IllegalArgumentException("기간 타입은 시작 날짜와 종료 날짜가 필요합니다.");
      }
      if (startDate.isBefore(LocalDate.of(1950, 1, 1)) || startDate.isAfter(today)) {
        throw new IllegalArgumentException("시작 날짜는 1950년부터 현재 날짜 사이여야 합니다.");
      }
      if (endDate.isBefore(LocalDate.of(1950, 1, 1)) || endDate.isAfter(today)) {
        throw new IllegalArgumentException("종료 날짜는 1950년부터 현재 날짜 사이여야 합니다.");
      }
      if (startDate.isAfter(endDate)) {
        throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
      }
    }
  }

  private void periodValidate(PeriodType periodType, String year, String month) {
    LocalDate today = LocalDate.now();
    if (periodType == PeriodType.MONTHLY) {
      if (year == null || month == null) {
        throw new IllegalArgumentException("월 타입은 연도과 월이 필요합니다.");
      }
      int parsedYear;
      try {
        parsedYear = Integer.parseInt(year);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("연도는 숫자여야 합니다.");
      }
      if (parsedYear < 1950 || parsedYear > today.getYear()) {
        throw new IllegalArgumentException("연도는 1950년부터 현재 연도 사이여야 합니다.");
      }
      int parsedMonth;
      try {
        parsedMonth = Integer.parseInt(month);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("월은 숫자여야 합니다.");
      }
      if (parsedMonth < 1 || parsedMonth > 12) {
        throw new IllegalArgumentException("월은 01부터 12 사이여야 합니다.");
      }
      YearMonth yearMonth = YearMonth.of(parsedYear, parsedMonth);
      if (yearMonth.atEndOfMonth().isAfter(today)) {
        throw new IllegalArgumentException("연도와 월은 현재 날짜 이전이어야 합니다.");
      }
    }
  }
  
  private void searchValidate(SearchType searchType, String searchWords) {
    if (searchWords == null || searchWords.trim().isEmpty()) {
      throw new IllegalArgumentException("검색어는 빈 문자열일 수 없습니다.");
    }
    switch (searchType) {
        case ADDRESS: break;
        case NAME: break;
        case EMAIL:
            if (!searchWords.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
            }
            break;
        case ESTIMATE_SEQ:
            if (!searchWords.matches("\\d+")) {
                throw new IllegalArgumentException("접수번호는 숫자만 포함해야 합니다.");
            }
            break;
        case PHONE:
            if (!searchWords.matches("^\\d{3,4}-\\d{3,4}-\\d{4}$")) {
                throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)");
            }
            break;
        default:
            throw new IllegalArgumentException("유효하지 않은 검색 타입입니다: " + searchType);
    }
  }

  public Status getStatus() {
    return status;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public SortType getSortType() {
    return sortType;
  }

  public PeriodType getPeriodType() {
    return periodType;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public String getYear() {
    return year;
  }

  public String getMonth() {
    return month;
  }

  public SearchType getSearchType() {
    return searchType;
  }

  public String getSearchWords() {
    return searchWords;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public void setSortType(SortType sortType) {
    this.sortType = sortType;
  }

  public void setPeriodType(PeriodType periodType) {
    this.periodType = periodType;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public void setSearchType(SearchType searchType) {
    this.searchType = searchType;
  }

  public void setSearchWords(String searchWords) {
    this.searchWords = searchWords;
  }

  @Override
  public String toString() {
    return "EstimateSearchRequest [status=" + status + ", page=" + page + ", size=" + size
        + ", sortType=" + sortType + ", periodType=" + periodType + ", startDate=" + startDate
        + ", endDate=" + endDate + ", year=" + year + ", month=" + month + ", searchType="
        + searchType + ", searchWords=" + searchWords + "]";
  }
  
  
  
}

