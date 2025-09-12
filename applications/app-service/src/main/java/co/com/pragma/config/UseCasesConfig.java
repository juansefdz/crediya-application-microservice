package co.com.pragma.config;

import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.notification.gateways.NotificationRepository;
import co.com.pragma.usecase.listapplications.ListApplicationsUseCase;
import co.com.pragma.usecase.loanaplication.LoanApplicationUseCase;
import co.com.pragma.usecase.updateapplicationstatus.UpdateApplicationStatusUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

        @Bean
        public LoanApplicationUseCase loanApplicationUseCase(
                LoanApplicationRepository loanApplicationRepository,
                LoanTypeRepository loanTypeRepository) {
                return new LoanApplicationUseCase(loanApplicationRepository, loanTypeRepository);
        }

        @Bean
        public UpdateApplicationStatusUseCase updateApplicationStatusUseCase(
                LoanApplicationRepository loanApplicationRepository,
                NotificationRepository notificationRepository) {
                return new UpdateApplicationStatusUseCase(loanApplicationRepository, notificationRepository);
        }

        @Bean
        public ListApplicationsUseCase listApplicationsUseCase(
                LoanApplicationRepository loanApplicationRepository) {
                return new ListApplicationsUseCase(loanApplicationRepository);
        }

}