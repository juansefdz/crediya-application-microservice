package co.com.pragma.config;

import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.notification.gateways.NotificationRepository;
import co.com.pragma.model.sqs.gateways.SqsNotificationGateway;
import co.com.pragma.usecase.listapplications.ListApplicationsUseCase;
import co.com.pragma.usecase.loanaplication.LoanApplicationUseCase;
import co.com.pragma.usecase.updateapplicationstatus.UpdateApplicationStatusUseCase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = UseCasesConfig.class)
class UseCasesConfigTest {

    @Autowired
    private ApplicationContext context;


    @MockitoBean
    private LoanApplicationRepository loanApplicationRepository;
    @MockitoBean
    private LoanTypeRepository loanTypeRepository;
    @MockitoBean
    private NotificationRepository notificationRepository;
    @MockitoBean
    private SqsNotificationGateway sqsNotificationGateway;


    @Test
    @DisplayName("Debe crear todos los beans de Casos de Uso exitosamente")
    void useCaseBeansShouldBeCreatedSuccessfully() {
        assertThat(context.getBean(LoanApplicationUseCase.class)).isNotNull();
        assertThat(context.getBean(UpdateApplicationStatusUseCase.class)).isNotNull();
        assertThat(context.getBean(ListApplicationsUseCase.class)).isNotNull();
    }
}