package dto.request;

import java.time.LocalDate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EstimateSearchRequestDto(
    
    @NotNull
    Status status,

    @Min(1)
    int page,

    @Min(10)
    int size,

    @NotNull
    SortType sortType,

    @NotNull
    PeriodType periodType,

    @PastOrPresent(message = "{validation.estimate.search.startdate.invalid}")
    LocalDate startDate,

    @PastOrPresent(message = "{validation.estimate.search.enddate.invalid}")
    LocalDate endDate,

    @Pattern(regexp = "^(19[5-9]\\d|20\\d{2})$", message = "{validation.estimate.search.year.invalid}")
    String year,

    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "{validation.estimate.search.month.invalid}")
    String month,

    SearchType searchType,

    @Size(max = 50, message = "{validation.estimate.search.searchWords.length}")
    @Pattern(regexp = "^[\\w\\s@.-]*$", message = "{validation.estimate.search.searchWords.invalid}")
    String searchWords
){

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
    
}
