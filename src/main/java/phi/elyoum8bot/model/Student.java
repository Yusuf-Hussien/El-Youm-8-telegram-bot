package phi.elyoum8bot.model;

import lombok.*;

@Data
@NoArgsConstructor
public class Student {
    Long seatNumber;
    String arabicName;
    Double totalDegree;
    Double percentage;
    Long studentRank;
    Long rankWithDuplicates;
}

