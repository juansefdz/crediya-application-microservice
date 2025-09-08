package co.com.pragma.r2dbc.config;

import co.com.pragma.model.LoanApplicationStatus;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;

import java.util.List;

@Configuration
public class R2dbcConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory) {
        var dialect = DialectResolver.getDialect(connectionFactory);
        return R2dbcCustomConversions.of(
                dialect,
                List.of(
                        new LoanApplicationStatusReadConverter(),
                        new LoanApplicationStatusWriteConverter()
                )
        );
    }

    @ReadingConverter
    // ✅ CORRECCIÓN: Cambia el tipo de entrada de Integer a Long
    public static class LoanApplicationStatusReadConverter
            implements Converter<Long, LoanApplicationStatus> {
        @Override
        public LoanApplicationStatus convert(Long source) {
            // Convierte el Long a int para usar tu método fromId
            return LoanApplicationStatus.fromId(source.intValue());
        }
    }

    @WritingConverter
    public static class LoanApplicationStatusWriteConverter
            implements Converter<LoanApplicationStatus, Integer> {
        @Override
        public Integer convert(LoanApplicationStatus source) {
            return source.getId();
        }
    }
}