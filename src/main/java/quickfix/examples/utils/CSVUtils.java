package quickfix.examples.utils;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CSVUtils {

    public static Object[] symbols = new Object[] {};
    public static List<String> symbolList = new ArrayList<>();

    @Getter
    public static class Symbol {
        @CsvBindByPosition(position = 0)
        private String symbolId;

        @CsvBindByPosition(position = 1)
        private String symbolCode;
    }

    public static List<Symbol> importSymbolCSV(String filePath) {
        try {
            return (List<Symbol>) new CsvToBeanBuilder(new FileReader(filePath))
                    .withSkipLines(1)
                    .withType(Symbol.class)
                    .build().parse();

        } catch (Exception e) {
            log.error("CSV Utils throws exception: {}", e.getMessage());
        }

        return new ArrayList<>();
    }
}
